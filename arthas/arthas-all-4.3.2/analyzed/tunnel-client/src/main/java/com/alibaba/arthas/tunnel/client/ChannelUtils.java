package com.alibaba.arthas.tunnel.client;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

/**
 * Netty Channel 关闭相关的工具方法。
 */
public final class ChannelUtils {

    /**
     * 在刷出所有待写数据后关闭指定 Channel。
     *
     * @param ch 待关闭的 Channel
     */
    public static void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    private ChannelUtils() {
    }
}
