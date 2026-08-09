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

import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler.ClientHandshakeStateEvent;
import io.netty.util.internal.ObjectUtil;

import static io.netty.util.internal.ObjectUtil.checkPositive;

/**
 * WebSocket 服务端协议配置（不可变），通过 {@link Builder} 构建。
 */
public final class WebSocketServerProtocolConfig {

    /** 默认握手超时（毫秒）。 */
    static final long DEFAULT_HANDSHAKE_TIMEOUT_MILLIS = 10000L;

    private final String websocketPath;
    private final String subprotocols;
    private final boolean checkStartsWith;
    private final long handshakeTimeoutMillis;
    private final long forceCloseTimeoutMillis;
    private final boolean handleCloseFrames;
    private final WebSocketCloseStatus sendCloseFrame;
    private final boolean dropPongFrames;
    private final WebSocketDecoderConfig decoderConfig;

    private WebSocketServerProtocolConfig(
        String websocketPath,
        String subprotocols,
        boolean checkStartsWith,
        long handshakeTimeoutMillis,
        long forceCloseTimeoutMillis,
        boolean handleCloseFrames,
        WebSocketCloseStatus sendCloseFrame,
        boolean dropPongFrames,
        WebSocketDecoderConfig decoderConfig
    ) {
        this.websocketPath = websocketPath;
        this.subprotocols = subprotocols;
        this.checkStartsWith = checkStartsWith;
        this.handshakeTimeoutMillis = checkPositive(handshakeTimeoutMillis, "handshakeTimeoutMillis");
        this.forceCloseTimeoutMillis = forceCloseTimeoutMillis;
        this.handleCloseFrames = handleCloseFrames;
        this.sendCloseFrame = sendCloseFrame;
        this.dropPongFrames = dropPongFrames;
        this.decoderConfig = decoderConfig == null ? WebSocketDecoderConfig.DEFAULT : decoderConfig;
    }

    /** 返回 WebSocket 升级路径。 */
    public String websocketPath() {
        return websocketPath;
    }

    /** 返回支持的子协议 CSV 列表。 */
    public String subprotocols() {
        return subprotocols;
    }

    /** 是否以路径前缀匹配升级 URI（否则精确匹配）。 */
    public boolean checkStartsWith() {
        return checkStartsWith;
    }

    /** 返回握手超时时间（毫秒）。 */
    public long handshakeTimeoutMillis() {
        return handshakeTimeoutMillis;
    }

    /** 返回强制关闭连接的超时（毫秒）；0 表示不强制。 */
    public long forceCloseTimeoutMillis() {
        return forceCloseTimeoutMillis;
    }

    /** 是否由本处理器直接处理 Close 帧（而非转发给下游）。 */
    public boolean handleCloseFrames() {
        return handleCloseFrames;
    }

    /** 返回未手动发送关闭帧时自动发送的 Close 状态码。 */
    public WebSocketCloseStatus sendCloseFrame() {
        return sendCloseFrame;
    }

    /** 是否丢弃 Pong 帧而不向下游传递。 */
    public boolean dropPongFrames() {
        return dropPongFrames;
    }

    /** 返回帧解码器配置。 */
    public WebSocketDecoderConfig decoderConfig() {
        return decoderConfig;
    }

    @Override
    public String toString() {
        return "WebSocketServerProtocolConfig" +
            " {websocketPath=" + websocketPath +
            ", subprotocols=" + subprotocols +
            ", checkStartsWith=" + checkStartsWith +
            ", handshakeTimeoutMillis=" + handshakeTimeoutMillis +
            ", forceCloseTimeoutMillis=" + forceCloseTimeoutMillis +
            ", handleCloseFrames=" + handleCloseFrames +
            ", sendCloseFrame=" + sendCloseFrame +
            ", dropPongFrames=" + dropPongFrames +
            ", decoderConfig=" + decoderConfig +
            "}";
    }

    /** 基于当前配置创建可变 {@link Builder}。 */
    public Builder toBuilder() {
        return new Builder(this);
    }

    /** 创建带默认值的 {@link Builder}。 */
    public static Builder newBuilder() {
        return new Builder("/", null, false, DEFAULT_HANDSHAKE_TIMEOUT_MILLIS, 0L,
                           true, WebSocketCloseStatus.NORMAL_CLOSURE, true, WebSocketDecoderConfig.DEFAULT);
    }

    /** 服务端 WebSocket 协议配置构建器。 */
    public static final class Builder {
        private String websocketPath;
        private String subprotocols;
        private boolean checkStartsWith;
        private long handshakeTimeoutMillis;
        private long forceCloseTimeoutMillis;
        private boolean handleCloseFrames;
        private WebSocketCloseStatus sendCloseFrame;
        private boolean dropPongFrames;
        private WebSocketDecoderConfig decoderConfig;
        private WebSocketDecoderConfig.Builder decoderConfigBuilder;

        private Builder(WebSocketServerProtocolConfig serverConfig) {
            this(ObjectUtil.checkNotNull(serverConfig, "serverConfig").websocketPath(),
                 serverConfig.subprotocols(),
                 serverConfig.checkStartsWith(),
                 serverConfig.handshakeTimeoutMillis(),
                 serverConfig.forceCloseTimeoutMillis(),
                 serverConfig.handleCloseFrames(),
                 serverConfig.sendCloseFrame(),
                 serverConfig.dropPongFrames(),
                 serverConfig.decoderConfig()
            );
        }

