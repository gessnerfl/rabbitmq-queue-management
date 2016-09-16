package de.gessnerfl.rabbitmq.queue.management.javaconfig;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import de.gessnerfl.rabbitmq.queue.management.javaconfig.ManagementApiConfig;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.remoteapi.ManagementApi;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.remoteapi.ManagementApiUrlBuilder;
import feign.Feign;

@RunWith(MockitoJUnitRunner.class)
public class ManagementApiConfigTest {

  @Mock
  private ManagementApiUrlBuilder managementApiUrlBuilder;
  @Mock
  private Feign.Builder feignBuilder;

  @InjectMocks
  private ManagementApiConfig sut;

  @Test
  public void shouldCreateNewInstance() {
    String url = "foo";
    when(managementApiUrlBuilder.buildForConfiguration()).thenReturn(url);

    ManagementApi api = mock(ManagementApi.class);
    when(feignBuilder.target(ManagementApi.class, url)).thenReturn(api);

    ManagementApi result = sut.managementApi();

    assertSame(api, result);
  }

}
