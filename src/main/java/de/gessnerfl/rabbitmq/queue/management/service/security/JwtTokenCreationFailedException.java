package de.gessnerfl.rabbitmq.queue.management.service.security;

public class JwtTokenCreationFailedException extends RuntimeException {
    public JwtTokenCreationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
