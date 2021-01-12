package com.wujincheng.mrpccommon.proxy;

import java.lang.reflect.InvocationHandler;

public interface ProxyFactory {
    <T> T getProxy(Class<?> clz, InvocationHandler invocationHandler) throws Throwable;
}