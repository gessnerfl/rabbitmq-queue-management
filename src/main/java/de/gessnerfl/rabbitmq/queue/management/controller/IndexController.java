package de.gessnerfl.rabbitmq.queue.management.controller;

import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.RabbitMqFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
    private final RabbitMqFacade facade;

    @Autowired
    public IndexController(RabbitMqFacade facade) {
        this.facade = facade;
    }

    @GetMapping("/")
    public String redirectToIndex(){
        return "redirect:/index";
    }

    @GetMapping("/index")
    public String getIndexPage(Model model){
        model.addAttribute("queues", facade.getQueues());
        return "index";
    }

}
