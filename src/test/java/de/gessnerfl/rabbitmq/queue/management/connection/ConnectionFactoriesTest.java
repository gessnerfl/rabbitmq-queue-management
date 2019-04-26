package de.gessnerfl.rabbitmq.queue.management.connection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.rabbitmq.client.ConnectionFactory;

import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.BrokerConfig;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.config.InvalidBrokerNameException;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.config.RabbitMqBrokers;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionFactoriesTest {
    
    private final static String BROKERNAME = "broker";
    private final static String INVALID_BROKERNAME = "invalid.broker";
    private final static String HOSTNAME = "host";
    private final static int PORT = 5672;
    private final static String VIRTUAL_HOST = "virtualHost";
    private final static String USERNAME = "username";
    private final static String PASSWORD = "password";
    
    @Mock
    private RabbitMqBrokers brokers;
    @Mock
    private BrokerConfig brokerConfig;
    
    @InjectMocks
    private ConnectionFactories sut;
    
    @Before
    public void init(){
        when(brokers.getBrokerConfig(BROKERNAME)).thenReturn(brokerConfig);
        when(brokers.getBrokerConfig(INVALID_BROKERNAME)).thenThrow(new InvalidBrokerNameException(INVALID_BROKERNAME));
        
        when(brokerConfig.getHostname()).thenReturn(HOSTNAME);
        when(brokerConfig.getPort()).thenReturn(PORT);
        when(brokerConfig.getVhost()).thenReturn(VIRTUAL_HOST);
        when(brokerConfig.getUsername()).thenReturn(USERNAME);
        when(brokerConfig.getPassword()).thenReturn(PASSWORD);
    }

    @Test
    public void shouldReturnNewConnectionFactoryIfNotInitialized(){
        ConnectionFactory connectionFactory = sut.getOrCreate(BROKERNAME);
        
        assertNotNull(connectionFactory);
        assertEquals(HOSTNAME, connectionFactory.getHost());
        assertEquals(PORT, connectionFactory.getPort());
        assertEquals(VIRTUAL_HOST, connectionFactory.getVirtualHost());
        assertEquals(USERNAME, connectionFactory.getUsername());
        assertEquals(PASSWORD, connectionFactory.getPassword());
        
        verify(brokers).getBrokerConfig(BROKERNAME);
    }
    
    @Test
    public void shouldCreateConnectionFactoryOnlyOnce(){
        sut.getOrCreate(BROKERNAME);
        verify(brokers).getBrokerConfig(BROKERNAME);

        ConnectionFactory connectionFactory = sut.getOrCreate(BROKERNAME);
        assertNotNull(connectionFactory);
        assertEquals(HOSTNAME, connectionFactory.getHost());
        assertEquals(PORT, connectionFactory.getPort());
        assertEquals(VIRTUAL_HOST, connectionFactory.getVirtualHost());
        assertEquals(USERNAME, connectionFactory.getUsername());
        assertEquals(PASSWORD, connectionFactory.getPassword());
        
        verifyNoMoreInteractions(brokers);
    }
    
    @Test(expected=InvalidBrokerNameException.class)
    public void shouldThrowExceptionIfBrokerNameIsNotValid(){
        sut.getOrCreate(INVALID_BROKERNAME);
    }
    
}
