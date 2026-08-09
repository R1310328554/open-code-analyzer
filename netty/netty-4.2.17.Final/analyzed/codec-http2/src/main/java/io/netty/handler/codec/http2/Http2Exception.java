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

import io.netty.util.internal.ThrowableUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static io.netty.handler.codec.http2.Http2CodecUtil.CONNECTION_STREAM_ID;
import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * HTTP/2 协议错误异常，携带 {@link Http2Error} 与 {@link ShutdownHint}，可映射为 RST_STREAM 或 GOAWAY。
 * <p>工厂方法 {@link #connectionError}、{@link #streamError} 等按错误作用域构造合适子类型。
 */
public class Http2Exception extends Exception {
    private static final long serialVersionUID = -6941186345430164209L;
    /** RFC 7540 错误码。 */
    private final Http2Error error;
    /** 建议的连接关闭策略，实现可忽略。 */
    private final ShutdownHint shutdownHint;

    public Http2Exception(Http2Error error) {
        this(error, ShutdownHint.HARD_SHUTDOWN);
    }

    public Http2Exception(Http2Error error, ShutdownHint shutdownHint) {
        this.error = checkNotNull(error, "error");
        this.shutdownHint = checkNotNull(shutdownHint, "shutdownHint");
    }

    public Http2Exception(Http2Error error, String message) {
        this(error, message, ShutdownHint.HARD_SHUTDOWN);
    }

    public Http2Exception(Http2Error error, String message, ShutdownHint shutdownHint) {
        super(message);
        this.error = checkNotNull(error, "error");
        this.shutdownHint = checkNotNull(shutdownHint, "shutdownHint");
    }

    public Http2Exception(Http2Error error, String message, Throwable cause) {
        this(error, message, cause, ShutdownHint.HARD_SHUTDOWN);
    }

    public Http2Exception(Http2Error error, String message, Throwable cause, ShutdownHint shutdownHint) {
        super(message, cause);
        this.error = checkNotNull(error, "error");
        this.shutdownHint = checkNotNull(shutdownHint, "shutdownHint");
    }

    static Http2Exception newStatic(Http2Error error, String message, ShutdownHint shutdownHint,
                                    Class<?> clazz, String method) {
        final Http2Exception exception = new StacklessHttp2Exception(error, message, shutdownHint, true);
        return ThrowableUtil.unknownStackTrace(exception, clazz, method);
    }

    private Http2Exception(Http2Error error, String message, ShutdownHint shutdownHint, boolean shared) {
        super(message, null, false, true);
        assert shared;
        this.error = checkNotNull(error, "error");
        this.shutdownHint = checkNotNull(shutdownHint, "shutdownHint");
    }

    public Http2Error error() {
        return error;
    }

    /**
     * Provide a hint as to what type of shutdown should be executed. Note this hint may be ignored.
     */
    public ShutdownHint shutdownHint() {
        return shutdownHint;
    }

    /**
     * 连接级错误：无法归属单一流，通常触发 GOAWAY。
     * @param error The type of error as defined by the HTTP/2 specification.
     * @param fmt String with the content and format for the additional debug data.
     * @param args Objects which fit into the format defined by {@code fmt}.
     * @return An exception which can be translated into an HTTP/2 error.
     */
    public static Http2Exception connectionError(Http2Error error, String fmt, Object... args) {
        return new Http2Exception(error, formatErrorMessage(fmt, args));
    }

    /**
     * 连接级错误：无法归属单一流，通常触发 GOAWAY。
     * @param error The type of error as defined by the HTTP/2 specification.
     * @param cause The object which caused the error.
     * @param fmt String with the content and format for the additional debug data.
     * @param args Objects which fit into the format defined by {@code fmt}.
     * @return An exception which can be translated into an HTTP/2 error.
     */
    public static Http2Exception connectionError(Http2Error error, Throwable cause,
            String fmt, Object... args) {
        return new Http2Exception(error, formatErrorMessage(fmt, args), cause);
    }

    /**
     * 在已关闭流上尝试创建新流时使用（如对端复用旧 stream id）。
     * @param error The type of error as defined by the HTTP/2 specification.
     * @param fmt String with the content and format for the additional debug data.
     * @param args Objects which fit into the format defined by {@code fmt}.
     * @return An exception which can be translated into an HTTP/2 error.
     */
    public static Http2Exception closedStreamError(Http2Error error, String fmt, Object... args) {
        return new ClosedStreamCreationException(error, formatErrorMessage(fmt, args));
    }

    /**
     * 流级错误：{@code id} 非 {@link Http2CodecUtil#CONNECTION_STREAM_ID} 时返回 {@link StreamException}。
     * @param id The stream id for which the error is isolated to.
     * @param error The type of error as defined by the HTTP/2 specification.
     * @param fmt String with the content and format for the additional debug data.
     * @param args Objects which fit into the format defined by {@code fmt}.
     * @return If the {@code id} is not
     * {@link Http2CodecUtil#CONNECTION_STREAM_ID} then a {@link StreamException} will be returned.
     * Otherwise the error is considered a connection error and a {@link Http2Exception} is returned.
     */
    public static Http2Exception streamError(int id, Http2Error error, String fmt, Object... args) {
        return CONNECTION_STREAM_ID == id ?
                connectionError(error, fmt, args) :
                    new StreamException(id, error, formatErrorMessage(fmt, args));
    }

    /**
     * 流级错误：{@code id} 非 {@link Http2CodecUtil#CONNECTION_STREAM_ID} 时返回 {@link StreamException}。
     * @param id The stream id for which the error is isolated to.
     * @param error The type of error as defined by the HTTP/2 specification.
     * @param cause The object which caused the error.
     * @param fmt String with the content and format for the additional debug data.
     * @param args Objects which fit into the format defined by {@code fmt}.
     * @return If the {@code id} is not
     * {@link Http2CodecUtil#CONNECTION_STREAM_ID} then a {@link StreamException} will be returned.
     * Otherwise the error is considered a connection error and a {@link Http2Exception} is returned.
     */
    public static Http2Exception streamError(int id, Http2Error error, Throwable cause,
            String fmt, Object... args) {
        return CONNECTION_STREAM_ID == id ?
                connectionError(error, cause, fmt, args) :
                    new StreamException(id, error, formatErrorMessage(fmt, args), cause);
    }

    /**
     * 头部列表过大；解码路径失败时 {@link HeaderListSizeException#duringDecode()} 为 true。
     * @param id The stream id for which the error is isolated to.
     * @param error The type of error as defined by the HTTP/2 specification.
     * @param onDecode Whether this error was caught while decoding headers
     * @param fmt String with the content and format for the additional debug data.
     * @param args Objects which fit into the format defined by {@code fmt}.
     * @return If the {@code id} is not
     * {@link Http2CodecUtil#CONNECTION_STREAM_ID} then a {@link HeaderListSizeException}
     * will be returned. Otherwise the error is considered a connection error and a {@link Http2Exception} is
     * returned.
     */
    public static Http2Exception headerListSizeError(int id, Http2Error error, boolean onDecode,
            String fmt, Object... args) {
        return CONNECTION_STREAM_ID == id ?
                connectionError(error, fmt, args) :
                    new HeaderListSizeException(id, error, formatErrorMessage(fmt, args), onDecode);
    }

    private static String formatErrorMessage(String fmt, Object[] args) {
        if (fmt == null) {
            if (args == null || args.length == 0) {
                return "Unexpected error";
            }
            return "Unexpected error: " + Arrays.toString(args);
        }
        return String.format(fmt, args);
    }

    /**
     * Check if an exception is isolated to a single stream or the entire connection.
     * @param e The exception to check.
     * @return {@code true} if {@code e} is an instance of {@link StreamException}.
     * {@code false} otherwise.
     */
    public static boolean isStreamError(Http2Exception e) {
        return e instanceof StreamException;
    }

    /**
     * Get the stream id associated with an exception.
     * @param e The exception to get the stream id for.
     * @return {@link Http2CodecUtil#CONNECTION_STREAM_ID} if {@code e} is a connection error.
     * Otherwise the stream id associated with the stream error.
     */
    public static int streamId(Http2Exception e) {
        return isStreamError(e) ? ((StreamException) e).streamId() : CONNECTION_STREAM_ID;
    }

    /**
     * 连接关闭策略提示。
     */
    public enum ShutdownHint {
        /** 不关闭底层 channel。 */
        NO_SHUTDOWN,
        /** 优雅关闭：具体语义由实现定义（如等待活跃流结束）。 */
        GRACEFUL_SHUTDOWN,
        /** 发送 GOAWAY 后立即关闭 channel。 */
        HARD_SHUTDOWN
    }

    /**
     * 流创建失败且可能因流此前已关闭所致。
     */
    public static final class ClosedStreamCreationException extends Http2Exception {
        private static final long serialVersionUID = -6746542974372246206L;

        public ClosedStreamCreationException(Http2Error error) {
            super(error);
        }

        public ClosedStreamCreationException(Http2Error error, String message) {
            super(error, message);
        }

        public ClosedStreamCreationException(Http2Error error, String message, Throwable cause) {
            super(error, message, cause);
        }
    }

    /**
     * 可隔离到单流的异常；{@link ShutdownHint#NO_SHUTDOWN}，由 handler 发 RST_STREAM 处理。
     */
    public static class StreamException extends Http2Exception {
        private static final long serialVersionUID = 602472544416984384L;
        private final int streamId;

        StreamException(int streamId, Http2Error error, String message) {
            super(error, message, ShutdownHint.NO_SHUTDOWN);
            this.streamId = streamId;
        }

        StreamException(int streamId, Http2Error error, String message, Throwable cause) {
            super(error, message, cause, ShutdownHint.NO_SHUTDOWN);
            this.streamId = streamId;
        }

        public int streamId() {
            return streamId;
        }
    }

    public static final class HeaderListSizeException extends StreamException {
        private static final long serialVersionUID = -8807603212183882637L;

        private final boolean decode;

        HeaderListSizeException(int streamId, Http2Error error, String message, boolean decode) {
            super(streamId, error, message);
            this.decode = decode;
        }

        public boolean duringDecode() {
            return decode;
        }
    }

    /**
     * 一次抛出多个 {@link StreamException} 的聚合容器。
     */
    public static final class CompositeStreamException extends Http2Exception implements Iterable<StreamException> {
        private static final long serialVersionUID = 7091134858213711015L;
        private final List<StreamException> exceptions;

        public CompositeStreamException(Http2Error error, int initialCapacity) {
            super(error, ShutdownHint.NO_SHUTDOWN);
            exceptions = new ArrayList<StreamException>(initialCapacity);
        }

        public void add(StreamException e) {
            exceptions.add(e);
        }

        @Override
        public Iterator<StreamException> iterator() {
            return exceptions.iterator();
        }
    }

    private static final class StacklessHttp2Exception extends Http2Exception {

        private static final long serialVersionUID = 1077888485687219443L;

        StacklessHttp2Exception(Http2Error error, String message, ShutdownHint shutdownHint) {
            super(error, message, shutdownHint);
        }

        StacklessHttp2Exception(Http2Error error, String message, ShutdownHint shutdownHint, boolean shared) {
            super(error, message, shutdownHint, shared);
        }

        // 避免 fillInStackTrace 分配栈并泄漏 ClassLoader
        @Override
        public Throwable fillInStackTrace() {
            return this;
        }
    }
}
