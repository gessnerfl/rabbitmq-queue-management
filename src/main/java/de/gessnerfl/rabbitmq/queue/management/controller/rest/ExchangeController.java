package de.gessnerfl.rabbitmq.queue.management.controller.rest;

import java.util.List;
import java.util.stream.Collectors;

import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Binding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Exchange;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.RabbitMqFacade;

import static de.gessnerfl.rabbitmq.queue.management.controller.rest.QueryParameters.*;

@RestController
public class ExchangeController {

    private final RabbitMqFacade facade;

    @Autowired
    public ExchangeController(RabbitMqFacade facade) {
        this.facade = facade;
    }

    @GetMapping("/api/exchanges")
    public List<Exchange> getExchanges(@RequestParam(name = VHOST, required = true) String vhost) {
        return facade.getExchanges(vhost);
    }

    @GetMapping("/api/routingKeys")
    public List<String> getSourceBindings(@RequestParam(name = VHOST, required = true) String vhost, @RequestParam(name = EXCHANGE, required = true) String exchange) {
        return facade.getExchangeSourceBindings(vhost, exchange)
                .stream()
                .map(Binding::getRoutingKey)
                .distinct()
                .collect(Collectors.toList());
    }

}
