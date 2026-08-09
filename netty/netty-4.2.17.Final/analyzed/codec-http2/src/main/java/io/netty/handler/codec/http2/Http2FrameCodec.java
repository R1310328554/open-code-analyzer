/*
 * Copyright 2016 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.codec.http2;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.UnsupportedMessageTypeException;
import io.netty.handler.codec.http.HttpServerUpgradeHandler.UpgradeEvent;
import io.netty.handler.codec.http2.Http2Connection.PropertyKey;
import io.netty.handler.codec.http2.Http2Stream.State;
import io.netty.handler.codec.http2.StreamBufferingEncoder.Http2ChannelClosedException;
import io.netty.handler.codec.http2.StreamBufferingEncoder.Http2GoAwayException;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ReferenceCounted;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import static io.netty.buffer.ByteBufUtil.writeAscii;
import static io.netty.handler.codec.http2.Http2CodecUtil.HTTP_UPGRADE_STREAM_ID;
import static io.netty.handler.codec.http2.Http2CodecUtil.isStreamIdValid;
import static io.netty.handler.codec.http2.Http2Error.NO_ERROR;
import static io.netty.util.internal.logging.InternalLogLevel.DEBUG;

/**
 * <p>将 HTTP/2 wire 帧与 {@link Http2Frame} 对象互转的高层 handler：入站经 {@link #channelRead} 向上游投递帧对象，
 * 出站 {@link #write} 接受 {@link Http2Frame} 并编码。流相关帧实现 {@link Http2StreamFrame}，由
 * {@link Http2FrameCodecBuilder} 构建；业务 handler 建议继承 {@link Http2ChannelDuplexHandler}。
 *
 * <h3>流生命周期</h3>
 * <p>活跃流在任一端发 {@code RST_STREAM} 或双方均带 {@code END_STREAM} 后关闭；每个 {@link Http2StreamFrame}
 * 附带 {@link Http2FrameStream} 标识流。读路径帧已绑定 stream，写路径需通过 {@link Http2StreamFrame#stream} 设置。
 *
 * <h3>流控</h3>
 * <p>codec 自动递增流/连接窗口。入站受控帧需写 {@link Http2WindowUpdateFrame} 告知已消费字节；
 * 本地初始窗口可通过 {@link Http2SettingsFrame} 调整，连接级窗口用 streamId=0 的 {@link Http2WindowUpdateFrame}。
 *
 * <h3>新入站流</h3>
 * <p>首帧须为 {@link Http2HeadersFrame}，并附带 {@link Http2FrameStream}。
 *
 * <h3>新出站流</h3>
 * <p>先 {@link Http2ChannelDuplexHandler#newStream()}，再写带 stream 的 {@link Http2HeadersFrame}；
 * stream id 耗尽时 promise 以 {@link Http2NoMoreStreamIdsException} 失败。
 *
 * <h3>错误与引用计数</h3>
 * <p>连接错误经 {@link ChannelInboundHandler#exceptionCaught} 传播；流错误包装为 {@link Http2FrameStreamException}。
 * 部分帧实现 {@link ReferenceCounted}，下游 handler 须负责 release。
 *
 * <h3>HTTP 升级</h3>
 * <p>服务端 h2c 升级配合 {@link Http2ServerUpgradeCodec} 自动完成。
 */
public class Http2FrameCodec extends Http2ConnectionHandler {

    private static final InternalLogger LOG = InternalLoggerFactory.getInstance(Http2FrameCodec.class);

    private static final Class<?>[] SUPPORTED_MESSAGES = new Class[] {
            Http2DataFrame.class, Http2HeadersFrame.class, Http2WindowUpdateFrame.class, Http2ResetFrame.class,
            Http2PingFrame.class, Http2SettingsFrame.class, Http2SettingsAckFrame.class, Http2GoAwayFrame.class,
            Http2PushPromiseFrame.class, Http2PriorityFrame.class, Http2UnknownFrame.class };

    /** 在 {@link Http2Stream} 上挂载 {@link Http2FrameStream} 的属性键。 */
    protected final PropertyKey streamKey;
    /** 标记流 1 是否来自 h2c 升级，升级流不参与常规流控 consume。 */
    private final PropertyKey upgradeKey;

    /** 初始 SETTINGS 中的窗口大小，用于服务端扩大连接级窗口。 */
    private final Integer initialFlowControlWindowSize;

    ChannelHandlerContext ctx;

