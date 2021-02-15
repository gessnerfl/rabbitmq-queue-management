package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.gessnerfl.rabbitmq.queue.management.model.Message;
import de.gessnerfl.rabbitmq.queue.management.util.RabbitMqTestEnvironment;

class QueueListOperationIntegrationTest extends AbstractOperationIntegrationTest {
    private final static int MAX_NUMBER_OF_MESSAGES = QueueListOperation.DEFAULT_FETCH_COUNT;

    @Autowired
    private QueueListOperation sut;

    @Test
    void shouldReturnAllMessagesWhenNumberOfExistingMessagesIsExactlyTheSameAsTheMaxNumbertoBeRetrieved()
            throws IOException {
        int expectedNumberOfMessages = QueueListOperation.DEFAULT_FETCH_COUNT;
        publishMessages(expectedNumberOfMessages);

        List<Message> messages = sut.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, QUEUE_NAME, MAX_NUMBER_OF_MESSAGES);

        assertThat(messages, hasSize(expectedNumberOfMessages));
        for (int i = 0; i < expectedNumberOfMessages; i++) {
            byte[] body = testEnvironment.buildMessage(i);
            assertArrayEquals(body, messages.get(i).getBody());
        }
    }

    @Test
    void shouldReturnAllMessagesWhenNumberOfExistingMessagesIsLessThenTheMaxNumberToBeRetrieved()
            throws IOException {
        int expectedNumberOfMessages = QueueListOperation.DEFAULT_FETCH_COUNT - 1;
        publishMessages(expectedNumberOfMessages);

        List<Message> messages = sut.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, QUEUE_NAME, MAX_NUMBER_OF_MESSAGES);

        assertThat(messages, hasSize(expectedNumberOfMessages));
        for (int i = 0; i < expectedNumberOfMessages; i++) {
            byte[] body = testEnvironment.buildMessage(i);
            assertArrayEquals(body, messages.get(i).getBody());
        }
    }

    @Test
    void shouldReturnSubsetOfMessagesWhenNumberOfExistingMessagesIsGreaterThanTheMaxNumbertoBeRetrieved()
            throws IOException {
        int expectedNumberOfMessages = QueueListOperation.DEFAULT_FETCH_COUNT + 1;
        publishMessages(expectedNumberOfMessages);

        List<Message> messages = sut.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, QUEUE_NAME, MAX_NUMBER_OF_MESSAGES);

        assertThat(messages, hasSize(QueueListOperation.DEFAULT_FETCH_COUNT));
        for (int i = 0; i < QueueListOperation.DEFAULT_FETCH_COUNT; i++) {
            byte[] body = testEnvironment.buildMessage(i);
            assertArrayEquals(body, messages.get(i).getBody());
        }
    }

    @Test
    void shouldNotChangeTheOrderOfMessages() throws Exception {
        publishMessages(2);

        List<Message> firstFetch = sut.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, QUEUE_NAME, 1);
        List<Message> secondFetch = sut.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, QUEUE_NAME, 2);

        assertThat(firstFetch, hasSize(1));
        assertThat(secondFetch, hasSize(2));
        assertEquals(firstFetch.get(0).getChecksum(), secondFetch.get(0).getChecksum());
        assertNotEquals(firstFetch.get(0).getChecksum(), secondFetch.get(1).getChecksum());
    }

    @Test
    void shouldReturnEmptyListIfNoMessageIsAvailable(){
        List<Message> firstFetch = sut.getMessagesFromQueue(RabbitMqTestEnvironment.VHOST, QUEUE_NAME, 1);
        
        assertThat(firstFetch, empty());
    }
}
