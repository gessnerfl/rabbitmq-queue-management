package de.gessnerfl.rabbitmq.queue.management.controller;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Queue;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.RabbitMqFacade;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.ui.Model;

@RunWith(MockitoJUnitRunner.class)
public class IndexControllerTest {

    @Mock
    private RabbitMqFacade rabbitMqFacade;
    
    @InjectMocks
    private IndexController sut;
    
    @Test
    public void shouldAddQueuesToModelAndNavigateToIndexPage(){
        List<Queue> queues = new ArrayList<>();
        when(rabbitMqFacade.getQueues()).thenReturn(queues);
        
        Model model = mock(Model.class);
        
        String result = sut.index(model);
        
        assertSame("index", result);
        verify(model).addAttribute("queues", queues);
    }
}
