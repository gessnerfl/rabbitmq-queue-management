package de.gessnerfl.rabbitmq.queue.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
@SpringBootApplication
public class RabbitmqQueueManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(RabbitmqQueueManagementApplication.class, args);
	}
}
