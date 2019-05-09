package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.utils.RoutingMessageHeaderModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;

import de.gessnerfl.rabbitmq.queue.management.connection.CloseableChannelWrapper;
import de.gessnerfl.rabbitmq.queue.management.connection.Connector;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.utils.MessageChecksum;

@Service
public class MessageOperationExecutor {
    static final long MAX_WAIT_FOR_CONFIRM = 5000;
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageOperationExecutor.class);

    private final Connector connector;
    private final MessageChecksum messageChecksum;
    private final OperationIdGenerator operationIdGenerator;
    private final RoutingMessageHeaderModifier routingMessageHeaderModifier;
    private final StateKeepingReturnListenerFactory stateKeepingReturnListenerFactory;

    @Autowired
    public MessageOperationExecutor(Connector connector, MessageChecksum messageChecksum, OperationIdGenerator operationIdGenerator, RoutingMessageHeaderModifier routingMessageHeaderModifier, StateKeepingReturnListenerFactory stateKeepingReturnListenerFactory) {
        this.connector = connector;
        this.messageChecksum = messageChecksum;
        this.operationIdGenerator = operationIdGenerator;
        this.routingMessageHeaderModifier = routingMessageHeaderModifier;
        this.stateKeepingReturnListenerFactory = stateKeepingReturnListenerFactory;
    }

    public void routeAllMessagesAndAcknowledgeOnSuccess(String vhost, String queueName, RouteResolvingFunction fn){
        consumeAllMessageAndApplyFunctionAndAcknowledgeOnSuccess(vhost, queueName, (operationId, channel, response) -> routeMessage(operationId, channel, response, fn));
    }

    public void routeFirstMessageAndAcknowledgeOnSuccess(String vhost, String queueName, String expectedChecksum, RouteResolvingFunction fn){
        consumeMessageAndApplyFunctionAndAcknowledgeOnSuccess(vhost, queueName,expectedChecksum, (operationId, channel, response) -> routeMessage(operationId, channel, response, fn));
    }

    private void routeMessage(OperationId operationId, Channel channel, GetResponse response, RouteResolvingFunction fn) throws IOException, TimeoutException, InterruptedException {
        RoutingDetails routingDetails = fn.resolve(response);

        StateKeepingReturnListener returnListener = stateKeepingReturnListenerFactory.createFor(operationId, LOGGER);
        channel.addReturnListener(returnListener);
        channel.confirmSelect();

        AMQP.BasicProperties props = routingMessageHeaderModifier.modifyHeaders(response.getProps(), operationId, routingDetails.getCountHeaderName());
        channel.basicPublish(routingDetails.getExchange(), routingDetails.getRoutingKey(), true, props, response.getBody());
        channel.waitForConfirmsOrDie(MAX_WAIT_FOR_CONFIRM);
        if(returnListener.isReceived()){
            throw new MessageOperationFailedException("Failed to perform operation, basic.return received");
        }

        channel.removeReturnListener(returnListener);
    }

    public void consumeAllMessageAndApplyFunctionAndAcknowledgeOnSuccess(String vhost, String queueName, MessageOperationFunction fn){
        final OperationId operationId = operationIdGenerator.generate();

        try (CloseableChannelWrapper wrapper = connector.connectAsClosable(vhost)) {
            Channel channel = wrapper.getChannel();
            GetResponse response = channel.basicGet(queueName, false);
            while(massOperationCanBeApplied(response, operationId)) {
                executeMessageOperationAndAckOrNackMessage(fn, operationId, channel, response);

                response = channel.basicGet(queueName, false);
            }
            if(response != null){
                channel.basicNack(response.getEnvelope().getDeliveryTag(), false, true);
            }
        } catch (MessageOperationFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new MessageOperationFailedException(e);
        }
    }

    private boolean massOperationCanBeApplied(GetResponse getResponse, OperationId operationId){
        if(getResponse != null) {
            if(getResponse.getProps() != null && getResponse.getProps().getHeaders() != null) {
                String header = getResponse.getProps().getHeaders().getOrDefault(OperationId.HEADER_NAME, "undefined").toString();
                return !operationId.equals(header);
            }
            return true;
        }
        return false;
    }

    public void consumeMessageAndApplyFunctionAndAcknowledgeOnSuccess(String vhost, String queueName, String expectedChecksum, MessageOperationFunction fn){
        OperationId operationId = operationIdGenerator.generate();
        try (CloseableChannelWrapper wrapper = connector.connectAsClosable(vhost)) {
            Channel channel = wrapper.getChannel();
            GetResponse response = getFirstMessage(queueName, channel);
            String checksum = messageChecksum.createFor(response.getProps(), response.getBody());
            if (checksum.equals(expectedChecksum)) {
                executeMessageOperationAndAckOrNackMessage(fn, operationId, channel, response);
            } else {
                channel.basicNack(response.getEnvelope().getDeliveryTag(), false, true);
                throw new MessageOperationFailedException("Checksum does not match");
            }
        } catch (MessageOperationFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new MessageOperationFailedException(e);
        }
    }

    private void executeMessageOperationAndAckOrNackMessage(MessageOperationFunction fn, OperationId operationId, Channel channel, GetResponse response) throws IOException, TimeoutException, InterruptedException {
        try {
            fn.apply(operationId, channel, response);
            channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
        } catch (Exception e){
            channel.basicNack(response.getEnvelope().getDeliveryTag(), false, true);
            throw e;
        }
    }

    private GetResponse getFirstMessage(String queue, Channel channel) throws IOException {
        GetResponse response = channel.basicGet(queue, false);
        if (response != null) {
            return response;
        }
        throw new MessageOperationFailedException("No message in queue");
    }
}
