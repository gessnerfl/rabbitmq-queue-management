package de.gessnerfl.rabbitmq.queue.management.controller;

public enum Pages {

    INDEX("/index"),
    MESSAGES("/messages");

    private String path;
    private Pages(String path){
        this.path=path;
    }

    public String redirectTo(){
        return "redirect:" + path;
    }
}
