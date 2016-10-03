package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.config;

public class InvalidBrokerNameException extends RuntimeException {
    
    private static final long serialVersionUID = 488589496489442991L;

    public InvalidBrokerNameException(String name){
        super(name+" is not a valid broker name");
    }

}
