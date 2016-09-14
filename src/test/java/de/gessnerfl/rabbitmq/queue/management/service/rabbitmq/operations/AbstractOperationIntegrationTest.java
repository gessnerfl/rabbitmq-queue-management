package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;

import de.gessnerfl.rabbitmq.queue.management.AbstractIntegrationTest;
import de.gessnerfl.rabbitmq.queue.management.connection.CloseableChannelWrapper;
import de.gessnerfl.rabbitmq.queue.management.connection.Connector;

public abstract class AbstractOperationIntegrationTest extends AbstractIntegrationTest {
    protected final static String EXCHANGE_NAME = "test.direct";
    protected final static String QUEUE_NAME = "test.queue";
    protected final static String DEFAULT_BODY_STRING = "default-body-";

    protected final Logger logger;

    @Autowired
    protected Connector connector;

    public AbstractOperationIntegrationTest() {
        logger = LoggerFactory.getLogger(this.getClass());
    }

    @Before
    public void init() throws Exception {
        try (CloseableChannelWrapper wrapper = connector.connectAsClosable()) {
            Channel channel = wrapper.getChannel();
            declareExchange(channel);
            declareQueue(channel);
        } catch (IOException e) {
            cleanup();
            throw e;
        }
    }

    @After
    public void cleanup() {
        try (CloseableChannelWrapper wrapper = connector.connectAsClosable()) {
            Channel channel = wrapper.getChannel();
            deleteQueue(channel);
            deleteExchange(channel);
        }
    }

    protected void declareExchange(Channel channel) throws IOException {
        channel.exchangeDeclare(EXCHANGE_NAME, "direct", false, true, null);
    }

    protected void declareQueue(Channel channel) throws IOException {
        channel.queueDeclare(QUEUE_NAME, false, false, true, null);
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, QUEUE_NAME);
    }

    protected void deleteQueue(Channel channel) {
        try {
            channel.queueDelete(QUEUE_NAME);
        } catch (IOException e) {
            logger.error("Failed to delete queue", e);
        }
    }

    protected void deleteExchange(Channel channel) {
        try {
            channel.exchangeDelete(EXCHANGE_NAME);
        } catch (IOException e) {
            logger.error("Failed to delete exchange", e);
        }
    }

    protected void publishMessages(int numberOfMessages) throws IOException {
        try (CloseableChannelWrapper wrapper = connector.connectAsClosable()) {
            Channel channel = wrapper.getChannel();
            for (int i = 0; i < numberOfMessages; i++) {
                byte[] body = buildMessage(i);
                channel.basicPublish(EXCHANGE_NAME, QUEUE_NAME, MessageProperties.TEXT_PLAIN, body);
            }
        }
    }

    protected byte[] buildMessage(int i) {
        String message = DEFAULT_BODY_STRING + i;
        byte[] body = message.getBytes(StandardCharsets.UTF_8);
        return body;
    }
}
