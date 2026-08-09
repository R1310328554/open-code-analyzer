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
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelPromise;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.util.AsciiString;
import io.netty.util.LeakPresenceDetector;
import io.netty.util.concurrent.EventExecutor;

import static io.netty.buffer.Unpooled.directBuffer;
import static io.netty.buffer.Unpooled.unreleasableBuffer;
import static io.netty.handler.codec.http2.Http2Error.PROTOCOL_ERROR;
import static io.netty.handler.codec.http2.Http2Exception.connectionError;
import static io.netty.handler.codec.http2.Http2Exception.headerListSizeError;
import static io.netty.util.CharsetUtil.UTF_8;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * HTTP/2 帧编解码常量与工具方法：连接前言、帧头格式、SETTINGS 标识符、流 ID 规则及流控辅助计算。
 */
public final class Http2CodecUtil {
    /** 连接级伪流 ID（stream 0），SETTINGS/PING/GOAWAY 等帧使用。 */
    public static final int CONNECTION_STREAM_ID = 0;
    /** h2c 升级后首个业务流 ID，客户端预留用于接收升级响应。 */
    public static final int HTTP_UPGRADE_STREAM_ID = 1;
    /** Upgrade 请求中携带本端 SETTINGS 的 HTTP/1 头名。 */
    public static final CharSequence HTTP_UPGRADE_SETTINGS_HEADER = AsciiString.cached("HTTP2-Settings");
    /** 明文升级协议标识（HTTP/1.1 Upgrade: h2c）。 */
    public static final CharSequence HTTP_UPGRADE_PROTOCOL_NAME = "h2c";
    /** TLS ALPN 协商的 HTTP/2 协议名。 */
    public static final CharSequence TLS_UPGRADE_PROTOCOL_NAME = ApplicationProtocolNames.HTTP_2;

    /** PING 帧固定 8 字节 opaque 载荷。 */
    public static final int PING_FRAME_PAYLOAD_LENGTH = 8;
    public static final short MAX_UNSIGNED_BYTE = 0xff;
    /**
     * 单帧 padding 上限：255 字节填充 + 1 字节 Pad Length 字段。
     */
    public static final int MAX_PADDING = 256;
    public static final long MAX_UNSIGNED_INT = 0xffffffffL;
    public static final int FRAME_HEADER_LENGTH = 9;
    public static final int SETTING_ENTRY_LENGTH = 6;
    public static final int PRIORITY_ENTRY_LENGTH = 5;
    public static final int INT_FIELD_LENGTH = 4;
    public static final short MAX_WEIGHT = 256;
    public static final short MIN_WEIGHT = 1;

    /** RFC 7540 连接前言（24 字节 magic string），握手首包必须发送/接收。 */
    private static final ByteBuf CONNECTION_PREFACE = LeakPresenceDetector.staticInitializer(() ->
            unreleasableBuffer(directBuffer(24).writeBytes("PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n".getBytes(UTF_8)))
                    .asReadOnly());

    private static final int MAX_PADDING_LENGTH_LENGTH = 1;
    public static final int DATA_FRAME_HEADER_LENGTH = FRAME_HEADER_LENGTH + MAX_PADDING_LENGTH_LENGTH;
    public static final int HEADERS_FRAME_HEADER_LENGTH =
            FRAME_HEADER_LENGTH + MAX_PADDING_LENGTH_LENGTH + INT_FIELD_LENGTH + 1;
    public static final int PRIORITY_FRAME_LENGTH = FRAME_HEADER_LENGTH + PRIORITY_ENTRY_LENGTH;
    public static final int RST_STREAM_FRAME_LENGTH = FRAME_HEADER_LENGTH + INT_FIELD_LENGTH;
    public static final int PUSH_PROMISE_FRAME_HEADER_LENGTH =
            FRAME_HEADER_LENGTH + MAX_PADDING_LENGTH_LENGTH + INT_FIELD_LENGTH;
    public static final int GO_AWAY_FRAME_HEADER_LENGTH = FRAME_HEADER_LENGTH + 2 * INT_FIELD_LENGTH;
    public static final int WINDOW_UPDATE_FRAME_LENGTH = FRAME_HEADER_LENGTH + INT_FIELD_LENGTH;
    public static final int CONTINUATION_FRAME_HEADER_LENGTH = FRAME_HEADER_LENGTH;

