package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import de.gessnerfl.rabbitmq.queue.management.model.Message;
import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Binding;
import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Exchange;
import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Queue;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations.Operations;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.remoteapi.ManagementApi;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RabbitMqFacadeTest {
    private final static String VHOST = "vhost";
    private final static String QUEUE = "queue";
    private final static int MAX_NUMBER_MESSAGE = 10;
    private final static String CHECKSUM = "checksum";
    private final static String EXCHANGE = "exchange";
    private final static String ROUTING_KEY = "routingKey";

    @Mock
    private ManagementApi managementApi;
    @Mock
    private Operations operations;

    @InjectMocks
    private RabbitMqFacade sut;

    @Test
    void shouldDelegateCallToGetExchanges() {
        Exchange exchange = mock(Exchange.class);
        List<Exchange> exchanges = Arrays.asList(exchange);
        when(managementApi.getExchanges(VHOST)).thenReturn(exchanges);
        
        List<Exchange> result = sut.getExchanges(VHOST);
        
        assertSame(exchanges, result);
        verify(managementApi).getExchanges(VHOST);
    }

    @Test
    void shouldDelegateCallToGetQueues() {
        Queue queue = mock(Queue.class);
        List<Queue> queues = Arrays.asList(queue);
        when(managementApi.getQueues(VHOST)).thenReturn(queues);

        List<Queue> result = sut.getQueues(VHOST);
        
        assertSame(queues, result);
        verify(managementApi).getQueues(VHOST);
    }

    @Test
    void shouldDelegateCallToGetExchangeBindings() {
        Binding binding = mock(Binding.class);
        List<Binding> bindings = Arrays.asList(binding);
        when(managementApi.getExchangeSourceBindings(VHOST, EXCHANGE)).thenReturn(bindings);

        List<Binding> result = sut.getExchangeSourceBindings(VHOST, EXCHANGE);
        
        assertSame(bindings, result);
        verify(managementApi).getExchangeSourceBindings(VHOST, EXCHANGE);
    }

    @Test
    void shouldDelegateCallToGetQueueBindings() {
        Binding binding = mock(Binding.class);
        List<Binding> bindings = Arrays.asList(binding);
        when(managementApi.getQueueBindings(VHOST, QUEUE)).thenReturn(bindings);

        List<Binding> result = sut.getQueueBindings(VHOST, QUEUE);
        
        assertSame(bindings, result);
        verify(managementApi).getQueueBindings(VHOST, QUEUE);
    }

    @Test
    void shouldDelegateCallToGetQueueMessages() {
        Message message = mock(Message.class);
        List<Message> messages = Arrays.asList(message);
        when(operations.getMessagesOfQueue(VHOST, QUEUE, MAX_NUMBER_MESSAGE)).thenReturn(messages);
        
        List<Message> result = sut.getMessagesOfQueue(VHOST, QUEUE, MAX_NUMBER_MESSAGE);
        
        assertSame(messages, result);
        verify(operations).getMessagesOfQueue(VHOST, QUEUE, MAX_NUMBER_MESSAGE);
    }

    @Test
    void shouldDelegateCallToPurgeQueue(){
        sut.purgeQueue(VHOST, QUEUE);

        verify(managementApi).purgeQueue(VHOST, QUEUE);
        verifyNoMoreInteractions(managementApi, operations);
    }

    @Test
    void shouldDelegateCallToDeleteFirstMessageInQueue() {
        sut.deleteFirstMessageInQueue(VHOST, QUEUE, CHECKSUM);
        
        verify(operations).deleteFirstMessageInQueue(VHOST, QUEUE, CHECKSUM);
        verifyNoMoreInteractions(managementApi, operations);
    }

    @Test
    void shouldDelegateCallToMoveAllMessagesInQueue() {
        sut.moveAllMessagesInQueue(VHOST, QUEUE, EXCHANGE, ROUTING_KEY);
        
        verify(operations).moveAllMessagesInQueue(VHOST, QUEUE, EXCHANGE, ROUTING_KEY);
        verifyNoMoreInteractions(managementApi, operations);
    }

    @Test
    void shouldDelegateCallToMoveFirstMessageInQueue() {
        sut.moveFirstMessageInQueue(VHOST, QUEUE, CHECKSUM, EXCHANGE, ROUTING_KEY);

        verify(operations).moveFirstMessageInQueue(VHOST, QUEUE, CHECKSUM, EXCHANGE, ROUTING_KEY);
        verifyNoMoreInteractions(managementApi, operations);
    }

    @Test
    void shouldDelegateCallToRequeueAllMessagesInQueue() {
        sut.requeueAllMessagesInQueue(VHOST, QUEUE);

        verify(operations).requeueAllMessagesInQueue(VHOST, QUEUE);
        verifyNoMoreInteractions(managementApi, operations);
    }

    @Test
    void shouldDelegateCallToRequeueFirstMessageInQueue() {
        sut.requeueFirstMessageInQueue(VHOST, QUEUE, CHECKSUM);

        verify(operations).requeueFirstMessageInQueue(VHOST, QUEUE, CHECKSUM);
        verifyNoMoreInteractions(managementApi, operations);
    }
}
