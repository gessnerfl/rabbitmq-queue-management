package de.gessnerfl.rabbitmq.deadletter.manager.service.rabbitmq.operations;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

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

import de.gessnerfl.rabbitmq.deadletter.manager.connection.CloseableChannelWrapper;
import de.gessnerfl.rabbitmq.deadletter.manager.connection.ConnectionFailedException;
import de.gessnerfl.rabbitmq.deadletter.manager.connection.Connector;
import de.gessnerfl.rabbitmq.deadletter.manager.model.Message;
import de.gessnerfl.rabbitmq.deadletter.manager.service.rabbitmq.operations.MessageFetchFailedException;
import de.gessnerfl.rabbitmq.deadletter.manager.service.rabbitmq.operations.QueueListOperation;
import de.gessnerfl.rabbitmq.deadletter.manager.service.rabbitmq.utils.MessageChecksum;

@RunWith(MockitoJUnitRunner.class)
public class QueueListOperationTest {
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
  public void init(){
    when(closeableChannelWrapper.getChannel()).thenReturn(channel);
    when(connector.connectAsClosable()).thenReturn(closeableChannelWrapper);
    when(DEFAULT_ENVELOPE.getDeliveryTag()).thenReturn(DEFAULT_DELIVERY_TAG);
    when(messageChecksum.createFor(DEFAULT_BASIC_PROPERTIES, DEFAULT_PAYLOAD)).thenReturn(DEFAULT_CHECKSUM);
  }
  
  @Test
  public void shouldReturnMessageFromQueueWhenMaxNumberOfRequestedMessagesIsEqualToTheNumberOfAvailableMessages() throws Exception {
     GetResponse getResponse1 = mockDefaultGetResponse(2);
     GetResponse getResponse2 = mockDefaultGetResponse(1);
     GetResponse getResponse3 = mockDefaultGetResponse(0);
     when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenReturn(getResponse1, getResponse2, getResponse3);
     
     List<Message> messages = sut.getMessagesFromQueue(DEFAULT_QUEUE_NAME, DEFAULT_MAX_NO_OF_MESSAGES);
     
     assertThat(messages, hasSize(3));
     assertDefaultMessage(messages.get(0));
     assertDefaultMessage(messages.get(1));
     assertDefaultMessage(messages.get(2));
    
     verify(channel, times(3)).basicGet(DEFAULT_QUEUE_NAME, false);
  }

  @Test
  public void shouldReturnMessageFromQueueWhenMaxNumberOfRequestedMessagesIsLessThanTheNumberOfAvailableMessages() throws Exception {
    GetResponse getResponse1 = mockDefaultGetResponse(3);
    GetResponse getResponse2 = mockDefaultGetResponse(2);
    GetResponse getResponse3 = mockDefaultGetResponse(1);
    GetResponse getResponse4 = mockDefaultGetResponse(0);
    when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenReturn(getResponse1, getResponse2, getResponse3, getResponse4);
    
    List<Message> messages = sut.getMessagesFromQueue(DEFAULT_QUEUE_NAME, DEFAULT_MAX_NO_OF_MESSAGES);
    
    assertThat(messages, hasSize(3));
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
    
    List<Message> messages = sut.getMessagesFromQueue(DEFAULT_QUEUE_NAME, DEFAULT_MAX_NO_OF_MESSAGES);
    
    assertThat(messages, hasSize(2));
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
    
    sut.getMessagesFromQueue(DEFAULT_QUEUE_NAME, DEFAULT_MAX_NO_OF_MESSAGES);
   
    verify(channel).basicNack(DEFAULT_DELIVERY_TAG, true, true);
  }
  
  @Test
  public void shouldThrowExcpetionWhenConnectionCannotBeEstablished() throws Exception {
    ConnectionFailedException expectedException = new ConnectionFailedException(null);
    when(connector.connectAsClosable()).thenThrow(expectedException);
    
    try{
      sut.getMessagesFromQueue(DEFAULT_QUEUE_NAME, DEFAULT_MAX_NO_OF_MESSAGES);
    }catch(ConnectionFailedException e){
      assertSame(expectedException, e);
    }
  }
  
  @Test
  public void shouldThrowExceptionWhenDataCannotBeFetched() throws Exception {
    IOException expectedException = new IOException();
    when(channel.basicGet(DEFAULT_QUEUE_NAME, false)).thenThrow(expectedException);
    
    try{
      sut.getMessagesFromQueue(DEFAULT_QUEUE_NAME, DEFAULT_MAX_NO_OF_MESSAGES);
    }catch(MessageFetchFailedException e){
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
    
    try{
      sut.getMessagesFromQueue(DEFAULT_QUEUE_NAME, DEFAULT_MAX_NO_OF_MESSAGES);
    }catch(MessageFetchFailedException e){
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
    when(response.getProps()).thenReturn(DEFAULT_BASIC_PROPERTIES);
    when(response.getBody()).thenReturn(DEFAULT_PAYLOAD);
    when(response.getMessageCount()).thenReturn(remainingMessageCount);
    return response;
  }

  private void assertDefaultMessage(Message message) {
    assertEquals(DEFAULT_ENVELOPE, message.getEnvelope());
    assertEquals(DEFAULT_BASIC_PROPERTIES, message.getProperties());
    assertEquals(DEFAULT_PAYLOAD, message.getBody());
  }
  
}
