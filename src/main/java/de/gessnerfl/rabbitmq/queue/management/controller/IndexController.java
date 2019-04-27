package de.gessnerfl.rabbitmq.queue.management.controller;

import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.RabbitMqFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
    
    private final RabbitMqFacade rabbitMqFacade;
    
    @Autowired
    public IndexController(RabbitMqFacade rabbitMqFacade) {
        this.rabbitMqFacade = rabbitMqFacade;
    }

    @GetMapping({"/", "/index"})
	public String index(Model model) {
        model.addAttribute("queues", rabbitMqFacade.getQueues());
        return "index";
    }
	
}
