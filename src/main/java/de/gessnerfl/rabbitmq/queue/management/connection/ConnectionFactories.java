package de.gessnerfl.rabbitmq.queue.management.connection;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.ConnectionFactory;

import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.config.RabbitMqConfig;

@Service
public class ConnectionFactories {
    private final RabbitMqConfig configuration;

    private final ConcurrentMap<String, ConnectionFactory> connectionsByVirtualHost = new ConcurrentHashMap<>();

    @Autowired
    public ConnectionFactories(RabbitMqConfig configuration) {
        this.configuration = configuration;
    }

    public ConnectionFactory getOrCreate(String vhost) {
        return connectionsByVirtualHost.computeIfAbsent(vhost, this::createFor);
    }

    private ConnectionFactory createFor(String vhost) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(configuration.getHostname());
        connectionFactory.setPort(configuration.getPort());
        connectionFactory.setVirtualHost(vhost);
        connectionFactory.setUsername(configuration.getUsername());
        connectionFactory.setPassword(configuration.getPassword());
        if (configuration.useSsl()) {
            try {
                connectionFactory.useSslProtocol();
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                throw new ConnectionFailedException(e);
            }
        }
        return connectionFactory;
    }
}
