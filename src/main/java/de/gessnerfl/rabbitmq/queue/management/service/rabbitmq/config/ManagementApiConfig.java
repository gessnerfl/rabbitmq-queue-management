package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.config;

import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.remoteapi.FeignBuilderFactory;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.remoteapi.ManagementApi;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.remoteapi.ManagementApiUrlBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.Feign;

@Configuration
public class ManagementApiConfig {

    private final FeignBuilderFactory feignBuilderFactory;
    private final ManagementApiUrlBuilder managementApiUrlBuilder;
    
    @Autowired
    public ManagementApiConfig(FeignBuilderFactory feignBuilderFactory, ManagementApiUrlBuilder managementApiUrlBuilder) {
        this.feignBuilderFactory = feignBuilderFactory;
        this.managementApiUrlBuilder = managementApiUrlBuilder;
    }

    @Bean
    public ManagementApi managementApi(){
        Feign.Builder feignBuilder = feignBuilderFactory.createFor();
        String url = managementApiUrlBuilder.buildForConfiguration();
        return feignBuilder.target(ManagementApi.class, url);
    }
}
