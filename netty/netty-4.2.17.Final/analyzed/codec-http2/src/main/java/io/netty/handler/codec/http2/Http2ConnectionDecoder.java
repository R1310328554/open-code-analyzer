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
import io.netty.channel.ChannelHandlerContext;

import java.io.Closeable;
import java.util.List;

/**
 * {@link Http2ConnectionHandler} 的入站解码门面：在调用 {@link Http2FrameListener} 前做协议一致性校验。
 * <p>未知帧类型（扩展帧）跳过校验直接交给 listener；标准帧违反 RFC 7540 则抛 {@link Http2Exception}。
 */
public interface Http2ConnectionDecoder extends Closeable {

    /**
     * 绑定生命周期管理器，解码器初始化阶段必须调用。
     */
    void lifecycleManager(Http2LifecycleManager lifecycleManager);

    /**
     * Provides direct access to the underlying connection.
     */
    Http2Connection connection();

    /**
     * Provides the local flow controller for managing inbound traffic.
     */
    Http2LocalFlowController flowController();

    /**
     * Set the {@link Http2FrameListener} which will be notified when frames are decoded.
     * <p>
     * This <strong>must</strong> be set before frames are decoded.
     */
    void frameListener(Http2FrameListener listener);

    /**
     * Get the {@link Http2FrameListener} which will be notified when frames are decoded.
     */
    Http2FrameListener frameListener();

    /**
     * 由 {@link Http2ConnectionHandler} 调用，从输入缓冲解码下一帧并写入 {@code out} 列表。
     */
    void decodeFrame(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Http2Exception;

    /**
     * Gets the local settings for this endpoint of the HTTP/2 connection.
     */
    Http2Settings localSettings();

    /**
     * 是否已收到对端首个 SETTINGS 帧（连接前言握手完成标志之一）。
     */
    boolean prefaceReceived();

    @Override
    void close();
}
