package de.gessnerfl.rabbitmq.queue.management.controller.rest;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Binding;
import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Exchange;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.RabbitMqFacade;

@RunWith(MockitoJUnitRunner.class)
public class ExchangeControllerTest {
    
    private final static String DEFAULT_VHOST = "vhost";

    @Mock
    private RabbitMqFacade rabbitMqFacade;
    
    @InjectMocks
    private ExchangeController sut;
    
    @Test
    public void shouldDelegateCallToRetriveAllExchanges(){
        Exchange exchange = mock(Exchange.class);
        List<Exchange> exchanges = Arrays.asList(exchange);
        
        when(rabbitMqFacade.getExchanges(DEFAULT_VHOST)).thenReturn(exchanges);
        
        List<Exchange> result = sut.getExchanges(DEFAULT_VHOST);
        
        assertSame(exchanges, result);
        verify(rabbitMqFacade).getExchanges(DEFAULT_VHOST);
    }
    
    @Test
    public void shouldReturnListOfDistinctRoutingKeys(){
        String exchangeName = "exchange";
        List<Binding> bindings = Arrays.asList(mockBinding("a"), mockBinding("b"), mockBinding("a"));
        when(rabbitMqFacade.getExchangeSourceBindings(DEFAULT_VHOST, exchangeName)).thenReturn(bindings);
        
        List<String> result = sut.getSourceBindings(DEFAULT_VHOST, exchangeName);
        
        assertThat(result, containsInAnyOrder("a", "b"));
        verify(rabbitMqFacade).getExchangeSourceBindings(DEFAULT_VHOST, exchangeName);
    }
    
    private Binding mockBinding(String routingKey){
        Binding binding = mock(Binding.class);
        when(binding.getRoutingKey()).thenReturn(routingKey);
        return binding;
    }
}
