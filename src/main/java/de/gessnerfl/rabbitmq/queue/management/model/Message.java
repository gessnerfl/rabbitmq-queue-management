package de.gessnerfl.rabbitmq.queue.management.model;

import com.rabbitmq.client.Envelope;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Message {
  private final Envelope envelope;
  private final BasicProperties properties;
  private final byte[] body;
  private final String checksum;
}
