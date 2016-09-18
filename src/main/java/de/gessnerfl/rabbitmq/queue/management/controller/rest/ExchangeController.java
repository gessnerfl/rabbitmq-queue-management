package de.gessnerfl.rabbitmq.queue.management.controller.rest;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Exchange;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.RabbitMqFacade;

@RestController
public class ExchangeController {

    private final RabbitMqFacade facade;

    @Autowired
    public ExchangeController(RabbitMqFacade facade) {
        this.facade = facade;
    }

    @GetMapping("/exchanges")
    public List<Exchange> getExchanges() {
        return facade.getExchanges();
    }

    @GetMapping("/exchanges/{exchange}/routingKeys")
    public List<String> getSourceBindings(@PathVariable String exchange) {
        return facade.getExchangeSourceBindings(exchange)
                .stream()
                .map(b -> b.getRoutingKey())
                .distinct()
                .collect(Collectors.toList());
    }

}
