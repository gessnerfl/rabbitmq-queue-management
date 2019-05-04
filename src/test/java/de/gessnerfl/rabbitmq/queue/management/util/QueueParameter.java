package de.gessnerfl.rabbitmq.queue.management.util;

import java.util.Optional;

public class QueueParameter {
    public final String exchangeName;
    public final String routingKey;
    public final String queueName;

    public final Optional<Integer> ttl;
    public final Optional<DeadLetterParameter> deadLetterParameter;
    
    public QueueParameter(String exchangeName, String routingKey, String queueName, Optional<DeadLetterParameter> deadLetterParameter, Optional<Integer> ttl) {
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
        this.queueName = queueName;
        this.deadLetterParameter = deadLetterParameter;
        this.ttl = ttl;
    }
    
}
