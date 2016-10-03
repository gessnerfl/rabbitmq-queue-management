package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.config;

public class NoBrokersAvailableException extends RuntimeException {

    private static final long serialVersionUID = 7672432350863983181L;

    public NoBrokersAvailableException(){
        super("There are not brokers available; check your configuration");
    }
}
