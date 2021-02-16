package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.utils;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.LongString;
import de.gessnerfl.rabbitmq.queue.management.model.BasicProperties;
import de.gessnerfl.rabbitmq.queue.management.model.Message;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageMapperTest {
    private final static byte[] DEFAULT_PAYLOAD = "defaultPayload".getBytes(StandardCharsets.UTF_8);

    @Mock
    private MessageChecksum messageChecksum;

    @InjectMocks
    private MessageMapper sut;

    @Test
    void shouldMapBasicMessageFields(){
        String checksum = "checksum";
        Envelope envelope = mock(Envelope.class);
        AMQP.BasicProperties basicProperties = mock(AMQP.BasicProperties.class);
        GetResponse getResponse = mockResponse(envelope, basicProperties);

        when(messageChecksum.createFor(basicProperties, DEFAULT_PAYLOAD)).thenReturn(checksum);

        Message result = sut.map(getResponse);

        assertEquals(DEFAULT_PAYLOAD, result.getBody());
        assertEquals(checksum, result.getChecksum());
        assertSame(envelope, result.getEnvelope());
        assertNotNull(result.getProperties());
    }


    @Test
    void shouldMapAmqpBasicProperties() {
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

        AMQP.BasicProperties basicProperties = mock(AMQP.BasicProperties.class);
        when(basicProperties.getContentType()).thenReturn(contentType);
        when(basicProperties.getContentEncoding()).thenReturn(contentEncoding);
        when(basicProperties.getHeaders()).thenReturn(headers);
        when(basicProperties.getDeliveryMode()).thenReturn(deliveryMode);
        when(basicProperties.getPriority()).thenReturn(priority);
        when(basicProperties.getCorrelationId()).thenReturn(correlationId);
        when(basicProperties.getReplyTo()).thenReturn(replyTo);
        when(basicProperties.getExpiration()).thenReturn(expiration);
        when(basicProperties.getMessageId()).thenReturn(messageId);
        when(basicProperties.getTimestamp()).thenReturn(timestamp);
        when(basicProperties.getType()).thenReturn(type);
        when(basicProperties.getUserId()).thenReturn(userId);
        when(basicProperties.getAppId()).thenReturn(appId);
        when(basicProperties.getClusterId()).thenReturn(clusterId);

        Envelope envelope = mock(Envelope.class);
        GetResponse getResponse = mockResponse(envelope, basicProperties);

        Message result = sut.map(getResponse);

        BasicProperties basicPropertiesOfResult = result.getProperties();
        assertNotNull(basicPropertiesOfResult);
        assertEquals(contentType, basicPropertiesOfResult.getContentType());
        assertEquals(contentEncoding, basicPropertiesOfResult.getContentEncoding());
        assertThat(basicPropertiesOfResult.getHeaders().keySet(), Matchers.hasSize(1));
        assertThat(basicPropertiesOfResult.getHeaders(), Matchers.hasKey(headerKey));
        assertEquals(headerValue, basicPropertiesOfResult.getHeaders().get(headerKey));
        assertEquals(deliveryMode, basicPropertiesOfResult.getDeliveryMode());
        assertEquals(priority, basicPropertiesOfResult.getPriority());
        assertEquals(correlationId, basicPropertiesOfResult.getCorrelationId());
        assertEquals(replyTo, basicPropertiesOfResult.getReplyTo());
        assertEquals(expiration, basicPropertiesOfResult.getExpiration());
        assertEquals(messageId, basicPropertiesOfResult.getMessageId());
        assertEquals(timestamp, basicPropertiesOfResult.getTimestamp());
        assertEquals(type, basicPropertiesOfResult.getType());
        assertEquals(userId, basicPropertiesOfResult.getUserId());
        assertEquals(appId, basicPropertiesOfResult.getAppId());
        assertEquals(clusterId, basicPropertiesOfResult.getClusterId());
    }

    @Test
    void shouldMapRabbitMqSpecificTypesInAmqpBasicPropertiesHeaders() {
        String headerKey = "headerKey";
        String headerValue = "headerValue";
        LongString longStringValue = mock(LongString.class);
        when(longStringValue.toString()).thenReturn(headerValue);
        Map<String, Object> headers = new HashMap<>();
        headers.put(headerKey, longStringValue);

        AMQP.BasicProperties basicProperties = mock(AMQP.BasicProperties.class);
        when(basicProperties.getHeaders()).thenReturn(headers);

        Envelope envelope = mock(Envelope.class);
        GetResponse getResponse = mockResponse(envelope, basicProperties);

        Message result = sut.map(getResponse);

        BasicProperties basicPropertiesOfResult = result.getProperties();
        assertNotNull(basicPropertiesOfResult);
        assertThat(basicPropertiesOfResult.getHeaders().keySet(), Matchers.hasSize(1));
        assertThat(basicPropertiesOfResult.getHeaders(), Matchers.hasKey(headerKey));
        assertEquals(headerValue, basicPropertiesOfResult.getHeaders().get(headerKey));
    }

    @Test
    void shouldMapRabbitMqSpecificTypesInAmqpBasicPropertiesHeadersDeepInLists() {
        String headerKey = "headerKey";
        String listValue = "listValue";
        List<String> list = Arrays.asList(listValue);
        Map<String, Object> headers = new HashMap<>();
        headers.put(headerKey, list);

        AMQP.BasicProperties basicProperties = mock(AMQP.BasicProperties.class);
        when(basicProperties.getHeaders()).thenReturn(headers);

        Envelope envelope = mock(Envelope.class);
        GetResponse getResponse = mockResponse(envelope, basicProperties);

        Message result = sut.map(getResponse);

        BasicProperties basicPropertiesOfResult = result.getProperties();
        assertNotNull(basicPropertiesOfResult);
        assertThat(basicPropertiesOfResult.getHeaders().keySet(), Matchers.hasSize(1));
        assertThat(basicPropertiesOfResult.getHeaders(), Matchers.hasKey(headerKey));
        assertThat(basicPropertiesOfResult.getHeaders().get(headerKey), Matchers.instanceOf(List.class));
        assertThat(((List<String>)basicPropertiesOfResult.getHeaders().get(headerKey)), Matchers.hasSize(1));
        assertThat(((List<String>)basicPropertiesOfResult.getHeaders().get(headerKey)), Matchers.contains(listValue));
    }

    @Test
    void shouldMapRabbitMqSpecificTypesInAmqpBasicPropertiesHeadersDeepInMap() {
        String headerKey = "headerKey";
        String subHeaderKey = "subHeaderKey";
        String subHeaderValue = "subHeaderValue";
        LongString longStringValue = mock(LongString.class);
        when(longStringValue.toString()).thenReturn(subHeaderValue);
        Map<String, Object> subHeaders = new HashMap<>();
        subHeaders.put(subHeaderKey, longStringValue);
        Map<String, Object> headers = new HashMap<>();
        headers.put(headerKey, subHeaders);

        AMQP.BasicProperties basicProperties = mock(AMQP.BasicProperties.class);
        when(basicProperties.getHeaders()).thenReturn(headers);

        Envelope envelope = mock(Envelope.class);
        GetResponse getResponse = mockResponse(envelope, basicProperties);

        Message result = sut.map(getResponse);

        BasicProperties basicPropertiesOfResult = result.getProperties();
        assertNotNull(basicPropertiesOfResult);
        assertThat(basicPropertiesOfResult.getHeaders().keySet(), Matchers.hasSize(1));
        assertThat(basicPropertiesOfResult.getHeaders(), Matchers.hasKey(headerKey));
        assertThat(basicPropertiesOfResult.getHeaders().get(headerKey), Matchers.instanceOf(Map.class));
        assertThat(((Map<String,Object>)basicPropertiesOfResult.getHeaders().get(headerKey)).keySet(), Matchers.hasSize(1));
        assertThat(((Map<String,Object>)basicPropertiesOfResult.getHeaders().get(headerKey)), Matchers.hasKey(subHeaderKey));
        assertEquals(subHeaderValue, ((Map)basicPropertiesOfResult.getHeaders().get(headerKey)).get(subHeaderKey));
    }

    private GetResponse mockResponse(Envelope envelope, AMQP.BasicProperties basicProperties) {
        GetResponse response = mock(GetResponse.class);
        when(response.getEnvelope()).thenReturn(envelope);
        when(response.getProps()).thenReturn(basicProperties);
        when(response.getBody()).thenReturn(DEFAULT_PAYLOAD);
        return response;
    }
}