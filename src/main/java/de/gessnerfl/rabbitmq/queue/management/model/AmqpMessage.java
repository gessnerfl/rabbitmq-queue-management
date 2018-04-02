package de.gessnerfl.rabbitmq.queue.management.model;

import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP.BasicProperties;

public class AmqpMessage {
  private final Envelope envelope;
  private final BasicProperties properties;
  private final byte[] body;
  private final String checksum;

  public AmqpMessage(Envelope envelope, BasicProperties properties, byte[] body, String checksum) {
    this.envelope = envelope;
    this.properties = properties;
    this.body = body;
    this.checksum = checksum;
  }

  public Envelope getEnvelope() {
    return envelope;
  }

  public BasicProperties getProperties() {
    return properties;
  }

  public byte[] getBody() {
    return body;
  }

  public String getChecksum() {
    return checksum;
  }

}
