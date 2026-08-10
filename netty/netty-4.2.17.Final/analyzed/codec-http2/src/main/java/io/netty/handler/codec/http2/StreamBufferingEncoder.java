/*
 * Copyright 2015 The Netty Project
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
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

import static io.netty.handler.codec.http2.Http2CodecUtil.SMALLEST_MAX_CONCURRENT_STREAMS;
import static io.netty.handler.codec.http2.Http2Error.PROTOCOL_ERROR;
import static io.netty.handler.codec.http2.Http2Exception.connectionError;

/**
 * {@link Http2ConnectionEncoder} 装饰器：在达到 {@code SETTINGS_MAX_CONCURRENT_STREAMS} 上限前透明转发，
 * 超出后将新流及其帧缓冲，待活跃流关闭或上限提高后再自动刷出。
 * <p/>
 * <p>收到对端 {@code GOAWAY} 时，{@code lastStreamId} 以下尚未刷出的缓冲写操作将以
 * {@link Http2GoAwayException} 失败。
 * <p/>
 * <p>连接/编码器关闭时，所有新写与缓冲写以 {@link Http2ChannelClosedException} 失败。
 * </p>
 * <p>可作为 {@link DefaultHttp2ConnectionEncoder} 的无缝 drop-in 装饰使用。
 * </p>
 */
public class StreamBufferingEncoder extends DecoratingHttp2ConnectionEncoder {

    /**
     * 编码器关闭导致缓冲流被终止时抛出。
     */
    public static final class Http2ChannelClosedException extends Http2Exception {
        private static final long serialVersionUID = 4768543442094476971L;

        public Http2ChannelClosedException() {
            super(Http2Error.REFUSED_STREAM, "Connection closed");
        }
    }

    private static final class GoAwayDetail {
        private final int lastStreamId;
        private final long errorCode;
        private final byte[] debugData;

        GoAwayDetail(int lastStreamId, long errorCode, byte[] debugData) {
            this.lastStreamId = lastStreamId;
            this.errorCode = errorCode;
            this.debugData = debugData.clone();
        }
    }

    /**
     * 因收到 {@code GOAWAY} 而终止缓冲流时抛出。
     */
    public static final class Http2GoAwayException extends Http2Exception {
        private static final long serialVersionUID = 1326785622777291198L;
        private final GoAwayDetail goAwayDetail;

        public Http2GoAwayException(int lastStreamId, long errorCode, byte[] debugData) {
            this(new GoAwayDetail(lastStreamId, errorCode, debugData));
        }

        Http2GoAwayException(GoAwayDetail goAwayDetail) {
            super(Http2Error.STREAM_CLOSED);
            this.goAwayDetail = goAwayDetail;
        }

        public int lastStreamId() {
            return goAwayDetail.lastStreamId;
        }

        public long errorCode() {
            return goAwayDetail.errorCode;
        }

        public byte[] debugData() {
            return goAwayDetail.debugData.clone();
        }
    }

    /**
     * 并发流达上限时，按流 ID 排序暂存待发送帧的缓冲表。
     */
    private final TreeMap<Integer, PendingStream> pendingStreams = new TreeMap<Integer, PendingStream>();
    /** 当前允许的最大并发流数（来自 SETTINGS）。 */
    private int maxConcurrentStreams;
    private boolean closed;
    private GoAwayDetail goAwayDetail;

    public StreamBufferingEncoder(Http2ConnectionEncoder delegate) {
        this(delegate, SMALLEST_MAX_CONCURRENT_STREAMS);
    }

    public StreamBufferingEncoder(Http2ConnectionEncoder delegate, int initialMaxConcurrentStreams) {
        super(delegate);
        maxConcurrentStreams = initialMaxConcurrentStreams;
        connection().addListener(new Http2ConnectionAdapter() {

            @Override
            public void onGoAwayReceived(int lastStreamId, long errorCode, ByteBuf debugData) {
                goAwayDetail = new GoAwayDetail(
                    // Using getBytes(..., false) is safe here as GoAwayDetail(...) will clone the byte[].
                    lastStreamId, errorCode,
                    ByteBufUtil.getBytes(debugData, debugData.readerIndex(), debugData.readableBytes(), false));
                cancelGoAwayStreams(goAwayDetail);
            }

            @Override
            public void onStreamClosed(Http2Stream stream) {
                tryCreatePendingStreams();
            }
        });
    }

