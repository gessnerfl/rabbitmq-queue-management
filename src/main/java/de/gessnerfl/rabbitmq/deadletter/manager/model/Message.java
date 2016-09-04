package de.gessnerfl.rabbitmq.deadletter.manager.model;

import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP.BasicProperties;

public class Message {
  private final Envelope envelope;
  private final BasicProperties properties;
  private final byte[] body;
  private final String checksum;

  public Message(Envelope envelope, BasicProperties properties, byte[] body, String checksum) {
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
