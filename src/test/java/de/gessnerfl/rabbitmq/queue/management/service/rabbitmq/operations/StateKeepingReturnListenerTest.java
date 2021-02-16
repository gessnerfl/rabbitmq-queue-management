package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.MessageProperties;
import org.apache.commons.codec.Charsets;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class StateKeepingReturnListenerTest {

    @Test
    void shouldSetStateToReceivedAndLogInfoWhenHandleReturnIsExecuted() throws Exception {
        final Logger logger = mock(Logger.class);
        final OperationId operation = mock(OperationId.class);
        final int replyCode = 1;
        final String replyText = "replyText";
        final String exchange = "exchange";
        final String routingKey = "routingKey";
        final AMQP.BasicProperties properties = MessageProperties.BASIC;
        final byte[] body = "body".getBytes(Charsets.UTF_8);

        StateKeepingReturnListener sut = new StateKeepingReturnListener(operation, logger);
        assertFalse(sut.isReceived());

        sut.handleReturn(replyCode, replyText, exchange, routingKey, properties, body);

        assertTrue(sut.isReceived());
        verify(logger).error(anyString(), eq(operation), eq(exchange), eq(routingKey), eq(replyCode), eq(replyText));
    }

}