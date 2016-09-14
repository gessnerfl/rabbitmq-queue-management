package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.any;
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
import org.mockito.runners.MockitoJUnitRunner;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.GetResponse;

import de.gessnerfl.rabbitmq.queue.management.connection.CloseableChannelWrapper;
import de.gessnerfl.rabbitmq.queue.management.connection.ConnectionFailedException;
import de.gessnerfl.rabbitmq.queue.management.connection.Connector;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.utils.MessageChecksum;

@RunWith(MockitoJUnitRunner.class)
public class MessageDeleteOperationTest {
    private final static String DEFAULT_QUEUE_NAME = "defaultQueue";
    private final static Envelope DEFAULT_ENVELOPE = mock(Envelope.class);
    private final static Long DEFAULT_DELIVERY_TAG = 123L;
    private final static AMQP.BasicProperties DEFAULT_BASIC_PROPERTIES = mock(AMQP.BasicProperties.class);
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
    
    @InjectMocks
    private MessageDeleteOperation sut;
    
    @Before
    public void init(){
      when(closeableChannelWrapper.getChannel()).thenReturn(channel);
      when(connector.connectAsClosable()).thenReturn(closeableChannelWrapper);
      when(DEFAULT_ENVELOPE.getDeliveryTag()).thenReturn(DEFAULT_DELIVERY_TAG);
      when(messageChecksum.createFor(DEFAULT_BASIC_PROPERTIES, DEFAULT_PAYLOAD)).thenReturn(DEFAULT_CHECKSUM);
    }
    
    @Test
    public void shouldRetrieveMessageFromQueueAndAckForDeletionWhenChecksumMatches() throws Exception {
        GetResponse response = mockDefaultGetResponse();
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenReturn(response);
        
        sut.deleteFirstMessageInQueue(DEFAULT_QUEUE_NAME, DEFAULT_CHECKSUM);
        
        verify(channel).basicAck(DEFAULT_DELIVERY_TAG, false);
    }
    
    @Test
    public void shouldRetrieveMessageFromQueueAndNackWithRequeuWhenChecksumDoesNotMatch() throws Exception {
        GetResponse response = mockDefaultGetResponse();
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenReturn(response);
        
        try{
            sut.deleteFirstMessageInQueue(DEFAULT_QUEUE_NAME, "invalidChecksum");
            fail();
        }catch(MessageDeletionFailedException e){
        }
        
        verify(channel).basicNack(DEFAULT_DELIVERY_TAG, false, true);
    }
    
    @Test
    public void shouldFailWhenQueueIsEmpty() throws Exception {
        when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenReturn(null);
        
        try{
            sut.deleteFirstMessageInQueue(DEFAULT_QUEUE_NAME, "anyChecksum");
            fail();
        }catch(MessageDeletionFailedException e){
        }
        
        verify(channel, never()).basicNack(any(Long.class), anyBoolean(), anyBoolean());
    }
    
    @Test
    public void shouldThrowExcpetionWhenConnectionCannotBeEstablished() throws Exception {
      ConnectionFailedException expectedException = new ConnectionFailedException(null);
      when(connector.connectAsClosable()).thenThrow(expectedException);
      
      try{
        sut.deleteFirstMessageInQueue(DEFAULT_QUEUE_NAME, DEFAULT_CHECKSUM);
      }catch(ConnectionFailedException e){
        assertSame(expectedException, e);
      }
    }
    
    @Test
    public void shouldThrowExceptionWhenMessageCannotBeFetched() throws Exception {
      IOException expectedException = new IOException();
      when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenThrow(expectedException);
      
      try{
        sut.deleteFirstMessageInQueue(DEFAULT_QUEUE_NAME, DEFAULT_CHECKSUM);
      }catch(MessageDeletionFailedException e){
        assertSame(expectedException, e.getCause());
      }
      
      verify(channel).basicGet(DEFAULT_QUEUE_NAME, false);
      verifyNoMoreInteractions(channel);
    }
    
    @Test
    public void shouldThrowExceptionWhenAckCannotBeSentAfterSuccessfulChecksumCheck() throws Exception {
      GetResponse getResponse = mockDefaultGetResponse();
      when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenReturn(getResponse);
      when(messageChecksum.createFor(DEFAULT_BASIC_PROPERTIES, DEFAULT_PAYLOAD)).thenReturn(DEFAULT_CHECKSUM);
      IOException expectedException = new IOException();
      doThrow(expectedException).when(channel).basicAck(DEFAULT_DELIVERY_TAG, false);
      
      try{
        sut.deleteFirstMessageInQueue(DEFAULT_QUEUE_NAME, DEFAULT_CHECKSUM);
      }catch(MessageDeletionFailedException e){
        assertSame(expectedException, e.getCause());
      }
      
      verify(messageChecksum).createFor(DEFAULT_BASIC_PROPERTIES, DEFAULT_PAYLOAD);
      verify(channel).basicGet(DEFAULT_QUEUE_NAME, false);
      verify(channel).basicAck(DEFAULT_DELIVERY_TAG, false);
      verifyNoMoreInteractions(channel);
    }
    
    @Test
    public void shouldThrowExceptionWhenNackCannotBeSentAfterChecksumMatchFailed() throws Exception {
      GetResponse getResponse = mockDefaultGetResponse();
      when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenReturn(getResponse);
      when(messageChecksum.createFor(DEFAULT_BASIC_PROPERTIES, DEFAULT_PAYLOAD)).thenReturn(DEFAULT_CHECKSUM);
      IOException expectedException = new IOException();
      doThrow(expectedException).when(channel).basicNack(DEFAULT_DELIVERY_TAG, false, true);
      
      try{
        sut.deleteFirstMessageInQueue(DEFAULT_QUEUE_NAME, "invalidMessage");
      }catch(MessageDeletionFailedException e){
        assertSame(expectedException, e.getCause());
      }

      verify(messageChecksum).createFor(DEFAULT_BASIC_PROPERTIES, DEFAULT_PAYLOAD);
      verify(channel).basicGet(DEFAULT_QUEUE_NAME, false);
      verify(channel).basicNack(DEFAULT_DELIVERY_TAG, false, true);
      verifyNoMoreInteractions(channel);
    }
    
    private GetResponse mockDefaultGetResponse() {
        GetResponse response = mock(GetResponse.class);
        when(response.getEnvelope()).thenReturn(DEFAULT_ENVELOPE);
        when(response.getProps()).thenReturn(DEFAULT_BASIC_PROPERTIES);
        when(response.getBody()).thenReturn(DEFAULT_PAYLOAD);
        return response;
      }
}
