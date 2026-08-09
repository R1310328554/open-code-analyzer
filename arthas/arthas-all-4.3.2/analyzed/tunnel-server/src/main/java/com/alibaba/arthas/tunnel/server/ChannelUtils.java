package com.alibaba.arthas.tunnel.server;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

/**
 * Netty 通道工具类。
 */
public final class ChannelUtils {

    /**
     * 在刷完待写数据后优雅关闭通道。
     */
    public static void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    private ChannelUtils() {
    }
}