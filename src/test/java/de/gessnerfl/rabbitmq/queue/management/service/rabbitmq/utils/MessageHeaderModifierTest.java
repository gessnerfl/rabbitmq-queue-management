package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.utils;

import com.rabbitmq.client.AMQP;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class MessageHeaderModifierTest {

    private static final String TEST_COUNTER = "x-test-counter";
    private MessageHeaderModifier sut;

    @Before
    public void init(){
        sut = new MessageHeaderModifier();
    }

    @Test
    public void shouldIncrementCounterHeaderWhenNoHeadersAreDefined(){
        AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder().contentType("text/plain").build();

        AMQP.BasicProperties result = sut.incrementCounter(properties, TEST_COUNTER);

        assertNotEquals(properties, result); //Should be a copy
        assertEquals(1, result.getHeaders().get(TEST_COUNTER));
    }

    @Test
    public void shouldIncrementCounterHeaderWhenHeadersIsDefinedAndCountHeaderIsNotSet(){
        Map<String,Object> headers = new HashMap<>();
        headers.put("foo", "bar");
        AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder().contentType("text/plain").headers(headers).build();

        AMQP.BasicProperties result = sut.incrementCounter(properties, TEST_COUNTER);

        assertNotEquals(properties, result); //Should be a copy
        assertEquals(1, result.getHeaders().get(TEST_COUNTER));
    }

    @Test
    public void shouldIncrementCounterHeaderWhenHeadersIsDefinedAndCountHeaderIsSet(){
        int initialValue = 2;
        Map<String,Object> headers = new HashMap<>();
        headers.put(TEST_COUNTER, initialValue);
        AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder().contentType("text/plain").headers(headers).build();

        AMQP.BasicProperties result = sut.incrementCounter(properties, TEST_COUNTER);

        assertNotEquals(properties, result); //Should be a copy
        assertEquals(initialValue+1, result.getHeaders().get(TEST_COUNTER));
    }

}