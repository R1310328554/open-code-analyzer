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

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.quic.QuicChannel;
import io.netty.handler.codec.quic.QuicClientCodecBuilder;
import io.netty.handler.codec.quic.QuicCodecBuilder;
import io.netty.handler.codec.quic.QuicServerCodecBuilder;
import io.netty.handler.codec.quic.QuicStreamChannel;
import io.netty.handler.codec.quic.QuicStreamChannelBootstrap;
import io.netty.handler.codec.quic.QuicStreamType;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import org.jetbrains.annotations.Nullable;

/**
 * Contains utility methods that help to bootstrap server / clients with HTTP3 support.
 * <p>封装 ALPN 协议名、QUIC 传输参数下限、请求流创建及连接级属性（控制流、QPACK 状态）的读写。
 */
public final class Http3 {

    private Http3() {  }

    /** 按优先级排列的 HTTP/3 ALPN 标识符（draft 版本与最终 h3）。 */
    private static final String[] H3_PROTOS = new String[] {
            "h3-29",
            "h3-30",
            "h3-31",
            "h3-32",
            "h3"
    };

    /** 本端发起的 HTTP/3 控制流，挂载在 {@link QuicChannel} 的 Channel 属性上。 */
    private static final AttributeKey<QuicStreamChannel> HTTP3_CONTROL_STREAM_KEY =
            AttributeKey.valueOf(Http3.class, "HTTP3ControlStream");

    /** 连接级 QPACK 编解码共享状态（动态表、阻塞流计数等）。 */
    private static final AttributeKey<QpackAttributes> QPACK_ATTRIBUTES_KEY =
            AttributeKey.valueOf(Http3.class, "QpackAttributes");

    /**
     * Returns the local initiated control stream for the HTTP/3 connection.
     * @param channel   the channel for the HTTP/3 connection.
     * @return          the control stream.
     */
    @Nullable
    public static QuicStreamChannel getLocalControlStream(Channel channel) {
        return channel.attr(HTTP3_CONTROL_STREAM_KEY).get();
    }

    /**
     * Returns the value of the <a
     * href="https://quicwg.org/base-drafts/draft-ietf-quic-http.html#name-max_push_id">max push ID</a> received for
     * this connection.
     *
     * @return Received <a
     * href="https://quicwg.org/base-drafts/draft-ietf-quic-http.html#name-max_push_id">max push ID</a> for this
     * connection.
     */
    static long maxPushIdReceived(QuicChannel channel) {
        final Http3ConnectionHandler connectionHandler = Http3CodecUtils.getConnectionHandlerOrClose(channel);
        if (connectionHandler == null) {
            throw new IllegalStateException("Connection handler not found.");
        }
        return connectionHandler.localControlStreamHandler.maxPushIdReceived();
    }

    static void setLocalControlStream(Channel channel, QuicStreamChannel controlStreamChannel) {
        channel.attr(HTTP3_CONTROL_STREAM_KEY).set(controlStreamChannel);
    }

    @Nullable
    static QpackAttributes getQpackAttributes(Channel channel) {
        return channel.attr(QPACK_ATTRIBUTES_KEY).get();
    }

    static void setQpackAttributes(Channel channel, QpackAttributes attributes) {
        channel.attr(QPACK_ATTRIBUTES_KEY).set(attributes);
    }

    /**
     * Returns a new HTTP/3 request-stream that will use the given {@link ChannelHandler}
     * to dispatch {@link Http3RequestStreamFrame}s too. The needed HTTP/3 codecs are automatically added to the
     * pipeline as well.
     *
     * If you need more control you can also use the {@link Http3RequestStreamInitializer} directly.
     *
     * @param channel   the {@link QuicChannel} for which we create the request-stream.
     * @param handler   the {@link ChannelHandler} to add.
     * @return          the {@link Future} that will be notified once the request-stream was opened.
     */
    public static Future<QuicStreamChannel> newRequestStream(QuicChannel channel, ChannelHandler handler) {
        return channel.createStream(QuicStreamType.BIDIRECTIONAL, requestStreamInitializer(handler));
    }

    /**
     * Returns a new HTTP/3 request-stream bootstrap that will use the given {@link ChannelHandler}
     * to dispatch {@link Http3RequestStreamFrame}s too. The needed HTTP/3 codecs are automatically added to the
     * pipeline as well.
     *
     * If you need more control you can also use the {@link Http3RequestStreamInitializer} directly.
     *
     * @param channel   the {@link QuicChannel} for which we create the request-stream.
     * @param handler   the {@link ChannelHandler} to add.
     * @return          the {@link QuicStreamChannelBootstrap} that should be used.
     */
    public static QuicStreamChannelBootstrap newRequestStreamBootstrap(QuicChannel channel, ChannelHandler handler) {
        return channel.newStreamBootstrap().handler(requestStreamInitializer(handler))
                .type(QuicStreamType.BIDIRECTIONAL);
    }

    /**
     * Returns the supported protocols for H3.
     *
     * @return the supported protocols.
     */
    public static String[] supportedApplicationProtocols() {
        return H3_PROTOS.clone();
    }

    /**
     * <a href="https://tools.ietf.org/html/draft-ietf-quic-http-32#section-6.2">
     *     Minimum number max unidirectional streams</a>.
     */
    // 至少需预留：控制流、QPACK 解码流、QPACK 编码流
    public static final int MIN_INITIAL_MAX_STREAMS_UNIDIRECTIONAL = 3;

    /**
     * <a href="https://tools.ietf.org/html/draft-ietf-quic-http-32#section-6.2">
     *     Minimum max data for unidirectional streams</a>.
     */
    public static final int MIN_INITIAL_MAX_STREAM_DATA_UNIDIRECTIONAL = 1024;

    /**
     * Returns a new {@link QuicServerCodecBuilder} that has preconfigured for HTTP3.
     *
     * @return a pre-configured builder for HTTP3.
     */
    public static QuicServerCodecBuilder newQuicServerCodecBuilder() {
        return configure(new QuicServerCodecBuilder());
    }

    /**
     * Returns a new {@link QuicClientCodecBuilder} that has preconfigured for HTTP3.
     *
     * @return a pre-configured builder for HTTP3.
     */
    public static QuicClientCodecBuilder newQuicClientCodecBuilder() {
        return configure(new QuicClientCodecBuilder());
    }

    /** 注入 HTTP/3 规范要求的最小单向流数量与流数据上限。 */
    private static <T extends QuicCodecBuilder<T>> T configure(T builder) {
        return builder.initialMaxStreamsUnidirectional(MIN_INITIAL_MAX_STREAMS_UNIDIRECTIONAL)
                .initialMaxStreamDataUnidirectional(MIN_INITIAL_MAX_STREAM_DATA_UNIDIRECTIONAL);
    }

    /** 若 handler 已是 Initializer 则直接复用，否则包装为在 initRequestStream 末尾追加 handler。 */
    private static Http3RequestStreamInitializer requestStreamInitializer(ChannelHandler handler) {
        if (handler instanceof Http3RequestStreamInitializer) {
            return (Http3RequestStreamInitializer) handler;
        }
        return new Http3RequestStreamInitializer() {
            @Override
            protected void initRequestStream(QuicStreamChannel ch) {
                ch.pipeline().addLast(handler);
            }
        };
    }
}
