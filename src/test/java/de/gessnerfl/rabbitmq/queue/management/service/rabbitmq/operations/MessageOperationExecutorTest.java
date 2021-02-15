package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.*;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.utils.RoutingMessageHeaderModifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import de.gessnerfl.rabbitmq.queue.management.connection.CloseableChannelWrapper;
import de.gessnerfl.rabbitmq.queue.management.connection.ConnectionFailedException;
import de.gessnerfl.rabbitmq.queue.management.connection.Connector;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.utils.MessageChecksum;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
public class MessageOperationExecutorTest {
    private static final String DEFAULT_VHOST_NAME = "defaultVhost";
    private static final String DEFAULT_QUEUE_NAME = "defaultQueue";
    private static final Envelope DEFAULT_ENVELOPE = mock(Envelope.class);
    private static final Long DEFAULT_DELIVERY_TAG = 123L;
    private static final AMQP.BasicProperties DEFAULT_BASIC_PROPERTIES = mock(AMQP.BasicProperties.class);
    private static final byte[] DEFAULT_PAYLOAD = "defaultPayload".getBytes(StandardCharsets.UTF_8);
    private static final String DEFAULT_CHECKSUM = "defaultChecksum";
    private static final String DEFAULT_ROUTING_TARGET_EXCHANGE = "targetExchange";
    private static final String DEFAULT_ROUTING_TARGET_ROUTING_KEY = "targetRoutingKey";
    private static final String DEFAULT_ROUTING_COUNT_HEADER = "countHeader";
    private static final OperationId DEFAULT_OPERATION_ID = new OperationId();

    @Mock
    private Connector connector;
    @Mock
    private MessageChecksum messageChecksum;
    @Mock
    private CloseableChannelWrapper closeableChannelWrapper;
    @Mock
    private OperationIdGenerator operationIdGenerator;
    @Mock
    private RoutingMessageHeaderModifier routingMessageHeaderModifier;
    @Mock
    private StateKeepingReturnListenerFactory stateKeepingReturnListenerFactory;

    @Mock
    private Channel channel;
    @Mock
    private MessageOperationFunction basicFunction;
    @Mock
    private RouteResolvingFunction routeResolvingFunction;
    @Mock
    private StateKeepingReturnListener stateKeepingReturnListener;

    @InjectMocks
    private MessageOperationExecutor sut;

    @BeforeEach
    public void init() {
        when(connector.connectAsClosable(DEFAULT_VHOST_NAME)).thenReturn(closeableChannelWrapper);
        when(DEFAULT_ENVELOPE.getDeliveryTag()).thenReturn(DEFAULT_DELIVERY_TAG);
        //

        when(operationIdGenerator.generate()).thenReturn(DEFAULT_OPERATION_ID);
        //
    }

    @Test
    public void shouldRetrieveMessageFromQueueAndPerformFunctionWhenChecksumMatches() throws Exception {
        GetResponse response = mockDefaultGetResponse();

        when(closeableChannelWrapper.getChannel()).thenReturn(channel);
        when(messageChecksum.createFor(DEFAULT_BASIC_PROPERTIES, DEFAULT_PAYLOAD)).thenReturn(DEFAULT_CHECKSUM);
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenReturn(response);

        sut.consumeMessageAndApplyFunctionAndAcknowledgeOnSuccess(DEFAULT_VHOST_NAME, DEFAULT_QUEUE_NAME, DEFAULT_CHECKSUM, basicFunction);

        verify(basicFunction).apply(DEFAULT_OPERATION_ID, channel, response);
        verify(channel).basicAck(DEFAULT_DELIVERY_TAG, false);
    }

