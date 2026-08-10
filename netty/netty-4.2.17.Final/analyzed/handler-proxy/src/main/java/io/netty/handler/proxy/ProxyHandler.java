/*
 * Copyright 2014 The Netty Project
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

package io.netty.handler.proxy;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.PendingWriteQueue;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.net.SocketAddress;
import java.nio.channels.ConnectionPendingException;
import java.util.concurrent.TimeUnit;

/**
 * 建立盲转发代理隧道的协议抽象基类。
 * <p>子类实现具体代理协议（HTTP CONNECT、SOCKS4/5 等），在握手完成后移除编解码器并透传后续流量。</p>
 */
public abstract class ProxyHandler extends ChannelDuplexHandler {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(ProxyHandler.class);

    /** 默认连接超时：10 秒 */
    private static final long DEFAULT_CONNECT_TIMEOUT_MILLIS = 10000;

    /** 表示「无认证」或「匿名」的字符串常量 */
    static final String AUTH_NONE = "none";

    /** 代理服务器地址 */
    private final SocketAddress proxyAddress;
    /** 经代理连接的目标地址 */
    private volatile SocketAddress destinationAddress;
    /** 连接超时（毫秒） */
    private volatile long connectTimeoutMillis = DEFAULT_CONNECT_TIMEOUT_MILLIS;

    private volatile ChannelHandlerContext ctx;
    /** 代理握手完成前暂存的出站写请求 */
    private PendingWriteQueue pendingWrites;
    /** 代理隧道是否已建立完成 */
    private boolean finished;
    /** 是否抑制 channelReadComplete 向下传播（握手阶段） */
    private boolean suppressChannelReadComplete;
    /** 是否在握手完成前已调用 flush */
    private boolean flushedPrematurely;
    /** 连接目标地址的异步结果 */
    private final LazyChannelPromise connectPromise = new LazyChannelPromise();
    /** 连接超时定时任务 */
    private Future<?> connectTimeoutFuture;
    /** 写代理服务器时的失败监听 */
    private final ChannelFutureListener writeListener = future -> {
        if (!future.isSuccess()) {
            setConnectFailure(future.cause());
        }
    };

    protected ProxyHandler(SocketAddress proxyAddress) {
        this.proxyAddress = ObjectUtil.checkNotNull(proxyAddress, "proxyAddress");
    }

    /**
     * 返回当前使用的代理协议名称。
     */
    public abstract String protocol();

    /**
     * 返回当前使用的认证方案名称。
     */
    public abstract String authScheme();

    /**
     * 返回代理服务器地址。
     */
    @SuppressWarnings("unchecked")
    public final <T extends SocketAddress> T proxyAddress() {
        return (T) proxyAddress;
    }

    /**
     * 返回经代理服务器连接的目标地址。
     */
    @SuppressWarnings("unchecked")
    public final <T extends SocketAddress> T destinationAddress() {
        return (T) destinationAddress;
    }

    /**
     * 当且仅当已成功建立到目标的连接时返回 {@code true}。
     */
    public final boolean isConnected() {
        return connectPromise.isSuccess();
    }

    /**
     * 返回在目标连接建立成功或失败时会被通知的 {@link Future}。
     */
    public final Future<Channel> connectFuture() {
        return connectPromise;
    }

    /**
     * 返回连接超时（毫秒）。若在此时间内未完成到目标的连接尝试，则标记失败。
     */
    public final long connectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    /**
     * 设置连接超时（毫秒）。若在此时间内未完成到目标的连接尝试，则标记失败。
     */
    public final void setConnectTimeoutMillis(long connectTimeoutMillis) {
        if (connectTimeoutMillis <= 0) {
            connectTimeoutMillis = 0;
        }

        this.connectTimeoutMillis = connectTimeoutMillis;
    }

    @Override
    public final void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        addCodec(ctx);

