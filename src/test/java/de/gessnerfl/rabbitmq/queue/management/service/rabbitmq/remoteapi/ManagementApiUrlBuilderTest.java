package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.remoteapi;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.BrokerConfig;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.config.InvalidBrokerNameException;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.config.RabbitMqBrokers;

@RunWith(MockitoJUnitRunner.class)
public class ManagementApiUrlBuilderTest {
    private final static String DEFAULT_BROKER_NAME = "defaultBroker";
    private final static String INVALID_BROKER_NAME = "invalidBroker";

    @Mock
    private RabbitMqBrokers rabbitMqBrokers;
    @Mock
    private BrokerConfig brokerConfig;

    @InjectMocks
    private ManagementApiUrlBuilder sut;

    @Before
    public void init() {
        when(rabbitMqBrokers.getBrokerConfig(DEFAULT_BROKER_NAME)).thenReturn(brokerConfig);
        when(rabbitMqBrokers.getBrokerConfig(INVALID_BROKER_NAME)).thenThrow(new InvalidBrokerNameException(INVALID_BROKER_NAME));
        when(brokerConfig.getHostname()).thenReturn("localhost");
        when(brokerConfig.getManagementPort()).thenReturn(15672);
    }

    @Test
    public void shouldReturnHttpUrlWhenManagmentApiIsNotSecured() {
        when(brokerConfig.isManagemnetPortSecured()).thenReturn(false);

        assertEquals("http://localhost:15672/api", sut.buildForConfiguration(DEFAULT_BROKER_NAME));
    }

    @Test
    public void shouldReturnHttpsUrlWhenManagementApiIsSecured() {
        when(brokerConfig.isManagemnetPortSecured()).thenReturn(true);

        assertEquals("https://localhost:15672/api", sut.buildForConfiguration(DEFAULT_BROKER_NAME));
    }

    @Test(expected=InvalidBrokerNameException.class)
    public void shouldThrowExceptionWhenBrokerNameIsNotValid(){
        sut.buildForConfiguration(INVALID_BROKER_NAME);
    }
}
