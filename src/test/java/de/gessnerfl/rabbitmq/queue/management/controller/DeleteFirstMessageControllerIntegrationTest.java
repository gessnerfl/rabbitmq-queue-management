package de.gessnerfl.rabbitmq.queue.management.controller;

import de.gessnerfl.rabbitmq.queue.management.model.Message;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.RabbitMqFacade;
import de.gessnerfl.rabbitmq.queue.management.util.RabbitMqTestEnvironment;
import de.gessnerfl.rabbitmq.queue.management.util.RabbitMqTestEnvironmentBuilder;
import de.gessnerfl.rabbitmq.queue.management.util.RabbitMqTestEnvironmentBuilderFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DeleteFirstMessageControllerIntegrationTest extends AbstractControllerIntegrationTest {
    private static final String VHOST_NAME = "/";
    private static final String EXCHANGE_NAME = "test.ex";
    private static final String QUEUE_NAME = "test.controller.in";

    @Autowired
    private RabbitMqTestEnvironmentBuilderFactory testEnvironmentBuilderFactor;
    private RabbitMqTestEnvironment testEnvironment;

    @Autowired
    private RabbitMqFacade facade;

    @BeforeEach
    void init() throws Exception {
        RabbitMqTestEnvironmentBuilder builder = testEnvironmentBuilderFactor.create();
        testEnvironment = builder.withExchange(EXCHANGE_NAME)
                .withQueue(QUEUE_NAME)
                .exchange(EXCHANGE_NAME)
                .build()
                .build();
        testEnvironment.setup();
    }

    @AfterEach
    void cleanup() {
        testEnvironment.cleanup();
    }

    @Test
    void shouldReturnPageOnGet() throws Exception {
        testEnvironment.publishMessage(EXCHANGE_NAME, QUEUE_NAME);
        List<Message> initialMessageList = facade.getMessagesOfQueue(VHOST_NAME, QUEUE_NAME, 10);

        mockMvc.perform(get("/messages/delete-first")
                    .param(Parameters.VHOST, VHOST_NAME)
                    .param(Parameters.QUEUE, QUEUE_NAME)
                    .param(Parameters.CHECKSUM, initialMessageList.get(0).getChecksum()))
                .andExpect(status().isOk())
                .andExpect(view().name(DeleteFirstMessageController.VIEW_NAME))
                .andExpect(model().attribute(Parameters.VHOST, VHOST_NAME))
                .andExpect(model().attribute(Parameters.QUEUE, QUEUE_NAME))
                .andExpect(model().attribute(Parameters.CHECKSUM, initialMessageList.get(0).getChecksum()));
    }

    @Test
    void shouldDeleteFirstMessageInQueueOnPost() throws Exception {
        testEnvironment.publishMessages(EXCHANGE_NAME, QUEUE_NAME, 2);
        List<Message> initialMessageList = facade.getMessagesOfQueue(VHOST_NAME, QUEUE_NAME, 10);

        assertThat(initialMessageList, hasSize(2));

        mockMvc.perform(post("/messages/delete-first")
                    .param(Parameters.VHOST, VHOST_NAME)
                    .param(Parameters.QUEUE, QUEUE_NAME)
                    .param(Parameters.CHECKSUM, initialMessageList.get(0).getChecksum()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/messages?vhost=%2F&queue=test.controller.in"));

        assertThat(facade.getMessagesOfQueue(VHOST_NAME, QUEUE_NAME, 10), hasSize(1));
    }

    @Test
    void shouldFailToDeleteFirstMessageInQueueWhenMessageWasAlreadyProcessedInParallel() throws Exception {
        testEnvironment.publishMessages(EXCHANGE_NAME, QUEUE_NAME, 2);
        List<Message> initialMessageList = facade.getMessagesOfQueue(VHOST_NAME, QUEUE_NAME, 10);

        assertThat(initialMessageList, hasSize(2));
        facade.deleteFirstMessageInQueue(VHOST_NAME, QUEUE_NAME, initialMessageList.get(0).getChecksum());
        assertThat(facade.getMessagesOfQueue(VHOST_NAME, QUEUE_NAME, 10), hasSize(1));

        mockMvc.perform(post("/messages/delete-first")
                .param(Parameters.VHOST, VHOST_NAME)
                .param(Parameters.QUEUE, QUEUE_NAME)
                .param(Parameters.CHECKSUM, initialMessageList.get(0).getChecksum()))
                .andExpect(status().isOk())
                .andExpect(view().name(DeleteFirstMessageController.VIEW_NAME))
                .andExpect(model().attribute(Parameters.VHOST, VHOST_NAME))
                .andExpect(model().attribute(Parameters.QUEUE, QUEUE_NAME))
                .andExpect(model().attribute(Parameters.CHECKSUM, initialMessageList.get(0).getChecksum()))
                .andExpect(model().attribute(Parameters.ERROR_MESSAGE, notNullValue(String.class)));

        assertThat(facade.getMessagesOfQueue(VHOST_NAME, QUEUE_NAME, 10), hasSize(1));
    }
}
