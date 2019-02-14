package org.fuelteam.watt.httpclient;

public class Proxy {

    private String host;
    
    private Integer port;
    
    private String protocol;
    
    private String username;
    
    private String password;
    
    public Proxy(String host, Integer port, String protocol) {
        super();
        this.host = host;
        this.port = port;
        this.protocol = protocol;
    }

    public Proxy(String host, Integer port, String protocol, String username, String password) {
        super();
        this.host = host;
        this.port = port;
        this.protocol = protocol;
        this.username = username;
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
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
