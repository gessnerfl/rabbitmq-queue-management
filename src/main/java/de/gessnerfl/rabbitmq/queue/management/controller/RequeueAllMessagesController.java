package de.gessnerfl.rabbitmq.queue.management.controller;

import de.gessnerfl.rabbitmq.queue.management.model.Message;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.RabbitMqFacade;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/messages/requeue-all")
public class RequeueAllMessagesController {

    public static final String VIEW_NAME = "messages-requeue-all";
    private final RabbitMqFacade facade;
    private final Logger logger;

    @Autowired
    public RequeueAllMessagesController(RabbitMqFacade facade, Logger logger) {
        this.facade = facade;
        this.logger = logger;
    }

    @GetMapping
    public String getRequeueAllMessagePage(@RequestParam(Parameters.VHOST) String vhost,
                                           @RequestParam(Parameters.QUEUE) String queue,
                                           Model model,
                                           RedirectAttributes redirectAttributes){
        List<Message> messages = facade.getMessagesOfQueue(vhost, queue, 1);
        if(messages.isEmpty() || !messages.get(0).isRequeueAllowed()){
            return Pages.MESSAGES.appendRedirectAttributesAndGetRedirectString(vhost,queue, redirectAttributes);
        }
        Message message = messages.get(0);
        ParameterAppender.of(model)
                .vhost(vhost)
                .queue(queue)
                .targetExchange(message.getRequeueDetails().getExchangeName())
                .targetRoutingKey(message.getRequeueDetails().getRoutingKey());
        return VIEW_NAME;
    }

    @PostMapping
    public String requeueAllMessages(@RequestParam(Parameters.VHOST) String vhost,
                                     @RequestParam(Parameters.QUEUE) String queue,
                                     @RequestParam(Parameters.TARGET_EXCHANGE) String targetExchange,
                                     @RequestParam(Parameters.TARGET_ROUTING_KEY) String targetRoutingKey,
                                     Model model,
                                     RedirectAttributes redirectAttributes){
        try {
            facade.requeueAllMessagesInQueue(vhost, queue);
            return Pages.MESSAGES.appendRedirectAttributesAndGetRedirectString(vhost,queue, redirectAttributes);
        } catch (Exception e) {
            logger.error("Failed to requeue all messages from queue {} of vhost {} to target exchange {} and routing key {}", queue, vhost, targetExchange, targetRoutingKey, e);
            ParameterAppender.of(model)
                    .vhost(vhost)
                    .queue(queue)
                    .targetExchange(targetExchange)
                    .targetRoutingKey(targetRoutingKey)
                    .errorMessage(e.getMessage());
            return VIEW_NAME;
        }
    }
}