    public static final char SETTINGS_HEADER_TABLE_SIZE = 1;
    public static final char SETTINGS_ENABLE_PUSH = 2;
    public static final char SETTINGS_MAX_CONCURRENT_STREAMS = 3;
    public static final char SETTINGS_INITIAL_WINDOW_SIZE = 4;
    public static final char SETTINGS_MAX_FRAME_SIZE = 5;
    public static final char SETTINGS_MAX_HEADER_LIST_SIZE = 6;
    public static final char SETTINGS_ENABLE_CONNECT_PROTOCOL = 8;
    public static final int NUM_STANDARD_SETTINGS = 7;

    public static final long MAX_HEADER_TABLE_SIZE = MAX_UNSIGNED_INT;
    public static final long MAX_CONCURRENT_STREAMS = MAX_UNSIGNED_INT;
    public static final int MAX_INITIAL_WINDOW_SIZE = Integer.MAX_VALUE;
    public static final int MAX_FRAME_SIZE_LOWER_BOUND = 0x4000;
    public static final int MAX_FRAME_SIZE_UPPER_BOUND = 0xffffff;
    public static final long MAX_HEADER_LIST_SIZE = MAX_UNSIGNED_INT;

    public static final long MIN_HEADER_TABLE_SIZE = 0;
    public static final long MIN_CONCURRENT_STREAMS = 0;
    public static final int MIN_INITIAL_WINDOW_SIZE = 0;
    public static final long MIN_HEADER_LIST_SIZE = 0;

    public static final int DEFAULT_WINDOW_SIZE = 65535;
    public static final short DEFAULT_PRIORITY_WEIGHT = 16;
    public static final int DEFAULT_HEADER_TABLE_SIZE = 4096;
    /**
     * RFC 7540 规定 SETTINGS_MAX_HEADER_LIST_SIZE 初始为「无限制」；
     * 实践中默认 8192 字节，利用规范允许的对端可强制执行更低上限的条款限制内存占用。
     */
    public static final long DEFAULT_HEADER_LIST_SIZE = 8192;
    public static final int DEFAULT_MAX_FRAME_SIZE = MAX_FRAME_SIZE_LOWER_BOUND;
    /**
     * The assumed minimum value for {@code SETTINGS_MAX_CONCURRENT_STREAMS} as
     * recommended by the <a href="https://tools.ietf.org/html/rfc7540#section-6.5.2">HTTP/2 spec</a>.
     */
    public static final int SMALLEST_MAX_CONCURRENT_STREAMS = 100;
    static final int DEFAULT_MAX_RESERVED_STREAMS = SMALLEST_MAX_CONCURRENT_STREAMS;
    static final int DEFAULT_MIN_ALLOCATION_CHUNK = 1024;
    static final int DEFAULT_MAX_SMALL_CONTINUATION_FRAME = 16;

    /**
     * While the RFC only specified a minimum we should still pick a default which is good enough that most people
     * no need to adjust it but still be somewhat protected. Let's use the minimum
     * defined by the <a href="https://tools.ietf.org/html/rfc7540#section-6.5.2">HTTP/2 spec</a>.
     */
    static final int DEFAULT_MAX_CONCURRENT_STREAMS = SMALLEST_MAX_CONCURRENT_STREAMS;

    /**
     * 计算触发 GOAWAY 的头列表大小阈值：为 {@code maxHeaderListSize * 1.25}（用位移避免浮点）。
     * @param maxHeaderListSize
     *      本端 {@link #SETTINGS_MAX_HEADER_LIST_SIZE} 当前值。
     * @return 超过此字节数应发送 GOAWAY 而非仅 RST_STREAM。
     */
    public static long calculateMaxHeaderListSizeGoAway(long maxHeaderListSize) {
        // 等价于 maxHeaderListSize * 1.25
        return maxHeaderListSize + (maxHeaderListSize >>> 2);
    }

    public static final long DEFAULT_GRACEFUL_SHUTDOWN_TIMEOUT_MILLIS = MILLISECONDS.convert(30, SECONDS);

    public static final int DEFAULT_MAX_QUEUED_CONTROL_FRAMES = 10000;

    /**
     * 判断 streamId 是否为本端（由 {@code server} 标识的角色）发起的出站流。
     * <p>规则：客户端用奇数 ID，服务器用偶数 ID（0 为连接级）。
     *
     * @param server    {@code true} 表示本端为服务器。
     * @param streamId  流标识符。
     */
    public static boolean isOutboundStream(boolean server, int streamId) {
        boolean even = (streamId & 1) == 0;
        return streamId > 0 && server == even;
    }

