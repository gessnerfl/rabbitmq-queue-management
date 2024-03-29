package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.MessageProperties;

import de.gessnerfl.rabbitmq.queue.management.service.json.JsonSerializer;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MessageChecksumTest {

  @Mock
  private JsonSerializer jsonSerializer;
  
  @InjectMocks
  private MessageChecksum sut;
  
  @SuppressWarnings("rawtypes")
  @Test
  void shouldCalculateChecksumFromJsonString(){
    final String expectedChecksum = "ca2e3c1843f0125ffa523e372cf16a1919c2e83b52130719c98a19ebf450fb9d";
    final String json = "{ \"foo\": \"bar\" }";
    when(jsonSerializer.toJson(anyMap())).thenReturn(json);
    
    final String bodyString = "foo";
    final String bodyBase64 = "Zm9v";
    final byte[] body = bodyString.getBytes(StandardCharsets.UTF_8);
    final AMQP.BasicProperties props = MessageProperties.BASIC;
    
    String result = sut.createFor(props, body);
   
    assertEquals(expectedChecksum, result);
    
    ArgumentCaptor<Object> argumentCaptor = ArgumentCaptor.forClass(Object.class);
    verify(jsonSerializer).toJson(argumentCaptor.capture());
    Object mapParameter = argumentCaptor.getValue();
    
    assertThat(mapParameter, instanceOf(Map.class));
    assertSame(props, ((Map)mapParameter).get(MessageChecksum.PROPERTIES_KEY));
    assertEquals(bodyBase64, ((Map)mapParameter).get(MessageChecksum.BODY_KEY));
  }
}
