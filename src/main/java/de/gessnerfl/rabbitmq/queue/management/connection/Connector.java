package de.gessnerfl.rabbitmq.queue.management.connection;

import java.io.IOException;
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

  private final ConnectionFactory connectionFactory;

  private Connection connection;

  @Autowired
  public Connector(ConnectionFactory connectionFactory) {
    this.connectionFactory = connectionFactory;
  }

  public CloseableChannelWrapper connectAsClosable() {
    try {
      Channel channel = connect();
      return new CloseableChannelWrapper(channel);
    } catch (IOException e) {
      throw new ConnectionFailedException(e);
    }
  }

  public Channel connect() throws IOException {
    Connection connection = getConnection();
    return connection.createChannel();
  }

  private Connection getConnection() {
    if (connection == null || !connection.isOpen()) {
      try {
        connection = connectionFactory.newConnection();
      } catch (IOException | TimeoutException e) {
        throw new ConnectionFailedException(e);
      }
    }
    return connection;
  }

  @PreDestroy
  public void shutdown() {
    try {
      connection.close();
    } catch (IOException e) {
      LOGGER.warn("Failed to close RabbitMQ connection", e);
    }
  }

}
