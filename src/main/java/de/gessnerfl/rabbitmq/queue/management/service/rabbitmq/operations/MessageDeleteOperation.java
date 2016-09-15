package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageDeleteOperation {
    private final MessageOperationExecutor messageOperationExecutor;

    @Autowired
    public MessageDeleteOperation(MessageOperationExecutor messageOperationExecutor) {
        this.messageOperationExecutor = messageOperationExecutor;
    }

    public void deleteFirstMessageInQueue(String queue, String messageChecksum) {
        messageOperationExecutor.consumeMessageApplyFunctionAndAckknowlegeOnSuccess(queue, messageChecksum, (c, r) -> {});
    }
}
