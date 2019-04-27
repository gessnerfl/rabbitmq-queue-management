package de.gessnerfl.rabbitmq.queue.management.model.remoteapi;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class Exchange {
  private String name;
  private String vhost;
  private String type;
  private boolean durable;
  @SerializedName("auto_delete")
  private boolean autoDelete;
  private boolean internal;
}
