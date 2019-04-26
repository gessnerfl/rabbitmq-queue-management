package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.remoteapi;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.remoteapi.ManagementApi;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.remoteapi.ManagementApiUrlBuilder;
import feign.Feign;

@RunWith(MockitoJUnitRunner.class)
public class ManagementApiFactoryTest {

    @Mock
    private ManagementApiUrlBuilder managementApiUrlBuilder;
    @Mock
    private FeignBuilderFactory feignBuilderFactory;

    @InjectMocks
    private ManagementApiFactory sut;

    @Test
    public void shouldCreateNewInstance() {
        String brokerName = "foo";
        String url = "bar";
        when(managementApiUrlBuilder.buildForConfiguration(brokerName)).thenReturn(url);

        ManagementApi api = mock(ManagementApi.class);
        Feign.Builder feignBuilder = mock(Feign.Builder.class);
        when(feignBuilder.target(ManagementApi.class, url)).thenReturn(api);
        when(feignBuilderFactory.createFor(brokerName)).thenReturn(feignBuilder);

        ManagementApi result = sut.createFor(brokerName);

        assertSame(api, result);
    }

}
