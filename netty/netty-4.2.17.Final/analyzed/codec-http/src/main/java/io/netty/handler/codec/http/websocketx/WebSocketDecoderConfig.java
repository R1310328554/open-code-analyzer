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

import io.netty.util.internal.ObjectUtil;

/**
 * WebSocket 帧解码器配置：最大载荷、掩码期望、扩展位与 UTF-8 校验等。
 */
public final class WebSocketDecoderConfig {

    static final WebSocketDecoderConfig DEFAULT =
        new WebSocketDecoderConfig(65536, true, false, false, true, true);

    private final int maxFramePayloadLength;
    private final boolean expectMaskedFrames;
    private final boolean allowMaskMismatch;
    private final boolean allowExtensions;
    private final boolean closeOnProtocolViolation;
    private final boolean withUTF8Validator;

    /**
     * 私有构造；请使用 {@link #newBuilder()} 或 {@link Builder#build()}。
     *
     * @param maxFramePayloadLength
     *            Maximum length of a frame's payload. Setting this to an appropriate value for you application
     *            helps check for denial of services attacks.
     * @param expectMaskedFrames
     *            Web socket servers must set this to true processed incoming masked payload. Client implementations
     *            must set this to false.
     * @param allowMaskMismatch
     *            Allows to loosen the masking requirement on received frames. When this is set to false then also
     *            frames which are not masked properly according to the standard will still be accepted.
     * @param allowExtensions
     *            Flag to allow reserved extension bits to be used or not
     * @param closeOnProtocolViolation
     *            Flag to send close frame immediately on any protocol violation.ion.
     * @param withUTF8Validator
     *            Allows you to avoid adding of Utf8FrameValidator to the pipeline on the
     *            WebSocketServerProtocolHandler creation. This is useful (less overhead)
     *            when you use only BinaryWebSocketFrame within your web socket connection.
     */
    private WebSocketDecoderConfig(int maxFramePayloadLength, boolean expectMaskedFrames, boolean allowMaskMismatch,
                                  boolean allowExtensions, boolean closeOnProtocolViolation,
                                  boolean withUTF8Validator) {
        this.maxFramePayloadLength = maxFramePayloadLength;
        this.expectMaskedFrames = expectMaskedFrames;
        this.allowMaskMismatch = allowMaskMismatch;
        this.allowExtensions = allowExtensions;
        this.closeOnProtocolViolation = closeOnProtocolViolation;
        this.withUTF8Validator = withUTF8Validator;
    }

    /** 单帧载荷最大字节数，防 DoS 超长帧 */
    public int maxFramePayloadLength() {
        return maxFramePayloadLength;
    }

    /** 服务端应为 true（要求入站帧带 mask）；客户端为 false */
    public boolean expectMaskedFrames() {
        return expectMaskedFrames;
    }

    /** 为 true 时放宽 mask 校验（非标准兼容） */
    public boolean allowMaskMismatch() {
        return allowMaskMismatch;
    }

    /** 是否允许使用 RSV 扩展位 */
    public boolean allowExtensions() {
        return allowExtensions;
    }

    /** 协议违规时是否立即发送 Close 并断开 */
    public boolean closeOnProtocolViolation() {
        return closeOnProtocolViolation;
    }

    /** 是否在 pipeline 中自动加入 {@link Utf8FrameValidator} */
    public boolean withUTF8Validator() {
        return withUTF8Validator;
    }

    @Override
    public String toString() {
        return "WebSocketDecoderConfig" +
            " [maxFramePayloadLength=" + maxFramePayloadLength +
            ", expectMaskedFrames=" + expectMaskedFrames +
            ", allowMaskMismatch=" + allowMaskMismatch +
            ", allowExtensions=" + allowExtensions +
            ", closeOnProtocolViolation=" + closeOnProtocolViolation +
            ", withUTF8Validator=" + withUTF8Validator +
            "]";
    }

    /** 基于当前配置创建可变 {@link Builder} */
    public Builder toBuilder() {
        return new Builder(this);
    }

    /** 从 {@link #DEFAULT} 默认值开始构建 */
    public static Builder newBuilder() {
        return new Builder(DEFAULT);
    }

    public static final class Builder {
        private int maxFramePayloadLength;
        private boolean expectMaskedFrames;
        private boolean allowMaskMismatch;
        private boolean allowExtensions;
        private boolean closeOnProtocolViolation;
        private boolean withUTF8Validator;

        private Builder(WebSocketDecoderConfig decoderConfig) {
            ObjectUtil.checkNotNull(decoderConfig, "decoderConfig");
            maxFramePayloadLength = decoderConfig.maxFramePayloadLength();
            expectMaskedFrames = decoderConfig.expectMaskedFrames();
            allowMaskMismatch = decoderConfig.allowMaskMismatch();
            allowExtensions = decoderConfig.allowExtensions();
            closeOnProtocolViolation = decoderConfig.closeOnProtocolViolation();
            withUTF8Validator = decoderConfig.withUTF8Validator();
        }

        public Builder maxFramePayloadLength(int maxFramePayloadLength) {
            this.maxFramePayloadLength = maxFramePayloadLength;
            return this;
        }

        public Builder expectMaskedFrames(boolean expectMaskedFrames) {
            this.expectMaskedFrames = expectMaskedFrames;
            return this;
        }

        public Builder allowMaskMismatch(boolean allowMaskMismatch) {
            this.allowMaskMismatch = allowMaskMismatch;
            return this;
        }

        public Builder allowExtensions(boolean allowExtensions) {
            this.allowExtensions = allowExtensions;
            return this;
        }

        public Builder closeOnProtocolViolation(boolean closeOnProtocolViolation) {
            this.closeOnProtocolViolation = closeOnProtocolViolation;
            return this;
        }

        public Builder withUTF8Validator(boolean withUTF8Validator) {
            this.withUTF8Validator = withUTF8Validator;
            return this;
        }

        public WebSocketDecoderConfig build() {
            return new WebSocketDecoderConfig(
                    maxFramePayloadLength, expectMaskedFrames, allowMaskMismatch,
                    allowExtensions, closeOnProtocolViolation, withUTF8Validator);
        }
    }
}
