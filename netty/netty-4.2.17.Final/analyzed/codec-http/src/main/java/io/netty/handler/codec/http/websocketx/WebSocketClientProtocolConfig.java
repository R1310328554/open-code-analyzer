/*
 * Copyright 2019 The Netty Project
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
package io.netty.handler.codec.http.websocketx;

import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler.ClientHandshakeStateEvent;
import io.netty.util.internal.ObjectUtil;

import java.net.URI;

import static io.netty.handler.codec.http.websocketx.WebSocketServerProtocolConfig.DEFAULT_HANDSHAKE_TIMEOUT_MILLIS;
import static io.netty.util.internal.ObjectUtil.checkPositive;

/**
 * WebSocket 客户端协议配置（不可变），通过 {@link Builder} 构建。
 */
public final class WebSocketClientProtocolConfig {

    static final boolean DEFAULT_PERFORM_MASKING = true;
    static final boolean DEFAULT_ALLOW_MASK_MISMATCH = false;
    static final boolean DEFAULT_HANDLE_CLOSE_FRAMES = true;
    static final boolean DEFAULT_DROP_PONG_FRAMES = true;
    static final boolean DEFAULT_GENERATE_ORIGIN_HEADER = true;
    static final boolean DEFAULT_WITH_UTF8_VALIDATOR = true;

    private final URI webSocketUri;
    private final String subprotocol;
    private final WebSocketVersion version;
    private final boolean allowExtensions;
    private final HttpHeaders customHeaders;
    private final int maxFramePayloadLength;
    private final boolean performMasking;
    private final boolean allowMaskMismatch;
    private final boolean handleCloseFrames;
    private final WebSocketCloseStatus sendCloseFrame;
    private final boolean dropPongFrames;
    private final long handshakeTimeoutMillis;
    private final long forceCloseTimeoutMillis;
    private final boolean absoluteUpgradeUrl;
    private final boolean generateOriginHeader;
    private final boolean withUTF8Validator;

    private WebSocketClientProtocolConfig(
        URI webSocketUri,
        String subprotocol,
        WebSocketVersion version,
        boolean allowExtensions,
        HttpHeaders customHeaders,
        int maxFramePayloadLength,
        boolean performMasking,
        boolean allowMaskMismatch,
        boolean handleCloseFrames,
        WebSocketCloseStatus sendCloseFrame,
        boolean dropPongFrames,
        long handshakeTimeoutMillis,
        long forceCloseTimeoutMillis,
        boolean absoluteUpgradeUrl,
        boolean generateOriginHeader,
        boolean withUTF8Validator
    ) {
        this.webSocketUri = webSocketUri;
        this.subprotocol = subprotocol;
        this.version = version;
        this.allowExtensions = allowExtensions;
        this.customHeaders = customHeaders;
        this.maxFramePayloadLength = maxFramePayloadLength;
        this.performMasking = performMasking;
        this.allowMaskMismatch = allowMaskMismatch;
        this.forceCloseTimeoutMillis = forceCloseTimeoutMillis;
        this.handleCloseFrames = handleCloseFrames;
        this.sendCloseFrame = sendCloseFrame;
        this.dropPongFrames = dropPongFrames;
        this.handshakeTimeoutMillis = checkPositive(handshakeTimeoutMillis, "handshakeTimeoutMillis");
        this.absoluteUpgradeUrl = absoluteUpgradeUrl;
        this.generateOriginHeader = generateOriginHeader;
        this.withUTF8Validator = withUTF8Validator;
    }

    /** 返回 WebSocket 连接 URI。 */
    public URI webSocketUri() {
        return webSocketUri;
    }

    /** 返回请求的 subprotocol。 */
    public String subprotocol() {
        return subprotocol;
    }

    /** 返回 WebSocket 协议版本。 */
    public WebSocketVersion version() {
        return version;
    }

    /** 是否允许使用帧 RSV 扩展位。 */
    public boolean allowExtensions() {
        return allowExtensions;
    }

    /** 返回握手请求的自定义 HTTP 头。 */
    public HttpHeaders customHeaders() {
        return customHeaders;
    }

    /** 返回帧载荷最大长度。 */
    public int maxFramePayloadLength() {
        return maxFramePayloadLength;
    }

    /** 出站帧是否掩码（客户端应为 true）。 */
    public boolean performMasking() {
        return performMasking;
    }

    /** 是否容忍掩码不符合规范的入站帧。 */
    public boolean allowMaskMismatch() {
        return allowMaskMismatch;
    }

    /** 是否在 handler 内自动处理 Close 帧（不向上游转发）。 */
    public boolean handleCloseFrames() {
        return handleCloseFrames;
    }

    /** 未手动发送时自动发出的 Close 帧状态；{@code null} 表示禁用。 */
    public WebSocketCloseStatus sendCloseFrame() {
        return sendCloseFrame;
    }

    /** 是否丢弃 Pong 帧（不向上游转发）。 */
    public boolean dropPongFrames() {
        return dropPongFrames;
    }

    /** 握手超时（毫秒）。 */
    public long handshakeTimeoutMillis() {
        return handshakeTimeoutMillis;
    }

