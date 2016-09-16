package de.gessnerfl.rabbitmq.queue.management.remoteapi;

import java.util.List;

import de.gessnerfl.rabbitmq.queue.management.remoteapi.model.Binding;
import de.gessnerfl.rabbitmq.queue.management.remoteapi.model.Exchange;
import de.gessnerfl.rabbitmq.queue.management.remoteapi.model.Queue;
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
