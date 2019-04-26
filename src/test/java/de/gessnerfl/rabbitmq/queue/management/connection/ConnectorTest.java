package de.gessnerfl.rabbitmq.queue.management.connection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

@RunWith(MockitoJUnitRunner.class)
public class ConnectorTest {
    private final static String BROKER_NAME = "broker";
    
    @Mock
    private ConnectionFactories connectionFactories;
    @Mock
    private ConnectionFactory connectionFactory;
    @Mock
    private Connection connection;
    @Mock
    private Channel channel;
    
    @InjectMocks
    private Connector sut;
    
    @Before
    public void init() throws Exception {
        when(connectionFactories.getOrCreate(BROKER_NAME)).thenReturn(connectionFactory);
        when(connectionFactory.newConnection()).thenReturn(connection);
        when(connection.createChannel()).thenReturn(channel);
    }
    
    @Test
    public void shouldCreateNewConnectionIfNoConnectionWasInitializedYet() throws Exception {
        Channel channel = sut.connect(BROKER_NAME);
        
        assertSame(this.channel, channel);
        verify(connectionFactories).getOrCreate(BROKER_NAME);
        verify(connectionFactory).newConnection();
        verify(connection).createChannel();
    }
    
    @Test
    public void shouldReuseExistingConnectionIfConnectionIsNotClosed() throws Exception {
        when(connection.isOpen()).thenReturn(true);
        
        sut.connect(BROKER_NAME);
        sut.connect(BROKER_NAME);
        
        assertSame(this.channel, channel);
        verify(connectionFactories).getOrCreate(BROKER_NAME);
        verify(connectionFactory).newConnection();
        verify(connection, times(2)).createChannel();
    }
    
    @Test
    public void shouldRecreateConnectionIfConnectionIsClosed() throws Exception {
        when(connection.isOpen()).thenReturn(false);
        
        sut.connect(BROKER_NAME);
        sut.connect(BROKER_NAME);
        
        assertSame(this.channel, channel);
        verify(connectionFactories, times(2)).getOrCreate(BROKER_NAME);
        verify(connectionFactory, times(2)).newConnection();
        verify(connection, times(2)).createChannel();
    }
    
    @Test
    public void shouldRecreateConnectionIfConnctionIsNull() throws Exception {
        sut.connections.put(BROKER_NAME, null);
        
        Channel channel = sut.connect(BROKER_NAME);
        
        assertSame(this.channel, channel);
        verify(connectionFactories).getOrCreate(BROKER_NAME);
        verify(connectionFactory).newConnection();
        verify(connection).createChannel();
    }
    
    @Test(expected=ConnectionFailedException.class)
    public void shouldThrowExceptionWhenConnectionCannotBeEstablished() throws Exception {
        when(connectionFactory.newConnection()).thenThrow(new IOException("foo"));
        
        sut.connect(BROKER_NAME);
    }
    
    @Test(expected=ConnectionFailedException.class)
    public void shouldThrowExceptionWhenChannelCannotBeOpened() throws Exception {
        when(connection.createChannel()).thenThrow(new IOException("foo"));
        
        sut.connect(BROKER_NAME);
    }

}
