package de.gessnerfl.rabbitmq.queue.management.controller;

import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.RabbitMqFacade;
import de.gessnerfl.rabbitmq.queue.management.util.RabbitMqTestEnvironment;
import de.gessnerfl.rabbitmq.queue.management.util.RabbitMqTestEnvironmentBuilder;
import de.gessnerfl.rabbitmq.queue.management.util.RabbitMqTestEnvironmentBuilderFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class MoveAllMessagesControllerIntegrationTest extends AbstractControllerIntegrationTest {
    private static final String VHOST_NAME = "/";
    private static final String EXCHANGE_NAME = "test.ex";
    private static final String QUEUE_1_NAME = "test1.controller.in";
    private static final String QUEUE_2_NAME = "test2.controller.in";

    @Autowired
    private RabbitMqTestEnvironmentBuilderFactory testEnvironmentBuilderFactor;
    private RabbitMqTestEnvironment testEnvironment;

    @Autowired
    private RabbitMqFacade facade;

    @Before
    public void init() throws Exception {
        RabbitMqTestEnvironmentBuilder builder = testEnvironmentBuilderFactor.create();
        testEnvironment = builder.withExchange(EXCHANGE_NAME)
                .withQueue(QUEUE_1_NAME)
                    .exchange(EXCHANGE_NAME)
                    .build()
                .withQueue(QUEUE_2_NAME)
                    .exchange(EXCHANGE_NAME)
                    .build()
                .build();
        testEnvironment.setup();
    }

    @After
    public void cleanup() {
        testEnvironment.cleanup();
    }

    @Test
    public void shouldReturnPageOnGet() throws Exception {
        mockMvc.perform(get("/messages/move-all")
                    .param(Parameters.VHOST, VHOST_NAME)
                    .param(Parameters.QUEUE, QUEUE_1_NAME))
                .andExpect(status().isOk())
                .andExpect(view().name(MoveAllMessagesController.VIEW_NAME))
                .andExpect(model().attribute(Parameters.VHOST, VHOST_NAME))
                .andExpect(model().attribute(Parameters.QUEUE, QUEUE_1_NAME));
    }

    @Test
    public void shouldProvideScreenToSelectRoutingKeyWhenTargetExchangeIsProvidedAndTargetRoutingKeyIsNotProvidedOnPost() throws Exception {
        testEnvironment.publishMessages(EXCHANGE_NAME, QUEUE_1_NAME, 2);

        assertThat(facade.getMessagesOfQueue(VHOST_NAME, QUEUE_1_NAME, 10), hasSize(2));
        assertThat(facade.getMessagesOfQueue(VHOST_NAME, QUEUE_2_NAME, 10), empty());

        mockMvc.perform(post("/messages/move-all")
                    .param(Parameters.VHOST, VHOST_NAME)
                    .param(Parameters.QUEUE, QUEUE_1_NAME)
                    .param(Parameters.TARGET_EXCHANGE, EXCHANGE_NAME))
                .andExpect(status().isOk())
                .andExpect(view().name(MoveAllMessagesController.VIEW_NAME))
                .andExpect(model().attribute(Parameters.VHOST, VHOST_NAME))
                .andExpect(model().attribute(Parameters.QUEUE, QUEUE_1_NAME))
                .andExpect(model().attribute(Parameters.TARGET_EXCHANGE, EXCHANGE_NAME))
                .andExpect(model().attribute(Parameters.ROUTING_KEYS, Arrays.asList(QUEUE_1_NAME, QUEUE_2_NAME)));

        assertThat(facade.getMessagesOfQueue(VHOST_NAME, QUEUE_1_NAME, 10), hasSize(2));
        assertThat(facade.getMessagesOfQueue(VHOST_NAME, QUEUE_2_NAME, 10), empty());
    }

    @Test
    public void shouldMoveAllMessagesFromSourceToTargetQueueOnPostWhenTargetExchangeAndRoutingKeyAreProvidedOnPost() throws Exception {
        testEnvironment.publishMessages(EXCHANGE_NAME, QUEUE_1_NAME, 2);

        assertThat(facade.getMessagesOfQueue(VHOST_NAME, QUEUE_1_NAME, 10), hasSize(2));
        assertThat(facade.getMessagesOfQueue(VHOST_NAME, QUEUE_2_NAME, 10), empty());

        mockMvc.perform(post("/messages/move-all")
                    .param(Parameters.VHOST, VHOST_NAME)
                    .param(Parameters.QUEUE, QUEUE_1_NAME)
                    .param(Parameters.TARGET_EXCHANGE, EXCHANGE_NAME)
                    .param(Parameters.TARGET_ROUTING_KEY, QUEUE_2_NAME))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/messages?vhost=%2F&queue=test1.controller.in"));

        assertThat(facade.getMessagesOfQueue(VHOST_NAME, QUEUE_1_NAME, 10), empty());
        assertThat(facade.getMessagesOfQueue(VHOST_NAME, QUEUE_2_NAME, 10), hasSize(2));
    }
}
