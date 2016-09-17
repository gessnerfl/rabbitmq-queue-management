package de.gessnerfl.rabbitmq.queue.management.javaconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Configuration
public class GsonConfig {
    
    private final static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

	@Bean
	public Gson gson(){
		return new GsonBuilder().setDateFormat(DATE_FORMAT).create();
	}
	
}
