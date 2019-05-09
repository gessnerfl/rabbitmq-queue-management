package de.gessnerfl.rabbitmq.queue.management.controller.rest;

import de.gessnerfl.rabbitmq.queue.management.controller.AbstractControllerIntegrationTest;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.RabbitMqFacade;
import de.gessnerfl.rabbitmq.queue.management.util.RabbitMqTestEnvironment;
import de.gessnerfl.rabbitmq.queue.management.util.RabbitMqTestEnvironmentBuilder;
import de.gessnerfl.rabbitmq.queue.management.util.RabbitMqTestEnvironmentBuilderFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static de.gessnerfl.rabbitmq.queue.management.controller.rest.QueryParameters.*;
import static de.gessnerfl.rabbitmq.queue.management.hamcrest.CustomMatchers.matchesInitialQueueStateNullOrRunning;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class QueueControllerIntegrationTest extends AbstractControllerIntegrationTest {
    private static final String VHOST_NAME = "/";
    private static final String EXCHANGE_NAME = "test.ex";
    private static final String QUEUE_IN_NAME = "test.controller.in";
    private static final String QUEUE_OUT_NAME = "test.controller.out";
    private static final String ALL_QUEUES_JSON_PATH_FILTER = "$[?(@.name in ['" + QUEUE_IN_NAME + "','" + QUEUE_OUT_NAME + "'])]";
    private static final String IN_QUEUE_JSON_PATH_FILTER = "$[?(@.name == '" + QUEUE_IN_NAME + "')]";
    private static final String OUT_QUEUE_JSON_PATH_FILTER = "$[?(@.name == '" + QUEUE_OUT_NAME + "')]";

    @Autowired
    private RabbitMqTestEnvironmentBuilderFactory testEnvironmentBuilderFactor;
    private RabbitMqTestEnvironment testEnvironment;

    @Autowired
    private RabbitMqFacade facade;

    @Before
    public void init() throws Exception {
        RabbitMqTestEnvironmentBuilder builder = testEnvironmentBuilderFactor.create();
        testEnvironment = builder.withExchange(EXCHANGE_NAME)
                .withQueue(QUEUE_IN_NAME)
                .exchange(EXCHANGE_NAME)
                .build()
                .withQueue(QUEUE_OUT_NAME)
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
    public void shouldReturnAllQueues() throws Exception {
        mockMvc.perform(get("/api/queues"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath(ALL_QUEUES_JSON_PATH_FILTER, hasSize(2)))
                .andExpect(jsonPath(IN_QUEUE_JSON_PATH_FILTER + ".name", contains(QUEUE_IN_NAME)))
                .andExpect(jsonPath(IN_QUEUE_JSON_PATH_FILTER + ".vhost", contains("/")))
                .andExpect(jsonPath(IN_QUEUE_JSON_PATH_FILTER + ".durable", contains(false)))
                .andExpect(jsonPath(IN_QUEUE_JSON_PATH_FILTER + ".autoDelete", contains(true)))
                .andExpect(jsonPath(IN_QUEUE_JSON_PATH_FILTER + ".exclusive", contains(false)))
                .andExpect(jsonPath(IN_QUEUE_JSON_PATH_FILTER + ".consumers", contains(0)))
                .andExpect(jsonPath(IN_QUEUE_JSON_PATH_FILTER + ".messages", contains(0)))
                .andExpect(jsonPath(IN_QUEUE_JSON_PATH_FILTER + ".messagesReady", contains(0)))
                .andExpect(jsonPath(IN_QUEUE_JSON_PATH_FILTER + ".messagesUnacknowledged", contains(0)))
                .andExpect(jsonPath(IN_QUEUE_JSON_PATH_FILTER + ".state", contains(matchesInitialQueueStateNullOrRunning())))
                .andExpect(jsonPath(OUT_QUEUE_JSON_PATH_FILTER + ".name", contains(QUEUE_OUT_NAME)))
                .andExpect(jsonPath(OUT_QUEUE_JSON_PATH_FILTER + ".vhost", contains("/")))
                .andExpect(jsonPath(OUT_QUEUE_JSON_PATH_FILTER + ".durable", contains(false)))
                .andExpect(jsonPath(OUT_QUEUE_JSON_PATH_FILTER + ".autoDelete", contains(true)))
                .andExpect(jsonPath(OUT_QUEUE_JSON_PATH_FILTER + ".exclusive", contains(false)))
                .andExpect(jsonPath(OUT_QUEUE_JSON_PATH_FILTER + ".consumers", contains(0)))
                .andExpect(jsonPath(OUT_QUEUE_JSON_PATH_FILTER + ".messages", contains(0)))
                .andExpect(jsonPath(OUT_QUEUE_JSON_PATH_FILTER + ".messagesReady", contains(0)))
                .andExpect(jsonPath(OUT_QUEUE_JSON_PATH_FILTER + ".messagesUnacknowledged", contains(0)))
                .andExpect(jsonPath(OUT_QUEUE_JSON_PATH_FILTER + ".state", contains(matchesInitialQueueStateNullOrRunning())));
    }

    @Test
    public void shouldReturnAllQueuesOfVhost() throws Exception {
        mockMvc.perform(get("/api/queues").param(VHOST, VHOST_NAME))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath(ALL_QUEUES_JSON_PATH_FILTER, hasSize(2)))
                .andExpect(jsonPath(IN_QUEUE_JSON_PATH_FILTER + ".name", contains(QUEUE_IN_NAME)))
                .andExpect(jsonPath(IN_QUEUE_JSON_PATH_FILTER + ".vhost", contains(VHOST_NAME)))
                .andExpect(jsonPath(IN_QUEUE_JSON_PATH_FILTER + ".durable", contains(false)))
                .andExpect(jsonPath(IN_QUEUE_JSON_PATH_FILTER + ".autoDelete", contains(true)))
                .andExpect(jsonPath(IN_QUEUE_JSON_PATH_FILTER + ".exclusive", contains(false)))
                .andExpect(jsonPath(IN_QUEUE_JSON_PATH_FILTER + ".consumers", contains(0)))
                .andExpect(jsonPath(IN_QUEUE_JSON_PATH_FILTER + ".messages", contains(0)))
                .andExpect(jsonPath(IN_QUEUE_JSON_PATH_FILTER + ".messagesReady", contains(0)))
                .andExpect(jsonPath(IN_QUEUE_JSON_PATH_FILTER + ".messagesUnacknowledged", contains(0)))
                .andExpect(jsonPath(IN_QUEUE_JSON_PATH_FILTER + ".state", contains(matchesInitialQueueStateNullOrRunning())))
                .andExpect(jsonPath(OUT_QUEUE_JSON_PATH_FILTER + ".name", contains(QUEUE_OUT_NAME)))
                .andExpect(jsonPath(OUT_QUEUE_JSON_PATH_FILTER + ".vhost", contains(VHOST_NAME)))
                .andExpect(jsonPath(OUT_QUEUE_JSON_PATH_FILTER + ".durable", contains(false)))
                .andExpect(jsonPath(OUT_QUEUE_JSON_PATH_FILTER + ".autoDelete", contains(true)))
                .andExpect(jsonPath(OUT_QUEUE_JSON_PATH_FILTER + ".exclusive", contains(false)))
                .andExpect(jsonPath(OUT_QUEUE_JSON_PATH_FILTER + ".consumers", contains(0)))
                .andExpect(jsonPath(OUT_QUEUE_JSON_PATH_FILTER + ".messages", contains(0)))
                .andExpect(jsonPath(OUT_QUEUE_JSON_PATH_FILTER + ".messagesReady", contains(0)))
                .andExpect(jsonPath(OUT_QUEUE_JSON_PATH_FILTER + ".messagesUnacknowledged", contains(0)))
                .andExpect(jsonPath(OUT_QUEUE_JSON_PATH_FILTER + ".state", contains(matchesInitialQueueStateNullOrRunning())));
    }
}
