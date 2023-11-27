
package com.my.proxy.entity;

/**
 * Back-end server entity.
 */
public class BackendServer {
    private String name;
    private String ip;
    private int port;
    private int weight;
    private boolean isCheck;
    private String uri;

    public BackendServer() {
    }

    public BackendServer(String name, String ip, int port, int weight, boolean isCheck, String uri) {
        this.name = name;
        this.ip = ip;
        this.port = port;
        this.weight = weight;
        this.isCheck = isCheck;
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean isCheck) {
        this.isCheck = isCheck;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
