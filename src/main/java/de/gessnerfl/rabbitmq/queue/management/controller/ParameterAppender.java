package de.gessnerfl.rabbitmq.queue.management.controller;

import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Exchange;
import org.springframework.ui.Model;

import java.util.List;

public class ParameterAppender {

    public static ParameterAppender of(Model model) {
        return new ParameterAppender(model);
    }

    private final Model model;

    private ParameterAppender(Model model) {
        this.model = model;
    }

    public ParameterAppender vhost(String vhost){
        model.addAttribute(Parameters.VHOST, vhost);
        return this;
    }

    public ParameterAppender queue(String queue){
        model.addAttribute(Parameters.QUEUE, queue);
        return this;
    }

    public ParameterAppender checksum(String checksum){
        model.addAttribute(Parameters.CHECKSUM, checksum);
        return this;
    }

    public ParameterAppender targetExchange(String targetExchange){
        model.addAttribute(Parameters.TARGET_EXCHANGE, targetExchange);
        return this;
    }

    public ParameterAppender exchanges(List<Exchange> exchanges){
        model.addAttribute(Parameters.EXCHANGES, exchanges);
        return this;
    }

    public ParameterAppender targetRoutingKey(String targetRoutingKey){
        model.addAttribute(Parameters.TARGET_ROUTING_KEY, targetRoutingKey);
        return this;
    }

    public ParameterAppender routingKeys(List<String> routingKeys){
        model.addAttribute(Parameters.ROUTING_KEYS, routingKeys);
        return this;
    }

    public ParameterAppender errorMessage(String errorMessage){
        model.addAttribute(Parameters.ERROR_MESSAGE, errorMessage);
        return this;
    }

}
