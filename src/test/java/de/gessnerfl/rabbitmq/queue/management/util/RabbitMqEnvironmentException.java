package de.gessnerfl.rabbitmq.queue.management.util;

public class RabbitMqEnvironmentException extends RuntimeException {

    private static final long serialVersionUID = 3038651570841180360L;

    public RabbitMqEnvironmentException(String message, Throwable cause) {
        super(message, cause);
    }

    public RabbitMqEnvironmentException(String message) {
        super(message);
    }

    public RabbitMqEnvironmentException(Throwable cause) {
        super(cause);
    }
    
}
