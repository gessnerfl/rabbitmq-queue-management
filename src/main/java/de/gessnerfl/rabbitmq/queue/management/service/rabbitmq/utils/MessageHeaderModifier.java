package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.utils;

import com.rabbitmq.client.AMQP;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MessageHeaderModifier {

    public AMQP.BasicProperties incrementCounter(AMQP.BasicProperties properties, String header){
        Map<String,Object> headers = getOrCreateHeaders(properties);
        headers.compute(header, (k,v) -> v == null ? 1 : Integer.sum(((Integer)v), 1));
        return properties.builder().headers(headers).build();
    }

    private Map<String, Object> getOrCreateHeaders(AMQP.BasicProperties properties){
        if(properties.getHeaders() == null || properties.getHeaders().isEmpty()) {
            return new HashMap<>();
        } else {
            return copyHeader(properties.getHeaders());
        }
    }

    private Map<String, Object> copyHeader(Map<String, Object> headers) {
        Map<String,Object> copy = new HashMap<>();
        headers.forEach((k,v) -> copy.put(k,v));
        return copy;
    }

}
