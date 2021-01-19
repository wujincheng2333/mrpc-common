package com.wujincheng.mrpccommon.init;

import io.netty.channel.ChannelHandlerContext;

public class SocketSimpleClientHandler extends SocketClientHandler  {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close();
    }
}