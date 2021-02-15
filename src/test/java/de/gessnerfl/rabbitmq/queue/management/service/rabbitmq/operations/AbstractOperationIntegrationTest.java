package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

import de.gessnerfl.rabbitmq.queue.management.AbstractIntegrationTestWithRabbitMqContainer;
import de.gessnerfl.rabbitmq.queue.management.util.RabbitMqTestEnvironment;
import de.gessnerfl.rabbitmq.queue.management.util.RabbitMqTestEnvironmentBuilder;
import de.gessnerfl.rabbitmq.queue.management.util.RabbitMqTestEnvironmentBuilderFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

abstract class AbstractOperationIntegrationTest extends AbstractIntegrationTestWithRabbitMqContainer {
    protected final static String EXCHANGE_NAME = "test.direct";
    protected final static String QUEUE_NAME = "test.queue";

    protected final Logger logger;

    @Autowired
    private RabbitMqTestEnvironmentBuilderFactory testEnvironmentBuilderFactor;
    protected RabbitMqTestEnvironment testEnvironment;

    AbstractOperationIntegrationTest() {
        logger = LoggerFactory.getLogger(this.getClass());
    }

    @BeforeEach
    void init() throws Exception {
        RabbitMqTestEnvironmentBuilder builder = testEnvironmentBuilderFactor.create();
        builder = builder.withExchange(EXCHANGE_NAME);
        for(String queueName : getQueueNames()){
            builder = builder.withQueue(queueName).exchange(EXCHANGE_NAME).build();
        }
        testEnvironment = builder.build();
        testEnvironment.setup();
    }

    @AfterEach
    void cleanup() {
        testEnvironment.cleanup();
    }

    protected List<String> getQueueNames(){
        return Arrays.asList(QUEUE_NAME);
    }

    protected void publishMessages(int numberOfMessages) throws IOException {
        testEnvironment.publishMessages(EXCHANGE_NAME, QUEUE_NAME, numberOfMessages);
    }

}
