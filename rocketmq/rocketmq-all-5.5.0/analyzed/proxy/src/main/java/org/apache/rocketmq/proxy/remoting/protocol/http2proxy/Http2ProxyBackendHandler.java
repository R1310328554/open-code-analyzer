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

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;

/**
 * HTTP/2 代理后端处理器：将 gRPC 服务端响应写回 Remoting 入站通道。
 */
public class Http2ProxyBackendHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.ROCKETMQ_REMOTING_NAME);

    /** 管道中本处理器的注册名称。 */
    public static final String HANDLER_NAME = "Http2ProxyBackendHandler";

    /** 客户端 Remoting 入站 {@link Channel}。 */
    private final Channel inboundChannel;

    /** 绑定待回写数据的入站通道。 */
    public Http2ProxyBackendHandler(Channel inboundChannel) {
        this.inboundChannel = inboundChannel;
    }

    @Override
    /** 出站连接激活后开始读取 gRPC 响应。 */
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.read();
    }

    @Override
    /** 将 gRPC 响应转发至入站通道，成功后继续读取下一帧。 */
    public void channelRead(final ChannelHandlerContext ctx, Object msg) {
        inboundChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                    ctx.channel().read();
                } else {
                    future.channel().close();
                }
            }
        });
    }

    @Override
    /** 出站断开时刷写并关闭入站通道。 */
    public void channelInactive(ChannelHandlerContext ctx) {
        Http2ProxyFrontendHandler.closeOnFlush(inboundChannel);
    }

    @Override
    /** 记录异常并关闭出站通道。 */
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Http2ProxyBackendHandler#exceptionCaught", cause);
        Http2ProxyFrontendHandler.closeOnFlush(ctx.channel());
    }
}
