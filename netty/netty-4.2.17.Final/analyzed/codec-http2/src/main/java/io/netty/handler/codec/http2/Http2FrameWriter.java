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

import java.io.Closeable;

/**
 * HTTP/2 帧写入器：将各类帧序列化到 channel 出站缓冲。
 * <p>所有 write 方法仅写入 context，<strong>不会自动 flush</strong>；需另行调用 {@link ChannelHandlerContext#flush()}。
 */
public interface Http2FrameWriter extends Http2DataWriter, Closeable {
    /**
     * {@link Http2FrameWriter} 的运行时可调配置。
     */
    interface Configuration {
        /**
         * 返回 HPACK 头块编码配置。
         */
        Http2HeadersEncoder.Configuration headersConfiguration();

        /**
         * 返回帧载荷最大长度策略。
         */
        Http2FrameSizePolicy frameSizePolicy();
    }

    /**
     * 写入 HEADERS 帧。
     *
     * @param ctx the context to use for writing.
     * @param streamId the stream for which to send the frame.
     * @param headers the headers to be sent.
     * @param padding additional bytes that should be added to obscure the true content size. Must be between 0 and
     *                256 (inclusive).
     * @param endStream indicates if this is the last frame to be sent for the stream.
     * @param promise the promise for the write.
     * @return the future for the write.
     * <a href="https://tools.ietf.org/html/rfc7540#section-10.5.1">Section 10.5.1</a> states the following:
     * <pre>
     * The header block MUST be processed to ensure a consistent connection state, unless the connection is closed.
     * </pre>
     * If this call has modified the HPACK header state you <strong>MUST</strong> throw a connection error.
     * <p>
     * If this call has <strong>NOT</strong> modified the HPACK header state you are free to throw a stream error.
     */
    ChannelFuture writeHeaders(ChannelHandlerContext ctx, int streamId, Http2Headers headers,
                               int padding, boolean endStream, ChannelPromise promise);

    /**
     * 写入带优先级信息的 HEADERS 帧。
     *
     * @param ctx the context to use for writing.
     * @param streamId the stream for which to send the frame.
     * @param headers the headers to be sent.
     * @param streamDependency the stream on which this stream should depend, or 0 if it should
     *            depend on the connection.
     * @param weight the weight for this stream.
     * @param exclusive whether this stream should be the exclusive dependant of its parent.
     * @param padding additional bytes that should be added to obscure the true content size. Must be between 0 and
     *                256 (inclusive).
     * @param endStream indicates if this is the last frame to be sent for the stream.
     * @param promise the promise for the write.
     * @return the future for the write.
     * <a href="https://tools.ietf.org/html/rfc7540#section-10.5.1">Section 10.5.1</a> states the following:
     * <pre>
     * The header block MUST be processed to ensure a consistent connection state, unless the connection is closed.
     * </pre>
     * If this call has modified the HPACK header state you <strong>MUST</strong> throw a connection error.
     * <p>
     * If this call has <strong>NOT</strong> modified the HPACK header state you are free to throw a stream error.
     */
    ChannelFuture writeHeaders(ChannelHandlerContext ctx, int streamId, Http2Headers headers,
                               int streamDependency, short weight, boolean exclusive, int padding, boolean endStream,
                               ChannelPromise promise);

    /**
     * 写入 PRIORITY 帧，调整流的依赖树与权重。
     *
     * @param ctx the context to use for writing.
     * @param streamId the stream for which to send the frame.
     * @param streamDependency the stream on which this stream should depend, or 0 if it should
     *            depend on the connection.
     * @param weight the weight for this stream.
     * @param exclusive whether this stream should be the exclusive dependant of its parent.
     * @param promise the promise for the write.
     * @return the future for the write.
     */
    ChannelFuture writePriority(ChannelHandlerContext ctx, int streamId, int streamDependency,
            short weight, boolean exclusive, ChannelPromise promise);

    /**
     * 写入 RST_STREAM 帧，以 errorCode 终止指定流。
     *
     * @param ctx the context to use for writing.
     * @param streamId the stream for which to send the frame.
     * @param errorCode the error code indicating the nature of the failure.
     * @param promise the promise for the write.
     * @return the future for the write.
     */
    ChannelFuture writeRstStream(ChannelHandlerContext ctx, int streamId, long errorCode,
            ChannelPromise promise);

