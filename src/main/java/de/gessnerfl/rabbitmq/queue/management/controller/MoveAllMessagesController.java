package de.gessnerfl.rabbitmq.queue.management.controller;

import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Binding;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.RabbitMqFacade;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/messages/move-all")
public class MoveAllMessagesController {

    public static final String VIEW_NAME = "messages-move-all";

    private final RabbitMqFacade facade;
    private final Logger logger;

    @Autowired
    public MoveAllMessagesController(RabbitMqFacade facade, Logger logger) {
        this.facade = facade;
        this.logger = logger;
    }

    @GetMapping
    public String getMoveAllMessagePage(@RequestParam(Parameters.VHOST) String vhost,
                                        @RequestParam(Parameters.QUEUE) String queue,
                                        Model model){
        model.addAttribute(Parameters.VHOST, vhost);
        model.addAttribute(Parameters.QUEUE, queue);
        model.addAttribute(Parameters.EXCHANGES, facade.getExchanges(vhost));
        return VIEW_NAME;
    }

    @PostMapping
    public String moveAllMessages(@RequestParam(Parameters.VHOST) String vhost,
                                  @RequestParam(Parameters.QUEUE) String queue,
                                  @RequestParam(Parameters.TARGET_EXCHANGE) String targetExchange,
                                  @RequestParam(name = Parameters.TARGET_ROUTING_KEY, required = false) String targetRoutingKey,
                                  Model model,
                                  RedirectAttributes redirectAttributes){
        if(!StringUtils.hasText(targetRoutingKey)){
            return showViewWithRoutingKeysForTargetExchange(vhost, queue, targetExchange, model);
        }

        try {
            facade.moveAllMessagesInQueue(vhost, queue, targetExchange, targetRoutingKey);
            return MessagesControllers.redirectToMessagesPage(vhost,queue, redirectAttributes);
        } catch (Exception e) {
            logger.error("Failed to move all messages from queue {} of vhost {} to exchange {} with routing key {}", queue, vhost, targetExchange, targetRoutingKey, e);
            model.addAttribute(Parameters.VHOST, vhost);
            model.addAttribute(Parameters.QUEUE, queue);
            model.addAttribute(Parameters.TARGET_EXCHANGE, targetExchange);
            model.addAttribute(Parameters.TARGET_ROUTING_KEY, targetRoutingKey);
            model.addAttribute(Parameters.ERROR_MESSAGE, e.getMessage());
            return VIEW_NAME;
        }
    }

    private String showViewWithRoutingKeysForTargetExchange(@RequestParam(Parameters.VHOST) String vhost, @RequestParam(Parameters.QUEUE) String queue, @RequestParam(Parameters.TARGET_EXCHANGE) String targetExchange, Model model) {
        List<String> routingKeys = facade.getExchangeSourceBindings(vhost, targetExchange)
                .stream()
                .map(Binding::getRoutingKey)
                .distinct()
                .collect(Collectors.toList());
        model.addAttribute(Parameters.VHOST, vhost);
        model.addAttribute(Parameters.QUEUE, queue);
        model.addAttribute(Parameters.TARGET_EXCHANGE, targetExchange);
        model.addAttribute(Parameters.ROUTING_KEYS, routingKeys);
        return VIEW_NAME;
    }
}
