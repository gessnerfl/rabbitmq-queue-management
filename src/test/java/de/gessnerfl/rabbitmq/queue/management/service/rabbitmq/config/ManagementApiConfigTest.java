package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.config;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.remoteapi.FeignBuilderFactory;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.remoteapi.ManagementApi;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.remoteapi.ManagementApiUrlBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import feign.Feign;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ManagementApiConfigTest {

    @Mock
    private ManagementApiUrlBuilder managementApiUrlBuilder;
    @Mock
    private FeignBuilderFactory feignBuilderFactory;

    @InjectMocks
    private ManagementApiConfig sut;

    @Test
    void shouldCreateNewInstance() {
        String url = "bar";
        when(managementApiUrlBuilder.buildForConfiguration()).thenReturn(url);

        ManagementApi api = mock(ManagementApi.class);
        Feign.Builder feignBuilder = mock(Feign.Builder.class);
        when(feignBuilder.target(ManagementApi.class, url)).thenReturn(api);
        when(feignBuilderFactory.createFor()).thenReturn(feignBuilder);

        ManagementApi result = sut.managementApi();

        assertSame(api, result);
    }

}
