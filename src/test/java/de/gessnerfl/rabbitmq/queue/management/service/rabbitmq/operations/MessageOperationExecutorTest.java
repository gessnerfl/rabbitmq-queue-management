package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.GetResponse;

import de.gessnerfl.rabbitmq.queue.management.connection.CloseableChannelWrapper;
import de.gessnerfl.rabbitmq.queue.management.connection.ConnectionFailedException;
import de.gessnerfl.rabbitmq.queue.management.connection.Connector;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.utils.MessageChecksum;

@RunWith(MockitoJUnitRunner.class)
public class MessageOperationExecutorTest {
    private final static String DEFAULT_VHOST_NAME = "defaultVhost";
    private final static String DEFAULT_QUEUE_NAME = "defaultQueue";
    private final static Envelope DEFAULT_ENVELOPE = mock(Envelope.class);
    private final static Long DEFAULT_DELIVERY_TAG = 123L;
    private final static AMQP.BasicProperties DEFAULT_BASIC_PROPERTIES =
            mock(AMQP.BasicProperties.class);
    private final static byte[] DEFAULT_PAYLOAD = "defaultPayload".getBytes(StandardCharsets.UTF_8);
    private final static String DEFAULT_CHECKSUM = "defaultChecksum";

    @Mock
    private Connector connector;
    @Mock
    private MessageChecksum messageChecksum;
    @Mock
    private CloseableChannelWrapper closeableChannelWrapper;
    @Mock
    private Channel channel;
    @Mock
    private MessageOperationFunction function;

    @InjectMocks
    private MessageOperationExecutor sut;

    @Before
    public void init() {
        when(closeableChannelWrapper.getChannel()).thenReturn(channel);
        when(connector.connectAsClosable(DEFAULT_VHOST_NAME)).thenReturn(closeableChannelWrapper);
        when(DEFAULT_ENVELOPE.getDeliveryTag()).thenReturn(DEFAULT_DELIVERY_TAG);
        when(messageChecksum.createFor(DEFAULT_BASIC_PROPERTIES, DEFAULT_PAYLOAD))
                .thenReturn(DEFAULT_CHECKSUM);
    }

    @Test
    public void shouldRetrieveMessageFromQueueAndPerformFunctionWhenChecksumMatches()
            throws Exception {
        GetResponse response = mockDefaultGetResponse();
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenReturn(response);

        sut.consumeMessageApplyFunctionAndAckknowlegeOnSuccess(DEFAULT_VHOST_NAME, DEFAULT_QUEUE_NAME, DEFAULT_CHECKSUM, function);

        verify(function).apply(channel, response);
        verify(channel).basicAck(DEFAULT_DELIVERY_TAG, false);
    }

    @Test
    public void shouldRetrieveMessageFromQueueAndNackWithRequeuWhenChecksumDoesNotMatch()
            throws Exception {
        GetResponse response = mockDefaultGetResponse();
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenReturn(response);

        try {
            sut.consumeMessageApplyFunctionAndAckknowlegeOnSuccess(DEFAULT_VHOST_NAME, DEFAULT_QUEUE_NAME, "invalidChecksum", function);
            fail();
        } catch (MessageOperationFailedException e) {
        }

        verify(channel).basicNack(DEFAULT_DELIVERY_TAG, false, true);
        verify(function, never()).apply(any(Channel.class), any(GetResponse.class));
    }

    @Test
    public void shouldFailWhenQueueIsEmpty() throws Exception {
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenReturn(null);

        try {
            sut.consumeMessageApplyFunctionAndAckknowlegeOnSuccess(DEFAULT_VHOST_NAME, DEFAULT_QUEUE_NAME, "anyChecksum", function);
            fail();
        } catch (MessageOperationFailedException e) {
        }

        verify(channel, never()).basicNack(any(Long.class), anyBoolean(), anyBoolean());
        verify(function, never()).apply(any(Channel.class), any(GetResponse.class));
    }

    @Test
    public void shouldThrowExcpetionWhenConnectionCannotBeEstablished() throws Exception {
        ConnectionFailedException expectedException = new ConnectionFailedException(null);
        when(connector.connectAsClosable(DEFAULT_VHOST_NAME)).thenThrow(expectedException);

        try {
            sut.consumeMessageApplyFunctionAndAckknowlegeOnSuccess(DEFAULT_VHOST_NAME, DEFAULT_QUEUE_NAME, DEFAULT_CHECKSUM, function);
        } catch (MessageOperationFailedException e) {
            assertSame(expectedException, e.getCause());
        }

        verify(function, never()).apply(any(Channel.class), any(GetResponse.class));
    }

