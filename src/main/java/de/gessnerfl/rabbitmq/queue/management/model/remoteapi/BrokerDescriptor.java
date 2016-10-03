package de.gessnerfl.rabbitmq.queue.management.model.remoteapi;

public class BrokerDescriptor {

    private final String name;
    private final String displayName;

    public BrokerDescriptor(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }


}
