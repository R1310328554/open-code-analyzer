package com.alibaba.arthas.tunnel.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/**
 * 双向中继处理器：将一端读到的消息写入对端，用于浏览器与 agent 隧道透明转发。
 */
public final class RelayHandler extends ChannelInboundHandlerAdapter {

    private final static Logger logger = LoggerFactory.getLogger(RelayHandler.class);

        /** 对端待写入的 Netty 通道 */
    private final Channel relayChannel;

    public RelayHandler(Channel relayChannel) {
        this.relayChannel = relayChannel;
    }

    /** 通道激活时发送空缓冲触发对端就绪 */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER);
    }

    /** 转发入站消息；对端已关闭则释放引用 */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (relayChannel.isActive()) {
            relayChannel.writeAndFlush(msg);
        } else {
            ReferenceCountUtil.release(msg);
        }
    }

    /** 本端断开时关闭对端通道 */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (relayChannel.isActive()) {
            relayChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("", cause);
        ctx.close();
    }
}
