package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

import org.junit.Before;
import org.junit.Test;

import static de.gessnerfl.rabbitmq.queue.management.hamcrest.CustomMatchers.matchesOperationId;
import static org.junit.Assert.*;

public class OperationIdGeneratorTest {

    private OperationIdGenerator sut;

    @Before
    public void init(){
        sut = new OperationIdGenerator();
    }

    @Test
    public void shouldGenerateNewImmutableOperationIds(){
        OperationId operationId1 = sut.generate();
        OperationId operationId2 = sut.generate();

        assertNotNull(operationId1);
        assertNotNull(operationId2);
        assertNotEquals(operationId1, operationId2);
        assertThat(operationId1.getValue(), matchesOperationId());
        assertThat(operationId2.getValue(), matchesOperationId());
    }

}