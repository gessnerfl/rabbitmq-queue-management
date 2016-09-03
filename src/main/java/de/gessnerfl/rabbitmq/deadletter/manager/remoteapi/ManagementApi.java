package de.gessnerfl.rabbitmq.deadletter.manager.remoteapi;

import java.util.List;

import de.gessnerfl.rabbitmq.deadletter.manager.remoteapi.model.Exchange;
import de.gessnerfl.rabbitmq.deadletter.manager.remoteapi.model.Queue;
import feign.RequestLine;


public interface ManagementApi {

  @RequestLine("GET /exchanges")
  List<Exchange> getExchanges();

  @RequestLine("GET /queues")
  List<Queue> getQueues();

}
