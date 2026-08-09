/*
 * Copyright 2024 The Netty Project
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
package io.netty.handler.codec.quic;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * RFC 9000 定义的 QUIC 传输层错误码。
 * 参见 <a href="https://www.rfc-editor.org/rfc/rfc9000.html#name-transport-error-codes">
 *     RFC9000 20.1. Transport Error Codes</a>。
 */
public final class QuicTransportError {

    /** 无错误情况下的正常关闭（CONNECTION_CLOSE 使用）。 */
    public static final QuicTransportError NO_ERROR =
            new QuicTransportError(0x0, "NO_ERROR");

    /** 端点内部错误，无法继续连接。 */
    public static final QuicTransportError INTERNAL_ERROR =
            new QuicTransportError(0x1, "INTERNAL_ERROR");

    /** 服务端拒绝接受新连接。 */
    public static final QuicTransportError CONNECTION_REFUSED =
            new QuicTransportError(0x2, "CONNECTION_REFUSED");

    /** 接收数据超出通告的流量控制上限。 */
    public static final QuicTransportError FLOW_CONTROL_ERROR =
            new QuicTransportError(0x3, "FLOW_CONTROL_ERROR");

    /** 收到超出已通告流数量上限的流标识符帧。 */
    public static final QuicTransportError STREAM_LIMIT_ERROR =
            new QuicTransportError(0x4, "STREAM_LIMIT_ERROR");

    /** 在不允许当前帧的流状态下收到该帧。 */
    public static final QuicTransportError STREAM_STATE_ERROR =
            new QuicTransportError(0x5, "STREAM_STATE_ERROR");

    /** 流最终大小不一致：超出已确立 final size、final size 小于已收数据，或与已确立值不同。 */
    public static final QuicTransportError FINAL_SIZE_ERROR =
            new QuicTransportError(0x6, "FINAL_SIZE_ERROR");

    /** 帧格式错误，例如未知帧类型或 ACK 范围超出报文剩余空间。 */
    public static final QuicTransportError FRAME_ENCODING_ERROR =
            new QuicTransportError(0x7, "FRAME_ENCODING_ERROR");

    /** 传输参数非法：格式错误、取值无效、缺少必填项、含禁止项等。 */
    public static final QuicTransportError TRANSPORT_PARAMETER_ERROR =
            new QuicTransportError(0x8, "TRANSPORT_PARAMETER_ERROR");

    /** 对端提供的连接 ID 数量超过 active_connection_id_limit。 */
    public static final QuicTransportError CONNECTION_ID_LIMIT_ERROR =
            new QuicTransportError(0x9, "CONNECTION_ID_LIMIT_ERROR");

    /** 协议违规，且无法归入更具体的错误码。 */
    public static final QuicTransportError PROTOCOL_VIOLATION =
            new QuicTransportError(0xa, "PROTOCOL_VIOLATION");

    /** 服务端收到含无效 Token 字段的客户端 Initial 报文。 */
    public static final QuicTransportError INVALID_TOKEN =
            new QuicTransportError(0xb, "INVALID_TOKEN");

    /** 应用或应用层协议主动关闭连接。 */
    public static final QuicTransportError APPLICATION_ERROR =
            new QuicTransportError(0xc, "APPLICATION_ERROR");

    /** CRYPTO 帧数据超出端点可缓冲容量。 */
    public static final QuicTransportError CRYPTO_BUFFER_EXCEEDED =
            new QuicTransportError(0xd, "CRYPTO_BUFFER_EXCEEDED");

    /** 密钥更新过程中检测到错误。 */
    public static final QuicTransportError KEY_UPDATE_ERROR =
            new QuicTransportError(0xe, "KEY_UPDATE_ERROR");

    /** 连接所用 AEAD 算法达到机密性或完整性使用上限。 */
    public static final QuicTransportError AEAD_LIMIT_REACHED =
            new QuicTransportError(0xf, "AEAD_LIMIT_REACHED");

    /** 网络路径无法支持 QUIC（常见于 MTU 过小）。 */
    public static final QuicTransportError NO_VIABLE_PATH =
            new QuicTransportError(0x10, "NO_VIABLE_PATH");

    private static final QuicTransportError[] INT_TO_ENUM_MAP;
    static {
        List<QuicTransportError> errorList = new ArrayList<>();
        errorList.add(NO_ERROR);
        errorList.add(INTERNAL_ERROR);
        errorList.add(CONNECTION_REFUSED);
        errorList.add(FLOW_CONTROL_ERROR);
        errorList.add(STREAM_LIMIT_ERROR);
        errorList.add(STREAM_STATE_ERROR);
        errorList.add(FINAL_SIZE_ERROR);
        errorList.add(FRAME_ENCODING_ERROR);
        errorList.add(TRANSPORT_PARAMETER_ERROR);
        errorList.add(CONNECTION_ID_LIMIT_ERROR);
        errorList.add(PROTOCOL_VIOLATION);
        errorList.add(INVALID_TOKEN);
        errorList.add(APPLICATION_ERROR);
        errorList.add(CRYPTO_BUFFER_EXCEEDED);
        errorList.add(KEY_UPDATE_ERROR);
        errorList.add(AEAD_LIMIT_REACHED);
        errorList.add(NO_VIABLE_PATH);

        // 加密握手错误码占用 0x0100–0x01ff 范围
        // 参见 https://www.rfc-editor.org/rfc/rfc9000.html#name-transport-error-codes:
        // The cryptographic handshake failed. A range of 256 values is reserved for carrying error codes specific to
        // the cryptographic handshake that is used. Codes for errors occurring when TLS is used for the cryptographic
        // handshake are described in Section 4.8 of [QUIC-TLS].
        for (int i = 0x0100; i <= 0x01ff; i++) {
            errorList.add(new QuicTransportError(i, "CRYPTO_ERROR"));
        }
        INT_TO_ENUM_MAP = errorList.toArray(new QuicTransportError[0]);
    }
    private final long code;
    private final String name;

    private QuicTransportError(long code, String name) {
        this.code = code;
        this.name = name;
    }

    /** 若错误码属于 {@code CRYPTO_ERROR} 范围（0x0100–0x01ff）则返回 {@code true}。 */
    public boolean isCryptoError() {
        return code >= 0x0100 && code <= 0x01ff;
    }

    /**
     * 返回 RFC 9000 定义的错误名称。
     *
     * @return name
     */
    public String name() {
        return name;
    }

    /** 返回线上使用的传输错误码数值。 */
    public long code() {
        return code;
    }

    /** 按数值解析传输错误码；未知值抛出 {@link IllegalArgumentException}。 */
    public static QuicTransportError valueOf(long value) {
        if (value > 17) {
            value -= 0x0100;
        }

        if (value < 0 || value >= INT_TO_ENUM_MAP.length) {
            throw new IllegalArgumentException("Unknown error code value: " + value);
        }
        return INT_TO_ENUM_MAP[(int) value];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        QuicTransportError quicError = (QuicTransportError) o;
        return code == quicError.code;
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return "QuicTransportError{" +
                "code=" + code +
                ", name='" + name + '\'' +
                '}';
    }
}
