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

/**
 * HTTP/2 帧读取器：从 {@link ByteBuf} 增量解析帧，完整帧就绪后回调 {@link Http2FrameListener}。
 * <p>典型实现（如 {@code DefaultHttp2FrameReader}）会维护解析状态，数据不足时静默等待更多字节。
 */
public interface Http2FrameReader extends Closeable {
    /**
     * {@link Http2FrameReader} 的运行时可调配置。
     */
    interface Configuration {
        /**
         * 返回 HPACK 头块解码配置（动态表大小等）。
         */
        Http2HeadersDecoder.Configuration headersConfiguration();

        /**
         * 返回帧载荷最大长度策略，对应 SETTINGS_MAX_FRAME_SIZE。
         */
        Http2FrameSizePolicy frameSizePolicy();
    }

    /**
     * 尝试从输入缓冲读取下一帧；数据足够时解析并通知 listener，不足则留待下次调用。
     *
     * @param ctx 当前 channel handler 上下文
     * @param input 待解析的字节缓冲（reader index 会随已消费数据前移）
     * @param listener 帧解析完成后的回调
     */
    void readFrame(ChannelHandlerContext ctx, ByteBuf input, Http2FrameListener listener)
            throws Http2Exception;

    /**
     * 获取本 reader 的配置对象，用于响应 SETTINGS 帧更新解码参数。
     */
    Configuration configuration();

    /**
     * 关闭 reader 并释放内部资源（如 HPACK 解码器状态）。
     */
    @Override
    void close();
}
