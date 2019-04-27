package de.gessnerfl.rabbitmq.queue.management.model.remoteapi;

import java.util.Map;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class Queue {
  public static final String DEAD_LETTER_EXCHANGE_ARGUMENT = "x-dead-letter-exchange";
  public static final String DEAD_LETTER_ROUTINGKEY_ARGUMENT = "x-dead-letter-routing-key";
  
  private String name;
  private String vhost;
  private boolean durable;
  @SerializedName("auto_delete")
  private boolean autoDelete;
  private boolean exclusive;
  private Map<String, Object> arguments;

  public boolean isDeadLetterExchangeConfigured(){
    return arguments != null && arguments.containsKey(DEAD_LETTER_EXCHANGE_ARGUMENT);
  }

  public boolean isDeadLetterRoutingKeyConfigured(){
    return arguments != null && arguments.containsKey(DEAD_LETTER_ROUTINGKEY_ARGUMENT);
  }

}
