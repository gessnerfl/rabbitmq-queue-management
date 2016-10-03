package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.remoteapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.BrokerConfig;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.config.RabbitMqBrokers;
import feign.Feign;
import feign.auth.BasicAuthRequestInterceptor;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.slf4j.Slf4jLogger;

@Service
public class FeignBuilderFactory {

    private final RabbitMqBrokers rabbitMqBrokers;
    private final Gson gson;

    @Autowired
    public FeignBuilderFactory(RabbitMqBrokers rabbitMqBrokers, Gson gson) {
        this.rabbitMqBrokers = rabbitMqBrokers;
        this.gson = gson;
    }

    public Feign.Builder createFor(String system) {
        return Feign.builder()
                .encoder(new GsonEncoder(gson))
                .decoder(new GsonDecoder(gson))
                .logger(new Slf4jLogger())
                .requestInterceptor(buildBasicAuthentication(system));
    }

    private BasicAuthRequestInterceptor buildBasicAuthentication(String system) {
        BrokerConfig brokerConfig = rabbitMqBrokers.getBrokerConfig(system);
        return new BasicAuthRequestInterceptor(brokerConfig.getUsername(), brokerConfig.getPassword());
    }

}
