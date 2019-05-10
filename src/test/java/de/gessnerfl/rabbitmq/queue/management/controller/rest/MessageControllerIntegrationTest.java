package de.gessnerfl.rabbitmq.queue.management.controller.rest;

import de.gessnerfl.rabbitmq.queue.management.controller.AbstractControllerIntegrationTest;
import de.gessnerfl.rabbitmq.queue.management.model.Message;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.RabbitMqFacade;
import de.gessnerfl.rabbitmq.queue.management.util.RabbitMqTestEnvironment;
import de.gessnerfl.rabbitmq.queue.management.util.RabbitMqTestEnvironmentBuilder;
import de.gessnerfl.rabbitmq.queue.management.util.RabbitMqTestEnvironmentBuilderFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static de.gessnerfl.rabbitmq.queue.management.controller.rest.QueryParameters.QUEUE;
import static de.gessnerfl.rabbitmq.queue.management.controller.rest.QueryParameters.VHOST;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MessageControllerIntegrationTest extends AbstractControllerIntegrationTest {
    private static final String VHOST_NAME = "/";
    private static final String EXCHANGE_NAME = "test.ex";
    private static final String QUEUE_IN_NAME = "test.controller.in";
    private static final String QUEUE_OUT_NAME = "test.controller.out";
    private static final String QUEUE2_NAME = "test.controller.q2";
    private static final String QUEUE2_DLX_NAME = "test.controller.q2.dlx";
    private static final int MESSAGE_TTL_OF_QUEUE2 = 100;
    public static final int GET_MESSAGE_LIMIT = 10;

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
    public void shouldDeleteAllMessagesFromQueue() throws Exception {
        testEnvironment.publishMessage(EXCHANGE_NAME, QUEUE_IN_NAME);
        testEnvironment.publishMessage(EXCHANGE_NAME, QUEUE_IN_NAME);

        List<Message> messages = facade.getMessagesOfQueue(VHOST_NAME, QUEUE_IN_NAME, GET_MESSAGE_LIMIT);
        assertThat(messages, hasSize(2));
        Message message = messages.get(0);

        mockMvc.perform(delete("/api/messages/").param(VHOST, VHOST_NAME).param(QUEUE, QUEUE_IN_NAME))
                .andExpect(status().isOk());

        assertThat(facade.getMessagesOfQueue(VHOST_NAME, QUEUE_IN_NAME, GET_MESSAGE_LIMIT), empty());
    }

    @Test
    public void shouldDeleteFirstMessageFromQueue() throws Exception {
        testEnvironment.publishMessage(EXCHANGE_NAME, QUEUE_IN_NAME);

        List<Message> messages = facade.getMessagesOfQueue(VHOST_NAME, QUEUE_IN_NAME, GET_MESSAGE_LIMIT);
        assertThat(messages, hasSize(1));
        Message message = messages.get(0);

        mockMvc.perform(delete("/api/messages/" + message.getChecksum()).param(VHOST, VHOST_NAME).param(QUEUE, QUEUE_IN_NAME))
                .andExpect(status().isOk());

        assertThat(facade.getMessagesOfQueue(VHOST_NAME, QUEUE_IN_NAME, GET_MESSAGE_LIMIT), empty());
    }

    @Test
    public void shouldMoveAllMessagesFromQueueInToQueueOut() throws Exception {
        testEnvironment.publishMessage(EXCHANGE_NAME, QUEUE_IN_NAME);
        testEnvironment.publishMessage(EXCHANGE_NAME, QUEUE_IN_NAME);

        List<Message> messages = facade.getMessagesOfQueue(VHOST_NAME, QUEUE_IN_NAME, GET_MESSAGE_LIMIT);
        assertThat(messages, hasSize(2));
        assertThat(facade.getMessagesOfQueue(VHOST_NAME, QUEUE_OUT_NAME, GET_MESSAGE_LIMIT), empty());

        mockMvc.perform(post("/api/messages/move")
                .param(VHOST, VHOST_NAME)
                .param(QUEUE, QUEUE_IN_NAME)
                .param("targetExchange", EXCHANGE_NAME)
                .param("targetRoutingKey", QUEUE_OUT_NAME))
                .andExpect(status().isOk());

        assertThat(facade.getMessagesOfQueue(VHOST_NAME, QUEUE_IN_NAME, GET_MESSAGE_LIMIT), empty());
        assertThat(facade.getMessagesOfQueue(VHOST_NAME, QUEUE_OUT_NAME, GET_MESSAGE_LIMIT), hasSize(2));
    }

    @Test
    public void shouldMoveFirstMessageFromQueueInToQueueOut() throws Exception {
        testEnvironment.publishMessage(EXCHANGE_NAME, QUEUE_IN_NAME);

        List<Message> messages = facade.getMessagesOfQueue(VHOST_NAME, QUEUE_IN_NAME, GET_MESSAGE_LIMIT);
        assertThat(messages, hasSize(1));
        assertThat(facade.getMessagesOfQueue(VHOST_NAME, QUEUE_OUT_NAME, GET_MESSAGE_LIMIT), empty());
        Message message = messages.get(0);

        mockMvc.perform(post("/api/messages/" + message.getChecksum() + "/move")
                .param(VHOST, VHOST_NAME)
                .param(QUEUE, QUEUE_IN_NAME)
                .param("targetExchange", EXCHANGE_NAME)
                .param("targetRoutingKey", QUEUE_OUT_NAME))
                .andExpect(status().isOk());

        assertThat(facade.getMessagesOfQueue(VHOST_NAME, QUEUE_IN_NAME, GET_MESSAGE_LIMIT), empty());
        assertThat(facade.getMessagesOfQueue(VHOST_NAME, QUEUE_OUT_NAME, GET_MESSAGE_LIMIT), hasSize(1));
    }

    @Test
    public void shouldRequeueAllMessagesInQueue() throws Exception {
        testEnvironment.publishMessage(EXCHANGE_NAME, QUEUE2_NAME);
        testEnvironment.publishMessage(EXCHANGE_NAME, QUEUE2_NAME);

        //wait until message is dead lettered
        TimeUnit.MILLISECONDS.sleep(MESSAGE_TTL_OF_QUEUE2 + GET_MESSAGE_LIMIT);

        List<Message> queueMessagesFirstFetch = facade.getMessagesOfQueue(VHOST_NAME, QUEUE2_NAME, GET_MESSAGE_LIMIT);
        List<Message> dlxQueueMessagesFirstFetch = facade.getMessagesOfQueue(VHOST_NAME, QUEUE2_DLX_NAME, GET_MESSAGE_LIMIT);

        assertThat(queueMessagesFirstFetch, empty());
        assertThat(dlxQueueMessagesFirstFetch, hasSize(2));

        mockMvc.perform(post("/api/messages/requeue")
                .param(VHOST, VHOST_NAME)
                .param(QUEUE, QUEUE2_DLX_NAME))
                .andExpect(status().isOk());

        List<Message> queueMessagesSecondFetch = facade.getMessagesOfQueue(VHOST_NAME, QUEUE2_NAME, GET_MESSAGE_LIMIT);
        List<Message> dlxQueueMessagesSecondFetch = facade.getMessagesOfQueue(VHOST_NAME, QUEUE2_DLX_NAME, GET_MESSAGE_LIMIT);

        assertThat(queueMessagesSecondFetch, hasSize(2));
        assertThat(dlxQueueMessagesSecondFetch, empty());
    }

    @Test
    public void shouldRequeueFirstMessageInQueue() throws Exception {
        testEnvironment.publishMessage(EXCHANGE_NAME, QUEUE2_NAME);

        //wait until message is dead lettered
        TimeUnit.MILLISECONDS.sleep(MESSAGE_TTL_OF_QUEUE2 + GET_MESSAGE_LIMIT);

        List<Message> queueMessagesFirstFetch = facade.getMessagesOfQueue(VHOST_NAME, QUEUE2_NAME, GET_MESSAGE_LIMIT);
        List<Message> dlxQueueMessagesFirstFetch = facade.getMessagesOfQueue(VHOST_NAME, QUEUE2_DLX_NAME, GET_MESSAGE_LIMIT);

        assertThat(queueMessagesFirstFetch, empty());
        assertThat(dlxQueueMessagesFirstFetch, hasSize(1));
        Message message = dlxQueueMessagesFirstFetch.get(0);

        mockMvc.perform(post("/api/messages/" + message.getChecksum() + "/requeue")
                .param(VHOST, VHOST_NAME)
                .param(QUEUE, QUEUE2_DLX_NAME))
                .andExpect(status().isOk());

        List<Message> queueMessagesSecondFetch = facade.getMessagesOfQueue(VHOST_NAME, QUEUE2_NAME, GET_MESSAGE_LIMIT);
        List<Message> dlxQueueMessagesSecondFetch = facade.getMessagesOfQueue(VHOST_NAME, QUEUE2_DLX_NAME, GET_MESSAGE_LIMIT);

        assertThat(queueMessagesSecondFetch, hasSize(1));
        assertThat(dlxQueueMessagesSecondFetch, empty());
    }
}
