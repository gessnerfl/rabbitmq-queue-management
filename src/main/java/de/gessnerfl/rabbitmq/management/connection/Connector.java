package de.gessnerfl.rabbitmq.management.connection;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import de.gessnerfl.rabbitmq.management.config.RabbitMqSettingsConfig;

@Service
public class Connector {
	private final static Logger LOGGER = LoggerFactory.getLogger(Connector.class);

	private final ConnectionFactory connectionFactory;
	private final RabbitMqSettingsConfig settingsConfig;

	private Connection connection;

	@Autowired
	public Connector(ConnectionFactory connectionFactory, RabbitMqSettingsConfig settingsConfig) {
		this.connectionFactory = connectionFactory;
		this.settingsConfig = settingsConfig;
	}

	public CloseableChannelWrapper connect() {
		Connection connection = getConnection();
		try {
			Channel channel = connection.createChannel();
			return new CloseableChannelWrapper(channel);
		} catch (IOException e) {
			throw new ConnectionFailedException(e);
		}
	}

	private Connection getConnection() {
		if (connection == null || !connection.isOpen()) {
			try {
				String addressesString = settingsConfig.getAddresses();
				Address[] addresses = Address.parseAddresses(addressesString);
				connection = connectionFactory.newConnection(addresses);
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
