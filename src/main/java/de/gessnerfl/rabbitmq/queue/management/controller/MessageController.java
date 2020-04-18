package de.gessnerfl.rabbitmq.queue.management.controller;

import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.RabbitMqFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/messages")
public class MessageController {
    public static final String VIEW_NAME = "messages";
    private final RabbitMqFacade facade;
    public static final int DEFAULT_LIMIT = 10;

    @Autowired
    public MessageController(RabbitMqFacade facade) {
        this.facade = facade;
    }

    @GetMapping
    public String getMessagePage(@RequestParam(Parameters.VHOST) String vhost,
                                 @RequestParam(Parameters.QUEUE) String queue,
                                 Model model){
        model.addAttribute(Parameters.VHOST, vhost);
        model.addAttribute(Parameters.QUEUE, queue);
        model.addAttribute(Parameters.MESSAGES, facade.getMessagesOfQueue(vhost, queue, DEFAULT_LIMIT));
        return VIEW_NAME;
    }
}
