package de.gessnerfl.rabbitmq.queue.management.connection;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

@Service
public class Connector {
    private static final Logger LOGGER = LoggerFactory.getLogger(Connector.class);

    private final ConnectionFactories connectionFactories;

    Map<String, Connection> connections = new HashMap<>();

    @Autowired
    public Connector(ConnectionFactories connectionFactories) {
        this.connectionFactories = connectionFactories;
    }

    public CloseableChannelWrapper connectAsClosable(String vhost) {
        Channel channel = connect(vhost);
        return new CloseableChannelWrapper(channel);
    }

    public Channel connect(String vhost) {
        try {
            Connection connection = getConnection(vhost);
            return connection.createChannel();
        } catch (IOException e) {
            throw new ConnectionFailedException(e);
        }
    }

    private synchronized Connection getConnection(String vhost) {
        if (connections.containsKey(vhost)) {
            Connection connection = connections.get(vhost);
            if (connection == null || !connection.isOpen()) {
                return initializeNewConnection(vhost);
            }
            return connection;
        } else {
            return initializeNewConnection(vhost);
        }
    }

    private Connection initializeNewConnection(String vhost) {
        try {
            ConnectionFactory connectionFactory = connectionFactories.getOrCreate(vhost);
            Connection connection = connectionFactory.newConnection();
            connections.put(vhost, connection);
            return connection;
        } catch (IOException | TimeoutException e) {
            throw new ConnectionFailedException(e);
        }
    }

    @PreDestroy
    public void shutdown() {
        for (Connection connection : connections.values()) {
            try {
                connection.close();
            } catch (IOException e) {
                LOGGER.warn("Failed to close RabbitMQ connection", e);
            }
        }
    }

}
