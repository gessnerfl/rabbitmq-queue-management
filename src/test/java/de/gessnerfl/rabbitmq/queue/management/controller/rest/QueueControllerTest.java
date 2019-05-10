package de.gessnerfl.rabbitmq.queue.management.controller.rest;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Queue;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.RabbitMqFacade;

@RunWith(MockitoJUnitRunner.class)
public class QueueControllerTest {
    
    private final static String VHOST = "vhost";
    
    @Mock
    private RabbitMqFacade rabbitMqFacade;
    
    @InjectMocks
    private QueueController sut;

    @Test
    public void shouldDelegateCallToGetAllQueues(){
        Queue queue = mock(Queue.class);
        List<Queue> queues = Arrays.asList(queue);

        when(rabbitMqFacade.getQueues()).thenReturn(queues);

        List<Queue> result = sut.getQueues("");

        assertSame(queues, result);
        verify(rabbitMqFacade).getQueues();
        verifyNoMoreInteractions(rabbitMqFacade);
    }
    
    @Test
    public void shouldDelegateCallToGetAllQueuesOfVhost(){
        Queue queue = mock(Queue.class);
        List<Queue> queues = Arrays.asList(queue);
        
        when(rabbitMqFacade.getQueues(VHOST)).thenReturn(queues);
        
        List<Queue> result = sut.getQueues(VHOST);
        
        assertSame(queues, result);
        verify(rabbitMqFacade).getQueues(VHOST);
        verifyNoMoreInteractions(rabbitMqFacade);
    }
}
