package com.wujincheng.mrpccommon.serviceImpl;

import com.wujincheng.mrpccommon.annotation.MRPCService;
import com.wujincheng.mrpccommon.common.Common;
import com.wujincheng.mrpccommon.context.RpcContext;
import com.wujincheng.mrpccommon.entity.Heartbeat;
import com.wujincheng.mrpccommon.service.MyNettyCommonService1207;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


@MRPCService
public class MyNettyCommonService1207Impl implements MyNettyCommonService1207 {

    private static final Logger logger= LoggerFactory.getLogger(MyNettyCommonService1207Impl.class);

    @Override
    public void heatbeat(Heartbeat heartbeat) {
        if(heartbeat==null){
            return;
        }
        //
        if(logger.isDebugEnabled()){
            logger.debug(heartbeat.toString());
        }

    }

    @Override
    public String echo(String str) {
        return str;
    }
}