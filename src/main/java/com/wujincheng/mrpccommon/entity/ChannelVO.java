package com.wujincheng.mrpccommon.entity;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;

public class ChannelVO {
    public final Channel channel;
    public final EventLoopGroup eventLoopGroup;

    public ChannelVO(Channel channel, EventLoopGroup eventLoopGroup) {
        this.channel = channel;
        this.eventLoopGroup = eventLoopGroup;
    }
}