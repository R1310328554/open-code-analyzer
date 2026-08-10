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

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.quic.QuicStreamChannel;
import org.jetbrains.annotations.Nullable;

import java.util.function.LongFunction;

/**
 * HTTP/3 客户端连接处理器：拒绝服务端发起的双向流，并为 push/单向流挂载客户端侧 pipeline。
 */
public class Http3ClientConnectionHandler extends Http3ConnectionHandler {

    /** 按 push ID 为 server push 流创建自定义 {@link ChannelHandler}，{@code null} 表示默认处理。 */
    private final LongFunction<ChannelHandler> pushStreamHandlerFactory;

    /**
     * Create a new instance.
     */
    public Http3ClientConnectionHandler() {
        this(null, null, null, null, true);
    }

    /**
     * Create a new instance.
     *
     * @param inboundControlStreamHandler           the {@link ChannelHandler} which will be notified about
     *                                              {@link Http3RequestStreamFrame}s or {@code null} if the user is not
     *                                              interested in these.
     * @param pushStreamHandlerFactory              the {@link LongFunction} that will provide a custom
     *                                              {@link ChannelHandler} for push streams {@code null} if no special
     *                                              handling should be done. When present, push ID will be passed as an
     *                                              argument to the {@link LongFunction}.
     * @param unknownInboundStreamHandlerFactory    the {@link LongFunction} that will provide a custom
     *                                              {@link ChannelHandler} for unknown inbound stream types or
     *                                              {@code null} if no special handling should be done.
     * @param localSettings                         the local {@link Http3SettingsFrame} that should be sent to the
     *                                              remote peer or {@code null} if the default settings should be used.
     * @param disableQpackDynamicTable              If QPACK dynamic table should be disabled.
     */
    public Http3ClientConnectionHandler(@Nullable ChannelHandler inboundControlStreamHandler,
                                        @Nullable LongFunction<ChannelHandler> pushStreamHandlerFactory,
                                        @Nullable LongFunction<ChannelHandler> unknownInboundStreamHandlerFactory,
                                        @Nullable Http3SettingsFrame localSettings, boolean disableQpackDynamicTable) {
        this(inboundControlStreamHandler, pushStreamHandlerFactory, unknownInboundStreamHandlerFactory, localSettings,
                disableQpackDynamicTable, null);
    }

    /**
     * Create a new instance.
     *
     * @param inboundControlStreamHandler           the {@link ChannelHandler} which will be notified about
     *                                              {@link Http3RequestStreamFrame}s or {@code null} if the user is not
     *                                              interested in these.
     * @param pushStreamHandlerFactory              the {@link LongFunction} that will provide a custom
     *                                              {@link ChannelHandler} for push streams {@code null} if no special
     *                                              handling should be done. When present, push ID will be passed as an
     *                                              argument to the {@link LongFunction}.
     * @param unknownInboundStreamHandlerFactory    the {@link LongFunction} that will provide a custom
     *                                              {@link ChannelHandler} for unknown inbound stream types or
     *                                              {@code null} if no special handling should be done.
     * @param localSettings                         the local {@link Http3SettingsFrame} that should be sent to the
     *                                              remote peer or {@code null} if the default settings should be used.
     * @param disableQpackDynamicTable              If QPACK dynamic table should be disabled.
     * @param nonStandardSettingsValidator          the {@link Http3Settings.NonStandardHttp3SettingsValidator} to use
     *                                              when validating settings that are non-standard.
     */
    public Http3ClientConnectionHandler(@Nullable ChannelHandler inboundControlStreamHandler,
                                        @Nullable LongFunction<ChannelHandler> pushStreamHandlerFactory,
                                        @Nullable LongFunction<ChannelHandler> unknownInboundStreamHandlerFactory,
                                        @Nullable Http3SettingsFrame localSettings, boolean disableQpackDynamicTable,
                                        @Nullable Http3Settings.NonStandardHttp3SettingsValidator
                                                nonStandardSettingsValidator) {
        this(inboundControlStreamHandler, pushStreamHandlerFactory, unknownInboundStreamHandlerFactory,
                localSettings, disableQpackDynamicTable, nonStandardSettingsValidator, null,
                Http3CodecUtils.DEFAULT_MAX_UNKNOWN_FRAME_PAYLOAD_LENGTH);
    }

