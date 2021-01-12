package com.wujincheng.mrpccommon.proxy;

import java.lang.reflect.InvocationHandler;

public class JavassistProxyFactory implements ProxyFactory {
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<?> clz, InvocationHandler invocationHandler){
        return (T) Proxy.getProxy(clz).newInstance(invocationHandler);
    }
}