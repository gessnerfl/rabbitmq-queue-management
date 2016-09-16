package de.gessnerfl.rabbitmq.queue.management.javaconfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.google.gson.Gson;

import feign.Feign;
import feign.auth.BasicAuthRequestInterceptor;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.slf4j.Slf4jLogger;

@Configuration
public class FeignConfig {

  @Autowired
  private RabbitMqSettingsConfig rabbitMqSettingsConfig;
  @Autowired
  private Gson gson;

  @Bean
  @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  public Feign.Builder feignBuilder() {
    return Feign.builder()
            .encoder(new GsonEncoder(gson))
            .decoder(new GsonDecoder(gson))
            .logger(new Slf4jLogger())
            .requestInterceptor(buildBasicAuthentication());
  }

  private BasicAuthRequestInterceptor buildBasicAuthentication() {
    return new BasicAuthRequestInterceptor(rabbitMqSettingsConfig.getUsername(),
        rabbitMqSettingsConfig.getPassword());
  }

}
