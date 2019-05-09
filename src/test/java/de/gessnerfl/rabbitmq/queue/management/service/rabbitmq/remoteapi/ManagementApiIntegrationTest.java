package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.remoteapi;

import static de.gessnerfl.rabbitmq.queue.management.hamcrest.CustomMatchers.matchesInitialQueueStateNullOrRunning;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import de.gessnerfl.rabbitmq.queue.management.model.Message;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.RabbitMqFacade;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.gessnerfl.rabbitmq.queue.management.AbstractIntegrationTest;
import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Binding;
import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Exchange;
import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Queue;
import de.gessnerfl.rabbitmq.queue.management.util.RabbitMqTestEnvironment;
import de.gessnerfl.rabbitmq.queue.management.util.RabbitMqTestEnvironmentBuilder;
import de.gessnerfl.rabbitmq.queue.management.util.RabbitMqTestEnvironmentBuilderFactory;

public class ManagementApiIntegrationTest extends AbstractIntegrationTest {

    private final static String VHOST = "/";
    private final static String EXCHANGE_NAME = "test.direct";
    private final static String DEAD_LETTER_EXCHANGE_NAME = "test.direct.dlx";
    private final static String DEAD_LETTER_ROUTING_KEY = "routing.key.dlx";
    private final static String QUEUE_NAME = "test.queue";
    private final static String ROUTING_KEY = "routing.key";
    private final static String DEAD_LETTERED_QUEUE_NAME = "test.queue.dl";

    @Autowired
    private RabbitMqTestEnvironmentBuilderFactory testEnvironmentBuilderFactor;
    @Autowired
    private RabbitMqFacade rabbitMqFacade;

    private RabbitMqTestEnvironment testEnvironment;
    
    @Autowired
    private ManagementApi sut;

    @Before
    public void init() {
        RabbitMqTestEnvironmentBuilder builder = testEnvironmentBuilderFactor.create();
        testEnvironment = builder.withExchange(EXCHANGE_NAME)
                            .withExchange(DEAD_LETTER_EXCHANGE_NAME)
                            .withQueue(QUEUE_NAME)
                                .exchange(EXCHANGE_NAME)
                                .routingKey(ROUTING_KEY)
                                .build()
                            .withQueue(DEAD_LETTERED_QUEUE_NAME)
                                .exchange(DEAD_LETTER_EXCHANGE_NAME)
                                .routingKey(ROUTING_KEY)
                                .deadLetterExchange(DEAD_LETTER_EXCHANGE_NAME)
                                .deadLetterRoutingKey(DEAD_LETTER_ROUTING_KEY)
                                .build()
                            .build();
        testEnvironment.setup();
    }

    @After
    public void cleanup() {
        testEnvironment.cleanup();
    }

    @Test
    public void shouldGetAllExchanges() {
        List<Exchange> exchanges = sut.getExchanges(VHOST);

        assertThat(exchanges, not(empty()));
        // check defined exchanges included
        assertExchangeIncluded(EXCHANGE_NAME, exchanges);
        assertExchangeIncluded(DEAD_LETTER_EXCHANGE_NAME, exchanges);
    }

    private void assertExchangeIncluded(String exchangeName, List<Exchange> exchanges) {
        boolean found = false;
        for (Exchange e : exchanges) {
            if (e.getName().equals(exchangeName)) {
                found = true;
                assertEquals("direct", e.getType());
                assertEquals(VHOST, e.getVhost());
                assertEquals(RabbitMqTestEnvironment.EXCHANGE_DURABLE, e.isDurable());
                assertEquals(RabbitMqTestEnvironment.EXCHANGE_AUTO_DELETE, e.isAutoDelete());
                assertFalse(e.isInternal());
            }
        }
        assertTrue(found);
    }

    @Test
    public void shouldGetAllQueues() {
        List<Queue> queues = sut.getQueues(VHOST);

        assertThat(queues, not(empty()));
        // check defined exchanges included
        assertQueueIncluded(QUEUE_NAME, queues, false);
        assertQueueIncluded(DEAD_LETTERED_QUEUE_NAME, queues, true);
    }

