/*
 * Copyright 2020 The Netty Project
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
package io.netty.handler.codec.http3;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.ChannelInputShutdownEvent;
import io.netty.handler.codec.quic.QuicChannel;
import io.netty.handler.codec.quic.QuicStreamChannel;
import io.netty.handler.codec.quic.QuicStreamType;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.jetbrains.annotations.Nullable;

import java.nio.channels.ClosedChannelException;

import static io.netty.handler.codec.http3.Http3CodecUtils.closeOnFailure;
import static io.netty.handler.codec.http3.Http3CodecUtils.connectionError;
import static io.netty.handler.codec.http3.Http3CodecUtils.criticalStreamClosed;
import static io.netty.handler.codec.http3.Http3ErrorCode.H3_FRAME_UNEXPECTED;
import static io.netty.handler.codec.http3.Http3ErrorCode.H3_ID_ERROR;
import static io.netty.handler.codec.http3.Http3ErrorCode.H3_MISSING_SETTINGS;
import static io.netty.handler.codec.http3.Http3ErrorCode.QPACK_ENCODER_STREAM_ERROR;
import static io.netty.handler.codec.http3.QpackUtil.toIntOrThrow;
import static io.netty.util.internal.ThrowableUtil.unknownStackTrace;

/**
 * 对端控制流入站 handler：强制首帧为 SETTINGS，解析后按需创建 QPACK 单向流，
 * 并校验 GOAWAY / MAX_PUSH_ID / CANCEL_PUSH 的协议约束。
 */
final class Http3ControlStreamInboundHandler extends Http3FrameTypeInboundValidationHandler<Http3ControlStreamFrame> {
    final boolean server;
    /** 非 null 时把合法控制帧 fire 给用户的 handler。 */
    private final ChannelHandler controlFrameHandler;
    private final QpackEncoder qpackEncoder;
    /** 对端出站控制流 handler，客户端校验 CANCEL_PUSH 时需读取其 sentMaxPushId。 */
    private final Http3ControlStreamOutboundHandler remoteControlStreamHandler;
    /** 是否已读过首帧（必须是 SETTINGS）。 */
    private boolean firstFrameRead;
    private Long receivedGoawayId;
    private Long receivedMaxPushId;

    Http3ControlStreamInboundHandler(boolean server, @Nullable ChannelHandler controlFrameHandler,
                                     QpackEncoder qpackEncoder,
                                     Http3ControlStreamOutboundHandler remoteControlStreamHandler) {
        super(Http3ControlStreamFrame.class);
        this.server = server;
        this.controlFrameHandler = controlFrameHandler;
        this.qpackEncoder = qpackEncoder;
        this.remoteControlStreamHandler = remoteControlStreamHandler;
    }

    boolean isServer() {
        return server;
    }

    boolean isGoAwayReceived() {
        return receivedGoawayId != null;
    }

    long maxPushIdReceived() {
        return receivedMaxPushId == null ? -1 : receivedMaxPushId;
    }

    private boolean forwardControlFrames() {
        return controlFrameHandler != null;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        // 用户关心控制帧时，将其 handler 挂到 pipeline 尾部接收 fireChannelRead
        if (controlFrameHandler != null) {
            ctx.pipeline().addLast(controlFrameHandler);
        }
    }

    @Override
    void readFrameDiscarded(ChannelHandlerContext ctx, Object discardedFrame) {
        if (!firstFrameRead && !(discardedFrame instanceof Http3SettingsFrame)) {
            connectionError(ctx, Http3ErrorCode.H3_MISSING_SETTINGS, "Missing settings frame.", forwardControlFrames());
        }
    }

    @Override
    void channelRead(ChannelHandlerContext ctx, Http3ControlStreamFrame frame) throws QpackException {
        boolean isSettingsFrame = frame instanceof Http3SettingsFrame;
        // RFC 9114：控制流第一帧必须是 SETTINGS
        if (!firstFrameRead && !isSettingsFrame) {
            connectionError(ctx, H3_MISSING_SETTINGS, "Missing settings frame.", forwardControlFrames());
            ReferenceCountUtil.release(frame);
            return;
        }
        if (firstFrameRead && isSettingsFrame) {
            connectionError(ctx, H3_FRAME_UNEXPECTED, "Second settings frame received.", forwardControlFrames());
            ReferenceCountUtil.release(frame);
            return;
        }
        firstFrameRead = true;

        final boolean valid;
        if (isSettingsFrame) {
            valid = handleHttp3SettingsFrame(ctx, (Http3SettingsFrame) frame);
        } else if (frame instanceof Http3GoAwayFrame) {
            valid = handleHttp3GoAwayFrame(ctx, (Http3GoAwayFrame) frame);
        } else if (frame instanceof Http3MaxPushIdFrame) {
            valid = handleHttp3MaxPushIdFrame(ctx, (Http3MaxPushIdFrame) frame);
        } else if (frame instanceof Http3CancelPushFrame) {
            valid = handleHttp3CancelPushFrame(ctx, (Http3CancelPushFrame) frame);
        } else {
            // Http3UnknownFrame 无需特殊校验，转发或释放即可
            assert frame instanceof Http3UnknownFrame;
            valid = true;
        }

        if (!valid || controlFrameHandler == null) {
            ReferenceCountUtil.release(frame);
            return;
        }

        // 用户注册了 controlFrameHandler 时才向上游传递
        ctx.fireChannelRead(frame);
    }

