package com.wujincheng.mrpccommon.invoke;

import java.util.Map;

public class Invocation {
    private String className;
    private String methodName;
    private String[] parameterClassType;
    private Object[] parameter;
    private Map<String,Object> attachments;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
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

    public Map<String, Object> getAttachments() {
        return attachments;
    }

    public void setAttachments(Map<String, Object> attachments) {
        this.attachments = attachments;
    }
}