    private void assertQueueIncluded(String queueName, List<Queue> queues, boolean isDeadLettered) {
        boolean found = false;
        for (Queue q : queues) {
            if (q.getName().equals(queueName)) {
                found = true;
                assertEquals(VHOST, q.getVhost());
                assertEquals(RabbitMqTestEnvironment.QUEUE_DURABLE, q.isDurable());
                assertEquals(RabbitMqTestEnvironment.QUEUE_AUTO_DELETE, q.isAutoDelete());
                assertEquals(RabbitMqTestEnvironment.QUEUE_EXCLUSIVE, q.isExclusive());

                assertEquals(isDeadLettered, q.isDeadLetterExchangeConfigured());
                assertEquals(isDeadLettered, q.isDeadLetterRoutingKeyConfigured());
                assertEquals(isDeadLettered, q.getArguments().containsKey(Queue.DEAD_LETTER_EXCHANGE_ARGUMENT));
                assertEquals(isDeadLettered, q.getArguments().containsKey(Queue.DEAD_LETTER_ROUTINGKEY_ARGUMENT));

                if (isDeadLettered) {
                    assertEquals(DEAD_LETTER_EXCHANGE_NAME, q.getArguments().get(Queue.DEAD_LETTER_EXCHANGE_ARGUMENT));
                    assertEquals(DEAD_LETTER_ROUTING_KEY, q.getArguments().get(Queue.DEAD_LETTER_ROUTINGKEY_ARGUMENT));
                }

                assertEquals(0, q.getConsumers());
                assertEquals(0, q.getMessages());
                assertEquals(0, q.getMessagesReady());
                assertEquals(0, q.getMessagesUnacknowledged());
                assertThat(q.getState(), matchesInitialQueueStateNullOrRunning());
            }
        }
        assertTrue(found);
    }
    
    @Test
    public void shouldGetBindingsOfExchanged(){
        List<Binding> bindings = sut.getExchangeSourceBindings(VHOST, EXCHANGE_NAME);
        
        assertThat(bindings, hasSize(1));
        
        Binding routing = bindings.get(0);
        assertEquals(EXCHANGE_NAME, routing.getSource());
        assertEquals(QUEUE_NAME, routing.getDestination());
        assertEquals("queue", routing.getDestinationType());
        assertEquals(ROUTING_KEY, routing.getRoutingKey());
        assertEquals(VHOST, routing.getVhost());
    }

    @Test
    public void shouldGetBindingsOfQueue() {
        List<Binding> bindings = sut.getQueueBindings(VHOST, QUEUE_NAME);

        assertThat(bindings, hasSize(2));

        // should contain default blank binding
        Binding defaultBinding = bindings.get(0);
        assertEquals("", defaultBinding.getSource());
        assertEquals(QUEUE_NAME, defaultBinding.getDestination());
        assertEquals("queue", defaultBinding.getDestinationType());
        assertEquals(QUEUE_NAME, defaultBinding.getRoutingKey());
        assertEquals(VHOST, defaultBinding.getVhost());

        // should contain routing from exchange
        Binding routing = bindings.get(1);
        assertEquals(EXCHANGE_NAME, routing.getSource());
        assertEquals(QUEUE_NAME, routing.getDestination());
        assertEquals("queue", routing.getDestinationType());
        assertEquals(ROUTING_KEY, routing.getRoutingKey());
        assertEquals(VHOST, routing.getVhost());
    }

    @Test
    public void shouldPurgeQueueContent(){
        testEnvironment.publishMessage(EXCHANGE_NAME, ROUTING_KEY);
        testEnvironment.publishMessage(EXCHANGE_NAME, ROUTING_KEY);

        List<Message> messages = rabbitMqFacade.getMessagesOfQueue(VHOST, QUEUE_NAME, 10);
        assertThat(messages, hasSize(2));

        sut.purgeQueue(VHOST, QUEUE_NAME);

        messages = rabbitMqFacade.getMessagesOfQueue(VHOST, QUEUE_NAME, 10);
        assertThat(messages, empty());
    }

}
