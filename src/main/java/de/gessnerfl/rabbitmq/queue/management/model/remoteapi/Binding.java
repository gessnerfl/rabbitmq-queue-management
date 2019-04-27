package de.gessnerfl.rabbitmq.queue.management.model.remoteapi;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class Binding {
    private String source;
    private String vhost;
    private String destination;
    @SerializedName("destination_type")
    private String destinationType;
    @SerializedName("routing_key")
    private String routingKey;
}
