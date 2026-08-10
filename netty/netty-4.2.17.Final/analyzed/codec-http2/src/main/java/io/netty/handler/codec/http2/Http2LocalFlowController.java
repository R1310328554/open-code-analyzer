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

/**
 * 入站流控控制器：管理远端发来的 {@code DATA} 帧，维护连接级与流级接收窗口。
 * <p>实现 {@link Http2FlowController}，负责校验入站数据是否超出窗口，并在应用消费后发送
 * {@code WINDOW_UPDATE} 归还信用额度。
 */
public interface Http2LocalFlowController extends Http2FlowController {
    /**
     * 设置用于发送 {@code WINDOW_UPDATE} 帧的写入器；必须在收到任何受流控约束的数据之前调用。
     *
     * @param frameWriter the HTTP/2 frame writer.
     */
    Http2LocalFlowController frameWriter(Http2FrameWriter frameWriter);

    /**
     * 处理远端入站 {@code DATA} 帧，对指定流与连接同时扣减接收窗口。
     * <p>若违反流控策略则立即抛出异常；否则视为通过流控检查。{@code stream} 为 {@code null}
     * 或已关闭时，仅作用于连接级窗口且字节立即视为已消费。
     *
     * @param stream the subject stream for the received frame. The connection stream object must not be used. If {@code
     * stream} is {@code null} or closed, flow control should only be applied to the connection window and the bytes are
     * immediately consumed.
     * @param data payload buffer for the frame.
     * @param padding additional bytes that should be added to obscure the true content size. Must be between 0 and
     *                256 (inclusive).
     * @param endOfStream Indicates whether this is the last frame to be sent from the remote endpoint for this stream.
     * @throws Http2Exception if any flow control errors are encountered.
     */
    void receiveFlowControlledFrame(Http2Stream stream, ByteBuf data, int padding,
                                    boolean endOfStream) throws Http2Exception;

    /**
     * 通知控制器应用已消费 {@code numBytes} 字节，可据此向远端发送 {@code WINDOW_UPDATE} 扩大接收窗口。
     * <p>应用必须及时消费已接收字节，否则窗口耗尽后将无法继续接收数据。
     * {@code stream} 为 {@code null} 或已关闭（{@link Http2Stream.State#CLOSED}）时调用无效。
     *
     * @param stream the stream for which window space should be freed. The connection stream object must not be used.
     * If {@code stream} is {@code null} or closed (i.e. {@link Http2Stream#state()} method returns {@link
     * Http2Stream.State#CLOSED}), calling this method has no effect.
     * @param numBytes the number of bytes to be returned to the flow control window.
     * @return true if a {@code WINDOW_UPDATE} was sent, false otherwise.
     * @throws Http2Exception if the number of bytes returned exceeds the {@link #unconsumedBytes(Http2Stream)} for the
     * stream.
     */
    boolean consumeBytes(Http2Stream stream, int numBytes) throws Http2Exception;

    /**
     * 返回指定流上已接收但尚未被应用消费的字节数。
     *
     * @param stream the stream for which window space should be freed.
     * @return the number of unconsumed bytes for the stream.
     */
    int unconsumedBytes(Http2Stream stream);

    /**
     * 获取指定流的初始流控窗口大小（字节）。
     * <p>不可用窗口部分可通过 {@link #initialWindowSize()} - {@link #windowSize(Http2Stream)} 计算。
     */
    int initialWindowSize(Http2Stream stream);
}