        if (ctx.channel().isActive()) {
            // channelActive 已触发，channelActive() 不会再被调用，需在此初始化
            sendInitialMessage(ctx);
        } else {
            // channelActive 尚未触发，将在 channelActive() 中初始化
        }
    }

    /**
     * 添加与代理服务器通信所需的编解码 handler。
     */
    protected abstract void addCodec(ChannelHandlerContext ctx) throws Exception;

    /**
     * 移除 {@link #addCodec(ChannelHandlerContext)} 中添加的编码器。
     */
    protected abstract void removeEncoder(ChannelHandlerContext ctx) throws Exception;

    /**
     * 移除 {@link #addCodec(ChannelHandlerContext)} 中添加的解码器。
     */
    protected abstract void removeDecoder(ChannelHandlerContext ctx) throws Exception;

    @Override
    public final void connect(
            ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress,
            ChannelPromise promise) throws Exception {

        if (destinationAddress != null) {
            promise.setFailure(new ConnectionPendingException());
            return;
        }

        destinationAddress = remoteAddress;
        ctx.connect(proxyAddress, localAddress, promise);
    }

    @Override
    public final void channelActive(ChannelHandlerContext ctx) throws Exception {
        sendInitialMessage(ctx);
        ctx.fireChannelActive();
    }

    /**
     * 向代理服务器发送初始消息，并启动超时任务：超时内未成功则标记 {@link #connectPromise} 失败。
     */
    private void sendInitialMessage(final ChannelHandlerContext ctx) throws Exception {
        final long connectTimeoutMillis = this.connectTimeoutMillis;
        if (connectTimeoutMillis > 0) {
            connectTimeoutFuture = ctx.executor().schedule(new Runnable() {
                @Override
                public void run() {
                    if (!connectPromise.isDone()) {
                        setConnectFailure(new ProxyConnectException(exceptionMessage("timeout")));
                    }
                }
            }, connectTimeoutMillis, TimeUnit.MILLISECONDS);
        }

        final Object initialMessage = newInitialMessage(ctx);
        if (initialMessage != null) {
            sendToProxyServer(initialMessage);
        }

        readIfNeeded(ctx);
    }

    /**
     * 返回与代理服务器建立连接后首次发送的消息。
     *
     * @return 初始消息；若期望由代理服务器先发消息则返回 {@code null}
     */
    protected abstract Object newInitialMessage(ChannelHandlerContext ctx) throws Exception;

    /**
     * 向代理服务器发送指定消息。在 {@link #handleResponse(ChannelHandlerContext, Object)} 中
     * 可用此方法回复代理服务器。
     */
    protected final void sendToProxyServer(Object msg) {
        ctx.writeAndFlush(msg).addListener(writeListener);
    }

    @Override
    public final void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (finished) {
            ctx.fireChannelInactive();
        } else {
            // 尚未连上目标即断开
            setConnectFailure(new ProxyConnectException(exceptionMessage("disconnected")));
        }
    }

    @Override
    public final void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (finished) {
            ctx.fireExceptionCaught(cause);
        } else {
            // 连接尝试完成前发生异常
            setConnectFailure(cause);
        }
    }

    @Override
    public final void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (finished) {
            // 隧道已建立，透传入站消息
            suppressChannelReadComplete = false;
            ctx.fireChannelRead(msg);
        } else {
            suppressChannelReadComplete = true;
            Throwable cause = null;
            try {
                boolean done = handleResponse(ctx, msg);
                if (done) {
                    setConnectSuccess();
                }
            } catch (Throwable t) {
                cause = t;
            } finally {
                ReferenceCountUtil.release(msg);
                if (cause != null) {
                    setConnectFailure(cause);
                }
            }
        }
    }

    /**
     * 处理来自代理服务器的消息。
     *
     * @return 若到目标的连接已建立则 {@code true}；若仍需等待更多代理响应则 {@code false}
     */
    protected abstract boolean handleResponse(ChannelHandlerContext ctx, Object response) throws Exception;

    /** 标记代理隧道建立成功，移除编解码器并刷新暂存写请求 */
    private void setConnectSuccess() {
        finished = true;
        cancelConnectTimeoutFuture();

        if (!connectPromise.isDone()) {
            boolean removedCodec = true;

            removedCodec &= safeRemoveEncoder();

            ctx.fireUserEventTriggered(
                    new ProxyConnectionEvent(protocol(), authScheme(), proxyAddress, destinationAddress));

            removedCodec &= safeRemoveDecoder();

            if (removedCodec) {
                writePendingWrites();

                if (flushedPrematurely) {
                    ctx.flush();
                }
                connectPromise.trySuccess(ctx.channel());
            } else {
                // 未能移除全部编解码 handler，状态不一致
                Exception cause = new ProxyConnectException(
                        "failed to remove all codec handlers added by the proxy handler; bug?");
                failPendingWritesAndClose(cause);
            }
        }
    }

    private boolean safeRemoveDecoder() {
        try {
            removeDecoder(ctx);
            return true;
        } catch (Exception e) {
            logger.warn("Failed to remove proxy decoders:", e);
        }

        return false;
    }

    private boolean safeRemoveEncoder() {
        try {
            removeEncoder(ctx);
            return true;
        } catch (Exception e) {
            logger.warn("Failed to remove proxy encoders:", e);
        }

        return false;
    }

    /** 标记连接失败，清理资源并关闭通道 */
    private void setConnectFailure(Throwable cause) {
        finished = true;
        cancelConnectTimeoutFuture();

        if (!connectPromise.isDone()) {

            if (!(cause instanceof ProxyConnectException)) {
                cause = new ProxyConnectException(
                        exceptionMessage(cause.toString()), cause);
            }

            safeRemoveDecoder();
            safeRemoveEncoder();
            failPendingWritesAndClose(cause);
        }
    }

    private void failPendingWritesAndClose(Throwable cause) {
        failPendingWrites(cause);
        connectPromise.tryFailure(cause);
        ctx.fireExceptionCaught(cause);
        ctx.close();
    }

    private void cancelConnectTimeoutFuture() {
        if (connectTimeoutFuture != null) {
            connectTimeoutFuture.cancel(false);
            connectTimeoutFuture = null;
        }
    }

    /**
     * 用协议、认证方案、代理地址、目标地址等公共信息装饰异常消息。
     */
    protected final String exceptionMessage(String msg) {
        if (msg == null) {
            msg = "";
        }

        StringBuilder buf = new StringBuilder(128 + msg.length())
            .append(protocol())
            .append(", ")
            .append(authScheme())
            .append(", ")
            .append(proxyAddress)
            .append(" => ")
            .append(destinationAddress);
        if (!msg.isEmpty()) {
            buf.append(", ").append(msg);
        }

        return buf.toString();
    }

    @Override
    public final void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        if (suppressChannelReadComplete) {
            suppressChannelReadComplete = false;

            readIfNeeded(ctx);
        } else {
            ctx.fireChannelReadComplete();
        }
    }

    @Override
    public final void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (finished) {
            writePendingWrites();
            ctx.write(msg, promise);
        } else {
            addPendingWrite(ctx, msg, promise);
        }
    }

    @Override
    public final void flush(ChannelHandlerContext ctx) throws Exception {
        if (finished) {
            writePendingWrites();
            ctx.flush();
        } else {
            flushedPrematurely = true;
        }
    }

    /** 非自动读模式下主动触发 read */
    private static void readIfNeeded(ChannelHandlerContext ctx) {
        if (!ctx.channel().config().isAutoRead()) {
            ctx.read();
        }
    }

    /** 写出握手期间暂存的所有写请求 */
    private void writePendingWrites() {
        if (pendingWrites != null) {
            pendingWrites.removeAndWriteAll();
            pendingWrites = null;
        }
    }

    private void failPendingWrites(Throwable cause) {
        if (pendingWrites != null) {
            pendingWrites.removeAndFailAll(cause);
            pendingWrites = null;
        }
    }

    private void addPendingWrite(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        PendingWriteQueue pendingWrites = this.pendingWrites;
        if (pendingWrites == null) {
            this.pendingWrites = pendingWrites = new PendingWriteQueue(ctx);
        }
        pendingWrites.add(msg, promise);
    }

    /** 延迟绑定 executor 的 Channel Promise，避免 handler 尚未加入 pipeline 时访问 ctx */
    private final class LazyChannelPromise extends DefaultPromise<Channel> {
        @Override
        protected EventExecutor executor() {
            if (ctx == null) {
                throw new IllegalStateException();
            }
            return ctx.executor();
        }
    }
}