    /** 关闭握手后强制断开连接的超时（毫秒）。 */
    public long forceCloseTimeoutMillis() {
        return forceCloseTimeoutMillis;
    }

    /** Upgrade 请求是否使用绝对 URL（经 HTTP 代理时常用）。 */
    public boolean absoluteUpgradeUrl() {
        return absoluteUpgradeUrl;
    }

    /** 是否自动生成 Origin / Sec-WebSocket-Origin 头。 */
    public boolean generateOriginHeader() {
        return generateOriginHeader;
    }

    /** 是否对文本帧载荷做 UTF-8 校验。 */
    public boolean withUTF8Validator() {
        return withUTF8Validator;
    }

    @Override
    public String toString() {
        return "WebSocketClientProtocolConfig" +
               " {webSocketUri=" + webSocketUri +
               ", subprotocol=" + subprotocol +
               ", version=" + version +
               ", allowExtensions=" + allowExtensions +
               ", customHeaders=" + customHeaders +
               ", maxFramePayloadLength=" + maxFramePayloadLength +
               ", performMasking=" + performMasking +
               ", allowMaskMismatch=" + allowMaskMismatch +
               ", handleCloseFrames=" + handleCloseFrames +
               ", sendCloseFrame=" + sendCloseFrame +
               ", dropPongFrames=" + dropPongFrames +
               ", handshakeTimeoutMillis=" + handshakeTimeoutMillis +
               ", forceCloseTimeoutMillis=" + forceCloseTimeoutMillis +
               ", absoluteUpgradeUrl=" + absoluteUpgradeUrl +
               ", generateOriginHeader=" + generateOriginHeader +
               "}";
    }

    /** 基于当前配置创建 {@link Builder} 副本。 */
    public Builder toBuilder() {
        return new Builder(this);
    }

    /** 创建带默认值的 {@link Builder}。 */
    public static Builder newBuilder() {
        return new Builder(
                URI.create("https://localhost/"),
                null,
                WebSocketVersion.V13,
                false,
                EmptyHttpHeaders.INSTANCE,
                65536,
                DEFAULT_PERFORM_MASKING,
                DEFAULT_ALLOW_MASK_MISMATCH,
                DEFAULT_HANDLE_CLOSE_FRAMES,
                WebSocketCloseStatus.NORMAL_CLOSURE,
                DEFAULT_DROP_PONG_FRAMES,
                DEFAULT_HANDSHAKE_TIMEOUT_MILLIS,
                -1,
                false,
                DEFAULT_GENERATE_ORIGIN_HEADER,
                DEFAULT_WITH_UTF8_VALIDATOR);
    }

    /** 客户端协议配置的流式构建器。 */
    public static final class Builder {
        private URI webSocketUri;
        private String subprotocol;
        private WebSocketVersion version;
        private boolean allowExtensions;
        private HttpHeaders customHeaders;
        private int maxFramePayloadLength;
        private boolean performMasking;
        private boolean allowMaskMismatch;
        private boolean handleCloseFrames;
        private WebSocketCloseStatus sendCloseFrame;
        private boolean dropPongFrames;
        private long handshakeTimeoutMillis;
        private long forceCloseTimeoutMillis;
        private boolean absoluteUpgradeUrl;
        private boolean generateOriginHeader;
        private boolean withUTF8Validator;

        private Builder(WebSocketClientProtocolConfig clientConfig) {
            this(ObjectUtil.checkNotNull(clientConfig, "clientConfig").webSocketUri(),
                 clientConfig.subprotocol(),
                 clientConfig.version(),
                 clientConfig.allowExtensions(),
                 clientConfig.customHeaders(),
                 clientConfig.maxFramePayloadLength(),
                 clientConfig.performMasking(),
                 clientConfig.allowMaskMismatch(),
                 clientConfig.handleCloseFrames(),
                 clientConfig.sendCloseFrame(),
                 clientConfig.dropPongFrames(),
                 clientConfig.handshakeTimeoutMillis(),
                 clientConfig.forceCloseTimeoutMillis(),
                 clientConfig.absoluteUpgradeUrl(),
                 clientConfig.generateOriginHeader(),
                 clientConfig.withUTF8Validator());
        }

        private Builder(URI webSocketUri,
                        String subprotocol,
                        WebSocketVersion version,
                        boolean allowExtensions,
                        HttpHeaders customHeaders,
                        int maxFramePayloadLength,
                        boolean performMasking,
                        boolean allowMaskMismatch,
                        boolean handleCloseFrames,
                        WebSocketCloseStatus sendCloseFrame,
                        boolean dropPongFrames,
                        long handshakeTimeoutMillis,
                        long forceCloseTimeoutMillis,
                        boolean absoluteUpgradeUrl,
                        boolean generateOriginHeader,
                        boolean withUTF8Validator) {
            this.webSocketUri = webSocketUri;
            this.subprotocol = subprotocol;
            this.version = version;
            this.allowExtensions = allowExtensions;
            this.customHeaders = customHeaders;
            this.maxFramePayloadLength = maxFramePayloadLength;
            this.performMasking = performMasking;
            this.allowMaskMismatch = allowMaskMismatch;
            this.handleCloseFrames = handleCloseFrames;
            this.sendCloseFrame = sendCloseFrame;
            this.dropPongFrames = dropPongFrames;
            this.handshakeTimeoutMillis = handshakeTimeoutMillis;
            this.forceCloseTimeoutMillis = forceCloseTimeoutMillis;
            this.absoluteUpgradeUrl = absoluteUpgradeUrl;
            this.generateOriginHeader = generateOriginHeader;
            this.withUTF8Validator = withUTF8Validator;
        }

