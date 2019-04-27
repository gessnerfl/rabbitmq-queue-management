package de.gessnerfl.rabbitmq.queue.management.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class QueueManagementController {

    @RequestMapping({"/queue-management"})
    public String index(Model model) {
        return "queue-management";
    }
    
}