    @Test
    public void shouldRetrieveMessageFromQueueAndNackWithRequeuWhenChecksumDoesNotMatch() throws Exception {
        GetResponse response = mockDefaultGetResponse();

        when(closeableChannelWrapper.getChannel()).thenReturn(channel);
        when(messageChecksum.createFor(DEFAULT_BASIC_PROPERTIES, DEFAULT_PAYLOAD)).thenReturn(DEFAULT_CHECKSUM);
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenReturn(response);

        try {
            sut.consumeMessageAndApplyFunctionAndAcknowledgeOnSuccess(DEFAULT_VHOST_NAME, DEFAULT_QUEUE_NAME, "invalidChecksum", basicFunction);
            fail();
        } catch (MessageOperationFailedException e) {
        }

        verify(channel).basicNack(DEFAULT_DELIVERY_TAG, false, true);
        verify(basicFunction, never()).apply(any(OperationId.class), any(Channel.class), any(GetResponse.class));
    }

    @Test
    public void shouldFailWhenQueueIsEmpty() throws Exception {
        when(closeableChannelWrapper.getChannel()).thenReturn(channel);
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenReturn(null);

        try {
            sut.consumeMessageAndApplyFunctionAndAcknowledgeOnSuccess(DEFAULT_VHOST_NAME, DEFAULT_QUEUE_NAME, "anyChecksum", basicFunction);
            fail();
        } catch (MessageOperationFailedException e) {
        }

        verify(channel, never()).basicNack(any(Long.class), anyBoolean(), anyBoolean());
        verify(basicFunction, never()).apply(any(OperationId.class), any(Channel.class), any(GetResponse.class));
    }

    @Test
    public void shouldThrowExceptionWhenConnectionCannotBeEstablished() throws Exception {
        ConnectionFailedException expectedException = new ConnectionFailedException(null);

        when(connector.connectAsClosable(DEFAULT_VHOST_NAME)).thenThrow(expectedException);

        try {
            sut.consumeMessageAndApplyFunctionAndAcknowledgeOnSuccess(DEFAULT_VHOST_NAME, DEFAULT_QUEUE_NAME, DEFAULT_CHECKSUM, basicFunction);
        } catch (MessageOperationFailedException e) {
            assertSame(expectedException, e.getCause());
        }

        verify(basicFunction, never()).apply(any(OperationId.class), any(Channel.class), any(GetResponse.class));
    }

    @Test
    public void shouldThrowExceptionWhenMessageCannotBeFetched() throws Exception {
        IOException expectedException = new IOException();

        when(closeableChannelWrapper.getChannel()).thenReturn(channel);
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenThrow(expectedException);

        try {
            sut.consumeMessageAndApplyFunctionAndAcknowledgeOnSuccess(DEFAULT_VHOST_NAME, DEFAULT_QUEUE_NAME, DEFAULT_CHECKSUM, basicFunction);
        } catch (MessageOperationFailedException e) {
            assertSame(expectedException, e.getCause());
        }

        verify(channel).basicGet(DEFAULT_QUEUE_NAME, false);
        verifyNoMoreInteractions(channel);
        verify(basicFunction, never()).apply(any(OperationId.class), any(Channel.class), any(GetResponse.class));
    }

    @Test
    public void shouldThrowExceptionWhenFunctionCannotBePerformedSuccessfulChecksumCheck()
            throws Exception {
        GetResponse getResponse = mockDefaultGetResponse();
        IOException expectedException = new IOException();

        when(closeableChannelWrapper.getChannel()).thenReturn(channel);
        when(messageChecksum.createFor(DEFAULT_BASIC_PROPERTIES, DEFAULT_PAYLOAD)).thenReturn(DEFAULT_CHECKSUM);
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenReturn(getResponse);
        doThrow(expectedException).when(basicFunction).apply(DEFAULT_OPERATION_ID, channel, getResponse);

        try {
            sut.consumeMessageAndApplyFunctionAndAcknowledgeOnSuccess(DEFAULT_VHOST_NAME, DEFAULT_QUEUE_NAME, DEFAULT_CHECKSUM, basicFunction);
        } catch (MessageOperationFailedException e) {
            assertSame(expectedException, e.getCause());
        }

        verify(messageChecksum).createFor(DEFAULT_BASIC_PROPERTIES, DEFAULT_PAYLOAD);
        verify(channel).basicGet(DEFAULT_QUEUE_NAME, false);
        verify(basicFunction).apply(DEFAULT_OPERATION_ID, channel, getResponse);
        verify(channel).basicNack(DEFAULT_DELIVERY_TAG, false, true);
        verifyNoMoreInteractions(channel);
    }

