package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.gessnerfl.rabbitmq.queue.management.javaconfig.RabbitMqSettingsConfig;
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
    private final RabbitMqSettingsConfig rabbitMqSettingsConfig;
    
    @Autowired
    public RabbitMqFacade(ManagementApi managementApi, Operations operations, RabbitMqSettingsConfig rabbitMqSettingsConfig){
        this.managementApi = managementApi;
        this.operations = operations;
        this.rabbitMqSettingsConfig = rabbitMqSettingsConfig;
    }

    public List<Exchange> getExchanges(){
        return managementApi.getExchanges(rabbitMqSettingsConfig.getVhost());
    }
    
    public List<Queue> getQueues(){
        return managementApi.getQueues(rabbitMqSettingsConfig.getVhost());
    }
    
    public List<Binding> getExchangeSourceBindings(String exchange){
        return managementApi.getExchangeSourceBindings(rabbitMqSettingsConfig.getVhost(), exchange);
    }
    
    public List<Binding> getQueueBindings(String queue){
        return managementApi.getQueueBindings(rabbitMqSettingsConfig.getVhost(), queue);
    }
    
    public List<Message> getMessagesOfQueue(String queue, int limit){
        return operations.getMessagesOfQueue(queue, limit);
    }
    
    public void deleteFirstMessageInQueue(String queue, String messageChecksum){
        operations.deleteFirstMessageInQueue(queue, messageChecksum);
    }
    
    public void moveFirstMessageInQueue(String queue, String messageChecksum, String targetExchange, String targetRoutingKey){
        operations.moveFirstMessageInQueue(queue, messageChecksum, targetExchange, targetRoutingKey);
    }
}
