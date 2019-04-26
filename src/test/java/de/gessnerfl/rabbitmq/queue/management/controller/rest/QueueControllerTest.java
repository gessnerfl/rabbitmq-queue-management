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
import org.mockito.junit.MockitoJUnitRunner;

import de.gessnerfl.rabbitmq.queue.management.model.Message;
import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Queue;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.RabbitMqFacade;

@RunWith(MockitoJUnitRunner.class)
public class QueueControllerTest {
    
    private final static String VHOST = "vhost";
    
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
        
        when(rabbitMqFacade.getQueues(VHOST)).thenReturn(queues);
        
        List<Queue> result = sut.getQueuesOfVirtualHost(VHOST);
        
        assertSame(queues, result);
        verify(rabbitMqFacade).getQueues(VHOST);
    }
    
    @Test
    public void shouldDelegateCallToGetAllMessagesOfQueue(){
        Message message = mock(Message.class);
        List<Message> messages = Arrays.asList(message);
        when(rabbitMqFacade.getMessagesOfQueue(VHOST, QUEUE, QueueController.DEFAULT_LIMIT)).thenReturn(messages);
        
        List<Message> result = sut.getQueueMessages(VHOST, QUEUE);
        
        assertSame(messages, result);
        verify(rabbitMqFacade).getMessagesOfQueue(VHOST, QUEUE, QueueController.DEFAULT_LIMIT);
    }

    @Test
    public void shouldDelegateCallToDeleteMessage(){
        sut.deleteFirstMessageInQueue(VHOST, QUEUE, CHECKSUM);
        
        verify(rabbitMqFacade).deleteFirstMessageInQueue(VHOST, QUEUE, CHECKSUM);
    }
    
    @Test
    public void shouldDelegateCallToMoveMessage(){
        sut.moveFirstMessageInQueue(VHOST, QUEUE, CHECKSUM, EXCHANGE, ROUTING_KEY);
        
        verify(rabbitMqFacade).moveFirstMessageInQueue(VHOST, QUEUE, CHECKSUM, EXCHANGE, ROUTING_KEY);
    }
}