    @Test
    public void shouldThrowExceptionWhenAckCannotBeSentAfterSuccessfulFunctionExecution() throws Exception {
        GetResponse getResponse = mockDefaultGetResponse();
        IOException expectedException = new IOException();

        when(closeableChannelWrapper.getChannel()).thenReturn(channel);
        when(messageChecksum.createFor(DEFAULT_BASIC_PROPERTIES, DEFAULT_PAYLOAD)).thenReturn(DEFAULT_CHECKSUM);
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenReturn(getResponse);
        doThrow(expectedException).when(channel).basicAck(DEFAULT_DELIVERY_TAG, false);

        try {
            sut.consumeMessageAndApplyFunctionAndAcknowledgeOnSuccess(DEFAULT_VHOST_NAME, DEFAULT_QUEUE_NAME, DEFAULT_CHECKSUM, basicFunction);
        } catch (MessageOperationFailedException e) {
            assertSame(expectedException, e.getCause());
        }

        verify(messageChecksum).createFor(DEFAULT_BASIC_PROPERTIES, DEFAULT_PAYLOAD);
        verify(channel).basicGet(DEFAULT_QUEUE_NAME, false);
        verify(basicFunction).apply(DEFAULT_OPERATION_ID, channel, getResponse);
        verify(channel).basicAck(DEFAULT_DELIVERY_TAG, false);
        verify(channel).basicNack(DEFAULT_DELIVERY_TAG, false, true);
        verifyNoMoreInteractions(channel);
    }

    @Test
    public void shouldThrowExceptionWhenNackCannotBeSentAfterChecksumMatchFailed()
            throws Exception {
        GetResponse getResponse = mockDefaultGetResponse();
        IOException expectedException = new IOException();

        when(closeableChannelWrapper.getChannel()).thenReturn(channel);
        when(messageChecksum.createFor(DEFAULT_BASIC_PROPERTIES, DEFAULT_PAYLOAD)).thenReturn(DEFAULT_CHECKSUM);
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenReturn(getResponse);
        doThrow(expectedException).when(channel).basicNack(DEFAULT_DELIVERY_TAG, false, true);

        try {
            sut.consumeMessageAndApplyFunctionAndAcknowledgeOnSuccess(DEFAULT_VHOST_NAME, DEFAULT_QUEUE_NAME, "invalidMessage", basicFunction);
        } catch (MessageOperationFailedException e) {
            assertSame(expectedException, e.getCause());
        }

        verify(messageChecksum).createFor(DEFAULT_BASIC_PROPERTIES, DEFAULT_PAYLOAD);
        verify(channel).basicGet(DEFAULT_QUEUE_NAME, false);
        verify(channel).basicNack(DEFAULT_DELIVERY_TAG, false, true);
        verifyNoMoreInteractions(channel);
        verify(basicFunction, never()).apply(any(OperationId.class), any(Channel.class), any(GetResponse.class));
    }

    @Test
    public void shouldPerformMassOperationUntilAllMessagesAreConsumedAndMessagesDoNotContainAnyBasicProperty() throws Exception {
        GetResponse getResponse = mock(GetResponse.class);
        when(getResponse.getEnvelope()).thenReturn(DEFAULT_ENVELOPE);

        testSuccessfulMassOperationForMessage(getResponse);
    }

    @Test
    public void shouldPerformMassOperationUntilAllMessagesAreConsumedAndMessagesDoNotContainAnyHeader() throws Exception {
        AMQP.BasicProperties basicProperties = mock(AMQP.BasicProperties.class);
        when(basicProperties.getHeaders()).thenReturn(null);
        GetResponse getResponse = mock(GetResponse.class);
        when(getResponse.getEnvelope()).thenReturn(DEFAULT_ENVELOPE);
        when(getResponse.getProps()).thenReturn(basicProperties);

        testSuccessfulMassOperationForMessage(getResponse);
    }

