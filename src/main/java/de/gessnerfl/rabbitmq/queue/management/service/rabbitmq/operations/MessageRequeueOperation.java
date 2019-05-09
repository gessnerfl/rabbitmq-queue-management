package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.LongString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MessageRequeueOperation {
    public static final String X_DEATH_HEADER_KEY_NAME = "x-death";
    public static final String X_DEATH_EXCHANGE_KEY_NAME = "exchange";
    public static final String X_DEATH_ROUTING_KEYS_KEY_NAME = "routing-keys";
    static final String REQUEUE_COUNT_HEADER = "x-rmqmgmt-requeue-count";

    private final MessageOperationExecutor messageOperationExecutor;

    @Autowired
    public MessageRequeueOperation(MessageOperationExecutor messageOperationExecutor) {
        this.messageOperationExecutor = messageOperationExecutor;
    }
    
    public void requeueAllMessages(String vhost, String queueName){
        messageOperationExecutor.routeAllMessagesAndAcknowledgeOnSuccess(vhost, queueName, this::getRoutingDetailsFromMessage);
    }

    public void requeueFirstMessage(String vhost, String queueName, String checksum){
        messageOperationExecutor.routeFirstMessageAndAcknowledgeOnSuccess(vhost, queueName, checksum, this::getRoutingDetailsFromMessage);
    }

    private RoutingDetails getRoutingDetailsFromMessage(GetResponse response){
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

        return new RoutingDetails(targetExchange, targetRoutingKeys.get(0).toString(), REQUEUE_COUNT_HEADER);
    }

}
