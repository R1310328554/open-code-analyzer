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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpScheme;

/**
 * 将入站 HTTP/1.x 完整消息（{@link FullHttpMessage}）转换为 HTTP/2 帧事件并交给 {@link Http2FrameListener}。
 * <p>常用于 cleartext 升级（h2c）路径上，把已解码的 HTTP/1 消息喂给 HTTP/2 状态机。
 */
public class InboundHttpToHttp2Adapter extends ChannelInboundHandlerAdapter {
    private final Http2Connection connection;
    private final Http2FrameListener listener;

    public InboundHttpToHttp2Adapter(Http2Connection connection, Http2FrameListener listener) {
        this.connection = connection;
        this.listener = listener;
    }

    private static int getStreamId(Http2Connection connection, HttpHeaders httpHeaders) {
        return httpHeaders.getInt(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text(),
                                  connection.remote().incrementAndGetNextStreamId());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpMessage) {
            handle(ctx, connection, listener, (FullHttpMessage) msg);
        } else {
            super.channelRead(ctx, msg);
        }
    }

    // h2c 升级时首条消息不受流控约束，但当前 API 尚无法显式标记该特性
    static void handle(ChannelHandlerContext ctx, Http2Connection connection,
                              Http2FrameListener listener, FullHttpMessage message) throws Http2Exception {
        try {
            int streamId = getStreamId(connection, message.headers());
            Http2Stream stream = connection.stream(streamId);
            if (stream == null) {
                stream = connection.remote().createStream(streamId, false);
            }
            message.headers().set(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), HttpScheme.HTTP.name());
            Http2Headers messageHeaders = HttpConversionUtil.toHttp2Headers(message, true);
            boolean hasContent = message.content().isReadable();
            boolean hasTrailers = !message.trailingHeaders().isEmpty();
            // 首帧 HEADERS；无正文且无 trailer 时置 END_STREAM
            listener.onHeadersRead(
                    ctx, streamId, messageHeaders, 0, !(hasContent || hasTrailers));
            if (hasContent) {
                listener.onDataRead(ctx, streamId, message.content(), 0, !hasTrailers);
            }
            if (hasTrailers) {
                Http2Headers headers = HttpConversionUtil.toHttp2Headers(message.trailingHeaders(), true);
                listener.onHeadersRead(ctx, streamId, headers, 0, true);
            }
            stream.closeRemoteSide();
        } finally {
            message.release();
        }
    }
}
