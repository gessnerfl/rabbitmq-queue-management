package de.gessnerfl.rabbitmq.queue.management.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.config.RabbitMqBrokers;

@Controller
public class IndexController {
    
    private final RabbitMqBrokers brokers;
    
    @Autowired
    public IndexController(RabbitMqBrokers brokers) {
        this.brokers = brokers;
    }

    @RequestMapping({"/", "/index"})
	public String index(Model model) {
        model.addAttribute("brokers", brokers.getBrokerDescriptors());
        return "index";
    }
	
}