    private boolean handleHttp3SettingsFrame(ChannelHandlerContext ctx, Http3SettingsFrame settingsFrame)
            throws QpackException {
        final QuicChannel quicChannel = (QuicChannel) ctx.channel().parent();
        final QpackAttributes qpackAttributes = Http3.getQpackAttributes(quicChannel);
        assert qpackAttributes != null;
        final GenericFutureListener<Future<? super QuicStreamChannel>> closeOnFailure = future -> {
            if (!future.isSuccess()) {
                criticalStreamClosed(ctx);
            }
        };
        if (qpackAttributes.dynamicTableDisabled()) {
            // 动态表禁用时仍调用 configureDynamicTable(0,0) 完成 encoder 初始化
            qpackEncoder.configureDynamicTable(qpackAttributes, 0, 0);
            return true;
        }
        // 根据 SETTINGS 中的 QPACK 参数创建 encoder/decoder 单向流
        quicChannel.createStream(QuicStreamType.UNIDIRECTIONAL,
                new QPackEncoderStreamInitializer(qpackEncoder, qpackAttributes,
                        settingsFrame
                                .settings()
                                .getOrDefault(
                                        Http3SettingIdentifier.HTTP3_SETTINGS_QPACK_MAX_TABLE_CAPACITY.id(),
                                        0L
                                ),
                        settingsFrame
                                .settings()
                                .getOrDefault(
                                        Http3SettingIdentifier.HTTP3_SETTINGS_QPACK_BLOCKED_STREAMS.id(),
                                        0L
                                )
                        )
                )
                .addListener(closeOnFailure);
        quicChannel.createStream(QuicStreamType.UNIDIRECTIONAL, new QPackDecoderStreamInitializer(qpackAttributes))
                .addListener(closeOnFailure);
        return true;
    }

    private boolean handleHttp3GoAwayFrame(ChannelHandlerContext ctx, Http3GoAwayFrame goAwayFrame) {
        long id = goAwayFrame.id();
        // 客户端：GOAWAY id 必须对应请求流（id % 4 == 0）
        if (!server && id % 4 != 0) {
            connectionError(ctx, H3_FRAME_UNEXPECTED, "GOAWAY received with ID of non-request stream.",
                    forwardControlFrames());
            return false;
        }
        if (receivedGoawayId != null && id > receivedGoawayId) {
            // 新 GOAWAY 的 id 不能大于先前收到的（只能缩小 graceful 窗口）
            connectionError(ctx, H3_ID_ERROR,
                    "GOAWAY received with ID larger than previously received.", forwardControlFrames());
            return false;
        }
        receivedGoawayId = id;
        return true;
    }

    private boolean handleHttp3MaxPushIdFrame(ChannelHandlerContext ctx, Http3MaxPushIdFrame frame) {
        long id = frame.id();
        // MAX_PUSH_ID 仅服务端可接收
        if (!server) {
            connectionError(ctx, H3_FRAME_UNEXPECTED, "MAX_PUSH_ID received by client.",
                    forwardControlFrames());
            return false;
        }
        if (receivedMaxPushId != null && id < receivedMaxPushId) {
            connectionError(ctx, H3_ID_ERROR, "MAX_PUSH_ID reduced limit.", forwardControlFrames());
            return false;
        }
        receivedMaxPushId = id;
        return true;
    }

    private boolean handleHttp3CancelPushFrame(ChannelHandlerContext ctx, Http3CancelPushFrame cancelPushFrame) {
        final Long maxPushId = server ? receivedMaxPushId : remoteControlStreamHandler.sentMaxPushId();
        if (maxPushId == null || maxPushId < cancelPushFrame.id()) {
            connectionError(ctx, H3_ID_ERROR, "CANCEL_PUSH received with an ID greater than MAX_PUSH_ID.",
                    forwardControlFrames());
            return false;
        }
        return true;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.fireChannelReadComplete();

        // control 流必须持续读取，即使用户关闭了 AUTO_READ
        Http3CodecUtils.readIfNoAutoRead(ctx);
    }

