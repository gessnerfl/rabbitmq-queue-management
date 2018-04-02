package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import de.gessnerfl.rabbitmq.queue.management.model.AmqpMessage;
import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Binding;
import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Exchange;
import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Queue;
import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.BrokerConfig;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.config.RabbitMqBrokers;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations.Operations;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.remoteapi.ManagementApi;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.remoteapi.ManagementApiFactory;

@RunWith(MockitoJUnitRunner.class)
public class RabbitMqFacadeTest {
    private final static String DEFAULT_BROKER = "broker";
    private final static String VHOST = "vhost";
    private final static String QUEUE = "queue";
    private final static int MAX_NUMBER_MESSAGE = 10;
    private final static String CHECKSUM = "checksum";
    private final static String EXCHANGE = "exchange";
    private final static String ROUTING_KEY = "routingKey";

    @Mock
    private ManagementApiFactory managementApiFactory;
    @Mock
    private ManagementApi managementApi;
    @Mock
    private Operations operations;
    @Mock
    private RabbitMqBrokers rabbitMqBrokers;
    @Mock
    private BrokerConfig brokerConfig;

    @InjectMocks
    private RabbitMqFacade sut;
    
    @Before
    public void init(){
        when(managementApiFactory.createFor(DEFAULT_BROKER)).thenReturn(managementApi);
        when(rabbitMqBrokers.getBrokerConfig(DEFAULT_BROKER)).thenReturn(brokerConfig);
        when(brokerConfig.getVhost()).thenReturn(VHOST);
    }

    @Test
    public void shouldDelegateCallToGetExchanges() {
        Exchange exchange = mock(Exchange.class);
        List<Exchange> exchanges = Arrays.asList(exchange);
        when(managementApi.getExchanges(VHOST)).thenReturn(exchanges);
        
        List<Exchange> result = sut.getExchanges(DEFAULT_BROKER);
        
        assertSame(exchanges, result);
        verify(managementApi).getExchanges(VHOST);
    }

    @Test
    public void shouldDelegateCallToGetQueues() {
        Queue queue = mock(Queue.class);
        List<Queue> queues = Arrays.asList(queue);
        when(managementApi.getQueues(VHOST)).thenReturn(queues);

        List<Queue> result = sut.getQueues(DEFAULT_BROKER);
        
        assertSame(queues, result);
        verify(managementApi).getQueues(VHOST);
    }

    @Test
    public void shouldDelegateCallToGetExchangeBindings() {
        Binding binding = mock(Binding.class);
        List<Binding> bindings = Arrays.asList(binding);
        when(managementApi.getExchangeSourceBindings(VHOST, EXCHANGE)).thenReturn(bindings);

        List<Binding> result = sut.getExchangeSourceBindings(DEFAULT_BROKER, EXCHANGE);
        
        assertSame(bindings, result);
        verify(managementApi).getExchangeSourceBindings(VHOST, EXCHANGE);
    }

    @Test
    public void shouldDelegateCallToGetQueueBindings() {
        Binding binding = mock(Binding.class);
        List<Binding> bindings = Arrays.asList(binding);
        when(managementApi.getQueueBindings(VHOST, QUEUE)).thenReturn(bindings);

        List<Binding> result = sut.getQueueBindings(DEFAULT_BROKER, QUEUE);
        
        assertSame(bindings, result);
        verify(managementApi).getQueueBindings(VHOST, QUEUE);
    }

    @Test
    public void shouldDelegateCallToGetQueueMessages() {
        AmqpMessage message = mock(AmqpMessage.class);
        List<AmqpMessage> messages = Arrays.asList(message);
        when(operations.getMessagesOfQueue(DEFAULT_BROKER, QUEUE, MAX_NUMBER_MESSAGE)).thenReturn(messages);
        
        List<AmqpMessage> result = sut.getMessagesOfQueue(DEFAULT_BROKER, QUEUE, MAX_NUMBER_MESSAGE);
        
        assertSame(messages, result);
        verify(operations).getMessagesOfQueue(DEFAULT_BROKER, QUEUE, MAX_NUMBER_MESSAGE);
    }

    @Test
    public void shouldDelegateCallToDeleteFirstMessageInQueue() {
        sut.deleteFirstMessageInQueue(DEFAULT_BROKER, QUEUE, CHECKSUM);
        
        verify(operations).deleteFirstMessageInQueue(DEFAULT_BROKER, QUEUE, CHECKSUM);
    }

    @Test
    public void shouldDelegateCallToMoveFirstMessageInQueue() {
        sut.moveFirstMessageInQueue(DEFAULT_BROKER, QUEUE, CHECKSUM, EXCHANGE, ROUTING_KEY);
        
        verify(operations).moveFirstMessageInQueue(DEFAULT_BROKER, QUEUE, CHECKSUM, EXCHANGE, ROUTING_KEY);
    }
}