    /**
     * 写入 SETTINGS 帧，协商连接参数。
     *
     * @param ctx the context to use for writing.
     * @param settings the settings to be sent.
     * @param promise the promise for the write.
     * @return the future for the write.
     */
    ChannelFuture writeSettings(ChannelHandlerContext ctx, Http2Settings settings,
            ChannelPromise promise);

    /**
     * 写入 SETTINGS ACK，确认已应用对端 SETTINGS。
     *
     * @param ctx the context to use for writing.
     * @param promise the promise for the write.
     * @return the future for the write.
     */
    ChannelFuture writeSettingsAck(ChannelHandlerContext ctx, ChannelPromise promise);

    /**
     * 写入 PING 帧；{@code ack=true} 时为对入站 PING 的应答。
     *
     * @param ctx the context to use for writing.
     * @param ack indicates whether this is an ack of a PING frame previously received from the
     *            remote endpoint.
     * @param data the payload of the frame.
     * @param promise the promise for the write.
     * @return the future for the write.
     */
    ChannelFuture writePing(ChannelHandlerContext ctx, boolean ack, long data,
            ChannelPromise promise);

    /**
     * 写入 PUSH_PROMISE 帧，预告服务端推送流。
     *
     * @param ctx the context to use for writing.
     * @param streamId the stream for which to send the frame.
     * @param promisedStreamId the ID of the promised stream.
     * @param headers the headers to be sent.
     * @param padding additional bytes that should be added to obscure the true content size. Must be between 0 and
     *                256 (inclusive).
     * @param promise the promise for the write.
     * @return the future for the write.
     * <a href="https://tools.ietf.org/html/rfc7540#section-10.5.1">Section 10.5.1</a> states the following:
     * <pre>
     * The header block MUST be processed to ensure a consistent connection state, unless the connection is closed.
     * </pre>
     * If this call has modified the HPACK header state you <strong>MUST</strong> throw a connection error.
     * <p>
     * If this call has <strong>NOT</strong> modified the HPACK header state you are free to throw a stream error.
     */
    ChannelFuture writePushPromise(ChannelHandlerContext ctx, int streamId, int promisedStreamId,
                                   Http2Headers headers, int padding, ChannelPromise promise);

    /**
     * 写入 GO_AWAY 帧，发起连接级关闭；{@code debugData} 由本方法负责释放。
     *
     * @param ctx the context to use for writing.
     * @param lastStreamId the last known stream of this endpoint.
     * @param errorCode the error code, if the connection was abnormally terminated.
     * @param debugData application-defined debug data. This will be released by this method.
     * @param promise the promise for the write.
     * @return the future for the write.
     */
    ChannelFuture writeGoAway(ChannelHandlerContext ctx, int lastStreamId, long errorCode,
            ByteBuf debugData, ChannelPromise promise);

    /**
     * 写入 WINDOW_UPDATE 帧，增加本地入站流控窗口。
     *
     * @param ctx the context to use for writing.
     * @param streamId the stream for which to send the frame.
     * @param windowSizeIncrement the number of bytes by which the local inbound flow control window
     *            is increasing.
     * @param promise the promise for the write.
     * @return the future for the write.
     */
    ChannelFuture writeWindowUpdate(ChannelHandlerContext ctx, int streamId,
            int windowSizeIncrement, ChannelPromise promise);

    /**
     * 通用帧写入入口，用于非标准扩展帧类型；{@code payload} 由本方法负责释放。
     *
     * @param ctx the context to use for writing.
     * @param frameType the frame type identifier.
     * @param streamId the stream for which to send the frame.
     * @param flags the flags to write for this frame.
     * @param payload the payload to write for this frame. This will be released by this method.
     * @param promise the promise for the write.
     * @return the future for the write.
     */
    ChannelFuture writeFrame(ChannelHandlerContext ctx, byte frameType, int streamId,
            Http2Flags flags, ByteBuf payload, ChannelPromise promise);

    /**
     * 获取本 writer 的配置对象。
     */
    Configuration configuration();

    /**
     * 关闭 writer 并释放内部资源。
     */
    @Override
    void close();
}
