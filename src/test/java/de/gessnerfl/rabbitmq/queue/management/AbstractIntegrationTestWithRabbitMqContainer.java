package de.gessnerfl.rabbitmq.queue.management;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ContextConfiguration(initializers = {AbstractIntegrationTestWithRabbitMqContainer.Initializer.class})
public abstract class AbstractIntegrationTestWithRabbitMqContainer extends AbstractIntegrationTest {

    private static final int MANAGEMENT_HTTP_PORT = 15672;
    private static final int AMQP_PORT = 5672;

    @Container
    public static GenericContainer rabbitMqContainer = new GenericContainer("rabbitmq:3.8-management-alpine").withExposedPorts(MANAGEMENT_HTTP_PORT, AMQP_PORT);

    static class Initializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "de.gessnerfl.rabbitmq.port=" + rabbitMqContainer.getMappedPort(AMQP_PORT),
                    "de.gessnerfl.rabbitmq.managementPort=" + rabbitMqContainer.getMappedPort(MANAGEMENT_HTTP_PORT)
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }
}
