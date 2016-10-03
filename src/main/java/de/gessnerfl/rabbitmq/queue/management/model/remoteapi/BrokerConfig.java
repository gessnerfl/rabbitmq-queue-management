package de.gessnerfl.rabbitmq.queue.management.model.remoteapi;

public class BrokerConfig {
    private String hostname;
    private String vhost = "/";
    private int port = 5672;
    private int managementPort = 15672;
    private boolean managemnetPortSecured = false;
    private String username = "guest";
    private String password = "guest";

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getVhost() {
        return vhost;
    }

    public void setVhost(String vhost) {
        this.vhost = vhost;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getManagementPort() {
        return managementPort;
    }

    public void setManagementPort(int managementPort) {
        this.managementPort = managementPort;
    }

    public boolean isManagemnetPortSecured() {
        return managemnetPortSecured;
    }

    public void setManagemnetPortSecured(boolean managemnetPortSecured) {
        this.managemnetPortSecured = managemnetPortSecured;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
