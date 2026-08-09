/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.netty.handler.codec.http2;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * HTTP/2 连接生命周期管理器：协调流关闭、RST_STREAM、GOAWAY 与 channel 关闭。
 * <p>核心职责是在所有活跃流结束后才关闭底层 channel，实现优雅停机。
 */
public interface Http2LifecycleManager {

    /**
     * 半关闭本地端（发送 END_STREAM）；视流状态可能触发完整关闭。
     * @param stream the stream to be half closed.
     * @param future See {@link #closeStream(Http2Stream, ChannelFuture)}.
     */
    void closeStreamLocal(Http2Stream stream, ChannelFuture future);

    /**
     * 半关闭远端（收到 END_STREAM）；视流状态可能触发完整关闭。
     * @param stream the stream to be half closed.
     * @param future See {@link #closeStream(Http2Stream, ChannelFuture)}.
     */
    void closeStreamRemote(Http2Stream stream, ChannelFuture future);

    /**
     * 完全关闭并注销流；{@code future} 完成后若 {@link Http2Connection#numActiveStreams()} 为 0 则关闭 channel。
     * @param stream the stream to be closed and deactivated.
     * @param future when completed if {@link Http2Connection#numActiveStreams()} is 0 then the underlying channel
     * will be closed.
     */
    void closeStream(Http2Stream stream, ChannelFuture future);

    /**
     * 确保指定流被重置：本地尚未 reset 时发送 RST_STREAM，已 reset 则直接返回成功。
     * @param ctx The context used for communication and buffer allocation if necessary.
     * @param streamId The identifier of the stream to reset.
     * @param errorCode Justification as to why this stream is being reset. See {@link Http2Error}.
     * @param promise Used to indicate the return status of this operation.
     * @return Will be considered successful when the connection and stream state has been updated, and a
     * {@code RST_STREAM} frame has been sent to the peer. If the stream state has already been updated and a
     * {@code RST_STREAM} frame has been sent then the return status may indicate success immediately.
     */
    ChannelFuture resetStream(ChannelHandlerContext ctx, int streamId, long errorCode,
            ChannelPromise promise);

    /**
     * 禁止对端创建新流；{@code errorCode != NO_ERROR} 时关闭连接。
     * 本地此后只能创建 {@code streamId <= lastStreamId} 的流，必要时发送 GOAWAY。
     * @param ctx The context used for communication and buffer allocation if necessary.
     * @param lastStreamId The last stream that the local endpoint is claiming it will accept.
     * @param errorCode The rational as to why the connection is being closed. See {@link Http2Error}.
     * @param debugData For diagnostic purposes (carries no semantic value).
     * @param promise Used to indicate the return status of this operation.
     * @return Will be considered successful when the connection and stream state has been updated, and a
     * {@code GO_AWAY} frame has been sent to the peer. If the stream state has already been updated and a
     * {@code GO_AWAY} frame has been sent then the return status may indicate success immediately.
     */
    ChannelFuture goAway(ChannelHandlerContext ctx, int lastStreamId, long errorCode,
            ByteBuf debugData, ChannelPromise promise);

    /**
     * 处理连接级或流级错误；{@code outbound=true} 表示由出站操作引发，同时 fail 对应 {@link ChannelPromise}。
     *
     * @param ctx The context used for communication and buffer allocation if necessary.
     * @param outbound {@code true} if the error was caused by an outbound operation and so the corresponding
     * {@link ChannelPromise} was failed as well.
     * @param cause the error.
     */
    void onError(ChannelHandlerContext ctx, boolean outbound, Throwable cause);
}
