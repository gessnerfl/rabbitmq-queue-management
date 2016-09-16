package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.remoteapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.gessnerfl.rabbitmq.queue.management.javaconfig.RabbitMqSettingsConfig;

@Service
public class ManagementApiUrlBuilder {
  private final RabbitMqSettingsConfig rabbitMqSettingsConfig;

  @Autowired
  public ManagementApiUrlBuilder(RabbitMqSettingsConfig rabbitMqSettingsConfig) {
    this.rabbitMqSettingsConfig = rabbitMqSettingsConfig;
  }
  
  public String buildForConfiguration(){
    String protocol = rabbitMqSettingsConfig.isManagemnetPortSecured() ? "https" : "http";
    String host = rabbitMqSettingsConfig.getHostname();
    Integer port = rabbitMqSettingsConfig.getManagementPort();
    return protocol + "://" + host + ":" + port + "/api";
  }
}