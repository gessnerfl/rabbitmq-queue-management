package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

import com.rabbitmq.client.impl.LongStringHelper;
import de.gessnerfl.rabbitmq.queue.management.model.Message;
import de.gessnerfl.rabbitmq.queue.management.util.RabbitMqTestEnvironment;
import de.gessnerfl.rabbitmq.queue.management.util.RabbitMqTestEnvironmentBuilder;
import de.gessnerfl.rabbitmq.queue.management.util.RabbitMqTestEnvironmentBuilderFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@DirtiesContext
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integrationtest")
@Testcontainers
@ContextConfiguration(initializers = {MessageRequeueOperationIntegrationTest.Initializer.class})
class MessageRequeueOperationIntegrationTest {
    private final static String EXCHANGE_NAME = "test.direct";
    private final static String QUEUE_NAME = "test.requeue.target";
    private final static String DLX_QUEUE_NAME = "test.requeue.target.dlx";
    private static final int MESSAGE_TTL_OF_QUEUE = 100;

    private static final int MANAGEMENT_HTTP_PORT = 15672;
    private static final int AMQP_PORT = 5672;
    public static final int MESSAGE_WAIT_OPERATION_TIME = 10;

    @Container
    static GenericContainer rabbitMqContainer = new GenericContainer("rabbitmq:3.8-management-alpine").withExposedPorts(MANAGEMENT_HTTP_PORT, AMQP_PORT);

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "de.gessnerfl.rabbitmq.port=" + rabbitMqContainer.getMappedPort(AMQP_PORT),
                    "de.gessnerfl.rabbitmq.managementPort=" + rabbitMqContainer.getMappedPort(MANAGEMENT_HTTP_PORT)
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

    @Autowired
    private QueueListOperation queueListOperation;
    @Autowired
    private RabbitMqTestEnvironmentBuilderFactory testEnvironmentBuilderFactor;

    @Autowired
    private MessageRequeueOperation sut;

    private RabbitMqTestEnvironment testEnvironment;

    @BeforeEach
    void init() throws Exception {
        RabbitMqTestEnvironmentBuilder builder = testEnvironmentBuilderFactor.create();
        builder = builder.withExchange(EXCHANGE_NAME);

        builder.withQueue(DLX_QUEUE_NAME).exchange(EXCHANGE_NAME).build();
        builder.withQueue(QUEUE_NAME).ttl(MESSAGE_TTL_OF_QUEUE).deadLetterExchange(EXCHANGE_NAME).deadLetterRoutingKey(DLX_QUEUE_NAME).exchange(EXCHANGE_NAME).build();

        testEnvironment = builder.build();
        testEnvironment.setup();
    }

    @AfterEach
    void cleanup() {
        testEnvironment.cleanup();
    }

    @Test
    void shouldRequeueMessageWhenMessageWasDeadLettered() throws Exception {
        //publish message to
        testEnvironment.publishMessage(EXCHANGE_NAME, QUEUE_NAME);

        //wait until message is dead lettered
        await().atMost(MESSAGE_TTL_OF_QUEUE + MESSAGE_WAIT_OPERATION_TIME, TimeUnit.MILLISECONDS)
                .until(() -> queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, DLX_QUEUE_NAME, 10).size() == 1);