    @Test
    public void shouldPerformMassOperationUntilAllMessagesAreConsumedAndOperationIdIsNotTheSame() throws Exception {
        AMQP.BasicProperties basicProperties = mock(AMQP.BasicProperties.class);
        when(basicProperties.getHeaders()).thenReturn(new HashMap<>());
        GetResponse getResponse = mock(GetResponse.class);
        when(getResponse.getEnvelope()).thenReturn(DEFAULT_ENVELOPE);
        when(getResponse.getProps()).thenReturn(basicProperties);

        testSuccessfulMassOperationForMessage(getResponse);
    }

    private void testSuccessfulMassOperationForMessage(GetResponse getResponse) throws Exception {
        when(closeableChannelWrapper.getChannel()).thenReturn(channel);
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenReturn(getResponse, getResponse, null);

        sut.consumeAllMessageAndApplyFunctionAndAcknowledgeOnSuccess(DEFAULT_VHOST_NAME, DEFAULT_QUEUE_NAME, basicFunction);

        InOrder io = Mockito.inOrder(channel, basicFunction);
        io.verify(channel).basicGet(DEFAULT_QUEUE_NAME, false);
        io.verify(basicFunction).apply(DEFAULT_OPERATION_ID, channel, getResponse);
        io.verify(channel).basicAck(DEFAULT_DELIVERY_TAG, false);
        io.verify(channel).basicGet(DEFAULT_QUEUE_NAME, false);
        io.verify(basicFunction).apply(DEFAULT_OPERATION_ID, channel, getResponse);
        io.verify(channel).basicAck(DEFAULT_DELIVERY_TAG, false);
        io.verify(channel).basicGet(DEFAULT_QUEUE_NAME, false);
    }

    @Test
    public void shouldStopToPerformMassOperationWhenSameOperationIdAppearsInHeader() throws Exception {
        final Map<String, Object> headers = mock(Map.class);
        AMQP.BasicProperties basicProperties = mock(AMQP.BasicProperties.class);
        GetResponse getResponse = mock(GetResponse.class);

        when(basicProperties.getHeaders()).thenReturn(headers);
        when(getResponse.getEnvelope()).thenReturn(DEFAULT_ENVELOPE);
        when(getResponse.getProps()).thenReturn(basicProperties);
        when(headers.getOrDefault(eq(OperationId.HEADER_NAME), anyString())).thenReturn("otherHeader", "otherHeader", DEFAULT_OPERATION_ID.getValue());

        when(closeableChannelWrapper.getChannel()).thenReturn(channel);
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenReturn(getResponse, getResponse, getResponse);

        sut.consumeAllMessageAndApplyFunctionAndAcknowledgeOnSuccess(DEFAULT_VHOST_NAME, DEFAULT_QUEUE_NAME, basicFunction);

        InOrder io = Mockito.inOrder(channel, basicFunction);
        io.verify(channel).basicGet(DEFAULT_QUEUE_NAME, false);
        io.verify(basicFunction).apply(DEFAULT_OPERATION_ID, channel, getResponse);
        io.verify(channel).basicAck(DEFAULT_DELIVERY_TAG, false);
        io.verify(channel).basicGet(DEFAULT_QUEUE_NAME, false);
        io.verify(basicFunction).apply(DEFAULT_OPERATION_ID, channel, getResponse);
        io.verify(channel).basicAck(DEFAULT_DELIVERY_TAG, false);
        io.verify(channel).basicGet(DEFAULT_QUEUE_NAME, false);
        io.verify(channel).basicNack(DEFAULT_DELIVERY_TAG, false, true);
    }

    @Test
    public void shouldFailToRunMassOperationWhenMessagesCannotBeConsumed() throws Exception {
        IOException expectedException = new IOException();

        when(closeableChannelWrapper.getChannel()).thenReturn(channel);
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenThrow(expectedException);

        try {
            sut.consumeAllMessageAndApplyFunctionAndAcknowledgeOnSuccess(DEFAULT_VHOST_NAME, DEFAULT_QUEUE_NAME, basicFunction);
            fail();
        } catch (MessageOperationFailedException e) {
            assertEquals(expectedException, e.getCause());
        }

        verify(channel).basicGet(DEFAULT_QUEUE_NAME, false);
        verifyNoMoreInteractions(channel);
    }

