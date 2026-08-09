package com.alibaba.arthas.tunnel.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/**
 * 双向 Channel 中继处理器：将一端收到的消息原样写入对端，
 * 任一端 inactive 或异常时联动关闭另一侧连接。
 */
public final class RelayHandler extends ChannelInboundHandlerAdapter {
    private final static Logger logger = LoggerFactory.getLogger(RelayHandler.class);
    /** 消息转发的目标 Channel（对端） */
    private final Channel relayChannel;

    public RelayHandler(Channel relayChannel) {
        this.relayChannel = relayChannel;
    }

    /** 激活时发送空 buffer，触发对端 pipeline 的 channelActive 链路 */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER);
    }

    /** 将入站消息转发至对端；对端已关闭则释放引用计数避免泄漏 */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (relayChannel.isActive()) {
            relayChannel.writeAndFlush(msg);
        } else {
            ReferenceCountUtil.release(msg);
        }
    }

    /** 本端断开时，刷写并关闭仍活跃的对端 */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (relayChannel.isActive()) {
            ChannelUtils.closeOnFlush(relayChannel);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("RelayHandler error", cause);
        try {
            if (relayChannel.isActive()) {
                relayChannel.close();
            }
        } finally {
            ctx.close();
        }
    }
}
