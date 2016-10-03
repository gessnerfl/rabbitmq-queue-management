package de.gessnerfl.rabbitmq.queue.management.connection;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.ConnectionFactory;

import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.BrokerConfig;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.config.RabbitMqBrokers;

@Service
public class ConnectionFactories {
    private final RabbitMqBrokers brokers;

    private ConcurrentMap<String, ConnectionFactory> connectionFactories = new ConcurrentHashMap<>();
    
    @Autowired
    public ConnectionFactories(RabbitMqBrokers brokers) {
        this.brokers = brokers;
    }
    
    public ConnectionFactory getOrCreate(String brokerName){
        return connectionFactories.computeIfAbsent(brokerName, this::createFor);
    }
    
    private ConnectionFactory createFor(String brokerName){
        BrokerConfig brokerConfig = brokers.getBrokerConfig(brokerName);
        
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(brokerConfig.getHostname());
        connectionFactory.setPort(brokerConfig.getPort());
        connectionFactory.setVirtualHost(brokerConfig.getVhost());
        connectionFactory.setUsername(brokerConfig.getUsername());
        connectionFactory.setPassword(brokerConfig.getPassword());
        return connectionFactory;
    }
}
