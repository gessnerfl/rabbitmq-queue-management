package de.gessnerfl.rabbitmq.deadletter.manager.javaconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Configuration
public class GsonConfig {

	@Bean
	public Gson gson(){
		return new GsonBuilder().create();
	}
	
}
