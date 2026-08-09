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

import io.netty.channel.ChannelHandlerContext;

/**
 * HTTP/2 流控控制器公共接口，本地/远端实现分别管理入站消费与出站发送窗口。
 */
public interface Http2FlowController {
    /**
     * 绑定将要应用流控的 {@link ChannelHandlerContext}；未调用视为编程错误。
     * <p>
     * This <strong>must</strong> be called to properly initialize the {@link Http2FlowController}.
     * Not calling this is considered a programming error.
     * @param ctx The {@link ChannelHandlerContext} for which to apply flow control on.
     * @throws Http2Exception if any protocol-related error occurred.
     */
    void channelHandlerContext(ChannelHandlerContext ctx) throws Http2Exception;

    /**
     * 更新连接级初始窗口，并按增量同步调整各流窗口（连接流本身除外）。
     * <p>对应 {@code SETTINGS_INITIAL_WINDOW_SIZE}，通常由收到 SETTINGS 帧时内部调用。
     *
     * @param newWindowSize the new initial window size.
     * @throws Http2Exception thrown if any protocol-related error occurred.
     */
    void initialWindowSize(int newWindowSize) throws Http2Exception;

    /**
     * 返回新建流时使用的初始窗口大小；默认应为 {@link Http2CodecUtil#DEFAULT_WINDOW_SIZE}。
     */
    int initialWindowSize();

    /**
     * 指定流当前可用于发送/接收受流控帧的字节数（窗口余量）。
     */
    int windowSize(Http2Stream stream);

    /**
     * 将流的流控窗口扩大 {@code delta} 字节。
     * <p>远端控制器在收到 {@code WINDOW_UPDATE} 时调用；本地控制器可主动请求扩大对端可见窗口。
     *
     * @param stream The subject stream. Use {@link Http2Connection#connectionStream()} for
     *            requesting the size of the connection window.
     * @param delta the change in size of the flow control window.
     * @throws Http2Exception thrown if a protocol-related error occurred.
     */
    void incrementWindowSize(Http2Stream stream, int delta) throws Http2Exception;
}