    @Test
    public void shouldThrowExceptionWhenMessageCannotBeFetched() throws Exception {
        IOException expectedException = new IOException();
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenThrow(expectedException);

        try {
            sut.consumeMessageApplyFunctionAndAckknowlegeOnSuccess(DEFAULT_VHOST_NAME, DEFAULT_QUEUE_NAME, DEFAULT_CHECKSUM, function);
        } catch (MessageOperationFailedException e) {
            assertSame(expectedException, e.getCause());
        }

        verify(channel).basicGet(DEFAULT_QUEUE_NAME, false);
        verifyNoMoreInteractions(channel);
        verify(function, never()).apply(any(Channel.class), any(GetResponse.class));
    }

    @Test
    public void shouldThrowExceptionWhenFunctionCannotBePerformedSuccessfulChecksumCheck()
            throws Exception {
        GetResponse getResponse = mockDefaultGetResponse();
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenReturn(getResponse);
        when(messageChecksum.createFor(DEFAULT_BASIC_PROPERTIES, DEFAULT_PAYLOAD))
                .thenReturn(DEFAULT_CHECKSUM);
        IOException expectedException = new IOException();
        doThrow(expectedException).when(function).apply(channel, getResponse);

        try {
            sut.consumeMessageApplyFunctionAndAckknowlegeOnSuccess(DEFAULT_VHOST_NAME, DEFAULT_QUEUE_NAME, DEFAULT_CHECKSUM, function);
        } catch (MessageOperationFailedException e) {
            assertSame(expectedException, e.getCause());
        }

        verify(messageChecksum).createFor(DEFAULT_BASIC_PROPERTIES, DEFAULT_PAYLOAD);
        verify(channel).basicGet(DEFAULT_QUEUE_NAME, false);
        verify(function).apply(channel, getResponse);
        verifyNoMoreInteractions(channel);
    }
    
    @Test
    public void shouldThrowExceptionWhenAckCannotBeSentAfterSuccessfulFunctionExecution() throws Exception {
        GetResponse getResponse = mockDefaultGetResponse();
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenReturn(getResponse);
        when(messageChecksum.createFor(DEFAULT_BASIC_PROPERTIES, DEFAULT_PAYLOAD))
                .thenReturn(DEFAULT_CHECKSUM);
        IOException expectedException = new IOException();
        doThrow(expectedException).when(channel).basicAck(DEFAULT_DELIVERY_TAG, false);

        try {
            sut.consumeMessageApplyFunctionAndAckknowlegeOnSuccess(DEFAULT_VHOST_NAME, DEFAULT_QUEUE_NAME, DEFAULT_CHECKSUM, function);
        } catch (MessageOperationFailedException e) {
            assertSame(expectedException, e.getCause());
        }

        verify(messageChecksum).createFor(DEFAULT_BASIC_PROPERTIES, DEFAULT_PAYLOAD);
        verify(channel).basicGet(DEFAULT_QUEUE_NAME, false);
        verify(function).apply(channel, getResponse);
        verify(channel).basicAck(DEFAULT_DELIVERY_TAG, false);
        verifyNoMoreInteractions(channel);
    }

    @Test
    public void shouldThrowExceptionWhenNackCannotBeSentAfterChecksumMatchFailed()
            throws Exception {
        GetResponse getResponse = mockDefaultGetResponse();
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenReturn(getResponse);
        when(messageChecksum.createFor(DEFAULT_BASIC_PROPERTIES, DEFAULT_PAYLOAD))
                .thenReturn(DEFAULT_CHECKSUM);
        IOException expectedException = new IOException();
        doThrow(expectedException).when(channel).basicNack(DEFAULT_DELIVERY_TAG, false, true);

        try {
            sut.consumeMessageApplyFunctionAndAckknowlegeOnSuccess(DEFAULT_VHOST_NAME, DEFAULT_QUEUE_NAME, "invalidMessage", function);
        } catch (MessageOperationFailedException e) {
            assertSame(expectedException, e.getCause());
        }

        verify(messageChecksum).createFor(DEFAULT_BASIC_PROPERTIES, DEFAULT_PAYLOAD);
        verify(channel).basicGet(DEFAULT_QUEUE_NAME, false);
        verify(channel).basicNack(DEFAULT_DELIVERY_TAG, false, true);
        verifyNoMoreInteractions(channel);
        verify(function, never()).apply(any(Channel.class), any(GetResponse.class));
    }

    private GetResponse mockDefaultGetResponse() {
        GetResponse response = mock(GetResponse.class);
        when(response.getEnvelope()).thenReturn(DEFAULT_ENVELOPE);
        when(response.getProps()).thenReturn(DEFAULT_BASIC_PROPERTIES);
        when(response.getBody()).thenReturn(DEFAULT_PAYLOAD);
        return response;
    }
}
