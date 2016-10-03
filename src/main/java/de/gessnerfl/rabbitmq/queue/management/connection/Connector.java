package de.gessnerfl.rabbitmq.queue.management.connection;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

@Service
public class Connector {
    private final static Logger LOGGER = LoggerFactory.getLogger(Connector.class);

    private final ConnectionFactories connectionFactories;

    Map<String, Connection> connections = new HashMap<>();

    @Autowired
    public Connector(ConnectionFactories connectionFactories) {
        this.connectionFactories = connectionFactories;
    }

    public CloseableChannelWrapper connectAsClosable(String brokerName) {
        Channel channel = connect(brokerName);
        return new CloseableChannelWrapper(channel);
    }

    public Channel connect(String brokerName) {
        try {
            Connection connection = getConnection(brokerName);
            return connection.createChannel();
        } catch (IOException e) {
            throw new ConnectionFailedException(e);
        }
    }

    private synchronized Connection getConnection(String brokerName) {
        if (connections.containsKey(brokerName)) {
            Connection connection = connections.get(brokerName);
            if (connection == null || !connection.isOpen()) {
                return initializeNewConnection(brokerName);
            }
            return connection;
        } else {
            return initializeNewConnection(brokerName);
        }
    }

    private Connection initializeNewConnection(String brokerName) {
        try {
            ConnectionFactory connectionFactory = connectionFactories.getOrCreate(brokerName);
            Connection connection = connectionFactory.newConnection();
            connections.put(brokerName, connection);
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
