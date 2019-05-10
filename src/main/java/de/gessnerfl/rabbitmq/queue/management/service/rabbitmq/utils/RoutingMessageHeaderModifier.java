package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.utils;

import com.rabbitmq.client.AMQP;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations.OperationId;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RoutingMessageHeaderModifier {

    public AMQP.BasicProperties modifyHeaders(AMQP.BasicProperties properties, OperationId operationId, String header){
        Map<String,Object> headers = getOrCreateHeaders(properties);
        headers.compute(header, (k,v) -> v == null ? 1 : Integer.sum(((Integer)v), 1));
        headers.put(OperationId.HEADER_NAME, operationId.getValue());
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
        headers.forEach(copy::put);
        return copy;
    }

}
