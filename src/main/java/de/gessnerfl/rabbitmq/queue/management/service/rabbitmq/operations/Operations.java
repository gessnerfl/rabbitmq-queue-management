package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.gessnerfl.rabbitmq.queue.management.model.Message;

@Service
public class Operations {
    private final QueueListOperation queueListOperation;
    private final MessageMoveOperation messageMoveOperation;
    private final MessageRequeueOperation messageRequeueOperation;
    private final MessageDeleteOperation messageDeleteOperation;

    @Autowired
    public Operations(QueueListOperation queueListOperation,
                      MessageMoveOperation messageMoveOperation,
                      MessageRequeueOperation messageRequeueOperation,
                      MessageDeleteOperation messageDeleteOperation) {
        this.queueListOperation = queueListOperation;
        this.messageMoveOperation = messageMoveOperation;
        this.messageRequeueOperation = messageRequeueOperation;
        this.messageDeleteOperation = messageDeleteOperation;
    }

    public List<Message> getMessagesOfQueue(String vhost, String queueName, int limit){
        return queueListOperation.getMessagesFromQueue(vhost, queueName, limit);
    }
    
    public void deleteFirstMessageInQueue(String vhost, String queueName, String messageChecksum){
        messageDeleteOperation.deleteFirstMessageInQueue(vhost, queueName, messageChecksum);
    }
    
    public void moveAllMessagesInQueue(String vhost, String queueName, String targetExchange, String targetRoutingKey){
        messageMoveOperation.moveAllMessages(vhost, queueName, targetExchange, targetRoutingKey);
    }

    public void moveFirstMessageInQueue(String vhost, String queueName, String messageChecksum, String targetExchange, String targetRoutingKey){
        messageMoveOperation.moveFirstMessage(vhost, queueName, messageChecksum, targetExchange, targetRoutingKey);
    }

    public void requeueAllMessagesInQueue(String vhost, String queueName){
        messageRequeueOperation.requeueAllMessages(vhost, queueName);
    }

    public void requeueFirstMessageInQueue(String vhost, String queueName, String messageChecksum){
        messageRequeueOperation.requeueFirstMessage(vhost, queueName, messageChecksum);
    }

}
