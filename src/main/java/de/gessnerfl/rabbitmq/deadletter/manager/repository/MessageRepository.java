package de.gessnerfl.rabbitmq.deadletter.manager.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;

import de.gessnerfl.rabbitmq.deadletter.manager.connection.CloseableChannelWrapper;
import de.gessnerfl.rabbitmq.deadletter.manager.connection.Connector;
import de.gessnerfl.rabbitmq.deadletter.manager.model.Message;

@Repository
public class MessageRepository {
  static final Logger LOGGER = LoggerFactory.getLogger(MessageRepository.class);
  public static final int DEFAULT_FETCH_COUNT = 10;

  private final Connector connector;

  @Autowired
  public MessageRepository(Connector connector) {
    this.connector = connector;
  }

  public List<Message> getMessagesFromQueue(String queue, int maxNumberOfMessages) {
    try (CloseableChannelWrapper wrapper = connector.connectAsClosable()) {
      List<Message> messages = new ArrayList<>();
      Channel channel = wrapper.getChannel();
      channel.basicQos(DEFAULT_FETCH_COUNT);
      int fetched = 0;
      boolean messagesAvailable = true;
      Long lastDeliveryTag = null;
      while (fetched < maxNumberOfMessages && messagesAvailable) {
        GetResponse response = channel.basicGet(queue, false);
        messages.add(new Message(response.getEnvelope(), response.getProps(), response.getBody()));
        lastDeliveryTag = response.getEnvelope().getDeliveryTag();
        fetched++;
        messagesAvailable = response.getMessageCount() > 0;
      }
      if (lastDeliveryTag != null) {
        channel.basicNack(lastDeliveryTag, true, true);
      }
      return messages;
    } catch (IOException e) {
      throw new MessageFetchFailedException(e);
    }
  }

}
