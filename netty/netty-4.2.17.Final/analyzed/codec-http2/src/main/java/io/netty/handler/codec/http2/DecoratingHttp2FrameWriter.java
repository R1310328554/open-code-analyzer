/*
 * Copyright 2015 The Netty Project
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

import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * 对另一个 {@link Http2FrameWriter} 的装饰器基类：所有写帧方法均透明转发给被包装实例。
 * <p>子类可覆写个别方法以插入日志、指标或协议扩展，而无需重写全部帧类型。
 */
public class DecoratingHttp2FrameWriter implements Http2FrameWriter {
    /** 实际执行帧序列化与写入的委托对象。 */
    private final Http2FrameWriter delegate;

    /**
     * @param delegate 非 {@code null} 的底层帧写入器
     */
    public DecoratingHttp2FrameWriter(Http2FrameWriter delegate) {
        this.delegate = checkNotNull(delegate, "delegate");
    }

    @Override
    public ChannelFuture writeData(ChannelHandlerContext ctx, int streamId, ByteBuf data, int padding,
                                   boolean endStream, ChannelPromise promise) {
        return delegate.writeData(ctx, streamId, data, padding, endStream, promise);
    }

    @Override
    public ChannelFuture writeHeaders(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int padding,
                                      boolean endStream, ChannelPromise promise) {
        return delegate.writeHeaders(ctx, streamId, headers, padding, endStream, promise);
    }

    @Override
    public ChannelFuture writeHeaders(ChannelHandlerContext ctx, int streamId, Http2Headers headers,
                                      int streamDependency, short weight, boolean exclusive, int padding,
                                      boolean endStream, ChannelPromise promise) {
        return delegate
                .writeHeaders(ctx, streamId, headers, streamDependency, weight, exclusive, padding, endStream, promise);
    }

    @Override
    public ChannelFuture writePriority(ChannelHandlerContext ctx, int streamId, int streamDependency, short weight,
                                       boolean exclusive, ChannelPromise promise) {
        return delegate.writePriority(ctx, streamId, streamDependency, weight, exclusive, promise);
    }

    @Override
    public ChannelFuture writeRstStream(ChannelHandlerContext ctx, int streamId, long errorCode,
                                        ChannelPromise promise) {
        return delegate.writeRstStream(ctx, streamId, errorCode, promise);
    }

    @Override
    public ChannelFuture writeSettings(ChannelHandlerContext ctx, Http2Settings settings, ChannelPromise promise) {
        return delegate.writeSettings(ctx, settings, promise);
    }

    @Override
    public ChannelFuture writeSettingsAck(ChannelHandlerContext ctx, ChannelPromise promise) {
        return delegate.writeSettingsAck(ctx, promise);
    }

    @Override
    public ChannelFuture writePing(ChannelHandlerContext ctx, boolean ack, long data, ChannelPromise promise) {
        return delegate.writePing(ctx, ack, data, promise);
    }

    @Override
    public ChannelFuture writePushPromise(ChannelHandlerContext ctx, int streamId, int promisedStreamId,
                                          Http2Headers headers, int padding, ChannelPromise promise) {
        return delegate.writePushPromise(ctx, streamId, promisedStreamId, headers, padding, promise);
    }

    @Override
    public ChannelFuture writeGoAway(ChannelHandlerContext ctx, int lastStreamId, long errorCode, ByteBuf debugData,
                                     ChannelPromise promise) {
        return delegate.writeGoAway(ctx, lastStreamId, errorCode, debugData, promise);
    }

    @Override
    public ChannelFuture writeWindowUpdate(ChannelHandlerContext ctx, int streamId, int windowSizeIncrement,
                                           ChannelPromise promise) {
        return delegate.writeWindowUpdate(ctx, streamId, windowSizeIncrement, promise);
    }

    @Override
    public ChannelFuture writeFrame(ChannelHandlerContext ctx, byte frameType, int streamId, Http2Flags flags,
                                    ByteBuf payload, ChannelPromise promise) {
        return delegate.writeFrame(ctx, frameType, streamId, flags, payload, promise);
    }

    @Override
    public Configuration configuration() {
        return delegate.configuration();
    }

    @Override
    public void close() {
        delegate.close();
    }
}
