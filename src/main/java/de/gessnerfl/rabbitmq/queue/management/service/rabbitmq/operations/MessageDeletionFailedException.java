package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

public class MessageDeletionFailedException extends RuntimeException {
    private static final String MESSAGE = "Failed to delete message";
    private static final long serialVersionUID = -2237214376121400828L;

    public MessageDeletionFailedException(String details){
        super(MESSAGE+": "+details);
    }
    
    public MessageDeletionFailedException(Throwable cause){
        super(MESSAGE, cause);
    }
}
