package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.remoteapi;

import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.config.RabbitMqConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ManagementApiUrlBuilder {
    private final RabbitMqConfig rabbitMqConfig;

    @Autowired
    public ManagementApiUrlBuilder(RabbitMqConfig rabbitMqConfig) {
        this.rabbitMqConfig = rabbitMqConfig;
    }

    public String buildForConfiguration() {
        String protocol = rabbitMqConfig.isManagemnetPortSecured() ? "https" : "http";
        String host = rabbitMqConfig.getHostname();
        Integer port = rabbitMqConfig.getManagementPort();
        return protocol + "://" + host + ":" + port + "/api";
    }
}
