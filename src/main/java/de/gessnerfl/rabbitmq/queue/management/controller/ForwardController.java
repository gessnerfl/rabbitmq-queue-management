package de.gessnerfl.rabbitmq.queue.management.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ForwardController {
    
    @GetMapping(value = "/**/{[path:[^\\.]*}")
    public String forward() {
        return "forward:/";
    }
	
}
