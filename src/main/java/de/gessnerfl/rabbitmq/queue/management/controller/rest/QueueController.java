package de.gessnerfl.rabbitmq.queue.management.controller.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import de.gessnerfl.rabbitmq.queue.management.model.Message;
import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Queue;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.RabbitMqFacade;

import static de.gessnerfl.rabbitmq.queue.management.controller.rest.QueryParameters.*;

@RestController
public class QueueController {
    
    public static final int DEFAULT_LIMIT = 10;
    
    private final RabbitMqFacade facade;
    
    @Autowired
    public QueueController(RabbitMqFacade facade) {
        this.facade = facade;
    }

    @GetMapping("/api/queues")
    public List<Queue> getQueues(@RequestParam(name = VHOST, required = false) String vhost) {
        if(StringUtils.hasText(vhost)){
            return facade.getQueues(vhost);
        }
        return facade.getQueues();
    }

    @GetMapping("/api/messages")
    public List<Message> getQueueMessages(@RequestParam(name = VHOST, required = true) String vhost, @RequestParam(name = QUEUE, required = true) String queue) {
        return facade.getMessagesOfQueue(vhost, queue, DEFAULT_LIMIT);
    }
    
    @RequestMapping(value = "/api/messages", method = RequestMethod.DELETE)
    public void deleteFirstMessageInQueue(@RequestParam(name = VHOST, required = true) String vhost, @RequestParam(name = QUEUE, required = true) String queue, @RequestParam(value=CHECKSUM, required=true) String checksum){
        facade.deleteFirstMessageInQueue(vhost, queue, checksum);
    }
    
    @RequestMapping(value = "/api/messages/move", method = RequestMethod.POST)
    public void moveFirstMessageInQueue(@RequestParam(name = VHOST, required = true) String vhost,
                                        @RequestParam(name = QUEUE, required = true) String queue,
                                        @RequestParam(value="checksum", required=true) String checksum,
                                        @RequestParam(value="targetExchange", required=true) String targetExchange,
                                        @RequestParam(value="targetRoutingKey", required=true) String targetRoutingKey){
        facade.moveFirstMessageInQueue(vhost, queue, checksum, targetExchange, targetRoutingKey);
    }
}
