package de.gessnerfl.rabbitmq.queue.management.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;

import de.gessnerfl.rabbitmq.queue.management.connection.CloseableChannelWrapper;
import de.gessnerfl.rabbitmq.queue.management.connection.Connector;
import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Queue;

public class RabbitMqTestEnvironment {

    private final static Logger LOGGER = LoggerFactory.getLogger(RabbitMqTestEnvironment.class);

    public final static String BROKER = "local";
    
    public final static boolean EXCHANGE_DURABLE = false;
    public final static boolean EXCHANGE_AUTO_DELETE = true;
    public final static String EXCHANGE_TYPE = "direct";
    
    public final static boolean QUEUE_DURABLE = false;
    public final static boolean QUEUE_EXCLUSIVE = false;
    public final static boolean QUEUE_AUTO_DELETE = true;
    
    public final static String DEFAULT_BODY_PREFIX = "default-body-";

    private final List<String> exchanges = new ArrayList<>();
    private final List<QueueParameter> queues = new ArrayList<>();

    private final Connector connector;

    public RabbitMqTestEnvironment(Connector connector) {
        this.connector = connector;
    }
    
    public void addExchange(String exchangeName){
        this.exchanges.add(exchangeName);
    }
    
    public void addQueue(QueueParameter queue){
        this.queues.add(queue);
    }

    public void setup() {
        try (CloseableChannelWrapper wrapper = connector.connectAsClosable(BROKER)) {
            Channel channel = wrapper.getChannel();
            declareExchanges(channel);
            declareQueues(channel);
        } catch (RabbitMqEnvironmentException e) {
            cleanup();
            throw e;
        }
    }

    private void declareExchanges(Channel channel) {
        exchanges.stream().forEach((e) -> declareExchange(e, channel));
    }

    private void declareExchange(String exchangeName, Channel channel) {
        try {
            channel.exchangeDeclare(exchangeName, EXCHANGE_TYPE, EXCHANGE_DURABLE, EXCHANGE_AUTO_DELETE, null);
        } catch (IOException e) {
            throw new RabbitMqEnvironmentException("Failed to setup exchange " + exchangeName, e);
        }
    }

    private void declareQueues(Channel channel) {
        //declare not dead lettered queues
        queues.stream().filter((q) -> !q.deadLetterParameter.isPresent()).forEach((q) -> declareQueue(q, channel));
        //declare dead lettered queues
        queues.stream().filter((q) -> q.deadLetterParameter.isPresent()).forEach((q) -> declareQueue(q, channel));
    }

    private void declareQueue(QueueParameter parameter, Channel channel) {
        try {
            Map<String, Object> arguments = new HashMap<>();
            parameter.deadLetterParameter.ifPresent((p) -> {
                arguments.put(Queue.DEAD_LETTER_EXCHANGE_ARGUMENT, p.exchangeName);
                p.routingKey.ifPresent((routingKey) -> arguments.put(Queue.DEAD_LETTER_ROUTINGKEY_ARGUMENT, routingKey));
            });

            channel.queueDeclare(parameter.queueName, QUEUE_DURABLE, QUEUE_EXCLUSIVE, QUEUE_AUTO_DELETE, arguments);
            channel.queueBind(parameter.queueName, parameter.exchangeName, parameter.routingKey);
        } catch (IOException e) {
            throw new RabbitMqEnvironmentException("Failed to setup queue" + parameter.queueName, e);
        }
    }

    public void cleanup() {
        try (CloseableChannelWrapper wrapper = connector.connectAsClosable(BROKER)) {
            Channel channel = wrapper.getChannel();
            deleteQueues(channel);
            deleteExchanges(channel);
        }
    }

    private void deleteQueues(Channel channel) {
        //delete dead lettered queues
        queues.stream().filter((q) -> q.deadLetterParameter.isPresent()).forEach((q) -> deleteQueue(q, channel));
        //delete not dead lettered queues
        queues.stream().filter((q) -> !q.deadLetterParameter.isPresent()).forEach((q) -> deleteQueue(q, channel));
    }

    private void deleteQueue(QueueParameter parameter, Channel channel) {
        try {
            channel.queueDelete(parameter.queueName);
        } catch (IOException e) {
            LOGGER.error("Failed to delete queue", e);
        }
    }

    private void deleteExchanges(Channel channel) {
        exchanges.stream().forEach((e) -> deleteExchange(e, channel));
    }

    private void deleteExchange(String exchangeName, Channel channel) {
        try {
            channel.exchangeDelete(exchangeName);
        } catch (IOException e) {
            LOGGER.error("Failed to delete exchange", e);
        }
    }
    
    public void publishMessages(String exchange, String routingKey, int numberOfMessages) {
        try (CloseableChannelWrapper wrapper = connector.connectAsClosable(BROKER)) {
            Channel channel = wrapper.getChannel();
            for (int i = 0; i < numberOfMessages; i++) {
                publishMessage(channel, exchange, routingKey, i);
            }
        }
    }
    
    public void publishMessage(String exchange, String routingKey) {
        try (CloseableChannelWrapper wrapper = connector.connectAsClosable(BROKER)) {
            Channel channel = wrapper.getChannel();
            publishMessage(channel, exchange, routingKey, 1);
        }
    }

    private void publishMessage(Channel channel, String exchange, String routingKey, int id) {
        try {
            byte[] body = buildMessage(id);
            channel.basicPublish(exchange, routingKey, MessageProperties.TEXT_PLAIN, body);
        } catch (IOException e) {
            throw new RabbitMqEnvironmentException("Failed to publish message", e);
        }
    }

    public byte[] buildMessage(int id) {
        String message = DEFAULT_BODY_PREFIX + id;
        byte[] body = message.getBytes(StandardCharsets.UTF_8);
        return body;
    }
}
