package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.gessnerfl.rabbitmq.queue.management.model.Message;
import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Binding;
import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Exchange;
import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Queue;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations.Operations;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.remoteapi.ManagementApi;

@Service
public class RabbitMqFacade {
    
    private final ManagementApi managementApi;
    private final Operations operations;
    
    @Autowired
    public RabbitMqFacade(ManagementApi managementApi, Operations operations){
        this.managementApi = managementApi;
        this.operations = operations;
    }

    public List<Exchange> getExchanges(String vhost){
        return managementApi.getExchanges(vhost);
    }

    public List<Queue> getQueues(){
        return managementApi.getQueues();
    }
    
    public List<Queue> getQueues(String vhost){
        return managementApi.getQueues(vhost);
    }
    
    public List<Binding> getExchangeSourceBindings(String vhost, String exchange){
        return managementApi.getExchangeSourceBindings(vhost, exchange);
    }
    
    public List<Binding> getQueueBindings(String vhost, String queueName){
        return managementApi.getQueueBindings(vhost, queueName);
    }
    
    public List<Message> getMessagesOfQueue(String vhost, String queueName, int limit){
        return operations.getMessagesOfQueue(vhost, queueName, limit);
    }
    
    public void deleteFirstMessageInQueue(String vhost, String queueName, String messageChecksum){
        operations.deleteFirstMessageInQueue(vhost, queueName, messageChecksum);
    }
    
    public void moveFirstMessageInQueue(String vhost, String queueName, String messageChecksum, String targetExchange, String targetRoutingKey){
        operations.moveFirstMessageInQueue(vhost, queueName, messageChecksum, targetExchange, targetRoutingKey);
    }

    public void requeueFirstMessageInQueue(String vhost, String queue, String checksum) {
        operations.requeueFirstMessageInQueue(vhost, queue, checksum);
    }
}
