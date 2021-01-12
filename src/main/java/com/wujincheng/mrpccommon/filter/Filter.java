package com.wujincheng.mrpccommon.filter;


import com.wujincheng.mrpccommon.invoke.Invocation;
import com.wujincheng.mrpccommon.invoke.MyInvoker;

public interface Filter {
    Object invoke(MyInvoker invoker, Invocation invocation);
}
