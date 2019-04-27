package de.gessnerfl.rabbitmq.queue.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class RabbitmqQueueManagementApplication {
    
	public static void main(String[] args) {
		SpringApplication.run(RabbitmqQueueManagementApplication.class, args);
	}

}
