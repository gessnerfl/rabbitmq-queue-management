package de.gessnerfl.rabbitmq.queue.management.controller;

import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Queue;
import de.gessnerfl.rabbitmq.queue.management.util.RabbitMqTestEnvironment;
import de.gessnerfl.rabbitmq.queue.management.util.RabbitMqTestEnvironmentBuilder;
import de.gessnerfl.rabbitmq.queue.management.util.RabbitMqTestEnvironmentBuilderFactory;
import org.hamcrest.CustomMatcher;
import org.hamcrest.Matcher;
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
                    .andExpect(model().attribute("queues", containsInAnyOrder(matchesQueue(QUEUE_1_NAME), matchesQueue(QUEUE_2_NAME))));
        } finally {
            testEnvironment.cleanup();
        }
    }

    private Matcher<Queue> matchesQueue(String name) {
        return matchesQueue(name, RabbitMqTestEnvironment.VHOST);
    }

    private Matcher<Queue> matchesQueue(String name, String vhost) {
        return new CustomMatcher<Queue>("matches when name and vhost of queue matches") {
            @Override
            public boolean matches(Object actual) {
                if (actual instanceof Queue) {
                    return ((Queue) actual).getName().equals(name) && ((Queue) actual).getVhost().equals(vhost);
                }
                return false;
            }
        };
    }
}