        private Builder(String websocketPath,
                        String subprotocols,
                        boolean checkStartsWith,
                        long handshakeTimeoutMillis,
                        long forceCloseTimeoutMillis,
                        boolean handleCloseFrames,
                        WebSocketCloseStatus sendCloseFrame,
                        boolean dropPongFrames,
                        WebSocketDecoderConfig decoderConfig) {
            this.websocketPath = websocketPath;
            this.subprotocols = subprotocols;
            this.checkStartsWith = checkStartsWith;
            this.handshakeTimeoutMillis = handshakeTimeoutMillis;
            this.forceCloseTimeoutMillis = forceCloseTimeoutMillis;
            this.handleCloseFrames = handleCloseFrames;
            this.sendCloseFrame = sendCloseFrame;
            this.dropPongFrames = dropPongFrames;
            this.decoderConfig = decoderConfig;
        }

        /**
         * 设置处理 WebSocket 升级请求的 URI 路径。
         */
        public Builder websocketPath(String websocketPath) {
            this.websocketPath = websocketPath;
            return this;
        }

        /** 设置支持的子协议 CSV 列表。 */
        public Builder subprotocols(String subprotocols) {
            this.subprotocols = subprotocols;
            return this;
        }

        /**
         * {@code true} 时匹配以 {@link WebSocketServerProtocolConfig#websocketPath()} 为前缀的 URI；
         * {@code false} 时要求精确匹配（默认）。
         */
        public Builder checkStartsWith(boolean checkStartsWith) {
            this.checkStartsWith = checkStartsWith;
            return this;
        }

        /**
         * 设置握手超时（毫秒）；超时后触发 {@link ClientHandshakeStateEvent#HANDSHAKE_TIMEOUT}。
         */
        public Builder handshakeTimeoutMillis(long handshakeTimeoutMillis) {
            this.handshakeTimeoutMillis = handshakeTimeoutMillis;
            return this;
        }

        /** 若客户端在指定超时内未关闭连接，则强制关闭。 */
        public Builder forceCloseTimeoutMillis(long forceCloseTimeoutMillis) {
            this.forceCloseTimeoutMillis = forceCloseTimeoutMillis;
            return this;
        }

        /** {@code true} 时 Close 帧不转发，直接关闭通道。 */
        public Builder handleCloseFrames(boolean handleCloseFrames) {
            this.handleCloseFrames = handleCloseFrames;
            return this;
        }

        /** 设置未手动发送关闭帧时自动发送的 Close 状态；{@code null} 表示禁用。 */
        public Builder sendCloseFrame(WebSocketCloseStatus sendCloseFrame) {
            this.sendCloseFrame = sendCloseFrame;
            return this;
        }

        /** {@code true} 时不向下游转发 Pong 帧。 */
        public Builder dropPongFrames(boolean dropPongFrames) {
            this.dropPongFrames = dropPongFrames;
            return this;
        }

        /** 设置帧解码器配置。 */
        public Builder decoderConfig(WebSocketDecoderConfig decoderConfig) {
            this.decoderConfig = decoderConfig == null ? WebSocketDecoderConfig.DEFAULT : decoderConfig;
            this.decoderConfigBuilder = null;
            return this;
        }

        private WebSocketDecoderConfig.Builder decoderConfigBuilder() {
            if (decoderConfigBuilder == null) {
                decoderConfigBuilder = decoderConfig.toBuilder();
            }
            return decoderConfigBuilder;
        }

        /** 设置单帧最大载荷长度。 */
        public Builder maxFramePayloadLength(int maxFramePayloadLength) {
            decoderConfigBuilder().maxFramePayloadLength(maxFramePayloadLength);
            return this;
        }

        /** 是否要求客户端帧必须带掩码。 */
        public Builder expectMaskedFrames(boolean expectMaskedFrames) {
            decoderConfigBuilder().expectMaskedFrames(expectMaskedFrames);
            return this;
        }

        /** 是否允许掩码格式不符合标准的帧。 */
        public Builder allowMaskMismatch(boolean allowMaskMismatch) {
            decoderConfigBuilder().allowMaskMismatch(allowMaskMismatch);
            return this;
        }

        /** 是否允许使用帧 RSV 扩展位。 */
        public Builder allowExtensions(boolean allowExtensions) {
            decoderConfigBuilder().allowExtensions(allowExtensions);
            return this;
        }

        /** 协议违规时是否关闭连接。 */
        public Builder closeOnProtocolViolation(boolean closeOnProtocolViolation) {
            decoderConfigBuilder().closeOnProtocolViolation(closeOnProtocolViolation);
            return this;
        }

        /** 是否在管道中安装 UTF-8 文本帧校验器。 */
        public Builder withUTF8Validator(boolean withUTF8Validator) {
            decoderConfigBuilder().withUTF8Validator(withUTF8Validator);
            return this;
        }

        /** 构建不可变的服务端协议配置。 */
        public WebSocketServerProtocolConfig build() {
            return new WebSocketServerProtocolConfig(
                websocketPath,
                subprotocols,
                checkStartsWith,
                handshakeTimeoutMillis,
                forceCloseTimeoutMillis,
                handleCloseFrames,
                sendCloseFrame,
                dropPongFrames,
                decoderConfigBuilder == null ? decoderConfig : decoderConfigBuilder.build()
            );
        }
    }
}
