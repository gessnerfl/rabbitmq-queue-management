package de.gessnerfl.rabbitmq.queue.management.connection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ConnectorTest {
    private final static String VHOST = "vhost";
    
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
    
    @BeforeEach
    public void init() throws Exception {
        when(connectionFactories.getOrCreate(VHOST)).thenReturn(connectionFactory);
        when(connectionFactory.newConnection()).thenReturn(connection);
    }
    
    @Test
    public void shouldCreateNewConnectionIfNoConnectionWasInitializedYet() throws Exception {
        when(connection.createChannel()).thenReturn(channel);

        Channel channel = sut.connect(VHOST);
        
        assertSame(this.channel, channel);
        verify(connectionFactories).getOrCreate(VHOST);
        verify(connectionFactory).newConnection();
        verify(connection).createChannel();
    }
    
    @Test
    public void shouldReuseExistingConnectionIfConnectionIsNotClosed() throws Exception {
        when(connection.createChannel()).thenReturn(channel);
        when(connection.isOpen()).thenReturn(true);
        
        sut.connect(VHOST);
        sut.connect(VHOST);
        
        verify(connectionFactories).getOrCreate(VHOST);
        verify(connectionFactory).newConnection();
        verify(connection, times(2)).createChannel();
    }
    
    @Test
    public void shouldRecreateConnectionIfConnectionIsClosed() throws Exception {
        when(connection.createChannel()).thenReturn(channel);
        when(connection.isOpen()).thenReturn(false);
        
        sut.connect(VHOST);
        sut.connect(VHOST);
        
        verify(connectionFactories, times(2)).getOrCreate(VHOST);
        verify(connectionFactory, times(2)).newConnection();
        verify(connection, times(2)).createChannel();
    }
    
    @Test
    public void shouldRecreateConnectionIfConnectionIsNull() throws Exception {
        when(connection.createChannel()).thenReturn(channel);
        sut.connections.put(VHOST, null);
        
        Channel channel = sut.connect(VHOST);
        
        assertSame(this.channel, channel);
        verify(connectionFactories).getOrCreate(VHOST);
        verify(connectionFactory).newConnection();
        verify(connection).createChannel();
    }
    
    @Test
    public void shouldThrowExceptionWhenConnectionCannotBeEstablished() throws Exception {
        assertThrows(ConnectionFailedException.class, () -> {
            when(connectionFactory.newConnection()).thenThrow(new IOException("foo"));

            sut.connect(VHOST);
        });
    }
    
    @Test
    public void shouldThrowExceptionWhenChannelCannotBeOpened() throws Exception {
        assertThrows(ConnectionFailedException.class, () -> {
            when(connection.createChannel()).thenThrow(new IOException("foo"));

            sut.connect(VHOST);
        });
    }

}
