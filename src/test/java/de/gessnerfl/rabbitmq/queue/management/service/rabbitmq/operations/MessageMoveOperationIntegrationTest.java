package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.gessnerfl.rabbitmq.queue.management.model.Message;
import de.gessnerfl.rabbitmq.queue.management.util.RabbitMqTestEnvironment;

public class MessageMoveOperationIntegrationTest extends AbstractOperationIntegrationTest {
    private final static String TARGET_QUEUE_NAME = "test.move.target";

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

        List<Message> sourceFirstFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, QUEUE_NAME, 5);
        List<Message> targetFirstFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, TARGET_QUEUE_NAME, 5);

        assertThat(sourceFirstFetch, hasSize(1));
        assertThat(targetFirstFetch, empty());

        sut.moveFirstMessage(RabbitMqTestEnvironment.VHOST, QUEUE_NAME, sourceFirstFetch.get(0).getChecksum(), EXCHANGE_NAME, TARGET_QUEUE_NAME);

        List<Message> sourceSecondFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, QUEUE_NAME, 5);
        List<Message> targetSecondFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, TARGET_QUEUE_NAME, 5);

        assertThat(sourceSecondFetch, empty());
        assertThat(targetSecondFetch, hasSize(1));
    }

    @Test
    public void shouldFailToMoveMessageWhenExchangeIsNotValid() throws Exception {
        publishMessages(1);

        List<Message> sourceFirstFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, QUEUE_NAME, 5);
        List<Message> targetFirstFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, TARGET_QUEUE_NAME, 5);

        assertThat(sourceFirstFetch, hasSize(1));
        assertThat(targetFirstFetch, empty());

        try {
            sut.moveFirstMessage(RabbitMqTestEnvironment.VHOST, QUEUE_NAME, sourceFirstFetch.get(0).getChecksum(), "invalidExchangeName", TARGET_QUEUE_NAME);
            fail();
        } catch (MessageOperationFailedException e) {
        }

        List<Message> sourceSecondFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, QUEUE_NAME, 5);
        List<Message> targetSecondFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, TARGET_QUEUE_NAME, 5);

        assertThat(sourceSecondFetch, hasSize(1));
        assertThat(targetSecondFetch, empty());
    }

    @Test
    public void shouldFailToMoveMessageWhenNoQueueIsBoundToTheGivenRoutingKey() throws Exception {
        publishMessages(1);

        List<Message> sourceFirstFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, QUEUE_NAME, 5);
        List<Message> targetFirstFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, TARGET_QUEUE_NAME, 5);

        assertThat(sourceFirstFetch, hasSize(1));
        assertThat(targetFirstFetch, empty());

        try {
            sut.moveFirstMessage(RabbitMqTestEnvironment.VHOST, QUEUE_NAME, sourceFirstFetch.get(0).getChecksum(), EXCHANGE_NAME, "invalidRoutingKey");
            fail();
        } catch (MessageOperationFailedException e) {
        }

        List<Message> sourceSecondFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, QUEUE_NAME, 5);
        List<Message> targetSecondFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, TARGET_QUEUE_NAME, 5);

        assertThat(sourceSecondFetch, hasSize(1));
        assertThat(targetSecondFetch, empty());
    }


    @Test
    public void shouldIncrementMoveCountHeaderWithEveryMove() throws Exception {
        publishMessages(1);

        List<Message> sourceFirstFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, QUEUE_NAME, 5);
        List<Message> targetFirstFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, TARGET_QUEUE_NAME, 5);

        assertThat(sourceFirstFetch, hasSize(1));
        assertThat(targetFirstFetch, empty());

        sut.moveFirstMessage(RabbitMqTestEnvironment.VHOST, QUEUE_NAME, sourceFirstFetch.get(0).getChecksum(), EXCHANGE_NAME, TARGET_QUEUE_NAME);

        List<Message> sourceSecondFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, QUEUE_NAME, 5);
        List<Message> targetSecondFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, TARGET_QUEUE_NAME, 5);

        assertThat(sourceSecondFetch, empty());
        assertThat(targetSecondFetch, hasSize(1));
        assertEquals(1, targetSecondFetch.get(0).getProperties().getHeaders().get(MessageMoveOperation.MOVE_COUNT_HEADER));

        sut.moveFirstMessage(RabbitMqTestEnvironment.VHOST, TARGET_QUEUE_NAME, targetSecondFetch.get(0).getChecksum(), EXCHANGE_NAME, QUEUE_NAME);

        List<Message> sourceThirdFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, QUEUE_NAME, 5);
        List<Message> targetThirdFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, TARGET_QUEUE_NAME, 5);

        assertThat(sourceThirdFetch, hasSize(1));
        assertThat(targetThirdFetch, empty());
        assertEquals(2, sourceThirdFetch.get(0).getProperties().getHeaders().get(MessageMoveOperation.MOVE_COUNT_HEADER));
    }

    @Test
    public void shouldMoveAllMessages() throws Exception {
        publishMessages(2);

        List<Message> sourceFirstFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, QUEUE_NAME, 5);
        List<Message> targetFirstFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, TARGET_QUEUE_NAME, 5);

        assertThat(sourceFirstFetch, hasSize(2));
        assertThat(targetFirstFetch, empty());

        sut.moveAllMessages(RabbitMqTestEnvironment.VHOST, QUEUE_NAME, EXCHANGE_NAME, TARGET_QUEUE_NAME);

        List<Message> sourceSecondFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, QUEUE_NAME, 5);
        List<Message> targetSecondFetch = queueListOperation.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, TARGET_QUEUE_NAME, 5);

        assertThat(sourceSecondFetch, empty());
        assertThat(targetSecondFetch, hasSize(2));
    }

}
