package de.gessnerfl.rabbitmq.queue.management.util;

import de.gessnerfl.rabbitmq.queue.management.connection.Connector;

public class RabbitMqTestEnvironmentBuilder {
    
    private final Connector connector;
    private RabbitMqTestEnvironment environment;

    public RabbitMqTestEnvironmentBuilder(Connector connector) {
        this.connector = connector;
    }
    
    public RabbitMqTestEnvironmentBuilder withExchange(String exchange){
        ensureInitialized();
        environment.addExchange(exchange);
        return this;
    }
    
    public QueueParameterBuilder withQueue(String queueName){
        ensureInitialized();
        QueueParameterBuilder builder = new QueueParameterBuilder(this);
        return builder.queue(queueName);
    }
    
    public RabbitMqTestEnvironmentBuilder withQueue(QueueParameter queueParameter){
        ensureInitialized();
        environment.addQueue(queueParameter);
        return this;
    }
    
    public RabbitMqTestEnvironment build(){
        ensureInitialized();
        RabbitMqTestEnvironment result = environment;
        environment = null;
        return result;
    }
    
    private void ensureInitialized(){
        if(environment == null){
            environment = new RabbitMqTestEnvironment(connector);
        }
    }

}
