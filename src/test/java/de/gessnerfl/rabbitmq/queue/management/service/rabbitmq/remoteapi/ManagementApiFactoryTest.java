package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.remoteapi;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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
        String url = "bar";
        when(managementApiUrlBuilder.buildForConfiguration()).thenReturn(url);

        ManagementApi api = mock(ManagementApi.class);
        Feign.Builder feignBuilder = mock(Feign.Builder.class);
        when(feignBuilder.target(ManagementApi.class, url)).thenReturn(api);
        when(feignBuilderFactory.createFor()).thenReturn(feignBuilder);

        ManagementApi result = sut.createFor();

        assertSame(api, result);
    }

}
