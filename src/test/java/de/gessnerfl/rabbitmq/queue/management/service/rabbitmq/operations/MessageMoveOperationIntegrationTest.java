package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.gessnerfl.rabbitmq.queue.management.model.AmqpMessage;
import de.gessnerfl.rabbitmq.queue.management.util.RabbitMqTestEnvironment;

public class MessageMoveOperationIntegrationTest extends AbstractOperationIntegrationTest {
    protected final static String TARGET_QUEUE_NAME = "test.requeue.target";
    @Autowired
    private QueueListOperation queueListOperation;

    @Autowired
    public MessageMoveOperation sut;

    @Override
    protected List<String> getQueueNames() {
        return Arrays.asList(QUEUE_NAME, TARGET_QUEUE_NAME);
    }

    @Test
    public void shouldMoveMessage() throws Exception {
        publishMessages(1);

        List<AmqpMessage> sourceFirstFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.BROKER, QUEUE_NAME, 5);
        List<AmqpMessage> targetFirstFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.BROKER, TARGET_QUEUE_NAME, 5);

        assertThat(sourceFirstFetch, hasSize(1));
        assertThat(targetFirstFetch, empty());

        sut.moveFirstMessage(RabbitMqTestEnvironment.BROKER, QUEUE_NAME, sourceFirstFetch.get(0).getChecksum(), EXCHANGE_NAME, TARGET_QUEUE_NAME);

        List<AmqpMessage> sourceSecondFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.BROKER, QUEUE_NAME, 5);
        List<AmqpMessage> targetSecondFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.BROKER, TARGET_QUEUE_NAME, 5);

        assertThat(sourceSecondFetch, empty());
        assertThat(targetSecondFetch, hasSize(1));
    }

    @Test
    public void shouldFailToMoveMessageWhenExchangeIsNotValid() throws Exception {
        publishMessages(1);

        List<AmqpMessage> sourceFirstFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.BROKER, QUEUE_NAME, 5);
        List<AmqpMessage> targetFirstFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.BROKER, TARGET_QUEUE_NAME, 5);

        assertThat(sourceFirstFetch, hasSize(1));
        assertThat(targetFirstFetch, empty());

        try {
            sut.moveFirstMessage(RabbitMqTestEnvironment.BROKER, QUEUE_NAME, sourceFirstFetch.get(0).getChecksum(), "invalidExchangeName", TARGET_QUEUE_NAME);
            fail();
        } catch (MessageOperationFailedException e) {
        }

        List<AmqpMessage> sourceSecondFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.BROKER, QUEUE_NAME, 5);
        List<AmqpMessage> targetSecondFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.BROKER, TARGET_QUEUE_NAME, 5);

        assertThat(sourceSecondFetch, hasSize(1));
        assertThat(targetSecondFetch, empty());
    }

    @Test
    public void shouldFailToMoveMessageWhenNoQueueIsBoundToTheGivenRoutingKey() throws Exception {
        publishMessages(1);

        List<AmqpMessage> sourceFirstFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.BROKER, QUEUE_NAME, 5);
        List<AmqpMessage> targetFirstFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.BROKER, TARGET_QUEUE_NAME, 5);

        assertThat(sourceFirstFetch, hasSize(1));
        assertThat(targetFirstFetch, empty());

        try {
            sut.moveFirstMessage(RabbitMqTestEnvironment.BROKER, QUEUE_NAME, sourceFirstFetch.get(0).getChecksum(), EXCHANGE_NAME, "invalidRoutingKey");
            fail();
        } catch (MessageOperationFailedException e) {
        }

        List<AmqpMessage> sourceSecondFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.BROKER, QUEUE_NAME, 5);
        List<AmqpMessage> targetSecondFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.BROKER, TARGET_QUEUE_NAME, 5);

        assertThat(sourceSecondFetch, hasSize(1));
        assertThat(targetSecondFetch, empty());
    }

}
