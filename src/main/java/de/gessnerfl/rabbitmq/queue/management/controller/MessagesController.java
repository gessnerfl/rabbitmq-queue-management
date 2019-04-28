package de.gessnerfl.rabbitmq.queue.management.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MessagesController {

    @GetMapping({"/messages"})
    public String index(Model model) {
        return "messages";
    }
    
}
