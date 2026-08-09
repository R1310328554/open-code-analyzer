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

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.MessageSizeEstimator;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.socket.DuplexChannelConfig;

/**
 * QUIC 流的 {@link DuplexChannelConfig}，扩展帧模式与半关闭等选项。
 */
public interface QuicStreamChannelConfig extends DuplexChannelConfig {
    /**
     * {@code true} 时读取 {@link QuicStreamFrame} 并沿 pipeline 传播；{@code false} 时使用 {@link io.netty.buffer.ByteBuf}。
     *
     * @param readFrames    {@code true} if {@link QuicStreamFrame}s should be used, {@code false} if
     *                      {@link io.netty.buffer.ByteBuf} should be used.
     * @return              this instance itself.
     *
     */
    QuicStreamChannelConfig setReadFrames(boolean readFrames);

    /**
     * 是否以 {@link QuicStreamFrame} 模式读取。
     *
     * @return  {@code true} if {@link QuicStreamFrame}s should be used, {@code false} if
     *          {@link io.netty.buffer.ByteBuf} should be used.
     */
    boolean isReadFrames();

    @Override
    QuicStreamChannelConfig setAllowHalfClosure(boolean allowHalfClosure);

    @Override
    QuicStreamChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead);

    @Override
    QuicStreamChannelConfig setWriteSpinCount(int writeSpinCount);

    @Override
    QuicStreamChannelConfig setAllocator(ByteBufAllocator allocator);

    @Override
    QuicStreamChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator);

    @Override
    QuicStreamChannelConfig setAutoRead(boolean autoRead);

    @Override
    QuicStreamChannelConfig setAutoClose(boolean autoClose);

    @Override
    QuicStreamChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator);

    @Override
    QuicStreamChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark);

    @Override
    QuicStreamChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis);

    @Override
    QuicStreamChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark);

    @Override
    QuicStreamChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark);
}
