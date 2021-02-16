package de.gessnerfl.rabbitmq.queue.management.service.security;

import java.text.ParseException;

public class InvalidJWTTokenException extends RuntimeException {
    public InvalidJWTTokenException(String message, ParseException e) {
        super(message, e);
    }
}