    /**
     * Indicates the number of streams that are currently buffered, awaiting creation.
     */
    public int numBufferedStreams() {
        return pendingStreams.size();
    }

    @Override
    public ChannelFuture writeHeaders(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int padding,
                                      boolean endStream, ChannelPromise promise) {
        return writeHeaders0(ctx, streamId, headers, false, 0, (short) 0,
                             false, padding, endStream, promise);
    }

    @Override
    public ChannelFuture writeHeaders(ChannelHandlerContext ctx, int streamId, Http2Headers headers,
                                      int streamDependency, short weight, boolean exclusive, int padding,
                                      boolean endOfStream, ChannelPromise promise) {
        return writeHeaders0(ctx, streamId, headers, true, streamDependency, weight, exclusive, padding,
                             endOfStream, promise);
    }

    private ChannelFuture writeHeaders0(ChannelHandlerContext ctx, int streamId, Http2Headers headers,
                                        boolean hasPriority, int streamDependency, short weight, boolean exclusive,
                                        int padding, boolean endOfStream, ChannelPromise promise) {
        if (closed) {
            return promise.setFailure(new Http2ChannelClosedException());
        }
        if (isExistingStream(streamId) || canCreateStream()) {
            if (hasPriority) {
                return super.writeHeaders(ctx, streamId, headers, streamDependency, weight,
                                          exclusive, padding, endOfStream, promise);
            }
            return super.writeHeaders(ctx, streamId, headers, padding, endOfStream, promise);
        }
        if (goAwayDetail != null) {
            return promise.setFailure(new Http2GoAwayException(goAwayDetail));
        }
        PendingStream pendingStream = pendingStreams.get(streamId);
        if (pendingStream == null) {
            pendingStream = new PendingStream(ctx, streamId);
            pendingStreams.put(streamId, pendingStream);
        }
        pendingStream.frames.add(new HeadersFrame(headers, hasPriority, streamDependency, weight, exclusive,
                padding, endOfStream, promise));
        return promise;
    }

    @Override
    public ChannelFuture writeRstStream(ChannelHandlerContext ctx, int streamId, long errorCode,
                                        ChannelPromise promise) {
        if (isExistingStream(streamId)) {
            return super.writeRstStream(ctx, streamId, errorCode, promise);
        }
        // Since the delegate doesn't know about any buffered streams we have to handle cancellation
        // of the promises and releasing of the ByteBufs here.
        PendingStream stream = pendingStreams.remove(streamId);
        if (stream != null) {
            // Sending a RST_STREAM to a buffered stream will succeed the promise of all frames
            // associated with the stream, as sending a RST_STREAM means that someone "doesn't care"
            // about the stream anymore and thus there is not point in failing the promises and invoking
            // error handling routines.
            stream.close(null);
            promise.setSuccess();
        } else {
            promise.setFailure(connectionError(PROTOCOL_ERROR, "Stream does not exist %d", streamId));
        }
        return promise;
    }

    @Override
    public ChannelFuture writeData(ChannelHandlerContext ctx, int streamId, ByteBuf data,
                                   int padding, boolean endOfStream, ChannelPromise promise) {
        if (isExistingStream(streamId)) {
            return super.writeData(ctx, streamId, data, padding, endOfStream, promise);
        }
        PendingStream pendingStream = pendingStreams.get(streamId);
        if (pendingStream != null) {
            pendingStream.frames.add(new DataFrame(data, padding, endOfStream, promise));
        } else {
            ReferenceCountUtil.safeRelease(data);
            promise.setFailure(connectionError(PROTOCOL_ERROR, "Stream does not exist %d", streamId));
        }
        return promise;
    }

    @Override
    public ChannelFuture writeSettingsAck(ChannelHandlerContext ctx, ChannelPromise promise) {
        final ChannelFuture future = super.writeSettingsAck(ctx, promise);
        // In case autoAckSettings was set to false, decorated DefaultHttp2ConnectionEncoder will dequeue pending
        // settings and call remoteSettings on its own instance. Therefore, we need to consume potentially updated value
        // after this method returns.
        updateMaxConcurrentStreams();
        return future;
    }

    @Override
    public void remoteSettings(Http2Settings settings) throws Http2Exception {
        // Need to let the delegate decoder handle the settings first, so that it sees the
        // new setting before we attempt to create any new streams.
        super.remoteSettings(settings);
        updateMaxConcurrentStreams();
    }

