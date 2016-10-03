package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;

import de.gessnerfl.rabbitmq.queue.management.connection.CloseableChannelWrapper;
import de.gessnerfl.rabbitmq.queue.management.connection.Connector;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.utils.MessageChecksum;

@Service
public class MessageOperationExecutor {
    private final Connector connector;
    private final MessageChecksum messageChecksum;

    @Autowired
    public MessageOperationExecutor(Connector connector, MessageChecksum messageChecksum) {
        this.connector = connector;
        this.messageChecksum = messageChecksum;
    }
    
    public void consumeMessageApplyFunctionAndAckknowlegeOnSuccess(String brokerName, String queueName, String expectedChecksum, MessageOperationFunction fn){
        try (CloseableChannelWrapper wrapper = connector.connectAsClosable(brokerName)) {
            Channel channel = wrapper.getChannel();
            GetResponse response = getFirstMessage(queueName, channel);
            String checksum = messageChecksum.createFor(response.getProps(), response.getBody());
            if (checksum.equals(expectedChecksum)) {
                fn.apply(channel, response);
                channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
            } else {
                channel.basicNack(response.getEnvelope().getDeliveryTag(), false, true);
                throw new MessageOperationFailedException("Checksum does not match");
            }
        } catch (Exception e) {
            if(e instanceof MessageOperationFailedException){
                throw (MessageOperationFailedException)e;
            }
            throw new MessageOperationFailedException(e);
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
