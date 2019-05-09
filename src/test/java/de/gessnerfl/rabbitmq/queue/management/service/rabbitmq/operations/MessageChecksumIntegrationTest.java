package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.MessageProperties;

import de.gessnerfl.rabbitmq.queue.management.AbstractIntegrationTest;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.utils.MessageChecksum;

public class MessageChecksumIntegrationTest extends AbstractIntegrationTest {

  @Autowired
  private MessageChecksum sut;
  
  @Test
  public void shouldCalculateChecksum(){
    BasicProperties properties = MessageProperties.TEXT_PLAIN;
    byte[] data = "testData".getBytes(StandardCharsets.UTF_8);
    
    String checksum = sut.createFor(properties, data);
    
    assertEquals("ec8459f97f78dd23565e946826c18c6e8fff707f62623c0ec84221eb2362bfdc", checksum);
  }
  
}