    @Test
    public void shouldFailToRunMassOperationWhenFunctionThrowsAnException() throws Exception {
        GetResponse getResponse = mock(GetResponse.class);
        MessageOperationFailedException expectedException = mock(MessageOperationFailedException.class);

        when(getResponse.getEnvelope()).thenReturn(DEFAULT_ENVELOPE);
        when(getResponse.getProps()).thenReturn(DEFAULT_BASIC_PROPERTIES);


        when(closeableChannelWrapper.getChannel()).thenReturn(channel);
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenReturn(getResponse);
        doThrow(expectedException).when(basicFunction).apply(DEFAULT_OPERATION_ID, channel, getResponse);

        try {
            sut.consumeAllMessageAndApplyFunctionAndAcknowledgeOnSuccess(DEFAULT_VHOST_NAME, DEFAULT_QUEUE_NAME, basicFunction);
            fail();
        } catch (MessageOperationFailedException e) {
            assertEquals(expectedException, e);
        }

        verify(channel).basicGet(DEFAULT_QUEUE_NAME, false);
        verify(basicFunction).apply(DEFAULT_OPERATION_ID, channel, getResponse);
        verify(channel).basicNack(DEFAULT_DELIVERY_TAG, false, true);
        verifyNoMoreInteractions(channel);
    }

    @Test
    public void shouldRouteFirstMessage() throws Exception {
        GetResponse getResponse = mockDefaultGetResponse();
        RoutingDetails routingDetails = mockDefaultRoutingDetails();
        AMQP.BasicProperties mappedBasicProperties = mock(AMQP.BasicProperties.class);

        when(closeableChannelWrapper.getChannel()).thenReturn(channel);
        when(messageChecksum.createFor(DEFAULT_BASIC_PROPERTIES, DEFAULT_PAYLOAD)).thenReturn(DEFAULT_CHECKSUM);
        when(stateKeepingReturnListenerFactory.createFor(eq(DEFAULT_OPERATION_ID), any(Logger.class))).thenReturn(stateKeepingReturnListener);
        when(routeResolvingFunction.resolve(getResponse)).thenReturn(routingDetails);
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenReturn(getResponse);
        when(routingMessageHeaderModifier.modifyHeaders(DEFAULT_BASIC_PROPERTIES, DEFAULT_OPERATION_ID, DEFAULT_ROUTING_COUNT_HEADER)).thenReturn(mappedBasicProperties);

        sut.routeFirstMessageAndAcknowledgeOnSuccess(DEFAULT_VHOST_NAME, DEFAULT_QUEUE_NAME, DEFAULT_CHECKSUM, routeResolvingFunction);

        InOrder io = Mockito.inOrder(channel, routeResolvingFunction, routingMessageHeaderModifier);
        verifyMessageRouted(getResponse, mappedBasicProperties, io);
        io.verifyNoMoreInteractions();
    }

