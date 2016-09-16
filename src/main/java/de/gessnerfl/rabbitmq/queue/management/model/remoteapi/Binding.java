package de.gessnerfl.rabbitmq.queue.management.model.remoteapi;

import com.google.gson.annotations.SerializedName;

public class Binding {
    private String source;
    private String vhost;
    private String destination;
    @SerializedName("destination_type")
    private String destinationType;
    @SerializedName("routing_key")
    private String routingKey;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getVhost() {
        return vhost;
    }

    public void setVhost(String vhost) {
        this.vhost = vhost;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDestinationType() {
        return destinationType;
    }

    public void setDestinationType(String destinationType) {
        this.destinationType = destinationType;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

}
