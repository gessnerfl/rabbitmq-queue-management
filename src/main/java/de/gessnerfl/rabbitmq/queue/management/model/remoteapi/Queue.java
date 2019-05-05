package de.gessnerfl.rabbitmq.queue.management.model.remoteapi;

import java.util.Map;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class Queue {
  public static final String DEAD_LETTER_EXCHANGE_ARGUMENT = "x-dead-letter-exchange";
  public static final String DEAD_LETTER_ROUTINGKEY_ARGUMENT = "x-dead-letter-routing-key";
  public static final String TTL_ARGUMENT = "x-message-ttl";

  private String name;
  private String vhost;
  private boolean durable;
  @SerializedName("auto_delete")
  private boolean autoDelete;
  private boolean exclusive;
  private Map<String, Object> arguments;

  private int consumers;

  private int messages;
  @SerializedName("messages_ready")
  private int messagesReady;
  @SerializedName("messages_unacknowledged")
  private int messagesUnacknowledged;

  private String state;

  public boolean isDeadLetterExchangeConfigured(){
    return arguments != null && arguments.containsKey(DEAD_LETTER_EXCHANGE_ARGUMENT);
  }

  public boolean isDeadLetterRoutingKeyConfigured(){
    return arguments != null && arguments.containsKey(DEAD_LETTER_ROUTINGKEY_ARGUMENT);
  }

}
