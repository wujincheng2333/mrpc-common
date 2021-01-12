package com.wujincheng.mrpccommon.filter;


import com.wujincheng.mrpccommon.context.RpcContext;
import com.wujincheng.mrpccommon.invoke.Invocation;
import com.wujincheng.mrpccommon.invoke.MyInvoker;

public class RemoveRpcContextFliter implements Filter {
    @Override
    public Object invoke(MyInvoker invoker, Invocation invocation) {
        try {
            invocation.setAttachments(RpcContext.getValues());
            return invoker.invoke(invocation);
        }finally {
            //System.out.println(RpcContext.toContextString());
            RpcContext.removeContext();
        }
    }
}