package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.remoteapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import feign.Feign;

@Service
public class ManagementApiFactory {

    private final FeignBuilderFactory feignBuilderFactory;
    private final ManagementApiUrlBuilder managementApiUrlBuilder;
    
    @Autowired
    public ManagementApiFactory(FeignBuilderFactory feignBuilderFactory, ManagementApiUrlBuilder managementApiUrlBuilder) {
        this.feignBuilderFactory = feignBuilderFactory;
        this.managementApiUrlBuilder = managementApiUrlBuilder;
    }

    public ManagementApi createFor(){
        Feign.Builder feignBuilder = feignBuilderFactory.createFor();
        String url = managementApiUrlBuilder.buildForConfiguration();
        return feignBuilder.target(ManagementApi.class, url);
    }
}
