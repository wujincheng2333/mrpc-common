package com.wujincheng.mrpccommon.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Response implements Serializable {
    private long id;
    private int type;
    private String dataType;
    private Object data;
    private Map<String,String> map=new HashMap<>();

    private Map<String,String> attachments=new HashMap<>();

    private String[] parameterClassType;
    private Object[] parameter;
    private String className;
    private String methodName;
    private Boolean hasExecption=false;

    private Throwable throwable;

    public Map<String, String> getAttachments() {
        return attachments;
    }

    public void setAttachments(Map<String, String> attachments) {
        this.attachments = attachments;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public Boolean getHasExecption() {
        return hasExecption;
    }

    public void setHasExecption(Boolean hasExecption) {
        this.hasExecption = hasExecption;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String[] getParameterClassType() {
        return parameterClassType;
    }

    public void setParameterClassType(String[] parameterClassType) {
        this.parameterClassType = parameterClassType;
    }

    public Object[] getParameter() {
        return parameter;
    }

    public void setParameter(Object[] parameter) {
        this.parameter = parameter;
    }

    public Response(long id, int type,String dataType, Object data) {
        this.id = id;
        this.type = type;
        this.data = data;
        this.dataType = dataType;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
}
