package de.gessnerfl.rabbitmq.queue.management;

import de.gessnerfl.rabbitmq.queue.management.connection.ConnectionFactories;
import de.gessnerfl.rabbitmq.queue.management.connection.Connector;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.config.RabbitMqConfig;
import de.gessnerfl.rabbitmq.queue.management.util.RabbitMqTestEnvironmentBuilderFactory;

public class TestDataCreator {
    private static final String VHOST_NAME = "/";
    private static final String EXCHANGE_NAME = "test.ex";
    private static final String QUEUE_1_NAME = "test1.controller.in";
    private static final String QUEUE_1_DLX_NAME = "test1-dlx.controller.in";
    private static final int MESSAGE_TTL_OF_QUEUE1 = 100;
    private static final String QUEUE_2_NAME = "test2.controller.in";

    public static void main(String[] args){
        var config = new RabbitMqConfig();
        config.setHostname("localhost");
        config.setPassword("guest");
        config.setPassword("guest");
        config.setPort(5672);
        config.setManagementPort(15672);
        var connectionFactories = new ConnectionFactories(config);
        var connector = new Connector(connectionFactories);
        var factory = new RabbitMqTestEnvironmentBuilderFactory(connector);

        var builder = factory.create();
        var testEnvironment = builder.withExchange(EXCHANGE_NAME)
                .withQueue(QUEUE_1_DLX_NAME)
                .exchange(EXCHANGE_NAME)
                .build()
                .withQueue(QUEUE_1_NAME)
                .exchange(EXCHANGE_NAME)
                .deadLetterExchange(EXCHANGE_NAME)
                .deadLetterRoutingKey(QUEUE_1_DLX_NAME)
                .ttl(MESSAGE_TTL_OF_QUEUE1)
                .build()
                .withQueue(QUEUE_2_NAME)
                .exchange(EXCHANGE_NAME)
                .build()
                .build();
        testEnvironment.setup();

        testEnvironment.publishMessages(EXCHANGE_NAME, QUEUE_1_NAME, 5);
        testEnvironment.publishMessages(EXCHANGE_NAME, QUEUE_2_NAME, 5);
        connector.shutdown();
    }
}
