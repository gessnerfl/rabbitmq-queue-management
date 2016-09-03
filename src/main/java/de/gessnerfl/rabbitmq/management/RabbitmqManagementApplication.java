package de.gessnerfl.rabbitmq.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
@SpringBootApplication
public class RabbitmqManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(RabbitmqManagementApplication.class, args);
	}
}
