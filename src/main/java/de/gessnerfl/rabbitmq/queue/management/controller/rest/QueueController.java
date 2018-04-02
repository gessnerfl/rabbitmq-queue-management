package de.gessnerfl.rabbitmq.queue.management.controller.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.gessnerfl.rabbitmq.queue.management.model.Message;
import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Queue;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.RabbitMqFacade;

@RestController
public class QueueController {
    
    public static final int DEFAULT_LIMIT = 10;
    
    private final RabbitMqFacade facade;
    
    @Autowired
    public QueueController(RabbitMqFacade facade) {
        this.facade = facade;
    }

    @GetMapping("/api/{broker}/queues")
    public List<Queue> getQueues(@PathVariable String broker) {
        return facade.getQueues(broker);
    }

    @GetMapping("/api/{broker}/queues/{queue}/messages")
    public List<Message> getQueueMessages(@PathVariable String broker, @PathVariable String queue) {
        return facade.getMessagesOfQueue(broker, queue, DEFAULT_LIMIT);
    }
    
    @RequestMapping(value = "/api/{broker}/queues/{queue}/messages", method = RequestMethod.DELETE)
    public void deleteFirstMessageInQueue(@PathVariable String broker, @PathVariable String queue, @RequestParam(value="checksum", required=false) String checksum){
        facade.deleteFirstMessageInQueue(broker, queue, checksum);
    }
    
    @RequestMapping(value = "/api/{broker}/queues/{queue}/messages/move", method = RequestMethod.POST)
    public void moveFirstMessageInQueue(@PathVariable String broker, @PathVariable String queue, 
            @RequestParam(value="checksum", required=false) String checksum,
            @RequestParam(value="targetExchange", required=false) String targetExchange,
            @RequestParam(value="targetRoutingKey", required=false) String targetRoutingKey){
        facade.moveFirstMessageInQueue(broker, queue, checksum, targetExchange, targetRoutingKey);
    }
}
