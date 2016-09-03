package de.gessnerfl.rabbitmq.deadletter.manager.connection;

public class ConnectionFailedException extends RuntimeException {

	private static final long serialVersionUID = -3401228640092783448L;

	public ConnectionFailedException(Exception cause){
		super("Failed to establish connection to RabbitMQ", cause);
	}
}
