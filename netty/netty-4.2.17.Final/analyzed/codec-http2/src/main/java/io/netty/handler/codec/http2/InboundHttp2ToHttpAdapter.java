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
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpStatusClass;
import io.netty.handler.codec.http.HttpUtil;

import static io.netty.handler.codec.http2.Http2Error.ENHANCE_YOUR_CALM;
import static io.netty.handler.codec.http2.Http2Error.PROTOCOL_ERROR;
import static io.netty.handler.codec.http2.Http2Exception.connectionError;
import static io.netty.handler.codec.http2.Http2Exception.streamError;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.util.internal.ObjectUtil.checkNotNull;
import static io.netty.util.internal.ObjectUtil.checkPositive;

/**
 * 将入站 HTTP/2 帧事件适配为 RFC 7540 §8.1 定义的 HTTP 消息语义（仅 HEADERS + DATA）。
 * <p>
 * 出站 HTTP/1.x → HTTP/2 帧转换见 {@link HttpToHttp2ConnectionHandler}。
 */
public class InboundHttp2ToHttpAdapter extends Http2EventAdapter {
    /** 检测 1xx 响应或 Expect: 100-continue 等需立即向上游投递的场景。 */
    private static final ImmediateSendDetector DEFAULT_SEND_DETECTOR = new ImmediateSendDetector() {
        @Override
        public boolean mustSendImmediately(FullHttpMessage msg) {
            if (msg instanceof FullHttpResponse) {
                return ((FullHttpResponse) msg).status().codeClass() == HttpStatusClass.INFORMATIONAL;
            }
            if (msg instanceof FullHttpRequest) {
                return msg.headers().contains(HttpHeaderNames.EXPECT);
            }
            return false;
        }

        @Override
        public FullHttpMessage copyIfNeeded(ByteBufAllocator allocator, FullHttpMessage msg) {
            if (msg instanceof FullHttpRequest) {
                FullHttpRequest copy = ((FullHttpRequest) msg).replace(allocator.buffer(0));
                copy.headers().remove(HttpHeaderNames.EXPECT);
                return copy;
            }
            return null;
        }
    };

    /** 单条消息允许累积的最大正文长度，超出则 RST_STREAM(ENHANCE_YOUR_CALM)。 */
    private final int maxContentLength;
    private final ImmediateSendDetector sendDetector;
    /** 在 {@link Http2Stream} 上挂载进行中的 {@link FullHttpMessage}。 */
    private final Http2Connection.PropertyKey messageKey;
    /** 是否将 SETTINGS 帧继续 fireChannelRead 给下游。 */
    private final boolean propagateSettings;
    protected final Http2Connection connection;
    protected final boolean validateHttpHeaders;

    protected InboundHttp2ToHttpAdapter(Http2Connection connection, int maxContentLength,
                                        boolean validateHttpHeaders, boolean propagateSettings) {
        this.connection = checkNotNull(connection, "connection");
        this.maxContentLength = checkPositive(maxContentLength, "maxContentLength");
        this.validateHttpHeaders = validateHttpHeaders;
        this.propagateSettings = propagateSettings;
        sendDetector = DEFAULT_SEND_DETECTOR;
        messageKey = connection.newKey();
    }

    /**
     * 流已超出 HTTP 消息流跟踪范围，清除关联的 {@link FullHttpMessage}。
     * @param stream The stream to remove associated state with
     * @param release {@code true} to call release on the value if it is present. {@code false} to not call release.
     */
    protected final void removeMessage(Http2Stream stream, boolean release) {
        FullHttpMessage msg = stream.removeProperty(messageKey);
        if (release && msg != null) {
            msg.release();
        }
    }

    /**
     * Get the {@link FullHttpMessage} associated with {@code stream}.
     * @param stream The stream to get the associated state from
     * @return The {@link FullHttpMessage} associated with {@code stream}.
     */
    protected final FullHttpMessage getMessage(Http2Stream stream) {
        return (FullHttpMessage) stream.getProperty(messageKey);
    }

    /**
     * Make {@code message} be the state associated with {@code stream}.
     * @param stream The stream which {@code message} is associated with.
     * @param message The message which contains the HTTP semantics.
     */
    protected final void putMessage(Http2Stream stream, FullHttpMessage message) {
        FullHttpMessage previous = stream.setProperty(messageKey, message);
        if (previous != message && previous != null) {
            previous.release();
        }
    }

    @Override
    public void onStreamRemoved(Http2Stream stream) {
        removeMessage(stream, true);
    }

    /**
     * 设置 Content-Length 并向 pipeline 投递完整消息。
     *
     * @param ctx The context to fire the event on
     * @param msg The message to send
     * @param release {@code true} to call release on the value if it is present. {@code false} to not call release.
     * @param stream the stream of the message which is being fired
     */
    protected void fireChannelRead(ChannelHandlerContext ctx, FullHttpMessage msg, boolean release,
                                   Http2Stream stream) {
        removeMessage(stream, release);
        HttpUtil.setContentLength(msg, msg.content().readableBytes());
        ctx.fireChannelRead(msg);
    }

