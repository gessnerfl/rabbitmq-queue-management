package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

import com.rabbitmq.client.AMQP;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.utils.MessageHeaderModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageMoveOperation {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageMoveOperation.class);
    private static final long MAX_WAIT_FOR_CONFIRM = 5000;
    static final String MOVE_COUNT_HEADER = "x-rmqmgmt-move-count";

    private final MessageOperationExecutor messageOperationExecutor;
    private final MessageHeaderModifier messageHeaderModifier;

    @Autowired
    public MessageMoveOperation(MessageOperationExecutor messageOperationExecutor, MessageHeaderModifier messageHeaderModifier) {
        this.messageOperationExecutor = messageOperationExecutor;
        this.messageHeaderModifier = messageHeaderModifier;
    }
    
    public void moveFirstMessage(String vhost, String queueName, String checksum, String targetExchange, String targetRoutingKey){
        messageOperationExecutor.consumeMessageApplyFunctionAndAckknowlegeOnSuccess(vhost, queueName, checksum, (channel,response) -> {
            StateKeepingReturnListener returnListener = new StateKeepingReturnListener("move", LOGGER);
            channel.addReturnListener(returnListener);
            channel.confirmSelect();

            AMQP.BasicProperties props = messageHeaderModifier.incrementCounter(response.getProps(), MOVE_COUNT_HEADER);
            channel.basicPublish(targetExchange, targetRoutingKey, true, props, response.getBody());
            channel.waitForConfirmsOrDie(MAX_WAIT_FOR_CONFIRM);
            if(returnListener.isReceived()){
                throw new MessageOperationFailedException("Move failed, basic.return received");
            }
            
            channel.removeReturnListener(returnListener);
        });
    }

}
