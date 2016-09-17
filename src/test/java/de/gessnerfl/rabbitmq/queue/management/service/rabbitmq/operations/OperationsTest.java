package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

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

import de.gessnerfl.rabbitmq.queue.management.model.Message;

@RunWith(MockitoJUnitRunner.class)
public class OperationsTest {
    private final static String QUEUE_NAME = "queue";
    private final static int MAX_NUMBER_MESSAGE = 10;
    private final static String CHECKSUM = "checksum";
    private final static String TARGET_EXCHANGE = "targetExchange";
    private final static String TARGET_ROUTING_KEY = "targetRoutingKey";
    
    @Mock
    private QueueListOperation queueListOperation;
    @Mock
    private MessageRequeueOperation messageRequeueOperation;
    @Mock
    private MessageDeleteOperation messageDeleteOperation;
    
    @InjectMocks
    private Operations sut;
    
    @Test
    public void shouldDelegateListOperation(){
        Message message = mock(Message.class);
        List<Message> messages = Arrays.asList(message);
        
        when(queueListOperation.getMessagesFromQueue(QUEUE_NAME, MAX_NUMBER_MESSAGE)).thenReturn(messages);
        
        List<Message> result = sut.getMessagesOfQueue(QUEUE_NAME, MAX_NUMBER_MESSAGE);
        
        assertSame(messages, result);
        verify(queueListOperation).getMessagesFromQueue(QUEUE_NAME, MAX_NUMBER_MESSAGE);
    }
    
    @Test
    public void shouldDelegateDeleteOperation(){
        sut.deleteFirstMessageInQueue(QUEUE_NAME, CHECKSUM);
        
        verify(messageDeleteOperation).deleteFirstMessageInQueue(QUEUE_NAME, CHECKSUM);
    }
    
    @Test
    public void shouldDelegateRequeueOperation(){
        sut.requeueFirstMessageInQueue(QUEUE_NAME, CHECKSUM, TARGET_EXCHANGE, TARGET_ROUTING_KEY);
        
        verify(messageRequeueOperation).requeueFirstMessage(QUEUE_NAME, CHECKSUM, TARGET_EXCHANGE, TARGET_ROUTING_KEY);
    }
}
