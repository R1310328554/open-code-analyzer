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
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.base64.Base64;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientUpgradeHandler;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.collection.CharObjectMap;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static io.netty.handler.codec.base64.Base64Dialect.URL_SAFE;
import static io.netty.handler.codec.http2.Http2CodecUtil.HTTP_UPGRADE_PROTOCOL_NAME;
import static io.netty.handler.codec.http2.Http2CodecUtil.HTTP_UPGRADE_SETTINGS_HEADER;
import static io.netty.handler.codec.http2.Http2CodecUtil.SETTING_ENTRY_LENGTH;
import static io.netty.util.CharsetUtil.UTF_8;
import static io.netty.util.ReferenceCountUtil.release;
import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * 客户端明文 HTTP/1.1 → HTTP/2（h2c）升级编解码器，实现 {@link HttpClientUpgradeHandler.UpgradeCodec}。
 * <p>在 Upgrade 请求中注入 Base64 编码的 {@code HTTP2-Settings} 头，101 响应后切换 pipeline 并预留流 1 给升级响应。
 */
public class Http2ClientUpgradeCodec implements HttpClientUpgradeHandler.UpgradeCodec {

    private static final List<CharSequence> UPGRADE_HEADERS = Collections.singletonList(HTTP_UPGRADE_SETTINGS_HEADER);

    /** pipeline 中 HTTP/2 连接 handler 的名称，null 时自动生成。 */
    private final String handlerName;
    /** 底层连接状态机，负责 onHttpClientUpgrade 预留流 1。 */
    private final Http2ConnectionHandler connectionHandler;
    /** 升级成功后实际写入 pipeline 的 handler（可与 connectionHandler 相同）。 */
    private final ChannelHandler upgradeToHandler;
    /** 可选的 {@link Http2MultiplexHandler}，与 {@link Http2FrameCodec} 配合使用。 */
    private final ChannelHandler http2MultiplexHandler;

    public Http2ClientUpgradeCodec(Http2FrameCodec frameCodec, ChannelHandler upgradeToHandler) {
        this(null, frameCodec, upgradeToHandler);
    }

    public Http2ClientUpgradeCodec(String handlerName, Http2FrameCodec frameCodec, ChannelHandler upgradeToHandler) {
        this(handlerName, (Http2ConnectionHandler) frameCodec, upgradeToHandler, null);
    }

    /**
     * Creates the codec using a default name for the connection handler when adding to the
     * pipeline.
     *
     * @param connectionHandler the HTTP/2 connection handler
     */
    public Http2ClientUpgradeCodec(Http2ConnectionHandler connectionHandler) {
        this((String) null, connectionHandler);
    }

    /**
     * Creates the codec using a default name for the connection handler when adding to the
     * pipeline.
     *
     * @param connectionHandler the HTTP/2 connection handler
     * @param http2MultiplexHandler the Http2 Multiplexer handler to work with Http2FrameCodec
     */
    public Http2ClientUpgradeCodec(Http2ConnectionHandler connectionHandler,
        Http2MultiplexHandler http2MultiplexHandler) {
        this((String) null, connectionHandler, http2MultiplexHandler);
    }

    /**
     * Creates the codec providing an upgrade to the given handler for HTTP/2.
     *
     * @param handlerName the name of the HTTP/2 connection handler to be used in the pipeline,
     *                    or {@code null} to auto-generate the name
     * @param connectionHandler the HTTP/2 connection handler
     */
    public Http2ClientUpgradeCodec(String handlerName, Http2ConnectionHandler connectionHandler) {
        this(handlerName, connectionHandler, connectionHandler, null);
    }

    /**
     * Creates the codec providing an upgrade to the given handler for HTTP/2.
     *
     * @param handlerName the name of the HTTP/2 connection handler to be used in the pipeline,
     *                    or {@code null} to auto-generate the name
     * @param connectionHandler the HTTP/2 connection handler
     */
    public Http2ClientUpgradeCodec(String handlerName, Http2ConnectionHandler connectionHandler,
        Http2MultiplexHandler http2MultiplexHandler) {
        this(handlerName, connectionHandler, connectionHandler, http2MultiplexHandler);
    }

    private Http2ClientUpgradeCodec(String handlerName, Http2ConnectionHandler connectionHandler, ChannelHandler
        upgradeToHandler, Http2MultiplexHandler http2MultiplexHandler) {
        this.handlerName = handlerName;
        this.connectionHandler = checkNotNull(connectionHandler, "connectionHandler");
        this.upgradeToHandler = checkNotNull(upgradeToHandler, "upgradeToHandler");
        this.http2MultiplexHandler = http2MultiplexHandler;
    }

    @Override
    public CharSequence protocol() {
        return HTTP_UPGRADE_PROTOCOL_NAME;
    }

    @Override
    public Collection<CharSequence> setUpgradeHeaders(ChannelHandlerContext ctx,
        HttpRequest upgradeRequest) {
        CharSequence settingsValue = getSettingsHeaderValue(ctx);
        upgradeRequest.headers().set(HTTP_UPGRADE_SETTINGS_HEADER, settingsValue);
        return UPGRADE_HEADERS;
    }

    @Override
    public void upgradeTo(ChannelHandlerContext ctx, FullHttpResponse upgradeResponse)
        throws Exception {
        try {
            // 将 HTTP/2 handler 插入当前 handler 之后，完成协议切换
            ctx.pipeline().addAfter(ctx.name(), handlerName, upgradeToHandler);

            // Http2MultiplexHandler 处理 connectionHandler 产生的事件（见 netty#9495）
            if (http2MultiplexHandler != null) {
                final String name = ctx.pipeline().context(connectionHandler).name();
                ctx.pipeline().addAfter(name, null, http2MultiplexHandler);
            }

            // 客户端升级场景下预留本地流 1，用于接收服务器对 Upgrade 的 HTTP/2 响应
            connectionHandler.onHttpClientUpgrade();
        } catch (Http2Exception e) {
            ctx.fireExceptionCaught(e);
            ctx.close();
        }
    }

    /**
     * 将本端 {@link Http2Settings} 序列化为 SETTINGS 帧载荷，再 Base64（URL_SAFE）编码供 HTTP2-Settings 头使用。
     */
    private CharSequence getSettingsHeaderValue(ChannelHandlerContext ctx) {
        ByteBuf buf = null;
        ByteBuf encodedBuf = null;
        try {
            // Get the local settings for the handler.
            Http2Settings settings = connectionHandler.decoder().localSettings();

            // Serialize the payload of the SETTINGS frame.
            int payloadLength = SETTING_ENTRY_LENGTH * settings.size();
            buf = ctx.alloc().buffer(payloadLength);
            for (CharObjectMap.PrimitiveEntry<Long> entry : settings.entries()) {
                buf.writeChar(entry.key());
                buf.writeInt(entry.value().intValue());
            }

            // Base64 encode the payload and then convert to a string for the header.
            encodedBuf = Base64.encode(buf, URL_SAFE);
            return encodedBuf.toString(UTF_8);
        } finally {
            release(buf);
            release(encodedBuf);
        }
    }
}
