package de.gessnerfl.rabbitmq.queue.management.controller;

import de.gessnerfl.rabbitmq.queue.management.model.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

public class GlobalControllerExceptionHandlerTest {

    private GlobalControllerExceptionHandler sut;

    @BeforeEach
    public void init(){
        sut = new GlobalControllerExceptionHandler();
    }

    @Test
    public void shouldMapExceptionToErrorMessage(){
        String message = "error message";
        Exception e = new Exception(message);

        ResponseEntity<ErrorResponse> result = sut.handleErrors(e);

        assertNotNull(result);
        assertEquals(message, result.getBody().getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    }

}