    /**
     * Returns true if the {@code streamId} is a valid HTTP/2 stream identifier.
     */
    public static boolean isStreamIdValid(int streamId) {
        return streamId >= 0;
    }

    static boolean isStreamIdValid(int streamId, boolean server) {
        return isStreamIdValid(streamId) && server == ((streamId & 1) == 0);
    }

    /**
     * Indicates whether or not the given value for max frame size falls within the valid range.
     */
    public static boolean isMaxFrameSizeValid(int maxFrameSize) {
        return maxFrameSize >= MAX_FRAME_SIZE_LOWER_BOUND && maxFrameSize <= MAX_FRAME_SIZE_UPPER_BOUND;
    }

    /**
     * 返回连接前言 {@link #CONNECTION_PREFACE} 的可 retain 副本，避免修改 readerIndex 影响全局缓存。
     */
    public static ByteBuf connectionPrefaceBuf() {
        // 返回 duplicate，修改读索引不影响原始只读 buffer
        return CONNECTION_PREFACE.retainedDuplicate();
    }

    /**
     * Iteratively looks through the causality chain for the given exception and returns the first
     * {@link Http2Exception} or {@code null} if none.
     */
    public static Http2Exception getEmbeddedHttp2Exception(Throwable cause) {
        while (cause != null) {
            if (cause instanceof Http2Exception) {
                return (Http2Exception) cause;
            }
            cause = cause.getCause();
        }
        return null;
    }

    /**
     * Creates a buffer containing the error message from the given exception. If the cause is
     * {@code null} returns an empty buffer.
     */
    public static ByteBuf toByteBuf(ChannelHandlerContext ctx, Throwable cause) {
        if (cause == null || cause.getMessage() == null) {
            return Unpooled.EMPTY_BUFFER;
        }

        return ByteBufUtil.writeUtf8(ctx.alloc(), cause.getMessage());
    }

    /**
     * Reads a big-endian (31-bit) integer from the buffer.
     */
    public static int readUnsignedInt(ByteBuf buf) {
        return buf.readInt() & 0x7fffffff;
    }

    /**
     * Writes an HTTP/2 frame header to the output buffer.
     */
    public static void writeFrameHeader(ByteBuf out, int payloadLength, byte type,
            Http2Flags flags, int streamId) {
        out.ensureWritable(FRAME_HEADER_LENGTH + payloadLength);
        writeFrameHeaderInternal(out, payloadLength, type, flags, streamId);
    }

    /**
     * Calculate the amount of bytes that can be sent by {@code state}. The lower bound is {@code 0}.
     */
    public static int streamableBytes(StreamByteDistributor.StreamState state) {
        return max(0, (int) min(state.pendingBytes(), state.windowSize()));
    }

    /**
     * Results in a RST_STREAM being sent for {@code streamId} due to violating
     * <a href="https://tools.ietf.org/html/rfc7540#section-6.5.2">SETTINGS_MAX_HEADER_LIST_SIZE</a>.
     * @param streamId The stream ID that was being processed when the exceptional condition occurred.
     * @param maxHeaderListSize The max allowed size for a list of headers in bytes which was exceeded.
     * @param onDecode {@code true} if the exception was encountered during decoder. {@code false} for encode.
     * @throws Http2Exception a stream error.
     */
    public static void headerListSizeExceeded(int streamId, long maxHeaderListSize,
                                              boolean onDecode) throws Http2Exception {
        throw headerListSizeError(streamId, PROTOCOL_ERROR, onDecode, "Header size exceeded max " +
                                  "allowed size (%d)", maxHeaderListSize);
    }

    /**
     * Results in a GO_AWAY being sent due to violating
     * <a href="https://tools.ietf.org/html/rfc7540#section-6.5.2">SETTINGS_MAX_HEADER_LIST_SIZE</a> in an unrecoverable
     * manner.
     * @param maxHeaderListSize The max allowed size for a list of headers in bytes which was exceeded.
     * @throws Http2Exception a connection error.
     */
    public static void headerListSizeExceeded(long maxHeaderListSize) throws Http2Exception {
        throw connectionError(PROTOCOL_ERROR, "Header size exceeded max " +
                "allowed size (%d)", maxHeaderListSize);
    }

