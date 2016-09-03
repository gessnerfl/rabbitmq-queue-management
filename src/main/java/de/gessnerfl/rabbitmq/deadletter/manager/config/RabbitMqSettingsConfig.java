package de.gessnerfl.rabbitmq.deadletter.manager.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "de.gessnerfl.rabbitmq")
public class RabbitMqSettingsConfig {
  private String addresses;
  private String username;
  private String password;
  private List<String> queues;

  public String getAddresses() {
    return addresses;
  }

  public void setAddresses(String addresses) {
    this.addresses = addresses;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public List<String> getQueues() {
    return queues;
  }

  public void setQueues(List<String> queues) {
    this.queues = queues;
  }

}
