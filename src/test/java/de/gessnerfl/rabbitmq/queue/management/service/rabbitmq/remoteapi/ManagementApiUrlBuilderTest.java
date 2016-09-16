package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.remoteapi;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import de.gessnerfl.rabbitmq.queue.management.javaconfig.RabbitMqSettingsConfig;

@RunWith(MockitoJUnitRunner.class)
public class ManagementApiUrlBuilderTest {

  @Mock
  private RabbitMqSettingsConfig rabbitMqSettingsConfig;

  @InjectMocks
  private ManagementApiUrlBuilder sut;

  @Before
  public void init() {
    when(rabbitMqSettingsConfig.getHostname()).thenReturn("localhost");
    when(rabbitMqSettingsConfig.getManagementPort()).thenReturn(15672);
  }

  @Test
  public void shouldReturnHttpUrlWhenManagmentApiIsNotSecured() {
    when(rabbitMqSettingsConfig.isManagemnetPortSecured()).thenReturn(false);

    assertEquals("http://localhost:15672/api", sut.buildForConfiguration());
  }

  @Test
  public void shouldReturnHttpsUrlWhenManagementApiIsSecured() {
    when(rabbitMqSettingsConfig.isManagemnetPortSecured()).thenReturn(true);

    assertEquals("https://localhost:15672/api", sut.buildForConfiguration());
  }

}
