package de.gessnerfl.rabbitmq.management.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rabbitmq.client.ConnectionFactory;

@Configuration
public class RabbitMqConfig {
	
	@Autowired
	private RabbitMqSettingsConfig settings;

	@Bean
	public ConnectionFactory connectionFactory(){
		ConnectionFactory connectionFactory = new ConnectionFactory();
		connectionFactory.setUsername(settings.getUsername());
		connectionFactory.setPassword(settings.getPassword());
		return connectionFactory;
	}
}
