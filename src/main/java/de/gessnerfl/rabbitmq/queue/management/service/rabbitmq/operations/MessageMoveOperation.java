package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageMoveOperation {
    static final String MOVE_COUNT_HEADER = "x-rmqmgmt-move-count";

    private final MessageOperationExecutor messageOperationExecutor;

    @Autowired
    public MessageMoveOperation(MessageOperationExecutor messageOperationExecutor) {
        this.messageOperationExecutor = messageOperationExecutor;
    }
    
    public void moveAllMessages(String vhost, String queueName, String targetExchange, String targetRoutingKey){
        messageOperationExecutor.routeAllMessagesAndAcknowledgeOnSuccess(vhost, queueName, m -> new RoutingDetails(targetExchange, targetRoutingKey, MOVE_COUNT_HEADER));
    }

    public void moveFirstMessage(String vhost, String queueName, String checksum, String targetExchange, String targetRoutingKey){
        messageOperationExecutor.routeFirstMessageAndAcknowledgeOnSuccess(vhost, queueName, checksum, m -> new RoutingDetails(targetExchange, targetRoutingKey, MOVE_COUNT_HEADER));
    }



}
