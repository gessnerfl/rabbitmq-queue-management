package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.ReturnListener;
import org.slf4j.Logger;

import java.io.IOException;

class StateKeepingReturnListener implements ReturnListener {
    private final String operation;
    private final Logger logger;

    private boolean received;

    StateKeepingReturnListener(String operation, Logger logger) {
        this.operation = operation;
        this.logger = logger;
    }

    @Override
    public void handleReturn(int replyCode, String replyText, String exchange, String routingKey, AMQP.BasicProperties properties, byte[] body) throws IOException {
        logger.error("basic.return received for {} operation: exchange={}, routingKey={}, replyCode={}, replyText={}", operation, exchange, routingKey, replyCode, replyText);
        received = true;
    }

    boolean isReceived(){
        return received;
    }
}
