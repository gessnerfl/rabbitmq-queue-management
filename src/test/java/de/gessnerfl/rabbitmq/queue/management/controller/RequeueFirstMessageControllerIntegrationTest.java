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
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RequeueFirstMessageControllerIntegrationTest extends AbstractControllerIntegrationTest {
    private static final String VHOST_NAME = "/";
    private static final String EXCHANGE_NAME = "test.ex";
    private static final String QUEUE_1_NAME = "test1.controller.in";
    private static final String QUEUE_1_DLX_NAME = "test1-dlx.controller.in";
    private static final int MESSAGE_TTL_OF_QUEUE1 = 100;
    private static final String QUEUE_2_NAME = "test2.controller.in";
    public static final int MESSAGE_WAIT_OPERATION_TIME = 50;
    public static final int MESSAGE_LIMIT = 10;

    @Autowired
    private RabbitMqTestEnvironmentBuilderFactory testEnvironmentBuilderFactor;
    private RabbitMqTestEnvironment testEnvironment;

    @Autowired
    private RabbitMqFacade facade;

    @BeforeEach
    void init() throws Exception {
        RabbitMqTestEnvironmentBuilder builder = testEnvironmentBuilderFactor.create();
        testEnvironment = builder.withExchange(EXCHANGE_NAME)
                .withQueue(QUEUE_1_DLX_NAME)
                    .exchange(EXCHANGE_NAME)
                    .build()
                .withQueue(QUEUE_1_NAME)
                    .exchange(EXCHANGE_NAME)
                    .deadLetterExchange(EXCHANGE_NAME)
                    .deadLetterRoutingKey(QUEUE_1_DLX_NAME)
                    .ttl(MESSAGE_TTL_OF_QUEUE1)
                    .build()
                .withQueue(QUEUE_2_NAME)
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
        testEnvironment.publishMessages(EXCHANGE_NAME, QUEUE_1_NAME, 2);

        //wait until message is dead lettered
        await().atMost(MESSAGE_TTL_OF_QUEUE1 + MESSAGE_WAIT_OPERATION_TIME, TimeUnit.MILLISECONDS)
                .until(() -> facade.getMessagesOfQueue(VHOST_NAME, QUEUE_1_DLX_NAME, MESSAGE_LIMIT).size() == 2);

        assertThat(facade.getMessagesOfQueue(VHOST_NAME, QUEUE_1_NAME, MESSAGE_LIMIT), empty());
        List<Message> initialMessages = facade.getMessagesOfQueue(VHOST_NAME, QUEUE_1_DLX_NAME, MESSAGE_LIMIT);
        assertThat(initialMessages, hasSize(2));
        assertThat(facade.getMessagesOfQueue(VHOST_NAME, QUEUE_2_NAME, MESSAGE_LIMIT), empty());

        mockMvc.perform(get("/messages/requeue-first")
                    .param(Parameters.VHOST, VHOST_NAME)
                    .param(Parameters.QUEUE, QUEUE_1_DLX_NAME)
                    .param(Parameters.CHECKSUM, initialMessages.get(0).getChecksum()))
                .andExpect(status().isOk())
                .andExpect(view().name(RequeueFirstMessageController.VIEW_NAME))
                .andExpect(model().attribute(Parameters.VHOST, VHOST_NAME))
                .andExpect(model().attribute(Parameters.QUEUE, QUEUE_1_DLX_NAME))
                .andExpect(model().attribute(Parameters.CHECKSUM, initialMessages.get(0).getChecksum()));
    }

    @Test
    void shouldReturnToMessagePageWhenNoMessagesAreAvailableOnQueue() throws Exception {
        mockMvc.perform(get("/messages/requeue-first")
                    .param(Parameters.VHOST, VHOST_NAME)
                    .param(Parameters.QUEUE, QUEUE_1_NAME)
                    .param(Parameters.CHECKSUM, "anyChecksum"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/messages?vhost=%2F&queue=test1.controller.in"));
    }

    @Test
    void shouldReturnToMessagePageWhenFirstMessageInQueueDoesNotSupportRequeuing() throws Exception {
        testEnvironment.publishMessages(EXCHANGE_NAME, QUEUE_2_NAME, 2);
        List<Message> initialMessages = facade.getMessagesOfQueue(VHOST_NAME, QUEUE_2_NAME, MESSAGE_LIMIT);

        mockMvc.perform(get("/messages/requeue-first")
                    .param(Parameters.VHOST, VHOST_NAME)
                    .param(Parameters.QUEUE, QUEUE_2_NAME)
                    .param(Parameters.CHECKSUM, initialMessages.get(0).getChecksum()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/messages?vhost=%2F&queue=test2.controller.in"));
    }

    @Test
    void shouldRequeueFirstMessageFromSourceToTargetQueueOnPostWhenTargetExchangeAndRoutingKeyAreProvidedOnPost() throws Exception {
        testEnvironment.publishMessages(EXCHANGE_NAME, QUEUE_1_NAME, 2);

        //wait until message is dead lettered
        await().atMost(MESSAGE_TTL_OF_QUEUE1 + MESSAGE_WAIT_OPERATION_TIME, TimeUnit.MILLISECONDS)
                .until(() -> facade.getMessagesOfQueue(VHOST_NAME, QUEUE_1_DLX_NAME, MESSAGE_LIMIT).size() == 2);

        assertThat(facade.getMessagesOfQueue(VHOST_NAME, QUEUE_1_NAME, MESSAGE_LIMIT), empty());
        List<Message> initialMessages = facade.getMessagesOfQueue(VHOST_NAME, QUEUE_1_DLX_NAME, MESSAGE_LIMIT);
        assertThat(initialMessages, hasSize(2));

        mockMvc.perform(post("/messages/requeue-first")
                    .param(Parameters.VHOST, VHOST_NAME)
                    .param(Parameters.QUEUE, QUEUE_1_DLX_NAME)
                    .param(Parameters.CHECKSUM, initialMessages.get(0).getChecksum())
                    .param(Parameters.TARGET_EXCHANGE, EXCHANGE_NAME)
                    .param(Parameters.TARGET_ROUTING_KEY, QUEUE_1_NAME))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/messages?vhost=%2F&queue=test1-dlx.controller.in"));

        assertThat(facade.getMessagesOfQueue(VHOST_NAME, QUEUE_1_NAME, MESSAGE_LIMIT), hasSize(1));
        assertThat(facade.getMessagesOfQueue(VHOST_NAME, QUEUE_1_DLX_NAME, MESSAGE_LIMIT), hasSize(1));
    }

    @Test
    void shouldFailToRequeueFirstMessageWhenFirstMessageWasAlreadyProcessedInParallel() throws Exception {
        testEnvironment.publishMessages(EXCHANGE_NAME, QUEUE_1_NAME, 2);

        //wait until message is dead lettered
        await().atMost(MESSAGE_TTL_OF_QUEUE1 + MESSAGE_WAIT_OPERATION_TIME, TimeUnit.MILLISECONDS)
                .until(() -> facade.getMessagesOfQueue(VHOST_NAME, QUEUE_1_DLX_NAME, MESSAGE_LIMIT).size() == 2);

        assertThat(facade.getMessagesOfQueue(VHOST_NAME, QUEUE_1_NAME, MESSAGE_LIMIT), empty());
        List<Message> initialMessages = facade.getMessagesOfQueue(VHOST_NAME, QUEUE_1_DLX_NAME, MESSAGE_LIMIT);
        assertThat(initialMessages, hasSize(2));

        facade.deleteFirstMessageInQueue(VHOST_NAME, QUEUE_1_DLX_NAME, initialMessages.get(0).getChecksum());
        assertThat(facade.getMessagesOfQueue(VHOST_NAME, QUEUE_1_DLX_NAME, MESSAGE_LIMIT), hasSize(1));

        mockMvc.perform(post("/messages/requeue-first")
                    .param(Parameters.VHOST, VHOST_NAME)
                    .param(Parameters.QUEUE, QUEUE_1_DLX_NAME)
                    .param(Parameters.CHECKSUM, initialMessages.get(0).getChecksum())
                    .param(Parameters.TARGET_EXCHANGE, EXCHANGE_NAME)
                    .param(Parameters.TARGET_ROUTING_KEY, QUEUE_1_NAME))
                .andExpect(status().isOk())
                .andExpect(view().name(RequeueFirstMessageController.VIEW_NAME))
                .andExpect(model().attribute(Parameters.VHOST, VHOST_NAME))
                .andExpect(model().attribute(Parameters.QUEUE, QUEUE_1_DLX_NAME))
                .andExpect(model().attribute(Parameters.CHECKSUM, initialMessages.get(0).getChecksum()))
                .andExpect(model().attribute(Parameters.TARGET_EXCHANGE, EXCHANGE_NAME))
                .andExpect(model().attribute(Parameters.TARGET_ROUTING_KEY, QUEUE_1_NAME))
                .andExpect(model().attribute(Parameters.ERROR_MESSAGE, notNullValue(String.class)));

        assertThat(facade.getMessagesOfQueue(VHOST_NAME, QUEUE_1_NAME, MESSAGE_LIMIT), empty());
        assertThat(facade.getMessagesOfQueue(VHOST_NAME, QUEUE_1_DLX_NAME, MESSAGE_LIMIT), hasSize(1));
    }
}