        /**
         * WebSocket 连接 URI，例如 {@code ws://myhost.com/mypath}。
         */
        public Builder webSocketUri(String webSocketUri) {
            return webSocketUri(URI.create(webSocketUri));
        }

        /**
         * WebSocket 连接 URI。
         */
        public Builder webSocketUri(URI webSocketUri) {
            this.webSocketUri = webSocketUri;
            return this;
        }

        /** 请求的 subprotocol。 */
        public Builder subprotocol(String subprotocol) {
            this.subprotocol = subprotocol;
            return this;
        }

        /** WebSocket 协议版本。 */
        public Builder version(WebSocketVersion version) {
            this.version = version;
            return this;
        }

        /** 是否允许帧 RSV 扩展位。 */
        public Builder allowExtensions(boolean allowExtensions) {
            this.allowExtensions = allowExtensions;
            return this;
        }

        /** 握手请求的自定义 HTTP 头。 */
        public Builder customHeaders(HttpHeaders customHeaders) {
            this.customHeaders = customHeaders;
            return this;
        }

        /** 帧载荷最大长度。 */
        public Builder maxFramePayloadLength(int maxFramePayloadLength) {
            this.maxFramePayloadLength = maxFramePayloadLength;
            return this;
        }

        /**
         * 出站帧是否掩码；符合规范时客户端必须为 true，
         * 与非标准服务端通信时可设为 false 以提升性能。
         */
        public Builder performMasking(boolean performMasking) {
            this.performMasking = performMasking;
            return this;
        }

        /** 是否接受掩码不符合 RFC 的入站帧。 */
        public Builder allowMaskMismatch(boolean allowMaskMismatch) {
            this.allowMaskMismatch = allowMaskMismatch;
            return this;
        }

        /** {@code true} 时在 handler 内处理 Close 帧并关闭连接，不向上游转发。 */
        public Builder handleCloseFrames(boolean handleCloseFrames) {
            this.handleCloseFrames = handleCloseFrames;
            return this;
        }

        /** 未手动关闭时自动发送的 Close 帧；{@code null} 禁用。 */
        public Builder sendCloseFrame(WebSocketCloseStatus sendCloseFrame) {
            this.sendCloseFrame = sendCloseFrame;
            return this;
        }

        /** {@code true} 时不向上游转发 Pong 帧。 */
        public Builder dropPongFrames(boolean dropPongFrames) {
            this.dropPongFrames = dropPongFrames;
            return this;
        }

        /**
         * 握手超时（毫秒）；超时触发 {@link ClientHandshakeStateEvent#HANDSHAKE_TIMEOUT}。
         */
        public Builder handshakeTimeoutMillis(long handshakeTimeoutMillis) {
            this.handshakeTimeoutMillis = handshakeTimeoutMillis;
            return this;
        }

        /** 关闭握手后若服务端未断开，强制关闭连接的超时（毫秒）。 */
        public Builder forceCloseTimeoutMillis(long forceCloseTimeoutMillis) {
            this.forceCloseTimeoutMillis = forceCloseTimeoutMillis;
            return this;
        }

        /** Upgrade 请求使用绝对 URL（经 HTTP 代理连接时常用）。 */
        public Builder absoluteUpgradeUrl(boolean absoluteUpgradeUrl) {
            this.absoluteUpgradeUrl = absoluteUpgradeUrl;
            return this;
        }

        /**
         * 是否根据 webSocketURI 自动生成 {@code Origin} / {@code Sec-WebSocket-Origin} 头；
         * 默认 {@code true} 以保持向后兼容。
         */
        public Builder generateOriginHeader(boolean generateOriginHeader) {
            this.generateOriginHeader = generateOriginHeader;
            return this;
        }

        /** 是否对文本 WebSocket 帧载荷启用 UTF-8 校验（默认开启）。 */
        public Builder withUTF8Validator(boolean withUTF8Validator) {
            this.withUTF8Validator = withUTF8Validator;
            return this;
        }

        /** 构建不可变的 {@link WebSocketClientProtocolConfig}。 */
        public WebSocketClientProtocolConfig build() {
            return new WebSocketClientProtocolConfig(
                webSocketUri,
                subprotocol,
                version,
                allowExtensions,
                customHeaders,
                maxFramePayloadLength,
                performMasking,
                allowMaskMismatch,
                handleCloseFrames,
                sendCloseFrame,
                dropPongFrames,
                handshakeTimeoutMillis,
                forceCloseTimeoutMillis,
                absoluteUpgradeUrl,
                generateOriginHeader,
                withUTF8Validator
            );
        }
    }
}
