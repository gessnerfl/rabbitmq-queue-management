package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

import com.rabbitmq.client.*;
import de.gessnerfl.rabbitmq.queue.management.connection.CloseableChannelWrapper;
import de.gessnerfl.rabbitmq.queue.management.connection.ConnectionFailedException;
import de.gessnerfl.rabbitmq.queue.management.connection.Connector;
import de.gessnerfl.rabbitmq.queue.management.model.BasicProperties;
import de.gessnerfl.rabbitmq.queue.management.model.Message;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.utils.MessageChecksum;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class QueueListOperationTest {
    private final static String DEFAULT_BROKER_NAME = "defaultBroker";
    private final static String DEFAULT_QUEUE_NAME = "defaultQueue";
    private final static int DEFAULT_MAX_NO_OF_MESSAGES = 3;
    private final static Envelope DEFAULT_ENVELOPE = mock(Envelope.class);
    private final static AMQP.BasicProperties DEFAULT_BASIC_PROPERTIES = mock(AMQP.BasicProperties.class);
    private final static byte[] DEFAULT_PAYLOAD = "defaultPayload".getBytes(StandardCharsets.UTF_8);
    private final static Long DEFAULT_DELIVERY_TAG = 123L;
    private final static String DEFAULT_CHECKSUM = "defaultChecksum";

    @Mock
    private Connector connector;
    @Mock
    private MessageChecksum messageChecksum;
    @Mock
    private CloseableChannelWrapper closeableChannelWrapper;
    @Mock
    private Channel channel;

    @InjectMocks
    private QueueListOperation sut;

    @Before
    public void init() {
        when(closeableChannelWrapper.getChannel()).thenReturn(channel);
        when(connector.connectAsClosable(DEFAULT_BROKER_NAME)).thenReturn(closeableChannelWrapper);
        when(DEFAULT_ENVELOPE.getDeliveryTag()).thenReturn(DEFAULT_DELIVERY_TAG);
        when(messageChecksum.createFor(DEFAULT_BASIC_PROPERTIES, DEFAULT_PAYLOAD)).thenReturn(DEFAULT_CHECKSUM);
    }

    @Test
    public void shouldReturnMessageFromQueueWhenMaxNumberOfRequestedMessagesIsEqualToTheNumberOfAvailableMessages() throws Exception {
        GetResponse getResponse1 = mockDefaultGetResponse(2);
        GetResponse getResponse2 = mockDefaultGetResponse(1);
        GetResponse getResponse3 = mockDefaultGetResponse(0);
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenReturn(getResponse1, getResponse2, getResponse3);

        List<Message> messages = sut.getMessagesFromQueue(DEFAULT_BROKER_NAME, DEFAULT_QUEUE_NAME, DEFAULT_MAX_NO_OF_MESSAGES);

        assertThat(messages, Matchers.hasSize(3));
        assertDefaultMessage(messages.get(0));
        assertDefaultMessage(messages.get(1));
        assertDefaultMessage(messages.get(2));

        verify(channel, times(3)).basicGet(DEFAULT_QUEUE_NAME, false);
    }

    @Test
    public void shouldReturnEmptyListIfNoMessagesAreAvailable() throws Exception {
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenReturn(null);

        List<Message> messages = sut.getMessagesFromQueue(DEFAULT_BROKER_NAME, DEFAULT_QUEUE_NAME, DEFAULT_MAX_NO_OF_MESSAGES);

        assertThat(messages, Matchers.empty());
        verify(channel).basicGet(DEFAULT_QUEUE_NAME, false);
        verify(channel, never()).basicNack(any(Long.class), anyBoolean(), anyBoolean());
    }

    @Test
    public void shouldReturnMessageFromQueueWhenMaxNumberOfRequestedMessagesIsLessThanTheNumberOfAvailableMessages() throws Exception {
        GetResponse getResponse1 = mockDefaultGetResponse(3);
        GetResponse getResponse2 = mockDefaultGetResponse(2);
        GetResponse getResponse3 = mockDefaultGetResponse(1);
        GetResponse getResponse4 = mockDefaultGetResponse(0);
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenReturn(getResponse1, getResponse2, getResponse3, getResponse4);

        List<Message> messages = sut.getMessagesFromQueue(DEFAULT_BROKER_NAME, DEFAULT_QUEUE_NAME, DEFAULT_MAX_NO_OF_MESSAGES);

        assertThat(messages, Matchers.hasSize(3));
        assertDefaultMessage(messages.get(0));
        assertDefaultMessage(messages.get(1));
        assertDefaultMessage(messages.get(2));

        verify(channel, times(3)).basicGet(DEFAULT_QUEUE_NAME, false);
    }

    @Test
    public void shouldReturnMessageFromQueueWhenMaxNumberOfRequestedMessagesIsGreaterThanTheNumberOfAvailableMessages() throws Exception {
        GetResponse getResponse1 = mockDefaultGetResponse(1);
        GetResponse getResponse2 = mockDefaultGetResponse(0);
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenReturn(getResponse1, getResponse2);

        List<Message> messages = sut.getMessagesFromQueue(DEFAULT_BROKER_NAME, DEFAULT_QUEUE_NAME, DEFAULT_MAX_NO_OF_MESSAGES);

        assertThat(messages, Matchers.hasSize(2));
        assertDefaultMessage(messages.get(0));
        assertDefaultMessage(messages.get(1));

        verify(channel, times(2)).basicGet(DEFAULT_QUEUE_NAME, false);
    }

    @Test
    public void shouldSendSingleNackWhenDataIsRetrievedIndependentFromTheNumberOfGetRequests() throws Exception {
        GetResponse getResponse1 = mockDefaultGetResponse(2);
        GetResponse getResponse2 = mockDefaultGetResponse(1);
        GetResponse getResponse3 = mockDefaultGetResponse(0);
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenReturn(getResponse1, getResponse2, getResponse3);

        sut.getMessagesFromQueue(DEFAULT_BROKER_NAME, DEFAULT_QUEUE_NAME, DEFAULT_MAX_NO_OF_MESSAGES);

        verify(channel).basicNack(DEFAULT_DELIVERY_TAG, true, true);
    }

    @Test
    public void shouldThrowExcpetionWhenConnectionCannotBeEstablished() throws Exception {
        ConnectionFailedException expectedException = new ConnectionFailedException(null);
        when(connector.connectAsClosable(DEFAULT_BROKER_NAME)).thenThrow(expectedException);

        try {
            sut.getMessagesFromQueue(DEFAULT_BROKER_NAME, DEFAULT_QUEUE_NAME, DEFAULT_MAX_NO_OF_MESSAGES);
        } catch (ConnectionFailedException e) {
            assertSame(expectedException, e);
        }
    }

    @Test
    public void shouldThrowExceptionWhenDataCannotBeFetched() throws Exception {
        IOException expectedException = new IOException();
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenThrow(expectedException);

        try {
            sut.getMessagesFromQueue(DEFAULT_BROKER_NAME, DEFAULT_QUEUE_NAME, DEFAULT_MAX_NO_OF_MESSAGES);
        } catch (MessageFetchFailedException e) {
            assertSame(expectedException, e.getCause());
        }

        verify(channel).basicQos(QueueListOperation.DEFAULT_FETCH_COUNT);
        verify(channel).basicGet(DEFAULT_QUEUE_NAME, false);
        verifyNoMoreInteractions(channel);
    }

    @Test
    public void shouldThrowExceptionWhenNackCannotBeSent() throws Exception {
        GetResponse getResponse1 = mockDefaultGetResponse(0);
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenReturn(getResponse1);
        IOException expectedException = new IOException();
        doThrow(expectedException).when(channel).basicNack(DEFAULT_DELIVERY_TAG, true, true);

        try {
            sut.getMessagesFromQueue(DEFAULT_BROKER_NAME, DEFAULT_QUEUE_NAME, DEFAULT_MAX_NO_OF_MESSAGES);
        } catch (MessageFetchFailedException e) {
            assertSame(expectedException, e.getCause());
        }

        verify(channel).basicQos(QueueListOperation.DEFAULT_FETCH_COUNT);
        verify(channel).basicGet(DEFAULT_QUEUE_NAME, false);
        verify(channel).basicNack(DEFAULT_DELIVERY_TAG, true, true);
        verifyNoMoreInteractions(channel);
    }

    @Test
    public void shouldMapAmqpBasicProperties() throws Exception {
        GetResponse getResponse = mockDefaultGetResponse(0);
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenReturn(getResponse);

        String contentType = "contentType";
        String contentEncoding = "contentEncoding";
        String headerKey = "headerKey";
        String headerValue = "headerValue";
        Map<String, Object> headers = new HashMap<>();
        headers.put(headerKey, headerValue);
        Integer deliveryMode = 12;
        Integer priority = 13;
        String correlationId = "correlationId";
        String replyTo = "replyTo";
        String expiration = "expiration";
        String messageId = "messageId";
        Date timestamp = new Date();
        String type = "type";
        String userId = "userId";
        String appId = "appId";
        String clusterId = "clusterId";
        when(DEFAULT_BASIC_PROPERTIES.getContentType()).thenReturn(contentType);
        when(DEFAULT_BASIC_PROPERTIES.getContentEncoding()).thenReturn(contentEncoding);
        when(DEFAULT_BASIC_PROPERTIES.getHeaders()).thenReturn(headers);
        when(DEFAULT_BASIC_PROPERTIES.getDeliveryMode()).thenReturn(deliveryMode);
        when(DEFAULT_BASIC_PROPERTIES.getPriority()).thenReturn(priority);
        when(DEFAULT_BASIC_PROPERTIES.getCorrelationId()).thenReturn(correlationId);
        when(DEFAULT_BASIC_PROPERTIES.getReplyTo()).thenReturn(replyTo);
        when(DEFAULT_BASIC_PROPERTIES.getExpiration()).thenReturn(expiration);
        when(DEFAULT_BASIC_PROPERTIES.getMessageId()).thenReturn(messageId);
        when(DEFAULT_BASIC_PROPERTIES.getTimestamp()).thenReturn(timestamp);
        when(DEFAULT_BASIC_PROPERTIES.getType()).thenReturn(type);
        when(DEFAULT_BASIC_PROPERTIES.getUserId()).thenReturn(userId);
        when(DEFAULT_BASIC_PROPERTIES.getAppId()).thenReturn(appId);
        when(DEFAULT_BASIC_PROPERTIES.getClusterId()).thenReturn(clusterId);

        List<Message> messages = sut.getMessagesFromQueue(DEFAULT_BROKER_NAME, DEFAULT_QUEUE_NAME, DEFAULT_MAX_NO_OF_MESSAGES);

        assertThat(messages, Matchers.hasSize(1));
        Message message = messages.get(0);
        BasicProperties basicProperties = message.getProperties();
        assertNotNull(basicProperties);
        assertEquals(contentType, basicProperties.getContentType());
        assertEquals(contentEncoding, basicProperties.getContentEncoding());
        assertThat(basicProperties.getHeaders().keySet(), Matchers.hasSize(1));
        assertThat(basicProperties.getHeaders(), Matchers.hasKey(headerKey));
        assertEquals(headerValue, basicProperties.getHeaders().get(headerKey));
        assertEquals(deliveryMode, basicProperties.getDeliveryMode());
        assertEquals(priority, basicProperties.getPriority());
        assertEquals(correlationId, basicProperties.getCorrelationId());
        assertEquals(replyTo, basicProperties.getReplyTo());
        assertEquals(expiration, basicProperties.getExpiration());
        assertEquals(messageId, basicProperties.getMessageId());
        assertEquals(timestamp, basicProperties.getTimestamp());
        assertEquals(type, basicProperties.getType());
        assertEquals(userId, basicProperties.getUserId());
        assertEquals(appId, basicProperties.getAppId());
        assertEquals(clusterId, basicProperties.getClusterId());
    }

    @Test
    public void shouldMapRabbitMqSpecificTypesInAmqpBasicPropertiesHeaders() throws Exception {
        GetResponse getResponse = mockDefaultGetResponse(0);
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenReturn(getResponse);

        String headerKey = "headerKey";
        String headerValue = "headerValue";
        LongString longStringValue = mock(LongString.class);
        when(longStringValue.toString()).thenReturn(headerValue);
        Map<String, Object> headers = new HashMap<>();
        headers.put(headerKey, longStringValue);
        when(DEFAULT_BASIC_PROPERTIES.getHeaders()).thenReturn(headers);

        List<Message> messages = sut.getMessagesFromQueue(DEFAULT_BROKER_NAME, DEFAULT_QUEUE_NAME, DEFAULT_MAX_NO_OF_MESSAGES);

        assertThat(messages, Matchers.hasSize(1));
        Message message = messages.get(0);
        BasicProperties basicProperties = message.getProperties();
        assertNotNull(basicProperties);
        assertThat(basicProperties.getHeaders().keySet(), Matchers.hasSize(1));
        assertThat(basicProperties.getHeaders(), Matchers.hasKey(headerKey));
        assertEquals(headerValue, basicProperties.getHeaders().get(headerKey));
    }

    @Test
    public void shouldMapRabbitMqSpecificTypesInAmqpBasicPropertiesHeadersDeepInLists() throws Exception {
        GetResponse getResponse = mockDefaultGetResponse(0);
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenReturn(getResponse);

        String headerKey = "headerKey";
        String listValue = "listValue";
        LongString longStringValue = mock(LongString.class);
        when(longStringValue.toString()).thenReturn(listValue);
        List<String> list = Arrays.asList(listValue);
        Map<String, Object> headers = new HashMap<>();
        headers.put(headerKey, list);
        when(DEFAULT_BASIC_PROPERTIES.getHeaders()).thenReturn(headers);

        List<Message> messages = sut.getMessagesFromQueue(DEFAULT_BROKER_NAME, DEFAULT_QUEUE_NAME, DEFAULT_MAX_NO_OF_MESSAGES);

        assertThat(messages, Matchers.hasSize(1));
        Message message = messages.get(0);
        BasicProperties basicProperties = message.getProperties();
        assertNotNull(basicProperties);
        assertThat(basicProperties.getHeaders().keySet(), Matchers.hasSize(1));
        assertThat(basicProperties.getHeaders(), Matchers.hasKey(headerKey));
        assertThat(basicProperties.getHeaders().get(headerKey), Matchers.instanceOf(List.class));
        assertThat(((List<String>)basicProperties.getHeaders().get(headerKey)), Matchers.hasSize(1));
        assertThat(((List<String>)basicProperties.getHeaders().get(headerKey)), Matchers.contains(listValue));
    }

    @Test
    public void shouldMapRabbitMqSpecificTypesInAmqpBasicPropertiesHeadersDeepInMap() throws Exception {
        GetResponse getResponse = mockDefaultGetResponse(0);
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenReturn(getResponse);

        String headerKey = "headerKey";
        String subHeaderKey = "subHeaderKey";
        String subHeaderValue = "subHeaderValue";
        LongString longStringValue = mock(LongString.class);
        when(longStringValue.toString()).thenReturn(subHeaderValue);
        Map<String, Object> subHeaders = new HashMap<>();
        subHeaders.put(subHeaderKey, longStringValue);
        Map<String, Object> headers = new HashMap<>();
        headers.put(headerKey, subHeaders);
        when(DEFAULT_BASIC_PROPERTIES.getHeaders()).thenReturn(headers);

        List<Message> messages = sut.getMessagesFromQueue(DEFAULT_BROKER_NAME, DEFAULT_QUEUE_NAME, DEFAULT_MAX_NO_OF_MESSAGES);

        assertThat(messages, Matchers.hasSize(1));
        Message message = messages.get(0);
        BasicProperties basicProperties = message.getProperties();
        assertNotNull(basicProperties);
        assertThat(basicProperties.getHeaders().keySet(), Matchers.hasSize(1));
        assertThat(basicProperties.getHeaders(), Matchers.hasKey(headerKey));
        assertThat(basicProperties.getHeaders().get(headerKey), Matchers.instanceOf(Map.class));
        assertThat(((Map<String,Object>)basicProperties.getHeaders().get(headerKey)).keySet(), Matchers.hasSize(1));
        assertThat(((Map<String,Object>)basicProperties.getHeaders().get(headerKey)), Matchers.hasKey(subHeaderKey));
        assertEquals(subHeaderValue, ((Map)basicProperties.getHeaders().get(headerKey)).get(subHeaderKey));
    }

    private GetResponse mockDefaultGetResponse(int remainingMessageCount) {
        GetResponse response = mock(GetResponse.class);
        when(response.getEnvelope()).thenReturn(DEFAULT_ENVELOPE);
        when(response.getProps()).thenReturn(DEFAULT_BASIC_PROPERTIES);
        when(response.getBody()).thenReturn(DEFAULT_PAYLOAD);
        when(response.getMessageCount()).thenReturn(remainingMessageCount);
        return response;
    }

    private void assertDefaultMessage(Message message) {
        assertEquals(DEFAULT_ENVELOPE, message.getEnvelope());
        assertThat(message.getProperties(), Matchers.instanceOf(BasicProperties.class));
        assertEquals(DEFAULT_PAYLOAD, message.getBody());
    }

}
