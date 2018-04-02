package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.gessnerfl.rabbitmq.queue.management.model.AmqpMessage;

@Service
public class Operations {
    private final QueueListOperation queueListOperation;
    private final MessageMoveOperation messageMoveOperation;
    private final MessageDeleteOperation messageDeleteOperation;

    @Autowired
    public Operations(QueueListOperation queueListOperation,
            MessageMoveOperation messageMoveOperation,
            MessageDeleteOperation messageDeleteOperation) {
        this.queueListOperation = queueListOperation;
        this.messageMoveOperation = messageMoveOperation;
        this.messageDeleteOperation = messageDeleteOperation;
    }

    public List<AmqpMessage> getMessagesOfQueue(String brokerName, String queueName, int limit){
        return queueListOperation.getMessagesFromQueue(brokerName, queueName, limit);
    }
    
    public void deleteFirstMessageInQueue(String brokerName, String queueName, String messageChecksum){
        messageDeleteOperation.deleteFirstMessageInQueue(brokerName, queueName, messageChecksum);
    }
    
    public void moveFirstMessageInQueue(String brokerName, String queueName, String messageChecksum, String targetExchange, String targetRoutingKey){
        messageMoveOperation.moveFirstMessage(brokerName, queueName, messageChecksum, targetExchange, targetRoutingKey);
    }
    
}
