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
        if (StringUtils.hasText(vhost)) {
            return facade.getQueues(vhost);
        }
        return facade.getQueues();
    }

    @GetMapping("/api/messages")
    public List<Message> getQueueMessages(@RequestParam(name = VHOST, required = true) String vhost,
                                          @RequestParam(name = QUEUE, required = true) String queue) {
        return facade.getMessagesOfQueue(vhost, queue, DEFAULT_LIMIT);
    }

    @DeleteMapping("/api/messages")
    public void deleteAllMessageInQueue(@RequestParam(name = VHOST, required = true) String vhost,
                                        @RequestParam(name = QUEUE, required = true) String queue) {
        facade.purgeQueue(vhost, queue);
    }

    @DeleteMapping("/api/messages/{checksum}")
    public void deleteFirstMessageInQueue(@RequestParam(name = VHOST, required = true) String vhost,
                                          @RequestParam(name = QUEUE, required = true) String queue,
                                          @PathVariable(value = CHECKSUM, required = true) String checksum) {
        facade.deleteFirstMessageInQueue(vhost, queue, checksum);
    }

    @PostMapping("/api/messages/move")
    public void moveAllMessageInQueue(@RequestParam(name = VHOST, required = true) String vhost,
                                      @RequestParam(name = QUEUE, required = true) String queue,
                                      @RequestParam(value = TARGET_EXCHANGE, required = true) String targetExchange,
                                      @RequestParam(value = TARGET_ROUTING_KEY, required = true) String targetRoutingKey) {
        facade.moveAllMessagesInQueue(vhost, queue, targetExchange, targetRoutingKey);
    }

    @PostMapping("/api/messages/{checksum}/move")
    public void moveFirstMessageInQueue(@RequestParam(name = VHOST, required = true) String vhost,
                                        @RequestParam(name = QUEUE, required = true) String queue,
                                        @PathVariable(value = CHECKSUM, required = true) String checksum,
                                        @RequestParam(value = TARGET_EXCHANGE, required = true) String targetExchange,
                                        @RequestParam(value = TARGET_ROUTING_KEY, required = true) String targetRoutingKey) {
        facade.moveFirstMessageInQueue(vhost, queue, checksum, targetExchange, targetRoutingKey);
    }

    @PostMapping("/api/messages/requeue")
    public void requeueAllMessageInQueue(@RequestParam(name = VHOST, required = true) String vhost,
                                         @RequestParam(name = QUEUE, required = true) String queue) {
        facade.requeueAllMessagesInQueue(vhost, queue);
    }

    @PostMapping("/api/messages/{checksum}/requeue")
    public void requeueFirstMessageInQueue(@RequestParam(name = VHOST, required = true) String vhost,
                                           @RequestParam(name = QUEUE, required = true) String queue,
                                           @PathVariable(value = CHECKSUM, required = true) String checksum) {
        facade.requeueFirstMessageInQueue(vhost, queue, checksum);
    }
}
