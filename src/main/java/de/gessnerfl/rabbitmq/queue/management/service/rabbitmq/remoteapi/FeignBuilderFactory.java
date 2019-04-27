package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.remoteapi;

import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.config.RabbitMqConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import feign.Feign;
import feign.auth.BasicAuthRequestInterceptor;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.slf4j.Slf4jLogger;

@Service
public class FeignBuilderFactory {

    private final RabbitMqConfig rabbitMqConfig;
    private final Gson gson;

    @Autowired
    public FeignBuilderFactory(RabbitMqConfig rabbitMqConfig, Gson gson) {
        this.rabbitMqConfig = rabbitMqConfig;
        this.gson = gson;
    }

    public Feign.Builder createFor() {
        return Feign.builder()
                .encoder(new GsonEncoder(gson))
                .decoder(new GsonDecoder(gson))
                .logger(new Slf4jLogger())
                .requestInterceptor(buildBasicAuthentication());
    }

    private BasicAuthRequestInterceptor buildBasicAuthentication() {
        return new BasicAuthRequestInterceptor(rabbitMqConfig.getUsername(), rabbitMqConfig.getPassword());
    }

}