    /**
     * 根据连接角色（服务端/客户端）构造 {@link FullHttpRequest} 或 {@link FullHttpResponse}。
     *
     * @param stream The stream to create a message for
     * @param headers The headers associated with {@code stream}
     * @param validateHttpHeaders
     * <ul>
     * <li>{@code true} to validate HTTP headers in the http-codec</li>
     * <li>{@code false} not to validate HTTP headers in the http-codec</li>
     * </ul>
     * @param alloc The {@link ByteBufAllocator} to use to generate the content of the message
     * @throws Http2Exception If there is an error when creating {@link FullHttpMessage} from
     *                        {@link Http2Stream} and {@link Http2Headers}
     */
    protected FullHttpMessage newMessage(Http2Stream stream, Http2Headers headers, boolean validateHttpHeaders,
                                         ByteBufAllocator alloc) throws Http2Exception {
        return connection.isServer() ? HttpConversionUtil.toFullHttpRequest(stream.id(), headers, alloc,
                validateHttpHeaders) : HttpConversionUtil.toFullHttpResponse(stream.id(), headers, alloc,
                validateHttpHeaders);
    }

    /**
     * HEADERS 帧处理的入口：新建或追加消息，并处理 1xx/Expect 等需立即投递的情况。
     *
     * @param ctx The context for which this message has been received.
     * Used to send informational header if detected.
     * @param stream The stream the {@code headers} apply to
     * @param headers The headers to process
     * @param endOfStream {@code true} if the {@code stream} has received the end of stream flag
     * @param allowAppend
     * <ul>
     * <li>{@code true} if headers will be appended if the stream already exists.</li>
     * <li>if {@code false} and the stream already exists this method returns {@code null}.</li>
     * </ul>
     * @param appendToTrailer
     * <ul>
     * <li>{@code true} if a message {@code stream} already exists then the headers
     * should be added to the trailing headers.</li>
     * <li>{@code false} then appends will be done to the initial headers.</li>
     * </ul>
     * @return The object used to track the stream corresponding to {@code stream}. {@code null} if
     *         {@code allowAppend} is {@code false} and the stream already exists.
     * @throws Http2Exception If the stream id is not in the correct state to process the headers request
     */
    protected FullHttpMessage processHeadersBegin(ChannelHandlerContext ctx, Http2Stream stream, Http2Headers headers,
                                                  boolean endOfStream, boolean allowAppend, boolean appendToTrailer)
            throws Http2Exception {
        FullHttpMessage msg = getMessage(stream);
        boolean release = true;
        if (msg == null) {
            msg = newMessage(stream, headers, validateHttpHeaders, ctx.alloc());
        } else if (allowAppend) {
            release = false;
            HttpConversionUtil.addHttp2ToHttpHeaders(stream.id(), headers, msg, appendToTrailer);
        } else {
            release = false;
            msg = null;
        }

        if (sendDetector.mustSendImmediately(msg)) {
            // 1xx 或 Expect 场景：先投递当前消息，必要时复制一份继续累积后续 DATA
            final FullHttpMessage copy = endOfStream ? null : sendDetector.copyIfNeeded(ctx.alloc(), msg);
            fireChannelRead(ctx, msg, release, stream);
            return copy;
        }

        return msg;
    }

    /**
     * {@link #processHeadersBegin} 之后的收尾：END_STREAM 则投递，否则暂存待 DATA 追加。
     *
     * @param ctx The context for which this message has been received
     * @param stream The stream the {@code objAccumulator} corresponds to
     * @param msg The object which represents all headers/data for corresponding to {@code stream}
     * @param endOfStream {@code true} if this is the last event for the stream
     */
    private void processHeadersEnd(ChannelHandlerContext ctx, Http2Stream stream, FullHttpMessage msg,
                                   boolean endOfStream) {
        if (endOfStream) {
            // 若 map 中仍是另一实例，需 release 旧引用
            fireChannelRead(ctx, msg, getMessage(stream) != msg, stream);
        } else {
            putMessage(stream, msg);
        }
    }

    @Override
    public int onDataRead(ChannelHandlerContext ctx, int streamId, ByteBuf data, int padding, boolean endOfStream)
            throws Http2Exception {
        Http2Stream stream = connection.stream(streamId);
        FullHttpMessage msg = getMessage(stream);
        if (msg == null) {
            throw connectionError(PROTOCOL_ERROR, "Data Frame received for unknown stream id %d", streamId);
        }

        ByteBuf content = msg.content();
        final int dataReadableBytes = data.readableBytes();
        if (content.readableBytes() > maxContentLength - dataReadableBytes) {
            throw streamError(streamId, ENHANCE_YOUR_CALM,
                    "Content length exceeded max of %d for stream id %d", maxContentLength, streamId);
        }

        content.writeBytes(data, data.readerIndex(), dataReadableBytes);

        if (endOfStream) {
            fireChannelRead(ctx, msg, false, stream);
        }

        // 所有 DATA 字节已计入流控窗口
        return dataReadableBytes + padding;
    }

