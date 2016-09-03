package de.gessnerfl.rabbitmq.management.service;

public class FailedToReadSettingsException extends RuntimeException {

	private static final long serialVersionUID = 3467680734760348229L;

	public FailedToReadSettingsException(){
		this(null);
	}
	
	public FailedToReadSettingsException(Exception cause){
		super("Failed to load settings", cause);
	}
}
