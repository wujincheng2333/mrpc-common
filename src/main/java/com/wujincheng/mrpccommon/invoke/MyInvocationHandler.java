package com.wujincheng.mrpccommon.invoke;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class MyInvocationHandler implements InvocationHandler {

    private MyInvoker invoker;

    public MyInvocationHandler(MyInvoker invoker) {
        this.invoker = invoker;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(invoker, args);
        }
        if ("toString".equals(methodName) && parameterTypes.length == 0) {
            return invoker.toString();
        }
        if ("hashCode".equals(methodName) && parameterTypes.length == 0) {
            return invoker.hashCode();
        }
        if ("equals".equals(methodName) && parameterTypes.length == 1) {
            return invoker.equals(args[0]);
        }

        String[] parameterClassType=new String[parameterTypes.length];
        for(int i=0;i<parameterTypes.length;i++){
            parameterClassType[i]=parameterTypes[i].getName();
        }
        Invocation invocation=new Invocation();
        invocation.setClassName(invoker.getInterface().getName());
        invocation.setMethodName(methodName);
        invocation.setParameter(args);
        invocation.setParameterClassType(parameterClassType);
        return invoker.invoke(invocation);
    }
}