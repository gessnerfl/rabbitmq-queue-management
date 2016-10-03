package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.BrokerConfig;
import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.BrokerDescriptor;

@Service
public class RabbitMqBrokers {

    private final RabbitMqBrokersConfig config;

    @Autowired
    public RabbitMqBrokers(RabbitMqBrokersConfig config) {
        this.config = config;
    }
    
    public List<BrokerDescriptor> getBrokerDescriptors(){
        Map<String, BrokerConfig> configs = config.getBrokers();
        if(configs != null && !configs.isEmpty()){
            return configs.entrySet().stream().map(this::toDescriptor).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
    
    private BrokerDescriptor toDescriptor(Entry<String, BrokerConfig> entry){
        return new BrokerDescriptor(entry.getKey(), renderDisplayName(entry.getValue()));
    }

    private String renderDisplayName(BrokerConfig value) {
        return value.getHostname()+":"+value.getPort()+" (vhost="+value.getVhost()+")";
    }

    public BrokerConfig getBrokerConfig(String name) {
        if(name == null || name.trim().isEmpty()){
            throw new IllegalArgumentException("Broker name is missing");
        }
        Map<String, BrokerConfig> configs = config.getBrokers();
        if (configs != null && !configs.isEmpty()) {
            BrokerConfig config = configs.get(name);
            if (config != null) {
                return config;
            }
            throw new InvalidBrokerNameException(name);
        }
        throw new NoBrokersAvailableException();
    }
}
