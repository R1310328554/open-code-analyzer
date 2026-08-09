/*
 * Copyright 2020 The Netty Project
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
package io.netty.handler.codec.quic;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelProgressivePromise;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.SSLEngine;
import java.net.SocketAddress;

/**
 * QUIC 连接的 {@link Channel} 抽象：承载多路复用流、TLS 与连接级 QUIC 语义。
 */
public interface QuicChannel extends Channel {

    @Override
    default ChannelFuture bind(SocketAddress localAddress) {
        return pipeline().bind(localAddress);
    }

    @Override
    default ChannelFuture connect(SocketAddress remoteAddress) {
        return pipeline().connect(remoteAddress);
    }

    @Override
    default ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) {
        return pipeline().connect(remoteAddress, localAddress);
    }

    @Override
    default ChannelFuture disconnect() {
        return pipeline().disconnect();
    }

    @Override
    default ChannelFuture close() {
        return pipeline().close();
    }

    @Override
    default ChannelFuture deregister() {
        return pipeline().deregister();
    }

    @Override
    default ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise) {
        return pipeline().bind(localAddress, promise);
    }

    @Override
    default ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise) {
        return pipeline().connect(remoteAddress, promise);
    }

    @Override
    default ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
        return pipeline().connect(remoteAddress, localAddress, promise);
    }

    @Override
    default ChannelFuture disconnect(ChannelPromise promise) {
        return pipeline().disconnect(promise);
    }

    @Override
    default ChannelFuture close(ChannelPromise promise) {
        return pipeline().close(promise);
    }

    @Override
    default ChannelFuture deregister(ChannelPromise promise) {
        return pipeline().deregister(promise);
    }

    @Override
    default ChannelFuture write(Object msg) {
        return pipeline().write(msg);
    }

    @Override
    default ChannelFuture write(Object msg, ChannelPromise promise) {
        return pipeline().write(msg, promise);
    }

    @Override
    default ChannelFuture writeAndFlush(Object msg, ChannelPromise promise) {
        return pipeline().writeAndFlush(msg, promise);
    }

    @Override
    default ChannelFuture writeAndFlush(Object msg) {
        return pipeline().writeAndFlush(msg);
    }

    @Override
    default ChannelPromise newPromise() {
        return pipeline().newPromise();
    }

    @Override
    default ChannelProgressivePromise newProgressivePromise() {
        return pipeline().newProgressivePromise();
    }

    @Override
    default ChannelFuture newSucceededFuture() {
        return pipeline().newSucceededFuture();
    }

    @Override
    default ChannelFuture newFailedFuture(Throwable cause) {
        return pipeline().newFailedFuture(cause);
    }

    @Override
    default ChannelPromise voidPromise() {
        return pipeline().voidPromise();
    }

    @Override
    QuicChannel read();

    @Override
    QuicChannel flush();

    /**
     * 返回本通道的配置。
     */
    @Override
    QuicChannelConfig config();

    /**
     * 返回当前使用的 {@link SSLEngine}，尚未建立 TLS 时为 {@code null}。
     *
     * @return TLS 引擎实例。
     */
    @Nullable
    SSLEngine sslEngine();

    /**
     * 返回对端仍允许创建的流数量；超出后新建流将失败并触发
     * {@link QuicTransportError#STREAM_LIMIT_ERROR}。
     *
     * @param type 流类型（双向/单向等）。
     * @return 剩余可创建流数。
     */
    long peerAllowedStreams(QuicStreamType type);

    /**
     * 连接是否因空闲超时而关闭。
     *
     * @return 空闲超时关闭则 {@code true}，否则 {@code false}。
     */
    boolean isTimedOut();

    /**
     * 返回已收到的对端 {@link QuicTransportParameters}，尚未收到时为 {@code null}。
     *
     * @return 对端传输参数。
     */
    @Nullable
    QuicTransportParameters peerTransportParameters();

    /**
     * 返回本地 {@link QuicConnectionAddress}；连接生命周期内可能变化。
     *
     * @return  本地连接地址，尚未分配或已失效时为 {@code null}。
     */
    @Override
    @Nullable
    QuicConnectionAddress localAddress();

    /**
     * 返回远端 {@link QuicConnectionAddress}；连接生命周期内可能变化。
     *
     * @return  远端连接地址，尚未分配或已失效时为 {@code null}。
     */
    @Override
    @Nullable
    QuicConnectionAddress remoteAddress();

    /**
     * 返回底层传输（UDP）接收数据所用的本地 {@link SocketAddress}。
     *
     * @return  底层本地套接字地址，未分配或已失效时为 {@code null}。
     */
    @Nullable
    SocketAddress localSocketAddress();

    /**
     * 返回底层传输发送数据所用的远端 {@link SocketAddress}。
     *
     * @return  底层远端套接字地址，未分配或已失效时为 {@code null}。
     */
    @Nullable
    SocketAddress remoteSocketAddress();

    /**
     * 在本 {@link QuicChannel} 上创建流，完成后通知 {@link Future}。
     * 若 {@code handler} 非 {@code null}，将自动加入 {@link QuicStreamChannel} 的 pipeline。
     *
     * @param type      {@link QuicStreamChannel} 的 {@link QuicStreamType}。
     * @param handler   创建时加入流 pipeline 的 {@link ChannelHandler}。
     * @return          操作完成时通知的 {@link Future}。
     */
    default Future<QuicStreamChannel> createStream(QuicStreamType type, @Nullable ChannelHandler handler) {
        return createStream(type, handler, eventLoop().newPromise());
    }

    /**
     * 在本 {@link QuicChannel} 上创建流，完成后通知 {@link Promise}。
     *
     * @param type      流类型。
     * @param handler   创建时加入流 pipeline 的 handler。
     * @param promise   操作完成时通知的 {@link ChannelPromise}。
     * @return          同 {@link Promise} 对应的 {@link Future}。
     */
    Future<QuicStreamChannel> createStream(QuicStreamType type, @Nullable ChannelHandler handler,
                                           Promise<QuicStreamChannel> promise);

    /**
     * 返回 {@link QuicStreamChannelBootstrap}，便于为新建流设置选项与属性。
     * 简单场景可直接使用 {@link #createStream(QuicStreamType, ChannelHandler)}。
     *
     * @return 用于引导 {@link QuicStreamChannel} 的 bootstrap。
     */
    default QuicStreamChannelBootstrap newStreamBootstrap() {
        return new QuicStreamChannelBootstrap(this);
    }

    /**
     * 关闭 {@link QuicChannel}。
     *
     * @param applicationClose  {@code true} 使用应用层关闭帧，{@code false} 使用普通关闭。
     * @param error             应用错误码，无特殊错误时为 {@code 0}。
     * @param reason            关闭原因（可为空 {@link ByteBuf}）。
     * @return                  关闭完成时通知的 future。
     */
    default ChannelFuture close(boolean applicationClose, int error, ByteBuf reason) {
        return close(applicationClose, error, reason, newPromise());
    }

    /**
     * 关闭 {@link QuicChannel} 并绑定指定 {@link ChannelPromise}。
     *
     * @param applicationClose  是否应用层关闭。
     * @param error             应用错误码。
     * @param reason            关闭原因。
     * @param promise           完成时通知的 promise。
     * @return                  关闭 future。
     */
    ChannelFuture close(boolean applicationClose, int error, ByteBuf reason, ChannelPromise promise);

    /**
     * 收集连接统计信息，完成后通知 {@link Future}。
     *
     * @return 统计收集完成时通知的 {@link Future}。
     */
    default Future<QuicConnectionStats> collectStats() {
        return collectStats(eventLoop().newPromise());
    }

    /**
     * 收集连接统计信息，完成后通知 {@link Promise}。
     *
     * @param   promise 统计完成时通知的 promise。
     * @return          同上的 {@link Future}。
     */
    Future<QuicConnectionStats> collectStats(Promise<QuicConnectionStats> promise);

    /**
     * 收集指定路径索引的连接路径统计，完成后通知 {@link Future}。
     *
     * @param pathIdx 路径索引。
     * @return 统计完成时通知的 {@link Future}。
     */
    default Future<QuicConnectionPathStats> collectPathStats(int pathIdx) {
        return collectPathStats(pathIdx, eventLoop().newPromise());
    }

    /**
     * 收集连接路径统计，完成后通知 {@link Promise}。
     *
     * @param   promise 统计完成时通知的 promise。
     * @return          同上的 {@link Future}。
     */
    Future<QuicConnectionPathStats> collectPathStats(int pathIdx, Promise<QuicConnectionPathStats> promise);

    /**
     * 基于给定传输层 {@link Channel} 创建客户端 {@link QuicChannelBootstrap}。
     *
     * @param channel   作为 UDP 传输的 {@link Channel}。
     * @return          用于引导客户端 {@link QuicChannel} 的 bootstrap。
     */
    static QuicChannelBootstrap newBootstrap(Channel channel) {
        return new QuicChannelBootstrap(channel);
    }
}
