package de.gessnerfl.rabbitmq.queue.management;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@DirtiesContext
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integrationtest")
public abstract class AbstractIntegrationTest {

	public AbstractIntegrationTest() {
		super();
	}

}