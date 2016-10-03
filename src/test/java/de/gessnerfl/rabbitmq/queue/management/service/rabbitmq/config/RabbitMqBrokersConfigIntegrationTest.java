package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.config;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.gessnerfl.rabbitmq.queue.management.AbstractIntegrationTest;
import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.BrokerConfig;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.config.RabbitMqBrokersConfig;

public class RabbitMqBrokersConfigIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private RabbitMqBrokersConfig rabbitMqBrokersConfig;

    @Test
    public void shouldReadDefaultSettings() {
        Map<String, BrokerConfig> configs = rabbitMqBrokersConfig.getBrokers();
        assertNotNull(configs);
        assertThat(configs.keySet(), hasSize(1));
        assertThat(configs, hasKey("local"));

        BrokerConfig config = configs.get("local");
        assertEquals("localhost", config.getHostname());
        assertEquals(5672, config.getPort());
        assertEquals("/", config.getVhost());
        assertEquals("guest", config.getUsername());
        assertEquals("guest", config.getPassword());
        assertEquals(15672, config.getManagementPort());
        assertFalse(config.isManagemnetPortSecured());
    }

}