package de.gessnerfl.rabbitmq.queue.management.service.security;

import java.text.ParseException;

public class InvalidJwtTokenException extends RuntimeException {
    public InvalidJwtTokenException(String message, ParseException e) {
        super(message);
    }
}
