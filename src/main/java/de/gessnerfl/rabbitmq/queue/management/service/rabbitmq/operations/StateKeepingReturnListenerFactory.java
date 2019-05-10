package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class StateKeepingReturnListenerFactory {

    public StateKeepingReturnListener createFor(OperationId operationId, Logger logger){
        return new StateKeepingReturnListener(operationId, logger);
    }

}
