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
 * HTTP/2 出站编码门面，继承 {@link Http2FrameWriter} 并集成连接状态、远端流控与 SETTINGS 队列。
 */
public interface Http2ConnectionEncoder extends Http2FrameWriter {

    /**
     * 绑定生命周期管理器，编码器初始化阶段必须调用。
     */
    void lifecycleManager(Http2LifecycleManager lifecycleManager);

    /**
     * Provides direct access to the underlying connection.
     */
    Http2Connection connection();

    /**
     * Provides the remote flow controller for managing outbound traffic.
     */
    Http2RemoteFlowController flowController();

    /**
     * Provides direct access to the underlying frame writer object.
     */
    Http2FrameWriter frameWriter();

    /**
     * 取出已发送但尚未 ACK 的本地 SETTINGS（队列头）；无则 {@code null}。
     */
    Http2Settings pollSentSettings();

    /**
     * Sets the settings for the remote endpoint of the HTTP/2 connection.
     */
    void remoteSettings(Http2Settings settings) throws Http2Exception;

    /**
     * 绕过连接/流状态检查，直接向底层 {@link Http2FrameWriter} 写原始帧（内部/测试用途）。
     */
    @Override
    ChannelFuture writeFrame(ChannelHandlerContext ctx, byte frameType, int streamId,
            Http2Flags flags, ByteBuf payload, ChannelPromise promise);
}
