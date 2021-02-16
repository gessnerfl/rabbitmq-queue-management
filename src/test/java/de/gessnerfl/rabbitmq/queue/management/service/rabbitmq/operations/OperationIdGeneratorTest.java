package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static de.gessnerfl.rabbitmq.queue.management.hamcrest.CustomMatchers.matchesOperationId;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class OperationIdGeneratorTest {

    private OperationIdGenerator sut;

    @BeforeEach
    void init(){
        sut = new OperationIdGenerator();
    }

    @Test
    void shouldGenerateNewImmutableOperationIds(){
        OperationId operationId1 = sut.generate();
        OperationId operationId2 = sut.generate();

        assertNotNull(operationId1);
        assertNotNull(operationId2);
        assertNotEquals(operationId1, operationId2);
        assertThat(operationId1.getValue(), matchesOperationId());
        assertThat(operationId2.getValue(), matchesOperationId());
    }

}