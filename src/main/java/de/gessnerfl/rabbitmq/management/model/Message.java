package de.gessnerfl.rabbitmq.management.model;

import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP.BasicProperties;

public class Message {
	private final Envelope envelope;
	private final BasicProperties properties;
	private final byte[] body;
	
	public Message(Envelope envelope, BasicProperties properties, byte[] body) {
		this.envelope = envelope;
		this.properties = properties;
		this.body = body;
	}

	public Envelope getEnvelope() {
		return envelope;
	}

	public BasicProperties getProperties() {
		return properties;
	}

	public byte[] getBody() {
		return body;
	}
	
}
