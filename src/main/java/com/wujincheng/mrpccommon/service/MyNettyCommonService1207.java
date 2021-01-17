package com.wujincheng.mrpccommon.service;


import com.wujincheng.mrpccommon.entity.Heartbeat;

public interface MyNettyCommonService1207 {

    void heatbeat(Heartbeat heartbeat);
    String echo(String str);
}