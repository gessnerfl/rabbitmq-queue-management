package de.gessnerfl.rabbitmq.queue.management.controller;

import de.gessnerfl.rabbitmq.queue.management.util.RabbitMqTestEnvironmentBuilder;
import de.gessnerfl.rabbitmq.queue.management.util.RabbitMqTestEnvironmentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class IndexControllerIntegrationTest extends AbstractControllerIntegrationTest {

    public static final String EXCHANGE_NAME = "exchange";
    public static final String QUEUE_1_NAME = "queue1";
    public static final String QUEUE_2_NAME = "queue2";
    @Autowired
    private RabbitMqTestEnvironmentBuilderFactory testEnvironmentBuilderFactor;

    @Test
    public void shouldRedirectToIndexPageOnRoot() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index"));
    }

    @Test
    public void shouldReturnListOfQueuesOnIndexPage() throws Exception {
        RabbitMqTestEnvironmentBuilder builder = testEnvironmentBuilderFactor.create();
        var testEnvironment = builder.withExchange(EXCHANGE_NAME)
                .withQueue(QUEUE_1_NAME)
                .exchange(EXCHANGE_NAME)
                .build()
                .withQueue(QUEUE_2_NAME)
                .exchange(EXCHANGE_NAME)
                .build()
                .build();
        testEnvironment.setup();

        try {
            mockMvc.perform(get("/index"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("queues", containsInAnyOrder(QUEUE_1_NAME, QUEUE_2_NAME)));
        } finally {
            testEnvironment.cleanup();
        }
    }
}
