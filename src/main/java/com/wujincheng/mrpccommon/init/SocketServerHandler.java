package com.wujincheng.mrpccommon.init;

import com.wujincheng.mrpccommon.common.Common;
import com.wujincheng.mrpccommon.common.Response;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class SocketServerHandler extends SimpleChannelInboundHandler<Response> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Response response) throws Exception {
        switch (response.getType()) {
            case Common.RPC:
                MRPCInit.handleServerRPC(response, ctx);
                break;
            default:
                break;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close();
    }
}