    /**
     * Create a new instance.
     *
     * @param inboundControlStreamHandler           the {@link ChannelHandler} which will be notified about
     *                                              {@link Http3RequestStreamFrame}s or {@code null} if the user is not
     *                                              interested in these.
     * @param pushStreamHandlerFactory              the {@link LongFunction} that will provide a custom
     *                                              {@link ChannelHandler} for push streams {@code null} if no special
     *                                              handling should be done. When present, push ID will be passed as an
     *                                              argument to the {@link LongFunction}.
     * @param unknownInboundStreamHandlerFactory    the {@link LongFunction} that will provide a custom
     *                                              {@link ChannelHandler} for unknown inbound stream types or
     *                                              {@code null} if no special handling should be done.
     * @param localSettings                         the local {@link Http3SettingsFrame} that should be sent to the
     *                                              remote peer or {@code null} if the default settings should be used.
     * @param disableQpackDynamicTable              If QPACK dynamic table should be disabled.
     * @param nonStandardSettingsValidator          the {@link Http3Settings.NonStandardHttp3SettingsValidator} to use
     *                                              when validating settings that are non-standard.
     * @param sensitivityDetector                   detector that marks sensitive headers for QPACK Never Indexed
     *                                              encoding, or {@code null} to use the historical default.
     * @param maxUnknownFramePayloadLength          the maximum payload size of an unknown frame.
     */
    public Http3ClientConnectionHandler(@Nullable ChannelHandler inboundControlStreamHandler,
                                        @Nullable LongFunction<ChannelHandler> pushStreamHandlerFactory,
                                        @Nullable LongFunction<ChannelHandler> unknownInboundStreamHandlerFactory,
                                        @Nullable Http3SettingsFrame localSettings, boolean disableQpackDynamicTable,
                                        @Nullable Http3Settings.NonStandardHttp3SettingsValidator
                                                nonStandardSettingsValidator,
                                        @Nullable QpackSensitivityDetector sensitivityDetector,
                                        int maxUnknownFramePayloadLength) {
        super(false, inboundControlStreamHandler, unknownInboundStreamHandlerFactory, localSettings,
                disableQpackDynamicTable, nonStandardSettingsValidator, sensitivityDetector,
                maxUnknownFramePayloadLength);
        this.pushStreamHandlerFactory = pushStreamHandlerFactory;
    }

    @Override
    protected void initBidirectionalStream(ChannelHandlerContext ctx, QuicStreamChannel channel) {
        // RFC 9114：客户端不得接受服务端发起的双向流，否则触发 H3_STREAM_CREATION_ERROR
        // See https://datatracker.ietf.org/doc/html/rfc9114#name-bidirectional-streams
        // https://datatracker.ietf.org/doc/html/rfc9114#section-6.1-3
        Http3CodecUtils.connectionError(ctx, Http3ErrorCode.H3_STREAM_CREATION_ERROR,
                "Server initiated bidirectional streams are not allowed", true);
    }

    @Override
    protected void initUnidirectionalStream(ChannelHandlerContext ctx, QuicStreamChannel streamChannel) {
        final long maxTableCapacity = maxTableCapacity();
        // 客户端单向流入站：区分控制流、push 流、QPACK 流及未知类型
        streamChannel.pipeline().addLast(
                new Http3UnidirectionalStreamInboundClientHandler(codecFactory, nonStandardSettingsValidator,
                        localControlStreamHandler, remoteControlStreamHandler,
                        unknownInboundStreamHandlerFactory, pushStreamHandlerFactory,
                        () -> new QpackEncoderHandler(maxTableCapacity, qpackDecoder),
                        () -> new QpackDecoderHandler(qpackEncoder)));
    }
}
