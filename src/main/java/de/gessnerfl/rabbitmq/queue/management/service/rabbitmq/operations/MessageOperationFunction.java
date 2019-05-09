package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;

@FunctionalInterface
public interface MessageOperationFunction {

    void apply(OperationId operationId, Channel channel, GetResponse message) throws IOException, TimeoutException, InterruptedException;
    
}
