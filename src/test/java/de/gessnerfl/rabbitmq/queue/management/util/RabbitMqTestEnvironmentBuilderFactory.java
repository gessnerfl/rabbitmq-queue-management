package de.gessnerfl.rabbitmq.queue.management.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.gessnerfl.rabbitmq.queue.management.connection.Connector;

@Service
public class RabbitMqTestEnvironmentBuilderFactory {

    private final Connector connector;
    
    @Autowired
    public RabbitMqTestEnvironmentBuilderFactory(Connector connector){
        this.connector = connector;
    }
    
    public RabbitMqTestEnvironmentBuilder create(){
        return new RabbitMqTestEnvironmentBuilder(connector);
    }
}
