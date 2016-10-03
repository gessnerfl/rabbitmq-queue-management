package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.remoteapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.BrokerConfig;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.config.RabbitMqBrokers;

@Service
public class ManagementApiUrlBuilder {
    private final RabbitMqBrokers rabbitMqBrokers;

    @Autowired
    public ManagementApiUrlBuilder(RabbitMqBrokers rabbitMqBrokers) {
        this.rabbitMqBrokers = rabbitMqBrokers;
    }

    public String buildForConfiguration(String brokerName) {
        BrokerConfig brokerConfig = rabbitMqBrokers.getBrokerConfig(brokerName);

        String protocol = brokerConfig.isManagemnetPortSecured() ? "https" : "http";
        String host = brokerConfig.getHostname();
        Integer port = brokerConfig.getManagementPort();
        return protocol + "://" + host + ":" + port + "/api";
    }
}
