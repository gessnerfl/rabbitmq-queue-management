package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

import org.springframework.stereotype.Service;

@Service
public class OperationIdGenerator {

    public OperationId generate(){
        return new OperationId();
    }

}
