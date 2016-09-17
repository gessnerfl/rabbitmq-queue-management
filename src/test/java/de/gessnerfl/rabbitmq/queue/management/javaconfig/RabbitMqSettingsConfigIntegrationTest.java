package de.gessnerfl.rabbitmq.queue.management.javaconfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.gessnerfl.rabbitmq.queue.management.AbstractIntegrationTest;
import de.gessnerfl.rabbitmq.queue.management.javaconfig.RabbitMqSettingsConfig;

public class RabbitMqSettingsConfigIntegrationTest extends AbstractIntegrationTest {

  @Autowired
  private RabbitMqSettingsConfig rabbitMqSettingsConfig;
  
  @Test
  public void shouldReadDefaultSettings(){
    assertEquals("localhost", rabbitMqSettingsConfig.getHostname());
    assertEquals(5672, rabbitMqSettingsConfig.getPort());
    assertEquals("/", rabbitMqSettingsConfig.getVhost());
    assertEquals("guest", rabbitMqSettingsConfig.getUsername());
    assertEquals("guest", rabbitMqSettingsConfig.getPassword());
    assertEquals(15672, rabbitMqSettingsConfig.getManagementPort());
    assertFalse(rabbitMqSettingsConfig.isManagemnetPortSecured());
  }
  
}
