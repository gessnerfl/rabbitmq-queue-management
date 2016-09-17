package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.remoteapi;

import java.util.List;

import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Binding;
import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Exchange;
import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Queue;
import feign.Param;
import feign.RequestLine;


public interface ManagementApi {

    @RequestLine(value = "GET /exchanges/{vhost}", decodeSlash = false)
    List<Exchange> getExchanges(@Param("vhost") String vhost);

    @RequestLine(value = "GET /exchanges/{vhost}/{exchange}/bindings/source", decodeSlash = false)
    List<Binding> getExchangeSourceBindings(@Param("vhost") String vhost,
            @Param("exchange") String exchange);

    @RequestLine(value = "GET /queues/{vhost}", decodeSlash = false)
    List<Queue> getQueues(@Param("vhost") String vhost);

    @RequestLine(value = "GET /queues/{vhost}/{queue}/bindings", decodeSlash = false)
    List<Binding> getQueueBindings(@Param("vhost") String vhost, @Param("queue") String queue);
}