    static void writeFrameHeaderInternal(ByteBuf out, int payloadLength, byte type,
            Http2Flags flags, int streamId) {
        out.writeMedium(payloadLength);
        out.writeByte(type);
        out.writeByte(flags.value());
        out.writeInt(streamId);
    }

    /**
     * 将多个 {@link ChannelPromise} 聚合为单一结果：全部成功才 success，任一失败则携带首个失败原因。
     */
    static final class SimpleChannelPromiseAggregator extends DefaultChannelPromise {
        private final ChannelPromise promise;
        private int expectedCount;
        private int doneCount;
        private Throwable aggregateFailure;
        private boolean doneAllocating;

        SimpleChannelPromiseAggregator(ChannelPromise promise, Channel c, EventExecutor e) {
            super(c, e);
            assert promise != null && !promise.isDone();
            this.promise = promise;
        }

        /**
         * Allocate a new promise which will be used to aggregate the overall success of this promise aggregator.
         * @return A new promise which will be aggregated.
         * {@code null} if {@link #doneAllocatingPromises()} was previously called.
         */
        public ChannelPromise newPromise() {
            assert !doneAllocating : "Done allocating. No more promises can be allocated.";
            ++expectedCount;
            return this;
        }

        /**
         * Signify that no more {@link #newPromise()} allocations will be made.
         * The aggregation can not be successful until this method is called.
         * @return The promise that is the aggregation of all promises allocated with {@link #newPromise()}.
         */
        public ChannelPromise doneAllocatingPromises() {
            if (!doneAllocating) {
                doneAllocating = true;
                if (doneCount == expectedCount || expectedCount == 0) {
                    return setPromise();
                }
            }
            return this;
        }

        @Override
        public boolean tryFailure(Throwable cause) {
            if (allowFailure()) {
                ++doneCount;
                setAggregateFailure(cause);
                if (allPromisesDone()) {
                    return tryPromise();
                }
                // TODO: We break the interface a bit here.
                // Multiple failure events can be processed without issue because this is an aggregation.
                return true;
            }
            return false;
        }

        /**
         * Fail this object if it has not already been failed.
         * <p>
         * This method will NOT throw an {@link IllegalStateException} if called multiple times
         * because that may be expected.
         */
        @Override
        public ChannelPromise setFailure(Throwable cause) {
            if (allowFailure()) {
                ++doneCount;
                setAggregateFailure(cause);
                if (allPromisesDone()) {
                    return setPromise();
                }
            }
            return this;
        }

        @Override
        public ChannelPromise setSuccess(Void result) {
            if (awaitingPromises()) {
                ++doneCount;
                if (allPromisesDone()) {
                    setPromise();
                }
            }
            return this;
        }

        @Override
        public boolean trySuccess(Void result) {
            if (awaitingPromises()) {
                ++doneCount;
                if (allPromisesDone()) {
                    return tryPromise();
                }
                // TODO: We break the interface a bit here.
                // Multiple success events can be processed without issue because this is an aggregation.
                return true;
            }
            return false;
        }

        private boolean allowFailure() {
            return awaitingPromises() || expectedCount == 0;
        }

        private boolean awaitingPromises() {
            return doneCount < expectedCount;
        }

        private boolean allPromisesDone() {
            return doneCount == expectedCount && doneAllocating;
        }

        private ChannelPromise setPromise() {
            if (aggregateFailure == null) {
                promise.setSuccess();
                return super.setSuccess(null);
            } else {
                promise.setFailure(aggregateFailure);
                return super.setFailure(aggregateFailure);
            }
        }

        private boolean tryPromise() {
            if (aggregateFailure == null) {
                promise.trySuccess();
                return super.trySuccess(null);
            } else {
                promise.tryFailure(aggregateFailure);
                return super.tryFailure(aggregateFailure);
            }
        }

        private void setAggregateFailure(Throwable cause) {
            if (aggregateFailure == null) {
                aggregateFailure = cause;
            }
        }
    }

    public static void verifyPadding(int padding) {
        if (padding < 0 || padding > MAX_PADDING) {
            throw new IllegalArgumentException(String.format("Invalid padding '%d'. Padding must be between 0 and " +
                                                             "%d (inclusive).", padding, MAX_PADDING));
        }
    }
    private Http2CodecUtil() { }
}
