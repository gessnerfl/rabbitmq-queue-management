package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

public class MessageOperationFailedException extends RuntimeException {
    private static final String MESSAGE = "Failed to perform operation on message";
    private static final long serialVersionUID = -2237214376121400828L;

    public MessageOperationFailedException(String details){
        super(MESSAGE+": "+details);
    }
    
    public MessageOperationFailedException(Throwable cause){
        super(MESSAGE, cause);
    }
}
