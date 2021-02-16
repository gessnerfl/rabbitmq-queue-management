package de.gessnerfl.rabbitmq.queue.management.service.security;

public class InvalidJwtSigningKeyException extends RuntimeException {
    public InvalidJwtSigningKeyException(){
        super("Signing key must have at least 32 characters (256Bit)");
    }
}
