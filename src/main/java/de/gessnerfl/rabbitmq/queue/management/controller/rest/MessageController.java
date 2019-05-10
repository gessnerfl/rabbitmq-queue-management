package de.gessnerfl.rabbitmq.queue.management.controller.rest;

import de.gessnerfl.rabbitmq.queue.management.model.Message;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.RabbitMqFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static de.gessnerfl.rabbitmq.queue.management.controller.rest.QueryParameters.*;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    public static final int DEFAULT_LIMIT = 10;

    private final RabbitMqFacade facade;

    @Autowired
    public MessageController(RabbitMqFacade facade) {
        this.facade = facade;
    }

    @GetMapping
    public List<Message> getQueueMessages(@RequestParam(name = VHOST, required = true) String vhost,
                                          @RequestParam(name = QUEUE, required = true) String queue) {
        return facade.getMessagesOfQueue(vhost, queue, DEFAULT_LIMIT);
    }

    @DeleteMapping
    public void deleteAllMessageInQueue(@RequestParam(name = VHOST, required = true) String vhost,
                                        @RequestParam(name = QUEUE, required = true) String queue) {
        facade.purgeQueue(vhost, queue);
    }

    @DeleteMapping("/{checksum}")
    public void deleteFirstMessageInQueue(@RequestParam(name = VHOST, required = true) String vhost,
                                          @RequestParam(name = QUEUE, required = true) String queue,
                                          @PathVariable(value = CHECKSUM, required = true) String checksum) {
        facade.deleteFirstMessageInQueue(vhost, queue, checksum);
    }

    @PostMapping("/move")
    public void moveAllMessageInQueue(@RequestParam(name = VHOST, required = true) String vhost,
                                      @RequestParam(name = QUEUE, required = true) String queue,
                                      @RequestParam(value = TARGET_EXCHANGE, required = true) String targetExchange,
                                      @RequestParam(value = TARGET_ROUTING_KEY, required = true) String targetRoutingKey) {
        facade.moveAllMessagesInQueue(vhost, queue, targetExchange, targetRoutingKey);
    }

    @PostMapping("/{checksum}/move")
    public void moveFirstMessageInQueue(@RequestParam(name = VHOST, required = true) String vhost,
                                        @RequestParam(name = QUEUE, required = true) String queue,
                                        @PathVariable(value = CHECKSUM, required = true) String checksum,
                                        @RequestParam(value = TARGET_EXCHANGE, required = true) String targetExchange,
                                        @RequestParam(value = TARGET_ROUTING_KEY, required = true) String targetRoutingKey) {
        facade.moveFirstMessageInQueue(vhost, queue, checksum, targetExchange, targetRoutingKey);
    }

    @PostMapping("/requeue")
    public void requeueAllMessageInQueue(@RequestParam(name = VHOST, required = true) String vhost,
                                         @RequestParam(name = QUEUE, required = true) String queue) {
        facade.requeueAllMessagesInQueue(vhost, queue);
    }

    @PostMapping("/{checksum}/requeue")
    public void requeueFirstMessageInQueue(@RequestParam(name = VHOST, required = true) String vhost,
                                           @RequestParam(name = QUEUE, required = true) String queue,
                                           @PathVariable(value = CHECKSUM, required = true) String checksum) {
        facade.requeueFirstMessageInQueue(vhost, queue, checksum);
    }
}
