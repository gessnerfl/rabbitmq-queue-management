package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

import com.rabbitmq.client.*;
import de.gessnerfl.rabbitmq.queue.management.connection.CloseableChannelWrapper;
import de.gessnerfl.rabbitmq.queue.management.connection.ConnectionFailedException;
import de.gessnerfl.rabbitmq.queue.management.connection.Connector;
import de.gessnerfl.rabbitmq.queue.management.model.Message;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.utils.MessageMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QueueListOperationTest {
    private final static String DEFAULT_VHOST_NAME = "defaultVhost";
    private final static String DEFAULT_QUEUE_NAME = "defaultQueue";
    private final static int DEFAULT_MAX_NO_OF_MESSAGES = 3;
    private final static Envelope DEFAULT_ENVELOPE = mock(Envelope.class);
    private final static Long DEFAULT_DELIVERY_TAG = 123L;

    private final static Message DEFAULT_MESSAGE = mock(Message.class);

    @Mock
    private Connector connector;
    @Mock
    private MessageMapper messageMapper;
    @Mock
    private CloseableChannelWrapper closeableChannelWrapper;
    @Mock
    private Channel channel;

    @InjectMocks
    private QueueListOperation sut;

    @BeforeEach
    void init() {
        when(connector.connectAsClosable(DEFAULT_VHOST_NAME)).thenReturn(closeableChannelWrapper);
        when(DEFAULT_ENVELOPE.getDeliveryTag()).thenReturn(DEFAULT_DELIVERY_TAG);
    }

    @Test
    void shouldReturnMessageFromQueueWhenMaxNumberOfRequestedMessagesIsEqualToTheNumberOfAvailableMessages() throws Exception {
        GetResponse getResponse1 = mockDefaultGetResponse(2);
        GetResponse getResponse2 = mockDefaultGetResponse(1);
        GetResponse getResponse3 = mockDefaultGetResponse(0);


        when(closeableChannelWrapper.getChannel()).thenReturn(channel);
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenReturn(getResponse1, getResponse2, getResponse3);
        when(messageMapper.map(any(GetResponse.class))).thenReturn(DEFAULT_MESSAGE);

        List<Message> messages = sut.getMessagesFromQueue(DEFAULT_VHOST_NAME, DEFAULT_QUEUE_NAME, DEFAULT_MAX_NO_OF_MESSAGES);

        assertThat(messages, Matchers.hasSize(3));

        verify(channel, times(3)).basicGet(DEFAULT_QUEUE_NAME, false);
    }

    @Test
    void shouldReturnEmptyListIfNoMessagesAreAvailable() throws Exception {
        when(closeableChannelWrapper.getChannel()).thenReturn(channel);
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenReturn(null);

        List<Message> messages = sut.getMessagesFromQueue(DEFAULT_VHOST_NAME, DEFAULT_QUEUE_NAME, DEFAULT_MAX_NO_OF_MESSAGES);

        assertThat(messages, Matchers.empty());
        verify(channel).basicGet(DEFAULT_QUEUE_NAME, false);
        verify(channel, never()).basicNack(any(Long.class), anyBoolean(), anyBoolean());
    }

    @Test
    void shouldReturnMessageFromQueueWhenMaxNumberOfRequestedMessagesIsLessThanTheNumberOfAvailableMessages() throws Exception {
        GetResponse getResponse1 = mockDefaultGetResponse(3);
        GetResponse getResponse2 = mockDefaultGetResponse(2);
        GetResponse getResponse3 = mockDefaultGetResponse(1);
        GetResponse getResponse4 = mock(GetResponse.class);


        when(closeableChannelWrapper.getChannel()).thenReturn(channel);
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenReturn(getResponse1, getResponse2, getResponse3, getResponse4);
        when(messageMapper.map(any(GetResponse.class))).thenReturn(DEFAULT_MESSAGE);

        List<Message> messages = sut.getMessagesFromQueue(DEFAULT_VHOST_NAME, DEFAULT_QUEUE_NAME, DEFAULT_MAX_NO_OF_MESSAGES);

        assertThat(messages, Matchers.hasSize(3));

        verify(channel, times(3)).basicGet(DEFAULT_QUEUE_NAME, false);
    }

    @Test
    void shouldReturnMessageFromQueueWhenMaxNumberOfRequestedMessagesIsGreaterThanTheNumberOfAvailableMessages() throws Exception {
        GetResponse getResponse1 = mockDefaultGetResponse(1);
        GetResponse getResponse2 = mockDefaultGetResponse(0);


        when(closeableChannelWrapper.getChannel()).thenReturn(channel);
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenReturn(getResponse1, getResponse2);
        when(messageMapper.map(any(GetResponse.class))).thenReturn(DEFAULT_MESSAGE);

        List<Message> messages = sut.getMessagesFromQueue(DEFAULT_VHOST_NAME, DEFAULT_QUEUE_NAME, DEFAULT_MAX_NO_OF_MESSAGES);

        assertThat(messages, Matchers.hasSize(2));

        verify(channel, times(2)).basicGet(DEFAULT_QUEUE_NAME, false);
    }

    @Test
    void shouldSendSingleNackWhenDataIsRetrievedIndependentFromTheNumberOfGetRequests() throws Exception {
        GetResponse getResponse1 = mockDefaultGetResponse(2);
        GetResponse getResponse2 = mockDefaultGetResponse(1);
        GetResponse getResponse3 = mockDefaultGetResponse(0);


        when(closeableChannelWrapper.getChannel()).thenReturn(channel);
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenReturn(getResponse1, getResponse2, getResponse3);
        when(messageMapper.map(any(GetResponse.class))).thenReturn(DEFAULT_MESSAGE);

        sut.getMessagesFromQueue(DEFAULT_VHOST_NAME, DEFAULT_QUEUE_NAME, DEFAULT_MAX_NO_OF_MESSAGES);

        verify(channel).basicNack(DEFAULT_DELIVERY_TAG, true, true);
    }

    @Test
    void shouldThrowExcpetionWhenConnectionCannotBeEstablished() throws Exception {
        ConnectionFailedException expectedException = new ConnectionFailedException(null);
        when(connector.connectAsClosable(DEFAULT_VHOST_NAME)).thenThrow(expectedException);

        try {
            sut.getMessagesFromQueue(DEFAULT_VHOST_NAME, DEFAULT_QUEUE_NAME, DEFAULT_MAX_NO_OF_MESSAGES);
        } catch (ConnectionFailedException e) {
            assertSame(expectedException, e);
        }
    }

    @Test
    void shouldThrowExceptionWhenDataCannotBeFetched() throws Exception {
        IOException expectedException = new IOException();


        when(closeableChannelWrapper.getChannel()).thenReturn(channel);
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenThrow(expectedException);

        try {
            sut.getMessagesFromQueue(DEFAULT_VHOST_NAME, DEFAULT_QUEUE_NAME, DEFAULT_MAX_NO_OF_MESSAGES);
        } catch (MessageFetchFailedException e) {
            assertSame(expectedException, e.getCause());
        }

        verify(channel).basicQos(QueueListOperation.DEFAULT_FETCH_COUNT);
        verify(channel).basicGet(DEFAULT_QUEUE_NAME, false);
        verifyNoMoreInteractions(channel);
    }

    @Test
    void shouldThrowExceptionWhenNackCannotBeSent() throws Exception {
        GetResponse getResponse1 = mockDefaultGetResponse(0);
        IOException expectedException = new IOException();

        when(closeableChannelWrapper.getChannel()).thenReturn(channel);
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenReturn(getResponse1);
        doThrow(expectedException).when(channel).basicNack(DEFAULT_DELIVERY_TAG, true, true);
        when(messageMapper.map(any(GetResponse.class))).thenReturn(DEFAULT_MESSAGE);

        try {
            sut.getMessagesFromQueue(DEFAULT_VHOST_NAME, DEFAULT_QUEUE_NAME, DEFAULT_MAX_NO_OF_MESSAGES);
        } catch (MessageFetchFailedException e) {
            assertSame(expectedException, e.getCause());
        }

        verify(channel).basicQos(QueueListOperation.DEFAULT_FETCH_COUNT);
        verify(channel).basicGet(DEFAULT_QUEUE_NAME, false);
        verify(channel).basicNack(DEFAULT_DELIVERY_TAG, true, true);
        verifyNoMoreInteractions(channel);
    }

    private GetResponse mockDefaultGetResponse(int remainingMessageCount) {
        GetResponse response = mock(GetResponse.class);
        when(response.getEnvelope()).thenReturn(DEFAULT_ENVELOPE);
        when(response.getMessageCount()).thenReturn(remainingMessageCount);
        return response;
    }

}
