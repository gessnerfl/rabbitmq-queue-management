package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class StateKeepingReturnListenerFactoryTest {

    private StateKeepingReturnListenerFactory sut;

    @Before
    public void init(){
        sut = new StateKeepingReturnListenerFactory();
    }

    @Test
    public void shouldCreateNewInsanceOfStateKeepingReturnListener(){
        OperationId operationId = mock(OperationId.class);
        Logger logger = mock(Logger.class);

        StateKeepingReturnListener listener = sut.createFor(operationId, logger);

        assertNotNull(listener);
    }

}