    @Test
    public void shouldFailToRouteFirstMessageWhenReturnListenerWasTriggered() throws Exception {
        GetResponse getResponse = mockDefaultGetResponse();
        RoutingDetails routingDetails = mockDefaultRoutingDetails();
        AMQP.BasicProperties mappedBasicProperties = mock(AMQP.BasicProperties.class);

        when(closeableChannelWrapper.getChannel()).thenReturn(channel);
        when(messageChecksum.createFor(DEFAULT_BASIC_PROPERTIES, DEFAULT_PAYLOAD)).thenReturn(DEFAULT_CHECKSUM);
        when(stateKeepingReturnListenerFactory.createFor(eq(DEFAULT_OPERATION_ID), any(Logger.class))).thenReturn(stateKeepingReturnListener);
        when(routeResolvingFunction.resolve(getResponse)).thenReturn(routingDetails);
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenReturn(getResponse);
        when(routingMessageHeaderModifier.modifyHeaders(DEFAULT_BASIC_PROPERTIES, DEFAULT_OPERATION_ID, DEFAULT_ROUTING_COUNT_HEADER)).thenReturn(mappedBasicProperties);
        when(stateKeepingReturnListener.isReceived()).thenReturn(true);

        try {
            sut.routeFirstMessageAndAcknowledgeOnSuccess(DEFAULT_VHOST_NAME, DEFAULT_QUEUE_NAME, DEFAULT_CHECKSUM, routeResolvingFunction);
            fail();
        } catch (MessageOperationFailedException e) {
            assertThat(e.getMessage(), containsString("basic.return received"));
        }

        InOrder io = Mockito.inOrder(channel, routeResolvingFunction, routingMessageHeaderModifier);
        io.verify(channel).basicGet(DEFAULT_QUEUE_NAME, false);
        io.verify(routeResolvingFunction).resolve(getResponse);
        io.verify(channel).addReturnListener(stateKeepingReturnListener);
        io.verify(channel).confirmSelect();
        io.verify(routingMessageHeaderModifier).modifyHeaders(DEFAULT_BASIC_PROPERTIES, DEFAULT_OPERATION_ID, DEFAULT_ROUTING_COUNT_HEADER);
        io.verify(channel).basicPublish(DEFAULT_ROUTING_TARGET_EXCHANGE, DEFAULT_ROUTING_TARGET_ROUTING_KEY, true, mappedBasicProperties, DEFAULT_PAYLOAD);
        io.verify(channel).waitForConfirmsOrDie(MessageOperationExecutor.MAX_WAIT_FOR_CONFIRM);
        io.verify(channel).basicNack(DEFAULT_DELIVERY_TAG, false, true);
        io.verifyNoMoreInteractions();
    }

    @Test
    public void shouldRouteAllMessage() throws Exception {
        GetResponse getResponse = mockDefaultGetResponse();
        RoutingDetails routingDetails = mockDefaultRoutingDetails();
        AMQP.BasicProperties mappedBasicProperties = mock(AMQP.BasicProperties.class);

        when(closeableChannelWrapper.getChannel()).thenReturn(channel);
        when(stateKeepingReturnListenerFactory.createFor(eq(DEFAULT_OPERATION_ID), any(Logger.class))).thenReturn(stateKeepingReturnListener);
        when(routeResolvingFunction.resolve(getResponse)).thenReturn(routingDetails);
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenReturn(getResponse, getResponse, null);
        when(routingMessageHeaderModifier.modifyHeaders(DEFAULT_BASIC_PROPERTIES, DEFAULT_OPERATION_ID, DEFAULT_ROUTING_COUNT_HEADER)).thenReturn(mappedBasicProperties);

        sut.routeAllMessagesAndAcknowledgeOnSuccess(DEFAULT_VHOST_NAME, DEFAULT_QUEUE_NAME, routeResolvingFunction);

        InOrder io = Mockito.inOrder(channel, routeResolvingFunction, routingMessageHeaderModifier);
        verifyMessageRouted(getResponse, mappedBasicProperties, io);
        verifyMessageRouted(getResponse, mappedBasicProperties, io);
        io.verify(channel).basicGet(DEFAULT_QUEUE_NAME, false);
        io.verifyNoMoreInteractions();
    }