    /**
     * 使用 {@link StreamBufferingEncoder} 时尚未激活的出站流数量。
     **/
    private int numBufferedStreams;
    /** 已分配 stream id 但尚未在 connection 上 materialize 的帧流。 */
    private final IntObjectMap<DefaultHttp2FrameStream> frameStreamToInitializeMap =
            new IntObjectHashMap<DefaultHttp2FrameStream>(8);

    protected Http2FrameCodec(Http2ConnectionEncoder encoder, Http2ConnectionDecoder decoder,
                              Http2Settings initialSettings, boolean decoupleCloseAndGoAway, boolean flushPreface) {
        super(decoder, encoder, initialSettings, decoupleCloseAndGoAway, flushPreface);

        decoder.frameListener(new FrameListener());
        connection().addListener(new ConnectionListener());
        connection().remote().flowController().listener(new Http2RemoteFlowControllerListener());
        streamKey = connection().newKey();
        upgradeKey = connection().newKey();
        initialFlowControlWindowSize = initialSettings.initialWindowSize();
    }

    /**
     * 创建新的出站/本地 {@link Http2FrameStream}（id 初始为 -1，写 HEADERS 后生效）。
     */
    DefaultHttp2FrameStream newStream() {
        return new DefaultHttp2FrameStream();
    }

    /**
     * Iterates over all active HTTP/2 streams.
     *
     * <p>This method must not be called outside of the event loop.
     */
    final void forEachActiveStream(final Http2FrameStreamVisitor streamVisitor) throws Http2Exception {
        assert ctx.executor().inEventLoop();
        if (connection().numActiveStreams() > 0) {
            connection().forEachActiveStream(new Http2StreamVisitor() {
                @Override
                public boolean visit(Http2Stream stream) {
                    try {
                        return streamVisitor.visit((Http2FrameStream) stream.getProperty(streamKey));
                    } catch (Throwable cause) {
                        onError(ctx, false, cause);
                        return false;
                    }
                }
            });
        }
    }

    /**
     * Retrieve the number of streams currently in the process of being initialized.
     * <p>
     * This is package-private for testing only.
     */
    int numInitializingStreams() {
        return frameStreamToInitializeMap.size();
    }

