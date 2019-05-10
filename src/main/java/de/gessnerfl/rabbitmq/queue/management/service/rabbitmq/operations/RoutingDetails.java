package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

public class RoutingDetails {
    private final String exchange;
    private final String routingKey;
    private final String countHeaderName;

    public RoutingDetails(String exchange, String routingKey, String countHeaderName) {
        this.exchange = exchange;
        this.routingKey = routingKey;
        this.countHeaderName = countHeaderName;
    }

    public String getExchange() {
        return exchange;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public String getCountHeaderName() {
        return countHeaderName;
    }
}
