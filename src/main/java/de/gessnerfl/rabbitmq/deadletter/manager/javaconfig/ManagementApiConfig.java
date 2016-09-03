package de.gessnerfl.rabbitmq.deadletter.manager.javaconfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import de.gessnerfl.rabbitmq.deadletter.manager.remoteapi.ManagementApi;
import de.gessnerfl.rabbitmq.deadletter.manager.remoteapi.ManagementApiUrlBuilder;
import feign.Feign;

@Configuration
public class ManagementApiConfig {

  @Autowired
  private ManagementApiUrlBuilder managementApiUrlBuilder;
  @Autowired
  private Feign.Builder feignBuilder;
  
  @Bean
  @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  public ManagementApi managementApi() {
    return feignBuilder.target(ManagementApi.class, buildUrl());
  }

  private String buildUrl() {
    return managementApiUrlBuilder.buildForConfiguration();
  }

}
