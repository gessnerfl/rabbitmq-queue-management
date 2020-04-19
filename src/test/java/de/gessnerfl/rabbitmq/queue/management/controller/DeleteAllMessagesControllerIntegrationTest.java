package de.gessnerfl.rabbitmq.queue.management.controller;

import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.RabbitMqFacade;
import de.gessnerfl.rabbitmq.queue.management.util.RabbitMqTestEnvironment;
import de.gessnerfl.rabbitmq.queue.management.util.RabbitMqTestEnvironmentBuilder;
import de.gessnerfl.rabbitmq.queue.management.util.RabbitMqTestEnvironmentBuilderFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class DeleteAllMessagesControllerIntegrationTest extends AbstractControllerIntegrationTest {
    private static final String VHOST_NAME = "/";
    private static final String EXCHANGE_NAME = "test.ex";
    private static final String QUEUE_NAME = "test.controller.in";

    @Autowired
    private RabbitMqTestEnvironmentBuilderFactory testEnvironmentBuilderFactor;
    private RabbitMqTestEnvironment testEnvironment;

    @Autowired
    private RabbitMqFacade facade;

    @Before
    public void init() throws Exception {
        RabbitMqTestEnvironmentBuilder builder = testEnvironmentBuilderFactor.create();
        testEnvironment = builder.withExchange(EXCHANGE_NAME)
                .withQueue(QUEUE_NAME)
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
        mockMvc.perform(get("/messages/delete-all").param(Parameters.VHOST, VHOST_NAME).param(Parameters.QUEUE, QUEUE_NAME))
                .andExpect(status().isOk())
                .andExpect(view().name(DeleteAllMessagesController.VIEW_NAME))
                .andExpect(model().attribute(Parameters.VHOST, VHOST_NAME))
                .andExpect(model().attribute(Parameters.QUEUE, QUEUE_NAME));
    }

    @Test
    public void shouldPurgeQueueOnPost() throws Exception {
        testEnvironment.publishMessages(EXCHANGE_NAME, QUEUE_NAME, 2);

        assertThat(facade.getMessagesOfQueue(VHOST_NAME, QUEUE_NAME, 10), hasSize(2));

        mockMvc.perform(post("/messages/delete-all").param(Parameters.VHOST, VHOST_NAME).param(Parameters.QUEUE, QUEUE_NAME))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/messages?vhost=%2F&queue=test.controller.in"));

        assertThat(facade.getMessagesOfQueue(VHOST_NAME, QUEUE_NAME, 10), empty());
    }
}
