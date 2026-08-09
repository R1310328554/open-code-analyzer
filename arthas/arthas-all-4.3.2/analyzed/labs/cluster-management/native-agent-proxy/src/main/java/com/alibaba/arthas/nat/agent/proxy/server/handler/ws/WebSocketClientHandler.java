package com.alibaba.arthas.nat.agent.proxy.server.handler.ws;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

/**
 * WebSocket 出站客户端处理器：将目标 Agent 返回的帧原样写回入站连接。
 *
 * @description: hello world
 * @author：flzjkl
 * @date: 2024-10-20 20:05
 */
public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {

    /** 与浏览器/管理端建立的入站 Channel */
    private final Channel inboundChannel;

    public WebSocketClientHandler(Channel inboundChannel) {
        this.inboundChannel = inboundChannel;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof WebSocketFrame) {
            inboundChannel.writeAndFlush(((WebSocketFrame) msg).retain());
        }
    }

    /** 出站连接断开时同步关闭入站 Channel */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        inboundChannel.close();
    }

}
