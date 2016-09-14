package de.gessnerfl.rabbitmq.queue.management.remoteapi.model;

import java.util.Map;

import com.google.gson.annotations.SerializedName;

public class Queue {
  public final static String DEAD_LETTER_EXCHANGE_ARGUMENT = "x-dead-letter-exchange";
  public final static String DEAD_LETTER_ROUTINGKEY_ARGUMENT = "x-dead-letter-routing-key";
  
  private String name;
  private String vhost;
  private boolean durable;
  @SerializedName("auto_delete")
  private boolean autoDelete;
  private boolean exclusive;
  private Map<String, Object> arguments;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getVhost() {
    return vhost;
  }

  public void setVhost(String vhost) {
    this.vhost = vhost;
  }

  public boolean isDurable() {
    return durable;
  }

  public void setDurable(boolean durable) {
    this.durable = durable;
  }

  public boolean isAutoDelete() {
    return autoDelete;
  }

  public void setAutoDelete(boolean autoDelete) {
    this.autoDelete = autoDelete;
  }

  public boolean isExclusive() {
    return exclusive;
  }

  public void setExclusive(boolean exclusive) {
    this.exclusive = exclusive;
  }

  public Map<String, Object> getArguments() {
    return arguments;
  }

  public void setArguments(Map<String, Object> arguments) {
    this.arguments = arguments;
  }

  public boolean isDeadLettered(){
    return arguments != null && arguments.containsKey(DEAD_LETTER_EXCHANGE_ARGUMENT);
  }

}
