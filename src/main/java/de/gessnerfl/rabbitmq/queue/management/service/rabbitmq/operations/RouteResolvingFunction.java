package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

import com.rabbitmq.client.GetResponse;

@FunctionalInterface
public interface RouteResolvingFunction {
    RoutingDetails resolve(GetResponse message);
}
