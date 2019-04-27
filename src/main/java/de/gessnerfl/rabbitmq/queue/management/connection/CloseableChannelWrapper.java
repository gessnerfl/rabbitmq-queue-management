package de.gessnerfl.rabbitmq.queue.management.connection;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;

public class CloseableChannelWrapper implements AutoCloseable {

	private static final Logger LOGGER = LoggerFactory.getLogger(CloseableChannelWrapper.class);
	private final Channel delegate;

	public CloseableChannelWrapper(Channel delegate) {
		super();
		this.delegate = delegate;
	}

	public Channel getChannel() {
		return delegate;
	}

	@Override
	public void close() {
		try {
			delegate.close();
		} catch (IOException | TimeoutException e) {
			LOGGER.warn("Failed to close RabbitMQ channel.", e);
		}
	}

}
