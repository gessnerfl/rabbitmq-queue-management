package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

import java.io.IOException;
import java.util.*;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.LongString;
import de.gessnerfl.rabbitmq.queue.management.model.BasicProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;

import de.gessnerfl.rabbitmq.queue.management.connection.CloseableChannelWrapper;
import de.gessnerfl.rabbitmq.queue.management.connection.Connector;
import de.gessnerfl.rabbitmq.queue.management.model.Message;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.utils.MessageChecksum;

@Repository
public class QueueListOperation {
    public static final int DEFAULT_FETCH_COUNT = 10;

    private final Connector connector;
    private final MessageChecksum messageChecksum;

    @Autowired
    public QueueListOperation(Connector connector, MessageChecksum messageChecksum) {
        this.connector = connector;
        this.messageChecksum = messageChecksum;
    }

    public List<Message> getMessagesFromQueue(String brokerName, String queueName, int maxNumberOfMessages) {
        try (CloseableChannelWrapper wrapper = connector.connectAsClosable(brokerName)) {
            List<Message> messages = new ArrayList<>();
            Channel channel = wrapper.getChannel();
            channel.basicQos(DEFAULT_FETCH_COUNT);
            int fetched = 0;
            boolean messagesAvailable = true;
            Long lastDeliveryTag = null;
            while (fetched < maxNumberOfMessages && messagesAvailable) {
                GetResponse response = channel.basicGet(queueName, false);
                if(response != null){
                    messages.add(createMessage(response));
                    lastDeliveryTag = response.getEnvelope().getDeliveryTag();
                    fetched++;
                    messagesAvailable = response.getMessageCount() > 0;
                }else{
                    messagesAvailable = false;
                }
            }
            if (lastDeliveryTag != null) {
                channel.basicNack(lastDeliveryTag, true, true);
            }
            return messages;
        } catch (IOException e) {
            throw new MessageFetchFailedException(e);
        }
    }

    private Message createMessage(GetResponse response) {
        String checksum =  messageChecksum.createFor(response.getProps(), response.getBody());
        return new Message(response.getEnvelope(), mapProperties(response.getProps()), response.getBody(), checksum);
    }

    private BasicProperties mapProperties(AMQP.BasicProperties input){
        BasicProperties result = new BasicProperties();
        result.setContentType(input.getContentType());
        result.setContentEncoding(input.getContentEncoding());
        if(input.getHeaders() != null) {
            for (Map.Entry<String, Object> entry : input.getHeaders().entrySet()) {
                Object value = entry.getValue();
                value = mapRabbitMqSpecificType(value);
                result.addHeader(entry.getKey(), value);
            }
        }
        result.setDeliveryMode(input.getDeliveryMode());
        result.setPriority(input.getPriority());
        result.setCorrelationId(input.getCorrelationId());
        result.setReplyTo(input.getReplyTo());
        result.setExpiration(input.getExpiration());
        result.setMessageId(input.getMessageId());
        result.setTimestamp(input.getTimestamp());
        result.setType(input.getType());
        result.setUserId(input.getUserId());
        result.setAppId(input.getAppId());
        result.setClusterId(input.getClusterId());
        return result;
    }

    private Object mapRabbitMqSpecificType(Object value) {
        if(value instanceof LongString){
            return value.toString();
        }else if(value instanceof Map){
            Map<String,Object> result = new HashMap<>();
            Set<Map.Entry> entrySet = ((Map) value).entrySet();
            for(Map.Entry e : entrySet){
                result.put(e.getKey().toString(), mapRabbitMqSpecificType(e.getValue()));
            }
            return result;
        }else if(value instanceof List){
            List<Object> result = new ArrayList<>();
            for(Object o : (List)value){
                result.add(mapRabbitMqSpecificType(o));
            }
            return  result;
        }
        return value;
    }

}
