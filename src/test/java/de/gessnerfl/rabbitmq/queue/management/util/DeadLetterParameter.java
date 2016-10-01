package de.gessnerfl.rabbitmq.queue.management.util;

import java.util.Optional;

public class DeadLetterParameter {
    public final String exchangeName;
    public final Optional<String> routingKey;

    public DeadLetterParameter(String exchangeName, Optional<String> routingKey) {
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
    }

}
