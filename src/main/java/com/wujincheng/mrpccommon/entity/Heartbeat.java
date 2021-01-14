package com.wujincheng.mrpccommon.entity;

import java.io.Serializable;
import java.util.List;

public class Heartbeat implements Serializable {
    private String ip;
    private String port;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "Heartbeat{" +
                "ip='" + ip + '\'' +
                ", port='" + port + '\'' +
                '}';
    }
}