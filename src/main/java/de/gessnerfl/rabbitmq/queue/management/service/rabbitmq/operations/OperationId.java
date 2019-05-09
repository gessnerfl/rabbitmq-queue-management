package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

import java.util.UUID;

public class OperationId {

    public static final String HEADER_NAME = "x-rmqmgmt-routing-operation-id";
    private final String value;

    public OperationId() {
        this.value = UUID.randomUUID().toString().replaceAll("-", "");
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    public boolean equals(String o) {
        return value.equals(o);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OperationId that = (OperationId) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
