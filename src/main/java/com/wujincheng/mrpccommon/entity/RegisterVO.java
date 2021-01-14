package com.wujincheng.mrpccommon.entity;

import java.io.Serializable;
import java.util.List;

public class RegisterVO implements Serializable {

    private String ip;
    private String port;
    private String serviceName;
    private Long time;
    private List<String> interfaces;

    @Override
    public String toString() {
        StringBuilder str=new StringBuilder();
        if(interfaces!=null&&interfaces.size()>0){
            for(String s:interfaces){
                str.append(",").append(s);
            }
        }
        return "RegisterVO{" +
                "ip='" + ip + '\'' +
                ", port='" + port + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", time=" + time +
                ", interfaces=" + str.toString().substring(1) +
                '}';
    }

    public List<String> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<String> interfaces) {
        this.interfaces = interfaces;
    }

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

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}