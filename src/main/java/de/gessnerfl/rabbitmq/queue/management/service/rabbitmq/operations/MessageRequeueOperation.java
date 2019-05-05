package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.LongString;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.utils.MessageHeaderModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MessageRequeueOperation {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageRequeueOperation.class);
    private static final long MAX_WAIT_FOR_CONFIRM = 5000;
    public static final String X_DEATH_HEADER_KEY_NAME = "x-death";
    public static final String X_DEATH_EXCHANGE_KEY_NAME = "exchange";
    public static final String X_DEATH_ROUTING_KEYS_KEY_NAME = "routing-keys";
    static final String REQUEUE_COUNT_HEADER = "x-rmqmgmt-requeue-count";

    private final MessageOperationExecutor messageOperationExecutor;
    private final MessageHeaderModifier messageHeaderModifier;

    @Autowired
    public MessageRequeueOperation(MessageOperationExecutor messageOperationExecutor, MessageHeaderModifier messageHeaderModifier) {
        this.messageOperationExecutor = messageOperationExecutor;
        this.messageHeaderModifier = messageHeaderModifier;
    }
    
    public void requeueFirstMessage(String vhost, String queueName, String checksum){
        messageOperationExecutor.consumeMessageApplyFunctionAndAckknowlegeOnSuccess(vhost, queueName, checksum, (channel,response) -> {
            StateKeepingReturnListener returnListener = new StateKeepingReturnListener("requeue", LOGGER);
            channel.addReturnListener(returnListener);
            channel.confirmSelect();

            if (response.getProps().getHeaders() == null || !response.getProps().getHeaders().containsKey(X_DEATH_HEADER_KEY_NAME)) {
                throw new MessageOperationFailedException("Requeue operation not available; x-death header missing");
            }

            List<Map<String,Object>> xDeath = (List<Map<String,Object>>)response.getProps().getHeaders().get(X_DEATH_HEADER_KEY_NAME);
            if(xDeath.isEmpty()) {
                throw new MessageOperationFailedException("Requeue operation not available; x-death header missing");
            }

            Map<String,Object> firstEntry = xDeath.get(0);
            if(!firstEntry.containsKey(X_DEATH_EXCHANGE_KEY_NAME)){
                throw new MessageOperationFailedException("Requeue operation not available; exchange is missing");
            }

            String targetExchange = firstEntry.get(X_DEATH_EXCHANGE_KEY_NAME).toString();
            if(!firstEntry.containsKey(X_DEATH_ROUTING_KEYS_KEY_NAME)){
                throw new MessageOperationFailedException("Requeue operation not available; routing keys are missing");
            }
            List<LongString> targetRoutingKeys = (List<LongString>)firstEntry.get(X_DEATH_ROUTING_KEYS_KEY_NAME);
            if(targetRoutingKeys.isEmpty()){
                throw new MessageOperationFailedException("Requeue operation not available; routing keys are missing");
            }

            AMQP.BasicProperties props = messageHeaderModifier.incrementCounter(response.getProps(), REQUEUE_COUNT_HEADER);
            channel.basicPublish(targetExchange, targetRoutingKeys.get(0).toString(), true, props, response.getBody());
            channel.waitForConfirmsOrDie(MAX_WAIT_FOR_CONFIRM);
            if(returnListener.isReceived()){
                throw new MessageOperationFailedException("requeue failed, basic.return received");
            }

            channel.removeReturnListener(returnListener);
        });
    }

}
