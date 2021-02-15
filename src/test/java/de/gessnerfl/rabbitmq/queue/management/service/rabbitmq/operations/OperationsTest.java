package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import de.gessnerfl.rabbitmq.queue.management.model.Message;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OperationsTest {
    private final static String VHOST = "vhost";
    private final static String QUEUE_NAME = "queue";
    private final static int MAX_NUMBER_MESSAGE = 10;
    private final static String CHECKSUM = "checksum";
    private final static String TARGET_EXCHANGE = "targetExchange";
    private final static String TARGET_ROUTING_KEY = "targetRoutingKey";
    
    @Mock
    private QueueListOperation queueListOperation;
    @Mock
    private MessageMoveOperation messageMoveOperation;
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
        
        when(queueListOperation.getMessagesFromQueue(VHOST, QUEUE_NAME, MAX_NUMBER_MESSAGE)).thenReturn(messages);
        
        List<Message> result = sut.getMessagesOfQueue(VHOST, QUEUE_NAME, MAX_NUMBER_MESSAGE);
        
        assertSame(messages, result);
        verify(queueListOperation).getMessagesFromQueue(VHOST, QUEUE_NAME, MAX_NUMBER_MESSAGE);
        verifyNoMoreInteractions(queueListOperation, messageMoveOperation, messageDeleteOperation, messageRequeueOperation);
    }
    
    @Test
    public void shouldDelegateDeleteOperation(){
        sut.deleteFirstMessageInQueue(VHOST, QUEUE_NAME, CHECKSUM);
        
        verify(messageDeleteOperation).deleteFirstMessageInQueue(VHOST, QUEUE_NAME, CHECKSUM);
        verifyNoMoreInteractions(queueListOperation, messageMoveOperation, messageDeleteOperation, messageRequeueOperation);
    }
    
    @Test
    public void shouldDelegateMoveAllMessagesOperation(){
        sut.moveAllMessagesInQueue(VHOST, QUEUE_NAME, TARGET_EXCHANGE, TARGET_ROUTING_KEY);
        
        verify(messageMoveOperation).moveAllMessages(VHOST, QUEUE_NAME, TARGET_EXCHANGE, TARGET_ROUTING_KEY);
        verifyNoMoreInteractions(queueListOperation, messageMoveOperation, messageDeleteOperation, messageRequeueOperation);
    }

    @Test
    public void shouldDelegateMoveSingleMessageOperation(){
        sut.moveFirstMessageInQueue(VHOST, QUEUE_NAME, CHECKSUM, TARGET_EXCHANGE, TARGET_ROUTING_KEY);

        verify(messageMoveOperation).moveFirstMessage(VHOST, QUEUE_NAME, CHECKSUM, TARGET_EXCHANGE, TARGET_ROUTING_KEY);
        verifyNoMoreInteractions(queueListOperation, messageMoveOperation, messageDeleteOperation, messageRequeueOperation);
    }

    @Test
    public void shouldDelegateRequeueAllMessagesOperation(){
        sut.requeueAllMessagesInQueue(VHOST, QUEUE_NAME);

        verify(messageRequeueOperation).requeueAllMessages(VHOST, QUEUE_NAME);
        verifyNoMoreInteractions(queueListOperation, messageMoveOperation, messageDeleteOperation, messageRequeueOperation);
    }

    @Test
    public void shouldDelegateRequeueSingleMessageOperation(){
        sut.requeueFirstMessageInQueue(VHOST, QUEUE_NAME, CHECKSUM);

        verify(messageRequeueOperation).requeueFirstMessage(VHOST, QUEUE_NAME, CHECKSUM);
        verifyNoMoreInteractions(queueListOperation, messageMoveOperation, messageDeleteOperation, messageRequeueOperation);
    }
}
