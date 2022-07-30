package de.gessnerfl.rabbitmq.queue.management.controller;

public enum Pages {

    INDEX("/index"),
    MESSAGES("/messages");

    private String path;

    Pages(String path){
        this.path=path;
    }

    public String getRedirectString(){
        return "redirect:" + path;
    }

}
