package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.config;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.BrokerConfig;
import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.BrokerDescriptor;

@RunWith(MockitoJUnitRunner.class)
public class RabbitMqBrokersTest {

    @Mock
    private RabbitMqBrokersConfig config;
    
    @InjectMocks
    private RabbitMqBrokers sut;
    
    @Test
    public void shouldReturnDescriptorList(){
        String name = "local";
        
        BrokerConfig brokerConfig = mock(BrokerConfig.class);
        when(brokerConfig.getHostname()).thenReturn("host");
        when(brokerConfig.getPort()).thenReturn(1234);
        when(brokerConfig.getVhost()).thenReturn("vhost");
        
        Map<String,BrokerConfig> brokerConfigs = new HashMap<>();
        brokerConfigs.put(name, brokerConfig);
        
        when(config.getBrokers()).thenReturn(brokerConfigs);
        
        List<BrokerDescriptor> result = sut.getBrokerDescriptors();
        
        assertThat(result, hasSize(1));
        BrokerDescriptor descriptor = result.get(0);
        assertEquals(name, descriptor.getName());
        assertEquals("host:1234 (vhost=vhost)", descriptor.getDisplayName());
    }
    
    @Test
    public void shouldReturnEmptyDescriptorListIfBrokersMapIsNull(){
        when(config.getBrokers()).thenReturn(null);
        
        assertThat(sut.getBrokerDescriptors(), empty());
    }
    
    @Test
    public void shouldReturnEmptyDescriptorListIfBrokersMapIsEmpty(){
        Map<String,BrokerConfig> brokerConfigs = new HashMap<>();
        when(config.getBrokers()).thenReturn(brokerConfigs);
        
        assertThat(sut.getBrokerDescriptors(), empty());
    }
    
    @Test
    public void shouldReturnBrokerForTheGivenBrokerName(){
        String name = "local";
        
        BrokerConfig brokerConfig = mock(BrokerConfig.class);
        Map<String,BrokerConfig> brokerConfigs = new HashMap<>();
        brokerConfigs.put(name, brokerConfig);
        
        when(config.getBrokers()).thenReturn(brokerConfigs);
        
        BrokerConfig result = sut.getBrokerConfig(name);
        
        assertSame(brokerConfig, result);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void shouldThrowExceptionIfBrokerNameIsNull(){
        sut.getBrokerConfig(null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void shouldThrowExceptionIfBrokerNameIsBlank(){
        sut.getBrokerConfig(" ");
    }
    
    @Test(expected=InvalidBrokerNameException.class)
    public void shouldThrowExceptionWhenNoBrokerIsAvailableForTheGivenName(){
        BrokerConfig brokerConfig = mock(BrokerConfig.class);
        Map<String,BrokerConfig> brokerConfigs = new HashMap<>();
        brokerConfigs.put("local", brokerConfig);
        when(config.getBrokers()).thenReturn(brokerConfigs);
        
        sut.getBrokerConfig("invalidBrokerName");
    }
    
    @Test(expected=NoBrokersAvailableException.class)
    public void shouldThrowExceptionWhenNoBrokersMapIsNull(){
        when(config.getBrokers()).thenReturn(null);
        
        sut.getBrokerConfig("foo");
    }
    
    @Test(expected=NoBrokersAvailableException.class)
    public void shouldThrowExceptionWhenNoBrokersMapIsEmpty(){
        Map<String,BrokerConfig> brokerConfigs = new HashMap<>();
        when(config.getBrokers()).thenReturn(brokerConfigs);
        
        sut.getBrokerConfig("foo");
    }
}