    @Override
    public boolean isSharable() {
        // 持有 firstFrameRead、GOAWAY 等 per-connection 状态
        return false;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof ChannelInputShutdownEvent) {
            // See https://www.ietf.org/archive/id/draft-ietf-quic-qpack-19.html#section-4.2
            criticalStreamClosed(ctx);
        }
        ctx.fireUserEventTriggered(evt);
    }

    /** QPACK 单向流建立时的公共逻辑：先写 stream type 前缀，再通知子类绑定 stream。 */
    private abstract static class AbstractQPackStreamInitializer extends ChannelInboundHandlerAdapter {
        private final int streamType;
        protected final QpackAttributes attributes;

        AbstractQPackStreamInitializer(int streamType, QpackAttributes attributes) {
            this.streamType = streamType;
            this.attributes = attributes;
        }

        @Override
        public final void channelActive(ChannelHandlerContext ctx) {
            // 单向流首字节为 stream type（encoder=0x02, decoder=0x03）
            ByteBuf buffer = ctx.alloc().buffer(8);
            Http3CodecUtils.writeVariableLengthInteger(buffer, streamType);
            closeOnFailure(ctx.writeAndFlush(buffer));
            streamAvailable(ctx);
            ctx.fireChannelActive();
        }

        @Override
        public final void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
            streamClosed(ctx);
            if (evt instanceof ChannelInputShutdownEvent) {
                // See https://quicwg.org/base-drafts/draft-ietf-quic-qpack.html#section-4.2
                criticalStreamClosed(ctx);
            }
            ctx.fireUserEventTriggered(evt);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            streamClosed(ctx);
            // See https://quicwg.org/base-drafts/draft-ietf-quic-qpack.html#section-4.2
            criticalStreamClosed(ctx);
            ctx.fireChannelInactive();
        }

        protected abstract void streamAvailable(ChannelHandlerContext ctx);

        protected abstract void streamClosed(ChannelHandlerContext ctx);
    }

    private static final class QPackEncoderStreamInitializer extends AbstractQPackStreamInitializer {
        private static final ClosedChannelException ENCODER_STREAM_INACTIVE =
                unknownStackTrace(new ClosedChannelException(), ClosedChannelException.class, "streamClosed()");
        private final QpackEncoder encoder;
        private final long maxTableCapacity;
        private final long blockedStreams;

        QPackEncoderStreamInitializer(QpackEncoder encoder, QpackAttributes attributes, long maxTableCapacity,
                                      long blockedStreams) {
            super(Http3CodecUtils.HTTP3_QPACK_ENCODER_STREAM_TYPE, attributes);
            this.encoder = encoder;
            this.maxTableCapacity = maxTableCapacity;
            this.blockedStreams = blockedStreams;
        }

        @Override
        protected void streamAvailable(ChannelHandlerContext ctx) {
            final QuicStreamChannel stream = (QuicStreamChannel) ctx.channel();
            attributes.encoderStream(stream);

            try {
                encoder.configureDynamicTable(attributes, maxTableCapacity, toIntOrThrow(blockedStreams));
            } catch (QpackException e) {
                connectionError(ctx, new Http3Exception(QPACK_ENCODER_STREAM_ERROR,
                        "Dynamic table configuration failed.", e), true);
            }
        }

        @Override
        protected void streamClosed(ChannelHandlerContext ctx) {
            attributes.encoderStreamInactive(ENCODER_STREAM_INACTIVE);
        }
    }

    private static final class QPackDecoderStreamInitializer extends AbstractQPackStreamInitializer {
        private static final ClosedChannelException DECODER_STREAM_INACTIVE =
                unknownStackTrace(new ClosedChannelException(), ClosedChannelException.class, "streamClosed()");
        private QPackDecoderStreamInitializer(QpackAttributes attributes) {
            super(Http3CodecUtils.HTTP3_QPACK_DECODER_STREAM_TYPE, attributes);
        }

        @Override
        protected void streamAvailable(ChannelHandlerContext ctx) {
            attributes.decoderStream((QuicStreamChannel) ctx.channel());
        }

        @Override
        protected void streamClosed(ChannelHandlerContext ctx) {
            attributes.decoderStreamInactive(DECODER_STREAM_INACTIVE);
        }
    }
}
