package de.gessnerfl.rabbitmq.queue.management.util;

import java.util.Optional;

public class QueueParameterBuilder {
    private final RabbitMqTestEnvironmentBuilder rabbitMqTestEnvironmentBuilder;
    
    private String queueName;
    private String exchangeName;
    private String routingKey;
    private String deadLetterExchangeName;
    private String deadLetterRoutingKey;
    
    QueueParameterBuilder(RabbitMqTestEnvironmentBuilder rabbitMqTestEnvironmentBuilder) {
        this.rabbitMqTestEnvironmentBuilder = rabbitMqTestEnvironmentBuilder;
    }

    public QueueParameterBuilder queue(String queueName){
        this.queueName = queueName;
        return this;
    }
    
    public QueueParameterBuilder exchange(String exchangeName){
        this.exchangeName = exchangeName;
        return this;
    }
    
    public QueueParameterBuilder routingKey(String routingKey){
        this.routingKey = routingKey;
        return this;
    }
    
    public QueueParameterBuilder deadLetterExchange(String deadLetterExchangeName){
        this.deadLetterExchangeName = deadLetterExchangeName;
        return this;
    }
    
    public QueueParameterBuilder deadLetterRoutingKey(String deadLetterRoutingKey){
        this.deadLetterRoutingKey = deadLetterRoutingKey;
        return this;
    }
    
    public RabbitMqTestEnvironmentBuilder build(){
        enrich();
        validate();
        DeadLetterParameter deadLetterParameter = deadLetterExchangeName != null ? new DeadLetterParameter(deadLetterExchangeName, Optional.ofNullable(deadLetterRoutingKey)) : null;
        QueueParameter parameter = new QueueParameter(exchangeName, routingKey, queueName, Optional.ofNullable(deadLetterParameter));
        return rabbitMqTestEnvironmentBuilder.withQueue(parameter);
    }

    private void enrich() {
        if(routingKey == null){
            routingKey = queueName;
        }
    }

    private void validate() {
        assertNotEmpty(exchangeName, "Exchange name");
        assertNotEmpty(queueName, "Queue name");
        assertNotEmpty(routingKey, "Routing key");
    }

    private void assertNotEmpty(String value, String attributeName) {
        if(value == null || value.trim().isEmpty()){
            throw new IllegalArgumentException(attributeName+" is missing");
        }
    }
}
