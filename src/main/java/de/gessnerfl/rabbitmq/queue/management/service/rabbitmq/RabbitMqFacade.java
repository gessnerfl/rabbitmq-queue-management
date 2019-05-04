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
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.remoteapi.ManagementApiFactory;

@Service
public class RabbitMqFacade {
    
    private final ManagementApiFactory managementApiFactory;
    private final Operations operations;
    
    @Autowired
    public RabbitMqFacade(ManagementApiFactory managementApiFactory, Operations operations){
        this.managementApiFactory = managementApiFactory;
        this.operations = operations;
    }

    public List<Exchange> getExchanges(String vhost){
        return getManagementApi().getExchanges(vhost);
    }

    public List<Queue> getQueues(){
        return getManagementApi().getQueues();
    }
    
    public List<Queue> getQueues(String vhost){
        return getManagementApi().getQueues(vhost);
    }
    
    public List<Binding> getExchangeSourceBindings(String vhost, String exchange){
        return getManagementApi().getExchangeSourceBindings(vhost, exchange);
    }
    
    public List<Binding> getQueueBindings(String vhost, String queueName){
        return getManagementApi().getQueueBindings(vhost, queueName);
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
    
    private ManagementApi getManagementApi(){
        return managementApiFactory.createFor();
    }

    public void requeueFirstMessageInQueue(String vhost, String queue, String checksum) {
        operations.requeueFirstMessageInQueue(vhost, queue, checksum);
    }
}
