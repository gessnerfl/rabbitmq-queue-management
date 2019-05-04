package de.gessnerfl.rabbitmq.queue.management.controller.rest;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.gessnerfl.rabbitmq.queue.management.controller.AbstractControllerIntegrationTest;
import de.gessnerfl.rabbitmq.queue.management.model.Message;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.RabbitMqFacade;
import de.gessnerfl.rabbitmq.queue.management.util.RabbitMqTestEnvironment;
import de.gessnerfl.rabbitmq.queue.management.util.RabbitMqTestEnvironmentBuilder;
import de.gessnerfl.rabbitmq.queue.management.util.RabbitMqTestEnvironmentBuilderFactory;

import static de.gessnerfl.rabbitmq.queue.management.controller.rest.QueryParameters.*;

public class QueueControllerIntegrationTest extends AbstractControllerIntegrationTest {
    private final static String VHOST_NAME = "/";
    private final static String EXCHANGE_NAME = "test.ex";
    private final static String QUEUE_IN_NAME = "test.controller.in";
    private final static String QUEUE_OUT_NAME = "test.controller.out";
    private final static String QUEUE2_NAME = "test.controller.q2";
    private final static String QUEUE2_DLX_NAME = "test.controller.q2.dlx";
    private final static String ALL_QUEUES_JSON_PATH_FILTER = "$[?(@.name in ['"+QUEUE_IN_NAME+"','"+QUEUE_OUT_NAME+"'])]";
    private final static String IN_QUEUE_JSON_PATH_FILTER = "$[?(@.name == '"+QUEUE_IN_NAME+"')]";
    private final static String OUT_QUEUE_JSON_PATH_FILTER = "$[?(@.name == '"+QUEUE_OUT_NAME+"')]";
    private static final int MESSAGE_TTL_OF_QUEUE2 = 100;
    
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
                            .withQueue(QUEUE2_DLX_NAME)
                                .exchange(EXCHANGE_NAME)
                                .build()
                            .withQueue(QUEUE2_NAME)
                                .exchange(EXCHANGE_NAME)
                                .ttl(MESSAGE_TTL_OF_QUEUE2)
                                .deadLetterExchange(EXCHANGE_NAME)
                                .deadLetterRoutingKey(QUEUE2_DLX_NAME)
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
            .andExpect(jsonPath(IN_QUEUE_JSON_PATH_FILTER+".name", contains(QUEUE_IN_NAME)))
            .andExpect(jsonPath(IN_QUEUE_JSON_PATH_FILTER+".vhost", contains("/")))
            .andExpect(jsonPath(IN_QUEUE_JSON_PATH_FILTER+".durable", contains(false)))
            .andExpect(jsonPath(IN_QUEUE_JSON_PATH_FILTER+".autoDelete", contains(true)))
            .andExpect(jsonPath(IN_QUEUE_JSON_PATH_FILTER+".exclusive", contains(false)))
            .andExpect(jsonPath(OUT_QUEUE_JSON_PATH_FILTER+".name", contains(QUEUE_OUT_NAME)))
            .andExpect(jsonPath(OUT_QUEUE_JSON_PATH_FILTER+".vhost", contains("/")))
            .andExpect(jsonPath(OUT_QUEUE_JSON_PATH_FILTER+".durable", contains(false)))
            .andExpect(jsonPath(OUT_QUEUE_JSON_PATH_FILTER+".autoDelete", contains(true)))
            .andExpect(jsonPath(OUT_QUEUE_JSON_PATH_FILTER+".exclusive", contains(false)));
    }

    @Test
    public void shouldReturnAllQueuesOfVhost() throws Exception {
        mockMvc.perform(get("/api/queues").param(VHOST, VHOST_NAME))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath(ALL_QUEUES_JSON_PATH_FILTER, hasSize(2)))
                .andExpect(jsonPath(IN_QUEUE_JSON_PATH_FILTER+".name", contains(QUEUE_IN_NAME)))
                .andExpect(jsonPath(IN_QUEUE_JSON_PATH_FILTER+".vhost", contains(VHOST_NAME)))
                .andExpect(jsonPath(IN_QUEUE_JSON_PATH_FILTER+".durable", contains(false)))
                .andExpect(jsonPath(IN_QUEUE_JSON_PATH_FILTER+".autoDelete", contains(true)))
                .andExpect(jsonPath(IN_QUEUE_JSON_PATH_FILTER+".exclusive", contains(false)))
                .andExpect(jsonPath(OUT_QUEUE_JSON_PATH_FILTER+".name", contains(QUEUE_OUT_NAME)))
                .andExpect(jsonPath(OUT_QUEUE_JSON_PATH_FILTER+".vhost", contains(VHOST_NAME)))
                .andExpect(jsonPath(OUT_QUEUE_JSON_PATH_FILTER+".durable", contains(false)))
                .andExpect(jsonPath(OUT_QUEUE_JSON_PATH_FILTER+".autoDelete", contains(true)))
                .andExpect(jsonPath(OUT_QUEUE_JSON_PATH_FILTER+".exclusive", contains(false)));
    }
    
    @Test
    public void shouldReturnEmptyListWhenQueueIsEmpty() throws Exception {
        mockMvc.perform(get("/api/messages").param(VHOST, VHOST_NAME).param(QUEUE, QUEUE_IN_NAME))
            .andExpect(status().isOk())
            .andExpect(content().contentType(contentType))
            .andExpect(jsonPath("$", empty()));
    }
    
    @Test
    public void shouldReturnMessagesOfQueue() throws Exception {
        testEnvironment.publishMessage(EXCHANGE_NAME, QUEUE_IN_NAME);
        mockMvc.perform(get("/api/messages").param(VHOST, VHOST_NAME).param(QUEUE, QUEUE_IN_NAME))
            .andExpect(status().isOk())
            .andExpect(content().contentType(contentType))
            .andExpect(jsonPath("$", hasSize(1)));
    }
    
    @Test
    public void shouldDeleteMessageFromQueue() throws Exception {
        testEnvironment.publishMessage(EXCHANGE_NAME, QUEUE_IN_NAME);
        
        List<Message> messages = facade.getMessagesOfQueue(VHOST_NAME, QUEUE_IN_NAME, 1);
        assertThat(messages, hasSize(1));
        Message message = messages.get(0);
        
        mockMvc.perform(delete("/api/messages").param(VHOST, VHOST_NAME).param(QUEUE, QUEUE_IN_NAME).param(CHECKSUM, message.getChecksum()))
            .andExpect(status().isOk());
        
        assertThat(facade.getMessagesOfQueue(VHOST_NAME, QUEUE_IN_NAME, 1), empty());
    }
    
    @Test
    public void shouldMoveMessageFromQueueInToQueueOut() throws Exception {
        testEnvironment.publishMessage(EXCHANGE_NAME, QUEUE_IN_NAME);
        
        List<Message> messages = facade.getMessagesOfQueue(VHOST_NAME, QUEUE_IN_NAME, 1);
        assertThat(messages, hasSize(1));
        assertThat(facade.getMessagesOfQueue(VHOST_NAME, QUEUE_OUT_NAME, 1), empty());
        Message message = messages.get(0);
        
        mockMvc.perform(post("/api/messages/move")
                            .param(VHOST, VHOST_NAME)
                            .param(QUEUE, QUEUE_IN_NAME)
                            .param(CHECKSUM, message.getChecksum())
                            .param("targetExchange", EXCHANGE_NAME)
                            .param("targetRoutingKey", QUEUE_OUT_NAME))
            .andExpect(status().isOk());
        
        assertThat(facade.getMessagesOfQueue(VHOST_NAME, QUEUE_IN_NAME, 1), empty());
        assertThat(facade.getMessagesOfQueue(VHOST_NAME, QUEUE_OUT_NAME, 1), hasSize(1));
    }

    @Test
    public void shouldRequeueMessage() throws Exception {
        testEnvironment.publishMessage(EXCHANGE_NAME, QUEUE2_NAME);

        //wait until message is dead lettered
        TimeUnit.MILLISECONDS.sleep(MESSAGE_TTL_OF_QUEUE2+10);

        List<Message> queueMessagesFirstFetch = facade.getMessagesOfQueue(VHOST_NAME, QUEUE2_NAME, 10);
        List<Message> dlxQueueMessagesFirstFetch = facade.getMessagesOfQueue(VHOST_NAME, QUEUE2_DLX_NAME, 10);

        assertThat(queueMessagesFirstFetch, empty());
        assertThat(dlxQueueMessagesFirstFetch, hasSize(1));
        Message message = dlxQueueMessagesFirstFetch.get(0);

        mockMvc.perform(post("/api/messages/requeue")
                .param(VHOST, VHOST_NAME)
                .param(QUEUE, QUEUE2_DLX_NAME)
                .param(CHECKSUM, message.getChecksum()))
                .andExpect(status().isOk());

        List<Message> queueMessagesSecondFetch = facade.getMessagesOfQueue(VHOST_NAME, QUEUE2_NAME, 10);
        List<Message> dlxQueueMessagesSecondFetch = facade.getMessagesOfQueue(VHOST_NAME, QUEUE2_DLX_NAME, 10);

        assertThat(queueMessagesSecondFetch, hasSize(1));
        assertThat(dlxQueueMessagesSecondFetch, empty());
    }
}
