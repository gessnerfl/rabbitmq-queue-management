package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageMoveOperation {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageMoveOperation.class);
    private static final long MAX_WAIT_FOR_CONFIRM = 5000;
    private final MessageOperationExecutor messageOperationExecutor;

    @Autowired
    public MessageMoveOperation(MessageOperationExecutor messageOperationExecutor) {
        this.messageOperationExecutor = messageOperationExecutor;
    }
    
    public void moveFirstMessage(String vhost, String queueName, String checksum, String targetExchange, String targetRoutingKey){
        messageOperationExecutor.consumeMessageApplyFunctionAndAckknowlegeOnSuccess(vhost, queueName, checksum, (channel,response) -> {
            StateKeepingReturnListener returnListener = new StateKeepingReturnListener("move", LOGGER);
            channel.addReturnListener(returnListener);
            channel.confirmSelect();
            
            channel.basicPublish(targetExchange, targetRoutingKey, true, response.getProps(), response.getBody());
            channel.waitForConfirmsOrDie(MAX_WAIT_FOR_CONFIRM);
            if(returnListener.isReceived()){
                throw new MessageOperationFailedException("Move failed, basic.return received");
            }
            
            channel.removeReturnListener(returnListener);
        });
    }

}
