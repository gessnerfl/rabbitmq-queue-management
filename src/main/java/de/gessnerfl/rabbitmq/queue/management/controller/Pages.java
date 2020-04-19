package de.gessnerfl.rabbitmq.queue.management.controller;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public enum Pages {

    INDEX("/index"),
    MESSAGES("/messages");

    private String path;
    private Pages(String path){
        this.path=path;
    }

    public String path(){
        return path;
    }

    public String redirectTo(){
        return "redirect:" + path;
    }

    public String redirectTo(String vhost, String queue, RedirectAttributes redirectAttributes){
        redirectAttributes.addAttribute(Parameters.VHOST, vhost);
        redirectAttributes.addAttribute(Parameters.QUEUE, queue);
        return redirectTo();
    }
}
