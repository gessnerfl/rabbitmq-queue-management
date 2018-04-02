package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.gessnerfl.rabbitmq.queue.management.model.AmqpMessage;
import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Binding;
import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Exchange;
import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Queue;
import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.BrokerConfig;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.config.RabbitMqBrokers;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations.Operations;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.remoteapi.ManagementApi;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.remoteapi.ManagementApiFactory;

@Service
public class RabbitMqFacade {
    
    private final ManagementApiFactory managementApiFactory;
    private final Operations operations;
    private final RabbitMqBrokers rabbitMqBrokers;
    
    @Autowired
    public RabbitMqFacade(ManagementApiFactory managementApiFactory, Operations operations, RabbitMqBrokers rabbitMqBrokers){
        this.managementApiFactory = managementApiFactory;
        this.operations = operations;
        this.rabbitMqBrokers = rabbitMqBrokers;
    }

    public List<Exchange> getExchanges(String brokerName){
        return getManagementApi(brokerName).getExchanges(getVhost(brokerName));
    }
    
    public List<Queue> getQueues(String brokerName){
        return getManagementApi(brokerName).getQueues(getVhost(brokerName));
    }
    
    public List<Binding> getExchangeSourceBindings(String brokerName, String exchange){
        return getManagementApi(brokerName).getExchangeSourceBindings(getVhost(brokerName), exchange);
    }
    
    public List<Binding> getQueueBindings(String brokerName, String queueName){
        return getManagementApi(brokerName).getQueueBindings(getVhost(brokerName), queueName);
    }
    
    public List<AmqpMessage> getMessagesOfQueue(String brokerName, String queueName, int limit){
        return operations.getMessagesOfQueue(brokerName, queueName, limit);
    }
    
    public void deleteFirstMessageInQueue(String brokerName, String queueName, String messageChecksum){
        operations.deleteFirstMessageInQueue(brokerName, queueName, messageChecksum);
    }
    
    public void moveFirstMessageInQueue(String brokerName, String queueName, String messageChecksum, String targetExchange, String targetRoutingKey){
        operations.moveFirstMessageInQueue(brokerName, queueName, messageChecksum, targetExchange, targetRoutingKey);
    }
    
    private ManagementApi getManagementApi(String brokerName){
        return managementApiFactory.createFor(brokerName);
    }

    private String getVhost(String brokerName) {
        BrokerConfig brokerConfig = rabbitMqBrokers.getBrokerConfig(brokerName);
        return brokerConfig.getVhost();
    }
}
