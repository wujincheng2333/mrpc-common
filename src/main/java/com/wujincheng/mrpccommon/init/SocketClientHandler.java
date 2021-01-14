package com.wujincheng.mrpccommon.init;

import com.wujincheng.mrpccommon.common.Common;
import com.wujincheng.mrpccommon.common.Request;
import com.wujincheng.mrpccommon.common.Response;
import com.wujincheng.mrpccommon.entity.ChannelVO;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class SocketClientHandler extends SimpleChannelInboundHandler<Response>  {
    private static final Logger logger= LoggerFactory.getLogger(SocketClientHandler.class);
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Response response) throws Exception {
        try {
            switch (response.getType()){
                case Common.XIN_TIAO:
                    break;
                case Common.RPC:
                    MRPCInit.handleClientRPC(response,ctx);
                    break;
                default:
                    break;
            }
        }catch (Throwable e){
            logger.error(e.getMessage(),e);
            //e.getStackTrace();
            Request request =new Request(response.getId(), response.getType(),"");
            request.setClassName(response.getClassName());
            request.setHasExecption(response.getHasExecption());
            request.setParameter(response.getParameter());
            request.setParameterClassType(response.getParameterClassType());
            request.setThrowable(response.getThrowable());
            request.setMethodName(response.getMethodName());
            request.setHasExecption(true);
            request.setThrowable(e);
            ctx.writeAndFlush(request);
        }

    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        InetSocketAddress inetSocketAddress=(InetSocketAddress)ctx.channel().remoteAddress();
        logger.info("连接成功：[{}:{}]",inetSocketAddress.getHostName(),inetSocketAddress.getPort());
        MRPCInit.startHeartbeat(inetSocketAddress.getHostName(),String.valueOf(inetSocketAddress.getPort()));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //super.exceptionCaught(ctx, cause);
        //logger.error(cause.getMessage(),cause);
        InetSocketAddress inetSocketAddress=(InetSocketAddress)ctx.channel().remoteAddress();
        logger.info("关闭连接:[{}]",inetSocketAddress.getHostName()+":"+inetSocketAddress.getPort());
        ChannelVO channelVO=CacheData.channelMap.remove(inetSocketAddress.getHostName()+":"+inetSocketAddress.getPort());
        MRPCInit.closeClientChannel(channelVO);
    }
}