package de.gessnerfl.rabbitmq.queue.management.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.rabbitmq.client.Envelope;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class Message {
  private final Envelope envelope;
  private final BasicProperties properties;
  private final byte[] body;
  private final String checksum;
  private RequeueDetails requeueDetails;

  public Message(Envelope envelope, BasicProperties properties, byte[] body, String checksum) {
    this.envelope = envelope;
    this.properties = properties;
    this.body = body;
    this.checksum = checksum;

    if(properties.getHeaders() != null && properties.getHeaders().get("x-death") != null){
      List xDeathList = (List) (properties.getHeaders().get("x-death"));
      if(!xDeathList.isEmpty()) {
        Map xdeath = (Map) xDeathList.get(0);
        String exchange = (String) xdeath.get("exchange");
        String routingKey = (String) (xdeath.get("routing-keys") != null && ((List) xdeath.get("routing-keys")).size() > 0 ? ((List) xdeath.get("routing-keys")).get(0) : null);
        if (StringUtils.hasText(exchange) && StringUtils.hasText(routingKey)) {
          requeueDetails = new RequeueDetails(exchange, routingKey);
        }
      }
    }
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

  public RequeueDetails getRequeueDetails() {
    return requeueDetails;
  }

  public boolean isRequeueAllowed() {
    return requeueDetails != null;
  }

  public boolean isRequeued(){
    return getRequeueCount() > 0;
  }

  public int getRequeueCount() {
    return properties.getHeaders() != null ? (int)properties.getHeaders().getOrDefault("x-rmqmgmt-requeue-count", 0) : 0;
  }

  public boolean isMoved(){
    return getMovedCount() > 0;
  }

  public int getMovedCount() {
    return properties.getHeaders() != null ? (int)properties.getHeaders().getOrDefault("x-rmqmgmt-move-count", 0) : 0;
  }

  public String formatEnvelope() {
    return envelope != null ? new Gson().toJson(envelope) : null;
  }

  public String formatProperties() {
    return properties != null ? new Gson().toJson(properties) : null;
  }

  public String formatBody(){
    String text = new String(body, StandardCharsets.UTF_8);
    try {
      JsonObject object = new Gson().fromJson(text, JsonObject.class);
      return new GsonBuilder().setPrettyPrinting().create().toJson(object);
    } catch (JsonSyntaxException e) {
      return text;
    }
  }

  public static class RequeueDetails {
    private final String exchangeName;
    private final String routingKey;

    public RequeueDetails(String exchangeName, String routingKey) {
      this.exchangeName = exchangeName;
      this.routingKey = routingKey;
    }

    public String getExchangeName() {
      return exchangeName;
    }

    public String getRoutingKey() {
      return routingKey;
    }
  }
}