    @Override
    public final void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        super.handlerAdded(ctx);
        handlerAdded0(ctx);
        // 服务端无连接前言，handler 就绪后即可扩大连接窗口以提升并发吞吐
        Http2Connection connection = connection();
        if (connection.isServer()) {
            tryExpandConnectionFlowControlWindow(connection);
        }
    }

    private void tryExpandConnectionFlowControlWindow(Http2Connection connection) throws Http2Exception {
        if (initialFlowControlWindowSize != null) {
            // SETTINGS 里的 initialWindowSize 不含连接流；此处单独放大连接窗口
            Http2Stream connectionStream = connection.connectionStream();
            Http2LocalFlowController localFlowController = connection.local().flowController();
            final int delta = initialFlowControlWindowSize - localFlowController.initialWindowSize(connectionStream);
            // 增量翻倍，避免单流占满连接窗口
            if (delta > 0) {
                localFlowController.incrementWindowSize(connectionStream, Math.max(delta << 1, delta));
                flush(ctx);
            }
        }
    }

    void handlerAdded0(@SuppressWarnings("unsed") ChannelHandlerContext ctx) throws Exception {
        // sub-class can override this for extra steps that needs to be done when the handler is added.
    }

    /**
     * Handles the cleartext HTTP upgrade event. If an upgrade occurred, sends a simple response via
     * HTTP/2 on stream 1 (the stream specifically reserved for cleartext HTTP upgrade).
     */
    @Override
    public final void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) throws Exception {
        if (evt == Http2ConnectionPrefaceAndSettingsFrameWrittenEvent.INSTANCE) {
            // The user event implies that we are on the client.
            tryExpandConnectionFlowControlWindow(connection());

            // We schedule this on the EventExecutor to allow to have any extra handlers added to the pipeline
            // before we pass the event to the next handler. This is needed as the event may be called from within
            // handlerAdded(...) which will be run before other handlers will be added to the pipeline.
            ctx.executor().execute(new Runnable() {
                @Override
                public void run() {
                    ctx.fireUserEventTriggered(evt);
                }
            });
        } else if (evt instanceof UpgradeEvent) {
            UpgradeEvent upgrade = (UpgradeEvent) evt;
            try {
                onUpgradeEvent(ctx, upgrade.retain());
                Http2Stream stream = connection().stream(HTTP_UPGRADE_STREAM_ID);
                if (stream.getProperty(streamKey) == null) {
                    // TODO: improve handler/stream lifecycle so that stream isn't active before handler added.
                    // The stream was already made active, but ctx may have been null so it wasn't initialized.
                    // https://github.com/netty/netty/issues/4942
                    onStreamActive0(stream);
                }
                upgrade.upgradeRequest().headers().setInt(
                        HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text(), HTTP_UPGRADE_STREAM_ID);
                stream.setProperty(upgradeKey, true);
                InboundHttpToHttp2Adapter.handle(
                        ctx, connection(), decoder().frameListener(), upgrade.upgradeRequest().retain());
            } finally {
                upgrade.release();
            }
        } else {
            onUserEventTriggered(ctx, evt);
            ctx.fireUserEventTriggered(evt);
        }
    }

    void onUserEventTriggered(final ChannelHandlerContext ctx, final Object evt) throws Exception {
        // noop
    }

    /**
     * 将 {@link Http2Frame} 写回 wire；{@link Http2StreamFrame} 须来自 {@link #newStream()} 或入站帧。
     */
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (msg instanceof Http2DataFrame) {
            Http2DataFrame dataFrame = (Http2DataFrame) msg;
            encoder().writeData(ctx, dataFrame.stream().id(), dataFrame.content(),
                    dataFrame.padding(), dataFrame.isEndStream(), promise);
        } else if (msg instanceof Http2HeadersFrame) {
            writeHeadersFrame(ctx, (Http2HeadersFrame) msg, promise);
        } else if (msg instanceof Http2WindowUpdateFrame) {
            Http2WindowUpdateFrame frame = (Http2WindowUpdateFrame) msg;
            Http2FrameStream frameStream = frame.stream();
            // stream 为 null 表示连接级 WINDOW_UPDATE（stream id 0）
            try {
                if (frameStream == null) {
                    increaseInitialConnectionWindow(frame.windowSizeIncrement());
                } else {
                    consumeBytes(frameStream.id(), frame.windowSizeIncrement());
                }
                promise.setSuccess();
            } catch (Throwable t) {
                promise.setFailure(t);
            }
        } else if (msg instanceof Http2ResetFrame) {
            Http2ResetFrame rstFrame = (Http2ResetFrame) msg;
            int id = rstFrame.stream().id();
            // Only ever send a reset frame if stream may have existed before as otherwise we may send a RST on a
            // stream in an invalid state and cause a connection error.
            if (connection().streamMayHaveExisted(id)) {
                encoder().writeRstStream(ctx, rstFrame.stream().id(), rstFrame.errorCode(), promise);
            } else {
                ReferenceCountUtil.release(rstFrame);
                promise.setFailure(Http2Exception.streamError(
                        rstFrame.stream().id(), Http2Error.PROTOCOL_ERROR, "Stream never existed"));
            }
        } else if (msg instanceof Http2PingFrame) {
            Http2PingFrame frame = (Http2PingFrame) msg;
            encoder().writePing(ctx, frame.ack(), frame.content(), promise);
        } else if (msg instanceof Http2SettingsFrame) {
            encoder().writeSettings(ctx, ((Http2SettingsFrame) msg).settings(), promise);
        } else if (msg instanceof Http2SettingsAckFrame) {
            // In the event of manual SETTINGS ACK, it is assumed the encoder will apply the earliest received but not
            // yet ACKed settings.
            encoder().writeSettingsAck(ctx, promise);
        } else if (msg instanceof Http2GoAwayFrame) {
            writeGoAwayFrame(ctx, (Http2GoAwayFrame) msg, promise);
        } else if (msg instanceof Http2PushPromiseFrame) {
            Http2PushPromiseFrame pushPromiseFrame = (Http2PushPromiseFrame) msg;
            writePushPromise(ctx, pushPromiseFrame, promise);
        } else if (msg instanceof Http2PriorityFrame) {
            Http2PriorityFrame priorityFrame = (Http2PriorityFrame) msg;
            encoder().writePriority(ctx, priorityFrame.stream().id(), priorityFrame.streamDependency(),
                    priorityFrame.weight(), priorityFrame.exclusive(), promise);
        } else if (msg instanceof Http2UnknownFrame) {
            Http2UnknownFrame unknownFrame = (Http2UnknownFrame) msg;
            encoder().writeFrame(ctx, unknownFrame.frameType(), unknownFrame.stream().id(),
                    unknownFrame.flags(), unknownFrame.content(), promise);
        } else if (!(msg instanceof Http2Frame)) {
            ctx.write(msg, promise);
        } else {
            ReferenceCountUtil.release(msg);
            throw new UnsupportedMessageTypeException(msg, SUPPORTED_MESSAGES);
        }
    }

    private void increaseInitialConnectionWindow(int deltaBytes) throws Http2Exception {
        // The LocalFlowController is responsible for detecting over/under flow.
        connection().local().flowController().incrementWindowSize(connection().connectionStream(), deltaBytes);
    }

    final boolean consumeBytes(int streamId, int bytes) throws Http2Exception {
        Http2Stream stream = connection().stream(streamId);
        // h2c 升级后的流 1 由 InboundHttpToHttp2Adapter 处理，不参与 consumeBytes
        if (stream != null && streamId == Http2CodecUtil.HTTP_UPGRADE_STREAM_ID) {
            Boolean upgraded = stream.getProperty(upgradeKey);
            if (Boolean.TRUE.equals(upgraded)) {
                return false;
            }
        }

        return connection().local().flowController().consumeBytes(stream, bytes);
    }

    private void writeGoAwayFrame(ChannelHandlerContext ctx, Http2GoAwayFrame frame, ChannelPromise promise) {
        if (frame.lastStreamId() > -1) {
            frame.release();
            throw new IllegalArgumentException("Last stream id must not be set on GOAWAY frame");
        }

        int lastStreamCreated = connection().remote().lastStreamCreated();
        long lastStreamId = lastStreamCreated + ((long) frame.extraStreamIds()) * 2;
        // Check if the computation overflowed.
        if (lastStreamId > Integer.MAX_VALUE) {
            lastStreamId = Integer.MAX_VALUE;
        }
        goAway(ctx, (int) lastStreamId, frame.errorCode(), frame.content(), promise);
    }

    private void writeHeadersFrame(final ChannelHandlerContext ctx, Http2HeadersFrame headersFrame,
                                   ChannelPromise promise) {

        if (isStreamIdValid(headersFrame.stream().id())) {
            encoder().writeHeaders(ctx, headersFrame.stream().id(), headersFrame.headers(), headersFrame.padding(),
                    headersFrame.isEndStream(), promise);
        } else if (initializeNewStream(ctx, (DefaultHttp2FrameStream) headersFrame.stream(), promise)) {
            promise = promise.unvoid();

            final int streamId = headersFrame.stream().id();

            encoder().writeHeaders(ctx, streamId, headersFrame.headers(), headersFrame.padding(),
                    headersFrame.isEndStream(), promise);

            if (!promise.isDone()) {
                numBufferedStreams++;
                // Clean up the stream being initialized if writing the headers fails and also
                // decrement the number of buffered streams.
                promise.addListener((ChannelFutureListener) channelFuture -> {
                    numBufferedStreams--;
                    handleHeaderFuture(channelFuture, streamId);
                });
            } else {
                handleHeaderFuture(promise, streamId);
            }
        }
    }

    private void writePushPromise(final ChannelHandlerContext ctx, Http2PushPromiseFrame pushPromiseFrame,
                                  final ChannelPromise promise) {
        if (isStreamIdValid(pushPromiseFrame.pushStream().id())) {
            encoder().writePushPromise(ctx, pushPromiseFrame.stream().id(), pushPromiseFrame.pushStream().id(),
                    pushPromiseFrame.http2Headers(), pushPromiseFrame.padding(), promise);
        } else if (initializeNewStream(ctx, (DefaultHttp2FrameStream) pushPromiseFrame.pushStream(), promise)) {
            final int streamId = pushPromiseFrame.stream().id();
            encoder().writePushPromise(ctx, streamId, pushPromiseFrame.pushStream().id(),
                    pushPromiseFrame.http2Headers(), pushPromiseFrame.padding(), promise);

            if (promise.isDone()) {
                handleHeaderFuture(promise, streamId);
            } else {
                numBufferedStreams++;
                // Clean up the stream being initialized if writing the headers fails and also
                // decrement the number of buffered streams.
                promise.addListener((ChannelFutureListener) channelFuture -> {
                    numBufferedStreams--;
                    handleHeaderFuture(channelFuture, streamId);
                });
            }
        }
    }

    private boolean initializeNewStream(ChannelHandlerContext ctx, DefaultHttp2FrameStream http2FrameStream,
                                        ChannelPromise promise) {
        final Http2Connection connection = connection();
        final int streamId = connection.local().incrementAndGetNextStreamId();
        if (streamId < 0) {
            promise.setFailure(new Http2NoMoreStreamIdsException());

            // Simulate a GOAWAY being received due to stream exhaustion on this connection. We use the maximum
            // valid stream ID for the current peer.
            onHttp2Frame(ctx, new DefaultHttp2GoAwayFrame(connection.isServer() ? Integer.MAX_VALUE :
                    Integer.MAX_VALUE - 1, NO_ERROR.code(),
                    writeAscii(ctx.alloc(), "Stream IDs exhausted on local stream creation")));

            return false;
        }
        http2FrameStream.id = streamId;

        // Use a Map to store all pending streams as we may have multiple. This is needed as if we would store the
        // stream in a field directly we may override the stored field before onStreamAdded(...) was called
        // and so not correctly set the property for the buffered stream.
        //
        // See https://github.com/netty/netty/issues/8692
        Object old = frameStreamToInitializeMap.put(streamId, http2FrameStream);

        // We should not re-use ids.
        assert old == null;
        return true;
    }

    private void handleHeaderFuture(ChannelFuture channelFuture, int streamId) {
        if (!channelFuture.isSuccess()) {
            frameStreamToInitializeMap.remove(streamId);
        }
    }

    private void onStreamActive0(Http2Stream stream) {
        if (stream.id() != Http2CodecUtil.HTTP_UPGRADE_STREAM_ID &&
                connection().local().isValidStreamId(stream.id())) {
            return;
        }

        DefaultHttp2FrameStream stream2 = newStream().setStreamAndProperty(streamKey, stream);
        onHttp2StreamStateChanged(ctx, stream2);
    }

    private final class ConnectionListener extends Http2ConnectionAdapter {
        @Override
        public void onStreamAdded(Http2Stream stream) {
            DefaultHttp2FrameStream frameStream = frameStreamToInitializeMap.remove(stream.id());

            if (frameStream != null) {
                frameStream.setStreamAndProperty(streamKey, stream);
            }
        }

        @Override
        public void onStreamActive(Http2Stream stream) {
            onStreamActive0(stream);
        }

        @Override
        public void onStreamClosed(Http2Stream stream) {
            onHttp2StreamStateChanged0(stream);
        }

        @Override
        public void onStreamHalfClosed(Http2Stream stream) {
            onHttp2StreamStateChanged0(stream);
        }

        private void onHttp2StreamStateChanged0(Http2Stream stream) {
            DefaultHttp2FrameStream stream2 = stream.getProperty(streamKey);
            if (stream2 != null) {
                onHttp2StreamStateChanged(ctx, stream2);
            }
        }
    }

    @Override
    protected void onConnectionError(
            ChannelHandlerContext ctx, boolean outbound, Throwable cause, Http2Exception http2Ex) {
        if (!outbound) {
            // allow the user to handle it first in the pipeline, and then automatically clean up.
            // If this is not desired behavior the user can override this method.
            //
            // We only forward non outbound errors as outbound errors will already be reflected by failing the promise.
            ctx.fireExceptionCaught(cause);
        }
        super.onConnectionError(ctx, outbound, cause, http2Ex);
    }

    /**
     * Exceptions for unknown streams, that is streams that have no {@link Http2FrameStream} object attached
     * are simply logged and replied to by sending a RST_STREAM frame.
     */
    @Override
    protected final void onStreamError(ChannelHandlerContext ctx, boolean outbound, Throwable cause,
                                       Http2Exception.StreamException streamException) {
        int streamId = streamException.streamId();
        Http2Stream connectionStream = connection().stream(streamId);
        if (connectionStream == null) {
            onHttp2UnknownStreamError(ctx, cause, streamException);
            // Write a RST_STREAM
            super.onStreamError(ctx, outbound, cause, streamException);
            return;
        }

        Http2FrameStream stream = connectionStream.getProperty(streamKey);
        if (stream == null) {
            LOG.warn("{} Stream exception thrown without stream object attached.", ctx.channel(), cause);
            // Write a RST_STREAM
            super.onStreamError(ctx, outbound, cause, streamException);
            return;
        }

        if (!outbound) {
            // We only forward non outbound errors as outbound errors will already be reflected by failing the promise.
            onHttp2FrameStreamException(ctx, new Http2FrameStreamException(stream, streamException.error(), cause));
        }
    }

    private static void onHttp2UnknownStreamError(@SuppressWarnings("unused") ChannelHandlerContext ctx,
            Throwable cause, Http2Exception.StreamException streamException) {
        // We log here for debugging purposes. This exception will be propagated to the upper layers through other ways:
        // - fireExceptionCaught
        // - fireUserEventTriggered(Http2ResetFrame), see Http2MultiplexHandler#channelRead(...)
        // - by failing write promise
        // Receiver of the error is responsible for correct handling of this exception.
        LOG.log(DEBUG, "{} Stream exception thrown for unknown stream {}.",
                ctx.channel(), streamException.streamId(), cause);
    }

    @Override
    protected final boolean isGracefulShutdownComplete() {
        return super.isGracefulShutdownComplete() && numBufferedStreams == 0;
    }

    /**  wire 帧 → {@link Http2Frame} 的内部 listener。 */
    private final class FrameListener implements Http2FrameListener {

        @Override
        public void onUnknownFrame(
                ChannelHandlerContext ctx, byte frameType, int streamId, Http2Flags flags, ByteBuf payload) {
            if (streamId == 0) {
                // 连接流上的未知帧忽略（如 GREASE）
                return;
            }
            Http2FrameStream stream = requireStream(streamId);
            onHttp2Frame(ctx, newHttp2UnknownFrame(frameType, streamId, flags, payload.retain()).stream(stream));
        }

        @Override
        public void onSettingsRead(ChannelHandlerContext ctx, Http2Settings settings) {
            onHttp2Frame(ctx, new DefaultHttp2SettingsFrame(settings));
        }

        @Override
        public void onPingRead(ChannelHandlerContext ctx, long data) {
            onHttp2Frame(ctx, new DefaultHttp2PingFrame(data, false));
        }

        @Override
        public void onPingAckRead(ChannelHandlerContext ctx, long data) {
            onHttp2Frame(ctx, new DefaultHttp2PingFrame(data, true));
        }

        @Override
        public void onRstStreamRead(ChannelHandlerContext ctx, int streamId, long errorCode) {
            Http2FrameStream stream = requireStream(streamId);
            onHttp2Frame(ctx, new DefaultHttp2ResetFrame(errorCode).stream(stream));
        }

        @Override
        public void onWindowUpdateRead(ChannelHandlerContext ctx, int streamId, int windowSizeIncrement) {
            if (streamId == 0) {
                // Ignore connection window updates.
                return;
            }
            Http2FrameStream stream = requireStream(streamId);
            onHttp2Frame(ctx, new DefaultHttp2WindowUpdateFrame(windowSizeIncrement).stream(stream));
        }

        @Override
        public void onHeadersRead(ChannelHandlerContext ctx, int streamId,
                                  Http2Headers headers, int streamDependency, short weight, boolean
                                          exclusive, int padding, boolean endStream) {
            onHeadersRead(ctx, streamId, headers, padding, endStream);
        }

        @Override
        public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers,
                                  int padding, boolean endOfStream) {
            Http2FrameStream stream = requireStream(streamId);
            onHttp2Frame(ctx, new DefaultHttp2HeadersFrame(headers, endOfStream, padding).stream(stream));
        }

        @Override
        public int onDataRead(ChannelHandlerContext ctx, int streamId, ByteBuf data, int padding,
                              boolean endOfStream) {
            Http2FrameStream stream = requireStream(streamId);
            final Http2DataFrame dataframe;
            try {
                dataframe = new DefaultHttp2DataFrame(data.retain(), endOfStream, padding);
            } catch (IllegalArgumentException e) {
                // Might be thrown in case of invalid padding / length.
                data.release();
                throw e;
            }
            dataframe.stream(stream);
            onHttp2Frame(ctx, dataframe);
            // 延迟归还流控：由下游写 WindowUpdateFrame 时再 consumeBytes
            return 0;
        }

        @Override
        public void onGoAwayRead(ChannelHandlerContext ctx, int lastStreamId, long errorCode, ByteBuf debugData) {
            onHttp2Frame(ctx, new DefaultHttp2GoAwayFrame(lastStreamId, errorCode, debugData.retain()));
        }

        @Override
        public void onPriorityRead(ChannelHandlerContext ctx, int streamId, int streamDependency,
                                   short weight, boolean exclusive) {

            Http2Stream stream = connection().stream(streamId);
            if (stream == null) {
                // The stream was not opened yet, let's just ignore this for now.
                return;
            }
            Http2FrameStream frameStream = requireStream(streamId);
            onHttp2Frame(ctx, new DefaultHttp2PriorityFrame(streamDependency, weight, exclusive)
                    .stream(frameStream));
        }

        @Override
        public void onSettingsAckRead(ChannelHandlerContext ctx) {
            onHttp2Frame(ctx, Http2SettingsAckFrame.INSTANCE);
        }

        @Override
        public void onPushPromiseRead(ChannelHandlerContext ctx, int streamId, int promisedStreamId,
                                      Http2Headers headers, int padding) {
            Http2FrameStream stream = requireStream(streamId);
            onHttp2Frame(ctx, new DefaultHttp2PushPromiseFrame(headers, padding, promisedStreamId)
                    .pushStream(new DefaultHttp2FrameStream()
                            .setStreamAndProperty(streamKey, connection().stream(promisedStreamId)))
                    .stream(stream));
        }

        private Http2FrameStream requireStream(int streamId) {
            Http2FrameStream stream = connection().stream(streamId).getProperty(streamKey);
            if (stream == null) {
                throw new IllegalStateException("Stream object required for identifier: " + streamId);
            }
            return stream;
        }
    }

    private void onUpgradeEvent(ChannelHandlerContext ctx, UpgradeEvent evt) {
        ctx.fireUserEventTriggered(evt);
    }

    private void onHttp2StreamWritabilityChanged(ChannelHandlerContext ctx, DefaultHttp2FrameStream stream,
                                                 @SuppressWarnings("unused") boolean writable) {
        ctx.fireUserEventTriggered(stream.writabilityChanged);
    }

    void onHttp2StreamStateChanged(ChannelHandlerContext ctx, DefaultHttp2FrameStream stream) {
        ctx.fireUserEventTriggered(stream.stateChanged);
    }

    void onHttp2Frame(ChannelHandlerContext ctx, Http2Frame frame) {
        ctx.fireChannelRead(frame);
    }

    /**
     * Create a Http2UnknownFrame. The ownership of the {@link ByteBuf} is transferred.
     * */
    protected Http2StreamFrame newHttp2UnknownFrame(byte frameType, int streamId, Http2Flags flags, ByteBuf payload) {
        return new DefaultHttp2UnknownFrame(frameType, flags, payload);
    }

    void onHttp2FrameStreamException(ChannelHandlerContext ctx, Http2FrameStreamException cause) {
        ctx.fireExceptionCaught(cause);
    }

    private final class Http2RemoteFlowControllerListener implements Http2RemoteFlowController.Listener {
        @Override
        public void writabilityChanged(Http2Stream stream) {
            DefaultHttp2FrameStream frameStream = stream.getProperty(streamKey);
            if (frameStream == null) {
                return;
            }
            onHttp2StreamWritabilityChanged(
                    ctx, frameStream, connection().remote().flowController().isWritable(stream));
        }
    }

    /**
     * {@link Http2FrameStream} 默认实现；在 stream 加入 connection 前仅持有预分配 id。
     */
    // TODO(buchgr): Merge Http2FrameStream and Http2Stream.
    static class DefaultHttp2FrameStream implements Http2FrameStream {

        private volatile int id = -1;
        private volatile Http2Stream stream;

        final Http2FrameStreamEvent stateChanged = Http2FrameStreamEvent.stateChanged(this);
        final Http2FrameStreamEvent writabilityChanged = Http2FrameStreamEvent.writabilityChanged(this);

        Channel attachment;

        DefaultHttp2FrameStream setStreamAndProperty(PropertyKey streamKey, Http2Stream stream) {
            assert id == -1 || stream.id() == id;
            this.stream = stream;
            this.id = stream.id();
            stream.setProperty(streamKey, this);
            return this;
        }

        @Override
        public int id() {
            Http2Stream stream = this.stream;
            return stream == null ? id : stream.id();
        }

        @Override
        public State state() {
            Http2Stream stream = this.stream;
            return stream == null ? State.IDLE : stream.state();
        }

        @Override
        public String toString() {
            return String.valueOf(id());
        }
    }
}
