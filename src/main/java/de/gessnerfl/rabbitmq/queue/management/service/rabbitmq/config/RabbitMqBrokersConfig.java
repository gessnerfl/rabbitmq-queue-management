package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.BrokerConfig;

@Component
@ConfigurationProperties(prefix = "de.gessnerfl.rabbitmq")
public class RabbitMqBrokersConfig {
    
    private Map<String, BrokerConfig> brokers = new HashMap<>();

    public Map<String, BrokerConfig> getBrokers() {
        return brokers;
    }

    public void setBrokers(Map<String, BrokerConfig> brokers) {
        this.brokers = brokers;
    }
    
    
}
