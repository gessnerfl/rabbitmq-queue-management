package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.remoteapi;

import java.util.List;

import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Binding;
import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Exchange;
import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Queue;
import feign.Param;
import feign.RequestLine;


public interface ManagementApi {

  @RequestLine("GET /exchanges")
  List<Exchange> getExchanges();

  @RequestLine("GET /queues")
  List<Queue> getQueues();

  @RequestLine(value="GET /queues/{vhost}/{queue}/bindings", decodeSlash = false)
  List<Binding> getBindings(@Param("vhost") String vhost, @Param("queue") String queue);
}
