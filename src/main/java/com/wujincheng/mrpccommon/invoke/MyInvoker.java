package com.wujincheng.mrpccommon.invoke;

public interface MyInvoker<T> {
    Class<T> getInterface();
    Object invoke(Invocation invocation);
}
