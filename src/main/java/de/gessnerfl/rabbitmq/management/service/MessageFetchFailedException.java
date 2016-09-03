package de.gessnerfl.rabbitmq.management.service;

public class MessageFetchFailedException extends RuntimeException {

	private static final long serialVersionUID = 6581356205487963189L;

	public MessageFetchFailedException(Exception cause){
		super("Failed to fetch messages from RabbitMQ", cause);
	}
	
}
