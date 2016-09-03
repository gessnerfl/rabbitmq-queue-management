package de.gessnerfl.rabbitmq.management.service;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;

import de.gessnerfl.rabbitmq.management.AbstractIntegrationTest;
import de.gessnerfl.rabbitmq.management.connection.CloseableChannelWrapper;
import de.gessnerfl.rabbitmq.management.connection.Connector;
import de.gessnerfl.rabbitmq.management.model.Message;
import de.gessnerfl.rabbitmq.management.service.MessageRepository;

public class MessageRepositoryIntegrationTest extends AbstractIntegrationTest {
  private final static Logger LOGGER =
      LoggerFactory.getLogger(MessageRepositoryIntegrationTest.class);

  private final static String EXCHANGE_NAME = "test.direct";
  private final static String QUEUE_NAME = "test.queue";
  private final static String DEFAULT_BODY_STRING = "default-body-";

  @Autowired
  private Connector connector;

  @Autowired
  private MessageRepository sut;

  @Before
  public void init() throws Exception {
    try (CloseableChannelWrapper wrapper = connector.connect()) {
      Channel channel = wrapper.getChannel();
      declareExchange(channel);
      declareQueue(channel);
    } catch (IOException e) {
      cleanup();
      throw e;
    }
  }

  @After
  public void cleanup() {
    try (CloseableChannelWrapper wrapper = connector.connect()) {
      Channel channel = wrapper.getChannel();
      deleteQueue(channel);
      deleteExchange(channel);
    }
  }

  @Test
  public void shouldReturnAllMessagesWhenNumberOfMessagesIsExactlyTheSameAsTheFetchCount() throws IOException {
    int numberOfMessages = MessageRepository.DEFAULT_FETCH_COUNT;
    publishMessages(numberOfMessages);
    
    List<Message> messages = sut.getMessagesFromQueue(QUEUE_NAME);

    assertThat(messages, hasSize(numberOfMessages));
    for(int i = 0; i < numberOfMessages; i++){
      byte[] body = buildMessage(i);
      assertArrayEquals(body, messages.get(i).getBody());
    }
  }
  
  @Test
  public void shouldReturnAllMessagesWhenNumberOfMessagesIsLessThenTheFetchCount() throws IOException {
    int numberOfMessages = MessageRepository.DEFAULT_FETCH_COUNT - 1;
    publishMessages(numberOfMessages);
    
    List<Message> messages = sut.getMessagesFromQueue(QUEUE_NAME);

    assertThat(messages, hasSize(numberOfMessages));
    for(int i = 0; i < numberOfMessages; i++){
      byte[] body = buildMessage(i);
      assertArrayEquals(body, messages.get(i).getBody());
    }
  }
  
  @Test
  public void shouldReturnSubsetOfMessagesWhenNumberOfMessagesIsAboveFetchCount() throws IOException {
    int numberOfMessages = MessageRepository.DEFAULT_FETCH_COUNT + 1;
    publishMessages(numberOfMessages);
    
    List<Message> messages = sut.getMessagesFromQueue(QUEUE_NAME);

    assertThat(messages, hasSize(MessageRepository.DEFAULT_FETCH_COUNT));
    for(int i = 0; i < MessageRepository.DEFAULT_FETCH_COUNT; i++){
      byte[] body = buildMessage(i);
      assertArrayEquals(body, messages.get(i).getBody());
    }
  }

  private void declareExchange(Channel channel) throws IOException {
    channel.exchangeDeclare(EXCHANGE_NAME, "direct", false, true, null);
  }

  private void declareQueue(Channel channel) throws IOException {
    channel.queueDeclare(QUEUE_NAME, false, false, true, null);
    channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, QUEUE_NAME);
  }

  private void publishMessages(int numberOfMessages) throws IOException {
    try (CloseableChannelWrapper wrapper = connector.connect()) {
      Channel channel = wrapper.getChannel();
      for (int i = 0; i < numberOfMessages; i++) {
        byte[] body = buildMessage(i);
        channel.basicPublish(EXCHANGE_NAME, QUEUE_NAME, MessageProperties.TEXT_PLAIN, body);
      }
    }
  }

  private byte[] buildMessage(int i) {
    String message = DEFAULT_BODY_STRING + i;
    byte[] body = message.getBytes(StandardCharsets.UTF_8);
    return body;
  }

  private void deleteQueue(Channel channel) {
    try {
      channel.queueDelete(QUEUE_NAME);
    } catch (IOException e) {
      LOGGER.error("Failed to delete queue", e);
    }
  }

  private void deleteExchange(Channel channel) {
    try {
      channel.exchangeDelete(EXCHANGE_NAME);
    } catch (IOException e) {
      LOGGER.error("Failed to delete queue", e);
    }
  }

}
