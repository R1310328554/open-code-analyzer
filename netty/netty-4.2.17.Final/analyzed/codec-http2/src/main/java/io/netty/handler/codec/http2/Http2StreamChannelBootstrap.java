/*
 * Copyright 2017 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.codec.http2;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.nio.channels.ClosedChannelException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 出站 {@link Http2StreamChannel} 的引导类：在已有 HTTP/2 父连接上创建客户端发起的逻辑流通道。
 * <p>用法类似 {@link io.netty.bootstrap.Bootstrap}，需父 pipeline 中已安装
 * {@link Http2MultiplexCodec} 或 {@link Http2MultiplexHandler}。
 */
public final class Http2StreamChannelBootstrap {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(Http2StreamChannelBootstrap.class);
    @SuppressWarnings("unchecked")
    private static final Map.Entry<ChannelOption<?>, Object>[] EMPTY_OPTION_ARRAY = new Map.Entry[0];
    @SuppressWarnings("unchecked")
    private static final Map.Entry<AttributeKey<?>, Object>[] EMPTY_ATTRIBUTE_ARRAY = new Map.Entry[0];

    // ChannelOption 应用顺序可能影响校验，故用 LinkedHashMap 保序
    private final Map<ChannelOption<?>, Object> options = new LinkedHashMap<ChannelOption<?>, Object>();
    private final Map<AttributeKey<?>, Object> attrs = new ConcurrentHashMap<AttributeKey<?>, Object>();
    private final Channel channel;
    private volatile ChannelHandler handler;

    // 缓存 multiplex handler 的 Context，加速重复 open
    private volatile ChannelHandlerContext multiplexCtx;

    public Http2StreamChannelBootstrap(Channel channel) {
        this.channel = ObjectUtil.checkNotNull(channel, "channel");
    }

    /**
     * 指定新建 {@link Http2StreamChannel} 的 {@link ChannelOption}；{@code value} 为 {@code null} 时移除先前设置。
     */
    public <T> Http2StreamChannelBootstrap option(ChannelOption<T> option, T value) {
        ObjectUtil.checkNotNull(option, "option");

        synchronized (options) {
            if (value == null) {
                options.remove(option);
            } else {
                options.put(option, value);
            }
        }
        return this;
    }

    /**
     * 指定新建通道的初始 {@link AttributeKey} 属性；{@code value} 为 {@code null} 时移除该键。
     */
    public <T> Http2StreamChannelBootstrap attr(AttributeKey<T> key, T value) {
        ObjectUtil.checkNotNull(key, "key");
        if (value == null) {
            attrs.remove(key);
        } else {
            attrs.put(key, value);
        }
        return this;
    }

    /**
     * 绑定处理该流上消息的 {@link ChannelHandler}。
     */
    public Http2StreamChannelBootstrap handler(ChannelHandler handler) {
        this.handler = ObjectUtil.checkNotNull(handler, "handler");
        return this;
    }

    /**
     * 打开新的出站 {@link Http2StreamChannel}。
     * @return the {@link Future} that will be notified once the channel was opened successfully or it failed.
     */
    public Future<Http2StreamChannel> open() {
        return open(channel.eventLoop().<Http2StreamChannel>newPromise());
    }

