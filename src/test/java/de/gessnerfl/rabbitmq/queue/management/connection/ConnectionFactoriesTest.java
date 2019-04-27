package de.gessnerfl.rabbitmq.queue.management.connection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.config.RabbitMqConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.rabbitmq.client.ConnectionFactory;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionFactoriesTest {
    
    private final static String VHOST = "vhost";
    private final static String HOSTNAME = "host";
    private final static int PORT = 5672;
    private final static String USERNAME = "username";
    private final static String PASSWORD = "password";

    @Mock
    private RabbitMqConfig rabbitMqConfig;
    
    @InjectMocks
    private ConnectionFactories sut;
    
    @Before
    public void init(){
        when(rabbitMqConfig.getHostname()).thenReturn(HOSTNAME);
        when(rabbitMqConfig.getPort()).thenReturn(PORT);
        when(rabbitMqConfig.getUsername()).thenReturn(USERNAME);
        when(rabbitMqConfig.getPassword()).thenReturn(PASSWORD);
    }

    @Test
    public void shouldReturnNewConnectionFactoryIfNotInitialized(){
        ConnectionFactory connectionFactory = sut.getOrCreate(VHOST);
        
        assertNotNull(connectionFactory);
        assertEquals(HOSTNAME, connectionFactory.getHost());
        assertEquals(PORT, connectionFactory.getPort());
        assertEquals(VHOST, connectionFactory.getVirtualHost());
        assertEquals(USERNAME, connectionFactory.getUsername());
        assertEquals(PASSWORD, connectionFactory.getPassword());
    }
    
    @Test
    public void shouldCreateConnectionFactoryOnlyOnce(){
        sut.getOrCreate(VHOST);

        ConnectionFactory connectionFactory = sut.getOrCreate(VHOST);
        assertNotNull(connectionFactory);
        assertEquals(HOSTNAME, connectionFactory.getHost());
        assertEquals(PORT, connectionFactory.getPort());
        assertEquals(VHOST, connectionFactory.getVirtualHost());
        assertEquals(USERNAME, connectionFactory.getUsername());
        assertEquals(PASSWORD, connectionFactory.getPassword());
    }
    
}
