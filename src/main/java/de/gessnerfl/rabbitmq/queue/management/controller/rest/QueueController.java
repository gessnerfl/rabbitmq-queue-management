package de.gessnerfl.rabbitmq.queue.management.controller.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Queue;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.RabbitMqFacade;

import static de.gessnerfl.rabbitmq.queue.management.controller.rest.QueryParameters.*;

@RestController
@RequestMapping("/api/queues")
public class QueueController {

    private final RabbitMqFacade facade;

    @Autowired
    public QueueController(RabbitMqFacade facade) {
        this.facade = facade;
    }

    @GetMapping
    public List<Queue> getQueues(@RequestParam(name = VHOST, required = false) String vhost) {
        if (StringUtils.hasText(vhost)) {
            return facade.getQueues(vhost);
        }
        return facade.getQueues();
    }
}