    @Test
    public void shouldFailToRouteAllMessageWhenReturnListenerWasTriggered() throws Exception {
        GetResponse getResponse = mockDefaultGetResponse();
        RoutingDetails routingDetails = mockDefaultRoutingDetails();
        AMQP.BasicProperties mappedBasicProperties = mock(AMQP.BasicProperties.class);

        when(closeableChannelWrapper.getChannel()).thenReturn(channel);
        when(stateKeepingReturnListenerFactory.createFor(eq(DEFAULT_OPERATION_ID), any(Logger.class))).thenReturn(stateKeepingReturnListener);
        when(routeResolvingFunction.resolve(getResponse)).thenReturn(routingDetails);
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenReturn(getResponse, getResponse, null);
        when(routingMessageHeaderModifier.modifyHeaders(DEFAULT_BASIC_PROPERTIES, DEFAULT_OPERATION_ID, DEFAULT_ROUTING_COUNT_HEADER)).thenReturn(mappedBasicProperties);
        when(stateKeepingReturnListener.isReceived()).thenReturn(false, true);

        try {
            sut.routeAllMessagesAndAcknowledgeOnSuccess(DEFAULT_VHOST_NAME, DEFAULT_QUEUE_NAME, routeResolvingFunction);
            fail();
        } catch (MessageOperationFailedException e) {
            assertThat(e.getMessage(), containsString("basic.return received"));
        }

        InOrder io = Mockito.inOrder(channel, routeResolvingFunction, routingMessageHeaderModifier);
        verifyMessageRouted(getResponse, mappedBasicProperties, io);
        io.verify(channel).basicGet(DEFAULT_QUEUE_NAME, false);
        io.verify(routeResolvingFunction).resolve(getResponse);
        io.verify(channel).addReturnListener(stateKeepingReturnListener);
        io.verify(channel).confirmSelect();
        io.verify(routingMessageHeaderModifier).modifyHeaders(DEFAULT_BASIC_PROPERTIES, DEFAULT_OPERATION_ID, DEFAULT_ROUTING_COUNT_HEADER);
        io.verify(channel).basicPublish(DEFAULT_ROUTING_TARGET_EXCHANGE, DEFAULT_ROUTING_TARGET_ROUTING_KEY, true, mappedBasicProperties, DEFAULT_PAYLOAD);
        io.verify(channel).waitForConfirmsOrDie(MessageOperationExecutor.MAX_WAIT_FOR_CONFIRM);
        io.verify(channel).basicNack(DEFAULT_DELIVERY_TAG, false, true);
        io.verifyNoMoreInteractions();
    }

    private void verifyMessageRouted(GetResponse getResponse, AMQP.BasicProperties mappedBasicProperties, InOrder io) throws IOException, InterruptedException, TimeoutException {
        io.verify(channel).basicGet(DEFAULT_QUEUE_NAME, false);
        io.verify(routeResolvingFunction).resolve(getResponse);
        io.verify(channel).addReturnListener(stateKeepingReturnListener);
        io.verify(channel).confirmSelect();
        io.verify(routingMessageHeaderModifier).modifyHeaders(DEFAULT_BASIC_PROPERTIES, DEFAULT_OPERATION_ID, DEFAULT_ROUTING_COUNT_HEADER);
        io.verify(channel).basicPublish(DEFAULT_ROUTING_TARGET_EXCHANGE, DEFAULT_ROUTING_TARGET_ROUTING_KEY, true, mappedBasicProperties, DEFAULT_PAYLOAD);
        io.verify(channel).waitForConfirmsOrDie(MessageOperationExecutor.MAX_WAIT_FOR_CONFIRM);
        io.verify(channel).removeReturnListener(stateKeepingReturnListener);
        io.verify(channel).basicAck(DEFAULT_DELIVERY_TAG, false);
    }

    private RoutingDetails mockDefaultRoutingDetails() {
        RoutingDetails routingDetails = mock(RoutingDetails.class);
        when(routingDetails.getCountHeaderName()).thenReturn(DEFAULT_ROUTING_COUNT_HEADER);
        when(routingDetails.getExchange()).thenReturn(DEFAULT_ROUTING_TARGET_EXCHANGE);
        when(routingDetails.getRoutingKey()).thenReturn(DEFAULT_ROUTING_TARGET_ROUTING_KEY);
        return routingDetails;
    }

    private GetResponse mockDefaultGetResponse() {
        GetResponse response = mock(GetResponse.class);
        when(response.getEnvelope()).thenReturn(DEFAULT_ENVELOPE);
        when(response.getProps()).thenReturn(DEFAULT_BASIC_PROPERTIES);
        when(response.getBody()).thenReturn(DEFAULT_PAYLOAD);
        return response;
    }
}
