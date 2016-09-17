package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.remoteapi;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.rabbitmq.client.Channel;

import de.gessnerfl.rabbitmq.queue.management.AbstractIntegrationTest;
import de.gessnerfl.rabbitmq.queue.management.connection.CloseableChannelWrapper;
import de.gessnerfl.rabbitmq.queue.management.connection.Connector;
import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Binding;
import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Exchange;
import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Queue;

public class ManagementApiIntegrationTest extends AbstractIntegrationTest {
    private final static Logger LOGGER =
            LoggerFactory.getLogger(ManagementApiIntegrationTest.class);

    private final static String VHOST = "/";
    private final static String EXCHANGE_NAME = "test.direct";
    private final static String DEAD_LETTER_EXCHANGE_NAME = "test.direct.dlx";
    private final static String QUEUE_NAME = "test.queue";
    private final static String ROUTING_KEY = "routing.key";
    private final static String DEAD_LETTERED_QUEUE_NAME = "test.queue.dl";

    @Autowired
    private Connector connector;

    @Autowired
    private ManagementApi sut;


    @Before
    public void init() throws Exception {
        try (CloseableChannelWrapper wrapper = connector.connectAsClosable()) {
            Channel channel = wrapper.getChannel();
            declareExchanges(channel);
            declareQueues(channel);
        } catch (IOException e) {
            cleanup();
            throw e;
        }
    }

    @After
    public void cleanup() {
        try (CloseableChannelWrapper wrapper = connector.connectAsClosable()) {
            Channel channel = wrapper.getChannel();
            deleteQueues(channel);
            deleteExchanges(channel);
        }
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
                assertFalse(e.isDurable());
                assertTrue(e.isAutoDelete());
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
                assertFalse(q.isDurable());
                assertTrue(q.isAutoDelete());
                assertFalse(q.isExclusive());

                assertEquals(isDeadLettered, q.isDeadLettered());
                assertEquals(isDeadLettered,
                        q.getArguments().containsKey(Queue.DEAD_LETTER_EXCHANGE_ARGUMENT));
                assertEquals(isDeadLettered,
                        q.getArguments().containsKey(Queue.DEAD_LETTER_ROUTINGKEY_ARGUMENT));

                if (isDeadLettered) {
                    assertEquals(DEAD_LETTER_EXCHANGE_NAME,
                            q.getArguments().get(Queue.DEAD_LETTER_EXCHANGE_ARGUMENT));
                    assertEquals(DEAD_LETTERED_QUEUE_NAME,
                            q.getArguments().get(Queue.DEAD_LETTER_ROUTINGKEY_ARGUMENT));
                }
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
    public void shouldGetBindingsOfQueue() throws Exception {
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

    private void declareExchanges(Channel channel) throws IOException {
        declareExchange(EXCHANGE_NAME, channel);
        declareExchange(DEAD_LETTER_EXCHANGE_NAME, channel);
    }

    private void declareExchange(String exchangeName, Channel channel) throws IOException {
        channel.exchangeDeclare(exchangeName, "direct", false, true, null);
    }

    private void declareQueues(Channel channel) throws IOException {
        channel.queueDeclare(QUEUE_NAME, false, false, true, null);

        Map<String, Object> arguments = new HashMap<>();
        arguments.put(Queue.DEAD_LETTER_EXCHANGE_ARGUMENT, DEAD_LETTER_EXCHANGE_NAME);
        arguments.put(Queue.DEAD_LETTER_ROUTINGKEY_ARGUMENT, DEAD_LETTERED_QUEUE_NAME);
        channel.queueDeclare(DEAD_LETTERED_QUEUE_NAME, false, false, true, arguments);

        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);
        channel.queueBind(DEAD_LETTERED_QUEUE_NAME, DEAD_LETTER_EXCHANGE_NAME, ROUTING_KEY);
    }

    private void deleteQueues(Channel channel) {
        deleteQueue(QUEUE_NAME, channel);
        deleteQueue(DEAD_LETTERED_QUEUE_NAME, channel);
    }

    private void deleteQueue(String queueName, Channel channel) {
        try {
            channel.queueDelete(queueName);
        } catch (IOException e) {
            LOGGER.error("Failed to delete queue", e);
        }
    }

    private void deleteExchanges(Channel channel) {
        deleteExchange(EXCHANGE_NAME, channel);
        deleteExchange(DEAD_LETTER_EXCHANGE_NAME, channel);
    }

    private void deleteExchange(String exchangeName, Channel channel) {
        try {
            channel.exchangeDelete(exchangeName);
        } catch (IOException e) {
            LOGGER.error("Failed to delete exchange", e);
        }
    }
}
