package de.gessnerfl.rabbitmq.queue.management.service.security;

public class JWTTokenCreationFailedException extends RuntimeException {
    public JWTTokenCreationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
