package de.gessnerfl.rabbitmq.deadletter.manager.service;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import de.gessnerfl.rabbitmq.deadletter.manager.model.Message;
import de.gessnerfl.rabbitmq.deadletter.manager.repository.MessageRepository;

@RunWith(MockitoJUnitRunner.class)
public class DeadLetterQueueServiceTest {

  @Mock
  private MessageRepository messageRepository;

  @InjectMocks
  private DeadLetterQueueService sut;

  @Test
  public void shouldReturnAllMessagesOfTheGivenQueue() {
    final String queueName = "foo";
    final int maxNumberOfMessages = 10;
    final Message message = mock(Message.class);
    final List<Message> messages = Arrays.asList(message);

    when(messageRepository.getMessagesFromQueue(queueName, maxNumberOfMessages))
        .thenReturn(messages);

    List<Message> result = sut.getMessages(queueName, maxNumberOfMessages);

    assertSame(messages, result);
    verify(messageRepository).getMessagesFromQueue(queueName, maxNumberOfMessages);
  }

}
