package de.gessnerfl.rabbitmq.queue.management.model;

import lombok.Data;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
public class BasicProperties {
    private String contentType;
    private String contentEncoding;
    private final Map<String,Object> headers = new HashMap<>();
    private Integer deliveryMode;
    private Integer priority;
    private String correlationId;
    private String replyTo;
    private String expiration;
    private String messageId;
    private Date timestamp;
    private String type;
    private String userId;
    private String appId;
    private String clusterId;

    public void addHeader(String key, Object value) {
        this.headers.put(key,value);
    }
}
