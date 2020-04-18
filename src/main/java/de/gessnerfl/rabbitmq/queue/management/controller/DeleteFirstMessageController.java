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
@RequestMapping("/messages/delete-first")
public class DeleteFirstMessageController {

    public static final String VIEW_NAME = "messages-delete-first";
    
    private final RabbitMqFacade facade;
    private final Logger logger;

    @Autowired
    public DeleteFirstMessageController(RabbitMqFacade facade, Logger logger) {
        this.facade = facade;
        this.logger = logger;
    }

    @GetMapping
    public String getDeleteFirstMessagePage(@RequestParam(Parameters.VHOST) String vhost,
                                            @RequestParam(Parameters.QUEUE) String queue,
                                            @RequestParam(Parameters.CHECKSUM) String checksum,
                                            Model model){
        ParameterAppender.of(model)
                .vhost(vhost)
                .queue(queue)
                .checksum(checksum);
        return VIEW_NAME;
    }

    @PostMapping
    public String deleteFirstMessage(@RequestParam(Parameters.VHOST) String vhost,
                                     @RequestParam(Parameters.QUEUE) String queue,
                                     @RequestParam(Parameters.CHECKSUM) String checksum,
                                     Model model,
                                     RedirectAttributes redirectAttributes){
        try {
            facade.deleteFirstMessageInQueue(vhost, queue, checksum);
            return Pages.MESSAGES.redirectTo(vhost,queue, redirectAttributes);
        } catch (Exception e) {
            logger.error("Failed to delete first message with checksum {} from queue {} of vhost {}", checksum, queue, vhost, e);
            ParameterAppender.of(model)
                    .vhost(vhost)
                    .queue(queue)
                    .checksum(checksum)
                    .errorMessage(e.getMessage());
            return VIEW_NAME;
        }
    }
}
