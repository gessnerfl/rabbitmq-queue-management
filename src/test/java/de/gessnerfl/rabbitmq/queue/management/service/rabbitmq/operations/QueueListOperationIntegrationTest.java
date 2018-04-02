package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.gessnerfl.rabbitmq.queue.management.model.AmqpMessage;
import de.gessnerfl.rabbitmq.queue.management.util.RabbitMqTestEnvironment;

public class QueueListOperationIntegrationTest extends AbstractOperationIntegrationTest {
    private final static int MAX_NUMBER_OF_MESSAGES = QueueListOperation.DEFAULT_FETCH_COUNT;

    @Autowired
    private QueueListOperation sut;

    @Test
    public void shouldReturnAllMessagesWhenNumberOfExistingMessagesIsExactlyTheSameAsTheMaxNumbertoBeRetrieved()
            throws IOException {
        int expectedNumberOfMessages = QueueListOperation.DEFAULT_FETCH_COUNT;
        publishMessages(expectedNumberOfMessages);

        List<AmqpMessage> messages = sut.getMessagesFromQueue(RabbitMqTestEnvironment.BROKER, QUEUE_NAME, MAX_NUMBER_OF_MESSAGES);

        assertThat(messages, hasSize(expectedNumberOfMessages));
        for (int i = 0; i < expectedNumberOfMessages; i++) {
            byte[] body = testEnvironment.buildMessage(i);
            assertArrayEquals(body, messages.get(i).getBody());
        }
    }

    @Test
    public void shouldReturnAllMessagesWhenNumberOfExistingMessagesIsLessThenTheMaxNumberToBeRetrieved()
            throws IOException {
        int expectedNumberOfMessages = QueueListOperation.DEFAULT_FETCH_COUNT - 1;
        publishMessages(expectedNumberOfMessages);

        List<AmqpMessage> messages = sut.getMessagesFromQueue(RabbitMqTestEnvironment.BROKER, QUEUE_NAME, MAX_NUMBER_OF_MESSAGES);

        assertThat(messages, hasSize(expectedNumberOfMessages));
        for (int i = 0; i < expectedNumberOfMessages; i++) {
            byte[] body = testEnvironment.buildMessage(i);
            assertArrayEquals(body, messages.get(i).getBody());
        }
    }

    @Test
    public void shouldReturnSubsetOfMessagesWhenNumberOfExistingMessagesIsGreaterThanTheMaxNumbertoBeRetrieved()
            throws IOException {
        int expectedNumberOfMessages = QueueListOperation.DEFAULT_FETCH_COUNT + 1;
        publishMessages(expectedNumberOfMessages);

        List<AmqpMessage> messages = sut.getMessagesFromQueue(RabbitMqTestEnvironment.BROKER, QUEUE_NAME, MAX_NUMBER_OF_MESSAGES);

        assertThat(messages, hasSize(QueueListOperation.DEFAULT_FETCH_COUNT));
        for (int i = 0; i < QueueListOperation.DEFAULT_FETCH_COUNT; i++) {
            byte[] body = testEnvironment.buildMessage(i);
            assertArrayEquals(body, messages.get(i).getBody());
        }
    }

    @Test
    public void shouldNoChangeTheOrderOfMessages() throws Exception {
        publishMessages(2);

        List<AmqpMessage> firstFetch = sut.getMessagesFromQueue(RabbitMqTestEnvironment.BROKER, QUEUE_NAME, 1);
        List<AmqpMessage> secondFetch = sut.getMessagesFromQueue(RabbitMqTestEnvironment.BROKER, QUEUE_NAME, 2);

        assertThat(firstFetch, hasSize(1));
        assertThat(secondFetch, hasSize(2));
        assertEquals(firstFetch.get(0).getChecksum(), secondFetch.get(0).getChecksum());
        assertNotEquals(firstFetch.get(0).getChecksum(), secondFetch.get(1).getChecksum());
    }

    @Test
    public void shouldReturnEmptyListIfNoMessageIsAvailable(){
        List<AmqpMessage> firstFetch = sut.getMessagesFromQueue(RabbitMqTestEnvironment.BROKER, QUEUE_NAME, 1);
        
        assertThat(firstFetch, empty());
    }
}
