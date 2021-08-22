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

    public String getRedirectString(){
        return "redirect:" + path;
    }

}