    /**
     * 打开新的出站 {@link Http2StreamChannel}，结果通过给定 {@link Promise} 通知。
     * @return the {@link Future} that will be notified once the channel was opened successfully or it failed.
     */
    public Future<Http2StreamChannel> open(final Promise<Http2StreamChannel> promise) {
        try {
            ChannelHandlerContext ctx = findCtx();
            EventExecutor executor = ctx.executor();
            if (executor.inEventLoop()) {
                open0(ctx, promise);
            } else {
                final ChannelHandlerContext finalCtx = ctx;
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (channel.isActive()) {
                            open0(finalCtx, promise);
                        } else {
                            promise.setFailure(new ClosedChannelException());
                        }
                    }
                });
            }
        } catch (Throwable cause) {
            promise.setFailure(cause);
        }
        return promise;
    }

    @SuppressWarnings("deprecation")
    private ChannelHandlerContext findCtx() throws ClosedChannelException {
        // 优先使用缓存；失效时在 pipeline 中查找 Multiplex 处理器
        ChannelHandlerContext ctx = multiplexCtx;
        if (ctx != null && !ctx.isRemoved()) {
            return ctx;
        }
        ChannelPipeline pipeline = channel.pipeline();
        ctx = pipeline.context(Http2MultiplexCodec.class);
        if (ctx == null) {
            ctx = pipeline.context(Http2MultiplexHandler.class);
        }
        if (ctx == null) {
            if (channel.isActive()) {
                throw new IllegalStateException(StringUtil.simpleClassName(Http2MultiplexCodec.class) + " or "
                        + StringUtil.simpleClassName(Http2MultiplexHandler.class)
                        + " must be in the ChannelPipeline of Channel " + channel);
            } else {
                throw new ClosedChannelException();
            }
        }
        multiplexCtx = ctx;
        return ctx;
    }

    /**
     * @deprecated should not be used directly. Use {@link #open()} or {@link #open(Promise)}
     */
    @Deprecated
    public void open0(ChannelHandlerContext ctx, final Promise<Http2StreamChannel> promise) {
        assert ctx.executor().inEventLoop();
        if (!promise.setUncancellable()) {
            return;
        }
        final Http2StreamChannel streamChannel;
        try {
            if (ctx.handler() instanceof Http2MultiplexCodec) {
                streamChannel = ((Http2MultiplexCodec) ctx.handler()).newOutboundStream();
            } else {
                streamChannel = ((Http2MultiplexHandler) ctx.handler()).newOutboundStream();
            }
        } catch (Exception e) {
            promise.setFailure(e);
            return;
        }
        try {
            init(streamChannel);
        } catch (Exception e) {
            streamChannel.unsafe().closeForcibly();
            promise.setFailure(e);
            return;
        }
        // 注册到 EventLoop 后才对用户可见
        ChannelFuture future = ctx.channel().eventLoop().register(streamChannel);
        future.addListener(f -> {
            if (f.isSuccess()) {
                promise.setSuccess(streamChannel);
            } else if (f.isCancelled()) {
                promise.cancel(false);
            } else {
                if (streamChannel.isRegistered()) {
                    streamChannel.close();
                } else {
                    streamChannel.unsafe().closeForcibly();
                }

                promise.setFailure(f.cause());
            }
        });
    }

    /** 向新通道 pipeline 追加 handler 并应用 option/attribute。 */
    private void init(Channel channel) {
        ChannelPipeline p = channel.pipeline();
        ChannelHandler handler = this.handler;
        if (handler != null) {
            p.addLast(handler);
        }
        final Map.Entry<ChannelOption<?>, Object> [] optionArray;
        synchronized (options) {
            optionArray = options.entrySet().toArray(EMPTY_OPTION_ARRAY);
        }

        setChannelOptions(channel, optionArray);
        setAttributes(channel, attrs.entrySet().toArray(EMPTY_ATTRIBUTE_ARRAY));
    }

    private static void setChannelOptions(
            Channel channel, Map.Entry<ChannelOption<?>, Object>[] options) {
        for (Map.Entry<ChannelOption<?>, Object> e: options) {
            setChannelOption(channel, e.getKey(), e.getValue());
        }
    }

    private static void setChannelOption(
            Channel channel, ChannelOption<?> option, Object value) {
        try {
            @SuppressWarnings("unchecked")
            ChannelOption<Object> opt = (ChannelOption<Object>) option;
            if (!channel.config().setOption(opt, value)) {
                logger.warn("{} Unknown channel option '{}'", channel, option);
            }
        } catch (Throwable t) {
            logger.warn("{} Failed to set channel option '{}' with value '{}'", channel, option, value, t);
        }
    }

    private static void setAttributes(
            Channel channel, Map.Entry<AttributeKey<?>, Object>[] options) {
        for (Map.Entry<AttributeKey<?>, Object> e: options) {
            @SuppressWarnings("unchecked")
            AttributeKey<Object> key = (AttributeKey<Object>) e.getKey();
            channel.attr(key).set(e.getValue());
        }
    }
}