    @Override
    public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int padding,
                              boolean endOfStream) throws Http2Exception {
        Http2Stream stream = connection.stream(streamId);
        FullHttpMessage msg = processHeadersBegin(ctx, stream, headers, endOfStream, true, true);
        if (msg != null) {
            processHeadersEnd(ctx, stream, msg, endOfStream);
        }
    }

    @Override
    public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int streamDependency,
                              short weight, boolean exclusive, int padding, boolean endOfStream)
            throws Http2Exception {
        Http2Stream stream = connection.stream(streamId);
        FullHttpMessage msg = processHeadersBegin(ctx, stream, headers, endOfStream, true, true);
        if (msg != null) {
            // 记录 PRIORITY 信息到扩展头，供反向转换时使用
            // See https://github.com/netty/netty/issues/5866
            if (streamDependency != Http2CodecUtil.CONNECTION_STREAM_ID) {
                msg.headers().setInt(HttpConversionUtil.ExtensionHeaderNames.STREAM_DEPENDENCY_ID.text(),
                        streamDependency);
            }
            msg.headers().setShort(HttpConversionUtil.ExtensionHeaderNames.STREAM_WEIGHT.text(), weight);

            processHeadersEnd(ctx, stream, msg, endOfStream);
        }
    }

    @Override
    public void onRstStreamRead(ChannelHandlerContext ctx, int streamId, long errorCode) throws Http2Exception {
        Http2Stream stream = connection.stream(streamId);
        FullHttpMessage msg = getMessage(stream);
        if (msg != null) {
            onRstStreamRead(stream, msg);
        }
        ctx.fireExceptionCaught(Http2Exception.streamError(streamId, Http2Error.valueOf(errorCode),
                "HTTP/2 to HTTP layer caught stream reset"));
    }

    @Override
    public void onPushPromiseRead(ChannelHandlerContext ctx, int streamId, int promisedStreamId,
                                  Http2Headers headers, int padding) throws Http2Exception {
        // PUSH_PROMISE 不允许向已存在消息的流追加头部
        Http2Stream promisedStream = connection.stream(promisedStreamId);
        if (headers.status() == null) {
            // PUSH_PROMISE 无状态码，语义上等价于服务端主动发起的请求，补默认 200
            headers.status(OK.codeAsText());
        }
        FullHttpMessage msg = processHeadersBegin(ctx, promisedStream, headers, false, false, false);
        if (msg == null) {
            throw connectionError(PROTOCOL_ERROR, "Push Promise Frame received for pre-existing stream id %d",
                    promisedStreamId);
        }

        msg.headers().setInt(HttpConversionUtil.ExtensionHeaderNames.STREAM_PROMISE_ID.text(), streamId);
        msg.headers().setShort(HttpConversionUtil.ExtensionHeaderNames.STREAM_WEIGHT.text(),
                Http2CodecUtil.DEFAULT_PRIORITY_WEIGHT);

        processHeadersEnd(ctx, promisedStream, msg, false);
    }

    @Override
    public void onSettingsRead(ChannelHandlerContext ctx, Http2Settings settings) throws Http2Exception {
        if (propagateSettings) {
            // 允许非 Http2FrameListener 的 handler 感知 SETTINGS 变更
            ctx.fireChannelRead(settings);
        }
    }

    /**
     * 收到 RST_STREAM 且流上仍有未投递消息时的清理钩子。
     */
    protected void onRstStreamRead(Http2Stream stream, FullHttpMessage msg) {
        removeMessage(stream, true);
    }

    /**
     * 判断消息是否应在 END_STREAM 之前提前向上游投递（如 1xx、Expect: 100-continue）。
     */
    private interface ImmediateSendDetector {
        /**
         * 是否立即 fireChannelRead，而非等待 END_STREAM。
         *
         * @param msg The response to test
         * @return {@code true} if the message should be sent immediately
         *         {@code false) if we should wait for the end of the stream
         */
        boolean mustSendImmediately(FullHttpMessage msg);

        /**
         * 立即投递后，若仍需继续接收 DATA，返回需继续累积的消息副本。
         * <p>
         * 典型场景：Expect: 100-continue —— 先投递带 Expect 的请求头，再等待正文。
         *
         * @param allocator The {@link ByteBufAllocator} that can be used to allocate
         * @param msg The message which has just been sent due to {@link #mustSendImmediately(FullHttpMessage)}
         * @return A modified copy of the {@code msg} or {@code null} if a copy is not needed.
         */
        FullHttpMessage copyIfNeeded(ByteBufAllocator allocator, FullHttpMessage msg);
    }
}
