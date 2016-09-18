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
    
    private final RabbitMqFacade facade;
    
    @Autowired
    public QueueController(RabbitMqFacade facade) {
        this.facade = facade;
    }

    @GetMapping("/queues")
    public List<Queue> getQueues() {
        return facade.getQueues();
    }

    @GetMapping("/queues/{queue}/messages")
    public List<Message> getQueueMessages(@PathVariable String queue) {
        return facade.getMessagesOfQueue(queue, 10);
    }
    
    @RequestMapping(value = "/queues/{queue}/messages", method = RequestMethod.DELETE)
    public void deleteFirstMessageInQueue(@PathVariable String queue, @RequestParam(value="checksum", required=false) String checksum){
        facade.deleteFirstMessageInQueue(queue, checksum);
    }
    
    @RequestMapping(value = "/queues/{queue}/messages/move", method = RequestMethod.POST)
    public void deleteFirstMessageInQueue(@PathVariable String queue, 
            @RequestParam(value="checksum", required=false) String checksum,
            @RequestParam(value="targetExchange", required=false) String targetExchange,
            @RequestParam(value="targetRoutingKey", required=false) String targetRoutingKey){
        facade.moveFirstMessageInQueue(queue, checksum, targetExchange, targetRoutingKey);
    }
}
