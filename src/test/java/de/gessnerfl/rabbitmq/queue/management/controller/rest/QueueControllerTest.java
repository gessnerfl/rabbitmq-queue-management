package de.gessnerfl.rabbitmq.queue.management.controller.rest;

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

import de.gessnerfl.rabbitmq.queue.management.model.AmqpMessage;
import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Queue;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.RabbitMqFacade;

@RunWith(MockitoJUnitRunner.class)
public class QueueControllerTest {
    
    private final static String BROKER = "broker";
    
    private final static String QUEUE = "queue";
    private final static String CHECKSUM = "checksum";
    private final static String EXCHANGE = "exchange";
    private final static String ROUTING_KEY = "routing-key";
    
    @Mock
    private RabbitMqFacade rabbitMqFacade;
    
    @InjectMocks
    private QueueController sut;
    
    @Test
    public void shouldDelegateCallToGetAllQueues(){
        Queue queue = mock(Queue.class);
        List<Queue> queues = Arrays.asList(queue);
        
        when(rabbitMqFacade.getQueues(BROKER)).thenReturn(queues);
        
        List<Queue> result = sut.getQueues(BROKER);
        
        assertSame(queues, result);
        verify(rabbitMqFacade).getQueues(BROKER);
    }
    
    @Test
    public void shouldDelegateCallToGetAllMessagesOfQueue(){
        AmqpMessage message = mock(AmqpMessage.class);
        List<AmqpMessage> messages = Arrays.asList(message);
        when(rabbitMqFacade.getMessagesOfQueue(BROKER, QUEUE, QueueController.DEFAULT_LIMIT)).thenReturn(messages);
        
        List<AmqpMessage> result = sut.getQueueMessages(BROKER, QUEUE);
        
        assertSame(messages, result);
        verify(rabbitMqFacade).getMessagesOfQueue(BROKER, QUEUE, QueueController.DEFAULT_LIMIT);
    }

    @Test
    public void shouldDelegateCallToDeleteMessage(){
        sut.deleteFirstMessageInQueue(BROKER, QUEUE, CHECKSUM);
        
        verify(rabbitMqFacade).deleteFirstMessageInQueue(BROKER, QUEUE, CHECKSUM);
    }
    
    @Test
    public void shouldDelegateCallToMoveMessage(){
        sut.moveFirstMessageInQueue(BROKER, QUEUE, CHECKSUM, EXCHANGE, ROUTING_KEY);
        
        verify(rabbitMqFacade).moveFirstMessageInQueue(BROKER, QUEUE, CHECKSUM, EXCHANGE, ROUTING_KEY);
    }
}
