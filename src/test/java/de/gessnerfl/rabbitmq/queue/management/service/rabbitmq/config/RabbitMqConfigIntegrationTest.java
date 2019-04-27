package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.gessnerfl.rabbitmq.queue.management.AbstractIntegrationTest;

public class RabbitMqConfigIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private RabbitMqConfig rabbitMqConfig;

    @Test
    public void shouldReadDefaultSettings() {
        assertEquals("localhost", rabbitMqConfig.getHostname());
        assertEquals(5672, rabbitMqConfig.getPort());
        assertEquals("guest", rabbitMqConfig.getUsername());
        assertEquals("guest", rabbitMqConfig.getPassword());
        assertEquals(15672, rabbitMqConfig.getManagementPort());
        assertFalse(rabbitMqConfig.isManagemnetPortSecured());
    }

}