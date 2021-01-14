package com.wujincheng.mrpccommon.serviceImpl;

import com.wujincheng.mrpccommon.annotation.MRPCReference;
import com.wujincheng.mrpccommon.annotation.MRPCServiceNative;
import com.wujincheng.mrpccommon.entity.Heartbeat;
import com.wujincheng.mrpccommon.service.MyNettyCommonService1207;

@MRPCServiceNative
public class MyNettyCommonService1207Native implements MyNettyCommonService1207{

    @MRPCReference
    private MyNettyCommonService1207 myNettyCommonService1207;

    @Override
    public void heatbeat(Heartbeat heartbeat) {
        myNettyCommonService1207.heatbeat(heartbeat);
    }
}