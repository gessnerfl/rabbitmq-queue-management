package de.gessnerfl.rabbitmq.queue.management.controller.rest;

import de.gessnerfl.rabbitmq.queue.management.model.Message;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.RabbitMqFacade;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class MessageControllerTest {

    private final static String VHOST = "vhost";

    private final static String QUEUE = "queue";
    private final static String CHECKSUM = "checksum";
    private final static String EXCHANGE = "exchange";
    private final static String ROUTING_KEY = "routing-key";

    @Mock
    private RabbitMqFacade rabbitMqFacade;

    @InjectMocks
    private MessageController sut;

    @Test
    public void shouldDelegateCallToGetAllMessagesOfQueue(){
        Message message = mock(Message.class);
        List<Message> messages = Arrays.asList(message);
        when(rabbitMqFacade.getMessagesOfQueue(VHOST, QUEUE, MessageController.DEFAULT_LIMIT)).thenReturn(messages);

        List<Message> result = sut.getQueueMessages(VHOST, QUEUE);

        assertSame(messages, result);
        verify(rabbitMqFacade).getMessagesOfQueue(VHOST, QUEUE, MessageController.DEFAULT_LIMIT);
        verifyNoMoreInteractions(rabbitMqFacade);
    }

    @Test
    public void shouldDelegateCallToDeleteAllMessagesInQueue(){
        sut.deleteAllMessageInQueue(VHOST, QUEUE);

        verify(rabbitMqFacade).purgeQueue(VHOST, QUEUE);
        verifyNoMoreInteractions(rabbitMqFacade);
    }

    @Test
    public void shouldDelegateCallToDeleteFirstMessageInQueue(){
        sut.deleteFirstMessageInQueue(VHOST, QUEUE, CHECKSUM);

        verify(rabbitMqFacade).deleteFirstMessageInQueue(VHOST, QUEUE, CHECKSUM);
        verifyNoMoreInteractions(rabbitMqFacade);
    }

    @Test
    public void shouldDelegateCallToMoveAllMessagesInQueue(){
        sut.moveAllMessageInQueue(VHOST, QUEUE, EXCHANGE, ROUTING_KEY);

        verify(rabbitMqFacade).moveAllMessagesInQueue(VHOST, QUEUE, EXCHANGE, ROUTING_KEY);
        verifyNoMoreInteractions(rabbitMqFacade);
    }

    @Test
    public void shouldDelegateCallToMoveFirstMessageInQueue(){
        sut.moveFirstMessageInQueue(VHOST, QUEUE, CHECKSUM, EXCHANGE, ROUTING_KEY);

        verify(rabbitMqFacade).moveFirstMessageInQueue(VHOST, QUEUE, CHECKSUM, EXCHANGE, ROUTING_KEY);
        verifyNoMoreInteractions(rabbitMqFacade);
    }

    @Test
    public void shouldDelegateCallToRequeueAllMessagesInQueue(){
        sut.requeueAllMessageInQueue(VHOST, QUEUE);

        verify(rabbitMqFacade).requeueAllMessagesInQueue(VHOST, QUEUE);
        verifyNoMoreInteractions(rabbitMqFacade);
    }

    @Test
    public void shouldDelegateCallToRequeueFirstMessageInQueue(){
        sut.requeueFirstMessageInQueue(VHOST, QUEUE, CHECKSUM);

        verify(rabbitMqFacade).requeueFirstMessageInQueue(VHOST, QUEUE, CHECKSUM);
        verifyNoMoreInteractions(rabbitMqFacade);
    }

}