        List<Message> queueMessagesFirstFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, QUEUE_NAME, 10);
        List<Message> dlxQueueMessagesFirstFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, DLX_QUEUE_NAME, 10);

        assertThat(queueMessagesFirstFetch, empty());
        assertThat(dlxQueueMessagesFirstFetch, hasSize(1));

        sut.requeueFirstMessage(RabbitMqTestEnvironment.VHOST, DLX_QUEUE_NAME, dlxQueueMessagesFirstFetch.get(0).getChecksum());

        List<Message> queueMessagesSecondFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, QUEUE_NAME, 10);
        List<Message> dlxQueueMessagesSecondFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, DLX_QUEUE_NAME, 10);

        assertThat(queueMessagesSecondFetch, hasSize(1));
        assertThat(dlxQueueMessagesSecondFetch, empty());

        //wait until message is again dead lettered
        await().atMost(MESSAGE_TTL_OF_QUEUE + MESSAGE_WAIT_OPERATION_TIME, TimeUnit.MILLISECONDS)
                .until(() -> queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, DLX_QUEUE_NAME, 10).size() == 1);

        List<Message> queueMessagesThirdFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, QUEUE_NAME, 10);
        List<Message> dlxQueueMessagesThirdFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, DLX_QUEUE_NAME, 10);

        assertThat(queueMessagesThirdFetch, empty());
        assertThat(dlxQueueMessagesThirdFetch, hasSize(1));
    }

    @Test
    void shouldFailToRequeueMessageWhenMessageWasNotDeadLetteredAndHeaderIsNotAvailable(){
        testEnvironment.publishMessage(EXCHANGE_NAME, DLX_QUEUE_NAME);

        List<Message> dlxQueueMessagesFirstFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, DLX_QUEUE_NAME, 10);

        assertThat(dlxQueueMessagesFirstFetch, hasSize(1));

        try {
            sut.requeueFirstMessage(RabbitMqTestEnvironment.VHOST, DLX_QUEUE_NAME, dlxQueueMessagesFirstFetch.get(0).getChecksum());
            fail();
        }catch(MessageOperationFailedException e){
            assertThat(e.getMessage(), containsString("x-death header missing"));
        }

        List<Message> dlxQueueMessagesSecondFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, DLX_QUEUE_NAME, 10);

        assertThat(dlxQueueMessagesSecondFetch, hasSize(1));
    }

    @Test
    void shouldFailToRequeueMessageWhenMessageWasNotDeadLetteredAndXDeathHeaderIsNotAvailable(){
        HashMap<String, Object> headers = new HashMap<>();
        headers.put("test", "test");
        testEnvironment.publishMessage(EXCHANGE_NAME, DLX_QUEUE_NAME, headers);

        List<Message> dlxQueueMessagesFirstFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, DLX_QUEUE_NAME, 10);

        assertThat(dlxQueueMessagesFirstFetch, hasSize(1));

        try {
            sut.requeueFirstMessage(RabbitMqTestEnvironment.VHOST, DLX_QUEUE_NAME, dlxQueueMessagesFirstFetch.get(0).getChecksum());
            fail();
        }catch(MessageOperationFailedException e){
            assertThat(e.getMessage(), containsString("x-death header missing"));
        }

        List<Message> dlxQueueMessagesSecondFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, DLX_QUEUE_NAME, 10);

        assertThat(dlxQueueMessagesSecondFetch, hasSize(1));
    }

    @Test
    void shouldFailToRequeueMessageWhenMessageWasNotDeadLetteredAndXDeathHeaderDoesNotContainEntry(){
        List<Map<String,Object>> xdeath = Collections.emptyList();
        HashMap<String, Object> headers = new HashMap<>();
        headers.put(MessageRequeueOperation.X_DEATH_HEADER_KEY_NAME, xdeath);
        testEnvironment.publishMessage(EXCHANGE_NAME, DLX_QUEUE_NAME, headers);

        List<Message> dlxQueueMessagesFirstFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, DLX_QUEUE_NAME, 10);

        assertThat(dlxQueueMessagesFirstFetch, hasSize(1));

        try {
            sut.requeueFirstMessage(RabbitMqTestEnvironment.VHOST, DLX_QUEUE_NAME, dlxQueueMessagesFirstFetch.get(0).getChecksum());
            fail();
        }catch(MessageOperationFailedException e){
            assertThat(e.getMessage(), containsString("x-death header missing"));
        }

        List<Message> dlxQueueMessagesSecondFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, DLX_QUEUE_NAME, 10);

        assertThat(dlxQueueMessagesSecondFetch, hasSize(1));
    }

    @Test
    void shouldFailToRequeueMessageWhenMessageWasNotDeadLetteredAndXDeathHeaderDoesNotContainExchangeName(){
        List<Map<String,Object>> xdeath = Collections.singletonList(new HashMap<>());
        HashMap<String, Object> headers = new HashMap<>();
        headers.put(MessageRequeueOperation.X_DEATH_HEADER_KEY_NAME, xdeath);
        testEnvironment.publishMessage(EXCHANGE_NAME, DLX_QUEUE_NAME, headers);

        List<Message> dlxQueueMessagesFirstFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, DLX_QUEUE_NAME, 10);

        assertThat(dlxQueueMessagesFirstFetch, hasSize(1));

        try {
            sut.requeueFirstMessage(RabbitMqTestEnvironment.VHOST, DLX_QUEUE_NAME, dlxQueueMessagesFirstFetch.get(0).getChecksum());
            fail();
        }catch(MessageOperationFailedException e){
            assertThat(e.getMessage(), containsString("exchange is missing"));
        }

        List<Message> dlxQueueMessagesSecondFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, DLX_QUEUE_NAME, 10);

        assertThat(dlxQueueMessagesSecondFetch, hasSize(1));
    }

    @Test
    void shouldFailToRequeueMessageWhenMessageWasNotDeadLetteredAndXDeathHeaderDoesNotContainRoutingKey(){
        HashMap<String, Object> xdeathEntry = new HashMap<>();
        xdeathEntry.put(MessageRequeueOperation.X_DEATH_EXCHANGE_KEY_NAME, LongStringHelper.asLongString("test"));
        List<Map<String,Object>> xdeath = Collections.singletonList(xdeathEntry);
        HashMap<String, Object> headers = new HashMap<>();
        headers.put(MessageRequeueOperation.X_DEATH_HEADER_KEY_NAME, xdeath);
        testEnvironment.publishMessage(EXCHANGE_NAME, DLX_QUEUE_NAME, headers);

        List<Message> dlxQueueMessagesFirstFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, DLX_QUEUE_NAME, 10);

        assertThat(dlxQueueMessagesFirstFetch, hasSize(1));

        try {
            sut.requeueFirstMessage(RabbitMqTestEnvironment.VHOST, DLX_QUEUE_NAME, dlxQueueMessagesFirstFetch.get(0).getChecksum());
            fail();
        }catch(MessageOperationFailedException e){
            assertThat(e.getMessage(), containsString("routing keys are missing"));
        }

        List<Message> dlxQueueMessagesSecondFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, DLX_QUEUE_NAME, 10);

        assertThat(dlxQueueMessagesSecondFetch, hasSize(1));
    }

    @Test
    void shouldFailToRequeueMessageWhenMessageWasNotDeadLetteredAndXDeathHeaderRoutingKeysAreEmpty(){
        HashMap<String, Object> xdeathEntry = new HashMap<>();
        xdeathEntry.put(MessageRequeueOperation.X_DEATH_EXCHANGE_KEY_NAME, LongStringHelper.asLongString("test"));
        xdeathEntry.put(MessageRequeueOperation.X_DEATH_ROUTING_KEYS_KEY_NAME, Collections.emptyList());
        List<Map<String,Object>> xdeath = Collections.singletonList(xdeathEntry);
        HashMap<String, Object> headers = new HashMap<>();
        headers.put(MessageRequeueOperation.X_DEATH_HEADER_KEY_NAME, xdeath);
        testEnvironment.publishMessage(EXCHANGE_NAME, DLX_QUEUE_NAME, headers);

        List<Message> dlxQueueMessagesFirstFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, DLX_QUEUE_NAME, 10);

        assertThat(dlxQueueMessagesFirstFetch, hasSize(1));

        try {
            sut.requeueFirstMessage(RabbitMqTestEnvironment.VHOST, DLX_QUEUE_NAME, dlxQueueMessagesFirstFetch.get(0).getChecksum());
            fail();
        }catch(MessageOperationFailedException e){
            assertThat(e.getMessage(), containsString("routing keys are missing"));
        }

        List<Message> dlxQueueMessagesSecondFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, DLX_QUEUE_NAME, 10);

        assertThat(dlxQueueMessagesSecondFetch, hasSize(1));
    }

    @Test
    void shouldIncrementRequeueCounterWithEveryExecution() throws Exception {
        //publish message to
        testEnvironment.publishMessage(EXCHANGE_NAME, QUEUE_NAME);

        //wait until message is dead lettered
        await().atMost(MESSAGE_TTL_OF_QUEUE + MESSAGE_WAIT_OPERATION_TIME, TimeUnit.MILLISECONDS)
                .until(() -> queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, DLX_QUEUE_NAME, 10).size() == 1);

        List<Message> dlxQueueMessagesFirstFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, DLX_QUEUE_NAME, 10);
        assertThat(dlxQueueMessagesFirstFetch, hasSize(1));

        //First Requeue
        sut.requeueFirstMessage(RabbitMqTestEnvironment.VHOST, DLX_QUEUE_NAME, dlxQueueMessagesFirstFetch.get(0).getChecksum());

        //wait until message is again dead lettered
        await().atMost(MESSAGE_TTL_OF_QUEUE + MESSAGE_WAIT_OPERATION_TIME, TimeUnit.MILLISECONDS)
                .until(() -> queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, DLX_QUEUE_NAME, 10).size() == 1);

        List<Message> dlxQueueMessagesSecondFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, DLX_QUEUE_NAME, 10);
        assertThat(dlxQueueMessagesSecondFetch, hasSize(1));
        assertEquals(1, dlxQueueMessagesSecondFetch.get(0).getProperties().getHeaders().get(MessageRequeueOperation.REQUEUE_COUNT_HEADER));

        //Second Requeue
        sut.requeueFirstMessage(RabbitMqTestEnvironment.VHOST, DLX_QUEUE_NAME, dlxQueueMessagesSecondFetch.get(0).getChecksum());

        //wait until message is again dead lettered
        await().atMost(MESSAGE_TTL_OF_QUEUE + MESSAGE_WAIT_OPERATION_TIME, TimeUnit.MILLISECONDS)
                .until(() -> queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, DLX_QUEUE_NAME, 10).size() == 1);

        List<Message> dlxQueueMessagesThirdFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, DLX_QUEUE_NAME, 10);
        assertThat(dlxQueueMessagesThirdFetch, hasSize(1));
        assertEquals(2, dlxQueueMessagesThirdFetch.get(0).getProperties().getHeaders().get(MessageRequeueOperation.REQUEUE_COUNT_HEADER));

    }

    @Test
    void shouldRequeueAllMessagesWhenMessageWasDeadLettered() throws Exception {
        //publish message to
        testEnvironment.publishMessage(EXCHANGE_NAME, QUEUE_NAME);
        testEnvironment.publishMessage(EXCHANGE_NAME, QUEUE_NAME);

        //wait until message is dead lettered
        await().atMost(MESSAGE_TTL_OF_QUEUE + MESSAGE_WAIT_OPERATION_TIME, TimeUnit.MILLISECONDS)
                .until(() -> queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, DLX_QUEUE_NAME, 10).size() == 2);

        List<Message> queueMessagesFirstFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, QUEUE_NAME, 10);
        List<Message> dlxQueueMessagesFirstFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, DLX_QUEUE_NAME, 10);

        assertThat(queueMessagesFirstFetch, empty());
        assertThat(dlxQueueMessagesFirstFetch, hasSize(2));

        sut.requeueAllMessages(RabbitMqTestEnvironment.VHOST, DLX_QUEUE_NAME);

        List<Message> queueMessagesSecondFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, QUEUE_NAME, 10);
        List<Message> dlxQueueMessagesSecondFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, DLX_QUEUE_NAME, 10);

        assertThat(queueMessagesSecondFetch, hasSize(2));
        assertThat(dlxQueueMessagesSecondFetch, empty());

        //wait until message is again dead lettered
        await().atMost(MESSAGE_TTL_OF_QUEUE + MESSAGE_WAIT_OPERATION_TIME, TimeUnit.MILLISECONDS)
                .until(() -> queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, DLX_QUEUE_NAME, 10).size() == 2);

        List<Message> queueMessagesThirdFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, QUEUE_NAME, 10);
        List<Message> dlxQueueMessagesThirdFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, DLX_QUEUE_NAME, 10);

        assertThat(queueMessagesThirdFetch, empty());
        assertThat(dlxQueueMessagesThirdFetch, hasSize(2));
    }

}