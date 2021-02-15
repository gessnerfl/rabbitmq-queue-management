package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.remoteapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.config.RabbitMqConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ManagementApiUrlBuilderTest {

    @Mock
    private RabbitMqConfig rabbitMqConfig;

    @InjectMocks
    private ManagementApiUrlBuilder sut;

    @BeforeEach
    public void init() {
        when(rabbitMqConfig.getHostname()).thenReturn("localhost");
        when(rabbitMqConfig.getManagementPort()).thenReturn(15672);
    }

    @Test
    public void shouldReturnHttpUrlWhenManagmentApiIsNotSecured() {
        when(rabbitMqConfig.isManagemnetPortSecured()).thenReturn(false);

        assertEquals("http://localhost:15672/api", sut.buildForConfiguration());
    }

    @Test
    public void shouldReturnHttpsUrlWhenManagementApiIsSecured() {
        when(rabbitMqConfig.isManagemnetPortSecured()).thenReturn(true);

        assertEquals("https://localhost:15672/api", sut.buildForConfiguration());
    }
}
