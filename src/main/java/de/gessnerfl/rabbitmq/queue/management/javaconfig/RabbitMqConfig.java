package de.gessnerfl.rabbitmq.queue.management.javaconfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rabbitmq.client.ConnectionFactory;

@Configuration
public class RabbitMqConfig {

  @Autowired
  private RabbitMqSettingsConfig settings;

  @Bean
  public ConnectionFactory connectionFactory() {
    ConnectionFactory connectionFactory = new ConnectionFactory();
    connectionFactory.setHost(settings.getHostname());
    connectionFactory.setPort(settings.getPort());
    connectionFactory.setUsername(settings.getUsername());
    connectionFactory.setPassword(settings.getPassword());
    return connectionFactory;
  }
}
