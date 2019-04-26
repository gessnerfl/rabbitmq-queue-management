package de.gessnerfl.rabbitmq.queue.management.controller;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.ui.Model;

import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.BrokerDescriptor;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.config.RabbitMqBrokers;

@RunWith(MockitoJUnitRunner.class)
public class IndexControllerTest {

    @Mock
    private RabbitMqBrokers brokers;
    
    @InjectMocks
    private IndexController sut;
    
    @Test
    public void shouldAddBrokersToModelAndNavigateToIndexPage(){
        List<BrokerDescriptor> descriptors = new ArrayList<>();
        when(brokers.getBrokerDescriptors()).thenReturn(descriptors);
        
        Model model = mock(Model.class);
        
        String result = sut.index(model);
        
        assertSame("index", result);
        verify(model).addAttribute("brokers", descriptors);
    }
}
