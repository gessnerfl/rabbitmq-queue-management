package de.gessnerfl.rabbitmq.queue.management.service.security;

public class InvalidJWTSigningKeyException extends RuntimeException {
    public InvalidJWTSigningKeyException(){
        super("Signing key must have at least 32 characters (256Bit)");
    }
}
