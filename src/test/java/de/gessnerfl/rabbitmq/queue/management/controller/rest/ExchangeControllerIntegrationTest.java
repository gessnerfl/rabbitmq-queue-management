package de.gessnerfl.rabbitmq.queue.management.controller.rest;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.gessnerfl.rabbitmq.queue.management.controller.AbstractControllerIntegrationTest;
import de.gessnerfl.rabbitmq.queue.management.util.RabbitMqTestEnvironment;
import de.gessnerfl.rabbitmq.queue.management.util.RabbitMqTestEnvironmentBuilder;
import de.gessnerfl.rabbitmq.queue.management.util.RabbitMqTestEnvironmentBuilderFactory;

public class ExchangeControllerIntegrationTest extends AbstractControllerIntegrationTest {
    private final static String BROKER_NAME = "local";
    private final static String EXCHANGE_NAME = "test.ex";
    private final static String QUEUE_NAME = "test.q";
    private final static String EXCHANGE_JSON_PATH_FILTER = "$[?(@.name == '"+EXCHANGE_NAME+"')]";
    
    @Autowired
    private RabbitMqTestEnvironmentBuilderFactory testEnvironmentBuilderFactor;
    private RabbitMqTestEnvironment testEnvironment;
    
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
    public void shouldReturnExchanges() throws Exception {
        mockMvc.perform(get("/api/"+BROKER_NAME+"/exchanges"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(contentType))
            .andExpect(jsonPath(EXCHANGE_JSON_PATH_FILTER, hasSize(1)))
            .andExpect(jsonPath(EXCHANGE_JSON_PATH_FILTER+".name", contains(EXCHANGE_NAME)))
            .andExpect(jsonPath(EXCHANGE_JSON_PATH_FILTER+".vhost", contains("/")))
            .andExpect(jsonPath(EXCHANGE_JSON_PATH_FILTER+".type", contains("direct")))
            .andExpect(jsonPath(EXCHANGE_JSON_PATH_FILTER+".durable", contains(false)))
            .andExpect(jsonPath(EXCHANGE_JSON_PATH_FILTER+".autoDelete", contains(true)))
            .andExpect(jsonPath(EXCHANGE_JSON_PATH_FILTER+".internal", contains(false)));
    }
    
    @Test
    public void shouldReturnRoutingKeysOfExchange() throws Exception {
        mockMvc.perform(get("/api/"+BROKER_NAME+"/exchanges/"+EXCHANGE_NAME+"/routingKeys"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.length()", is(1)))
        .andExpect(jsonPath("$", contains(QUEUE_NAME)));
    }
}
