package de.gessnerfl.rabbitmq.queue.management.controller;

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

@Controller
@RequestMapping("/messages/delete-all")
public class DeleteAllMessagesController {

    public static final String VIEW_NAME = "messages-delete-all";

    private final RabbitMqFacade facade;
    private final Logger logger;

    @Autowired
    public DeleteAllMessagesController(RabbitMqFacade facade, Logger logger) {
        this.facade = facade;
        this.logger = logger;
    }

    @GetMapping
    public String getDeleteAllMessagePage(@RequestParam(Parameters.VHOST) String vhost,
                                          @RequestParam(Parameters.QUEUE) String queue,
                                          Model model){
        ParameterAppender.of(model)
                .vhost(vhost)
                .queue(queue);
        return VIEW_NAME;
    }

    @PostMapping
    public String deleteAllMessages(@RequestParam(Parameters.VHOST) String vhost,
                                    @RequestParam(Parameters.QUEUE) String queue,
                                    Model model,
                                    RedirectAttributes redirectAttributes){
        try {
            facade.purgeQueue(vhost, queue);
            return Pages.MESSAGES.redirectTo(vhost,queue, redirectAttributes);
        } catch (Exception e) {
            logger.error("Failed to delete all messages or queue {} of vhost {}", queue, vhost, e);
            ParameterAppender.of(model)
                    .vhost(vhost)
                    .queue(queue)
                    .errorMessage(e.getMessage());
            return VIEW_NAME;
        }
    }
}
