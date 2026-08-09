/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.rocketmq.proxy.remoting.protocol.http2proxy;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.ssl.SslHandler;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;

/**
 * HTTP/2 代理前端处理器：将 Remoting 客户端请求转发至 gRPC 出站通道。
 */
public class Http2ProxyFrontendHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.ROCKETMQ_REMOTING_NAME);

    public static final String HANDLER_NAME = "SslHandler";

    // 出站与入站共用同一 EventLoop，因此 outboundChannel 无需 volatile
    /** 指向本机 gRPC 服务的出站 {@link Channel}。 */
    private final Channel outboundChannel;
    /** 可选 TLS 处理器，在首包写出前注入出站管道。 */
    private final SslHandler sslHandler;

    /** 绑定出站通道与可选 {@link SslHandler}。 */
    public Http2ProxyFrontendHandler(final Channel outboundChannel, final SslHandler sslHandler) {
        this.outboundChannel = outboundChannel;
        this.sslHandler = sslHandler;
    }

    @Override
    /** 按需注入 SSL 并将客户端数据写向 gRPC 出站通道。 */
    public void channelRead(final ChannelHandlerContext ctx, Object msg) {
        if (outboundChannel.isActive()) {
            if (sslHandler != null && outboundChannel.pipeline().get(HANDLER_NAME) == null) {
                outboundChannel.pipeline().addBefore(Http2ProxyBackendHandler.HANDLER_NAME, HANDLER_NAME, sslHandler);
            }

            outboundChannel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    // 刷写成功后继续读取下一数据块
                    ctx.channel().read();
                } else {
                    future.channel().close();
                }
            });
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (outboundChannel != null) {
            closeOnFlush(outboundChannel);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Http2ProxyFrontendHandler#exceptionCaught", cause);
        closeOnFlush(ctx.channel());
    }

    /**
     * 刷写所有待写数据后关闭指定 {@link Channel}。
     */
    /** 写入空缓冲并监听 CLOSE，实现优雅关闭。 */
    static void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