    private void updateMaxConcurrentStreams() {
        // Get the updated value for SETTINGS_MAX_CONCURRENT_STREAMS.
        maxConcurrentStreams = connection().local().maxActiveStreams();
        // Try to create new streams up to the new threshold.
        tryCreatePendingStreams();
    }

    @Override
    public void close() {
        try {
            if (!closed) {
                closed = true;

                // Fail all buffered streams.
                Http2ChannelClosedException e = new Http2ChannelClosedException();
                while (!pendingStreams.isEmpty()) {
                    PendingStream stream = pendingStreams.pollFirstEntry().getValue();
                    stream.close(e);
                }
            }
        } finally {
            super.close();
        }
    }

    private void tryCreatePendingStreams() {
        while (!pendingStreams.isEmpty() && canCreateStream()) {
            Map.Entry<Integer, PendingStream> entry = pendingStreams.pollFirstEntry();
            PendingStream pendingStream = entry.getValue();
            try {
                pendingStream.sendFrames();
            } catch (Throwable t) {
                pendingStream.close(t);
            }
        }
    }

    private void cancelGoAwayStreams(GoAwayDetail goAwayDetail) {
        Iterator<PendingStream> iter = pendingStreams.values().iterator();
        Exception e = new Http2GoAwayException(goAwayDetail);
        while (iter.hasNext()) {
            PendingStream stream = iter.next();
            if (stream.streamId > goAwayDetail.lastStreamId) {
                iter.remove();
                stream.close(e);
            }
        }
    }

    /**
     * Determines whether or not we're allowed to create a new stream right now.
     */
    private boolean canCreateStream() {
        return connection().local().numActiveStreams() < maxConcurrentStreams;
    }

    private boolean isExistingStream(int streamId) {
        return streamId <= connection().local().lastStreamCreated();
    }

    private static final class PendingStream {
        final ChannelHandlerContext ctx;
        final int streamId;
        final Queue<Frame> frames = new ArrayDeque<Frame>(2);

        PendingStream(ChannelHandlerContext ctx, int streamId) {
            this.ctx = ctx;
            this.streamId = streamId;
        }

        void sendFrames() {
            for (Frame frame : frames) {
                frame.send(ctx, streamId);
            }
        }

        void close(Throwable t) {
            for (Frame frame : frames) {
                frame.release(t);
            }
        }
    }

    private abstract static class Frame {
        final ChannelPromise promise;

        Frame(ChannelPromise promise) {
            this.promise = promise;
        }

        /**
         * Release any resources (features, buffers, ...) associated with the frame.
         */
        void release(Throwable t) {
            if (t == null) {
                promise.setSuccess();
            } else {
                promise.setFailure(t);
            }
        }

        abstract void send(ChannelHandlerContext ctx, int streamId);
    }

    private final class HeadersFrame extends Frame {
        final Http2Headers headers;
        final int streamDependency;
        final boolean hasPriority;
        final short weight;
        final boolean exclusive;
        final int padding;
        final boolean endOfStream;

        HeadersFrame(Http2Headers headers, boolean hasPriority, int streamDependency, short weight, boolean exclusive,
                     int padding, boolean endOfStream, ChannelPromise promise) {
            super(promise);
            this.headers = headers;
            this.hasPriority = hasPriority;
            this.streamDependency = streamDependency;
            this.weight = weight;
            this.exclusive = exclusive;
            this.padding = padding;
            this.endOfStream = endOfStream;
        }

        @Override
        void send(ChannelHandlerContext ctx, int streamId) {
            writeHeaders0(ctx, streamId, headers, hasPriority, streamDependency, weight, exclusive, padding,
                          endOfStream,
                          promise);
        }
    }

    private final class DataFrame extends Frame {
        final ByteBuf data;
        final int padding;
        final boolean endOfStream;

        DataFrame(ByteBuf data, int padding, boolean endOfStream, ChannelPromise promise) {
            super(promise);
            this.data = data;
            this.padding = padding;
            this.endOfStream = endOfStream;
        }

        @Override
        void release(Throwable t) {
            super.release(t);
            ReferenceCountUtil.safeRelease(data);
        }

        @Override
        void send(ChannelHandlerContext ctx, int streamId) {
            writeData(ctx, streamId, data, padding, endOfStream, promise);
        }
    }
}
