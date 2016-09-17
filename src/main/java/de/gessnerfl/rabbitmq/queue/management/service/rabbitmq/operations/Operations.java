package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.gessnerfl.rabbitmq.queue.management.model.Message;

@Service
public class Operations {
    private final QueueListOperation queueListOperation;
    private final MessageRequeueOperation messageRequeueOperation;
    private final MessageDeleteOperation messageDeleteOperation;

    @Autowired
    public Operations(QueueListOperation queueListOperation,
            MessageRequeueOperation messageRequeueOperation,
            MessageDeleteOperation messageDeleteOperation) {
        this.queueListOperation = queueListOperation;
        this.messageRequeueOperation = messageRequeueOperation;
        this.messageDeleteOperation = messageDeleteOperation;
    }

    public List<Message> getMessagesOfQueue(String queueName, int limit){
        return queueListOperation.getMessagesFromQueue(queueName, limit);
    }
    
    public void deleteFirstMessageInQueue(String queueName, String messageChecksum){
        messageDeleteOperation.deleteFirstMessageInQueue(queueName, messageChecksum);
    }
    
    public void requeueFirstMessageInQueue(String queueName, String messageChecksum, String targetExchange, String targetRoutingKey){
        messageRequeueOperation.requeueFirstMessage(queueName, messageChecksum, targetExchange, targetRoutingKey);
    }
    
}
