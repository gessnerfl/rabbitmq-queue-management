package de.gessnerfl.rabbitmq.management.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;

import de.gessnerfl.rabbitmq.management.connection.CloseableChannelWrapper;
import de.gessnerfl.rabbitmq.management.connection.Connector;
import de.gessnerfl.rabbitmq.management.model.Message;

@Service
public class MessageRepository {
	static final Logger LOGGER = LoggerFactory.getLogger(MessageRepository.class);
	public static final int DEFAULT_FETCH_COUNT = 10;

	private final Connector connector;

	@Autowired
	public MessageRepository(Connector connector) {
		this.connector = connector;
	}

	public List<Message> getMessagesFromQueue(String queue) {
		try (CloseableChannelWrapper wrapper = connector.connect()) {
			List<Message> messages = new ArrayList<>();
			Channel channel = wrapper.getChannel();
			channel.basicQos(DEFAULT_FETCH_COUNT);
			int fetched = 0;
			boolean messagesAvailable = true;
			Long lastDeliveryTag = null;
			while(fetched < DEFAULT_FETCH_COUNT && messagesAvailable){
				GetResponse response = channel.basicGet(queue, false);
				messages.add(new Message(response.getEnvelope(), response.getProps(), response.getBody()));
				lastDeliveryTag = response.getEnvelope().getDeliveryTag();
				fetched++;
				messagesAvailable = response.getMessageCount() > 0;
			}
			if(lastDeliveryTag != null){
			  channel.basicNack(lastDeliveryTag, true, true);
			}
			return messages;
		} catch (IOException e) {
			throw new MessageFetchFailedException(e);
		}
	}

}
