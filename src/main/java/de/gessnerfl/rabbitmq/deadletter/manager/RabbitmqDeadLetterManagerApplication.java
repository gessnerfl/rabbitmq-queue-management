package de.gessnerfl.rabbitmq.deadletter.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
@SpringBootApplication
public class RabbitmqDeadLetterManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(RabbitmqDeadLetterManagerApplication.class, args);
	}
}
