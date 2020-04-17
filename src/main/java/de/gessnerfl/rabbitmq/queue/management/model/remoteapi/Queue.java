package de.gessnerfl.rabbitmq.queue.management.model.remoteapi;

import java.util.Map;

import com.google.gson.annotations.SerializedName;

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

  public int getConsumers() {
    return consumers;
  }

  public void setConsumers(int consumers) {
    this.consumers = consumers;
  }

  public int getMessages() {
    return messages;
  }

  public void setMessages(int messages) {
    this.messages = messages;
  }

  public int getMessagesReady() {
    return messagesReady;
  }

  public void setMessagesReady(int messagesReady) {
    this.messagesReady = messagesReady;
  }

  public int getMessagesUnacknowledged() {
    return messagesUnacknowledged;
  }

  public void setMessagesUnacknowledged(int messagesUnacknowledged) {
    this.messagesUnacknowledged = messagesUnacknowledged;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public boolean isDeadLetterExchangeConfigured(){
    return arguments != null && arguments.containsKey(DEAD_LETTER_EXCHANGE_ARGUMENT);
  }

  public boolean isDeadLetterRoutingKeyConfigured(){
    return arguments != null && arguments.containsKey(DEAD_LETTER_ROUTINGKEY_ARGUMENT);
  }

}
