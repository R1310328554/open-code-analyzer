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

/**
 * RFC 7540 定义的全部 HTTP/2 错误码，用于 {@code RST_STREAM}、{@code GOAWAY} 及 {@link Http2Exception}。
 */
public enum Http2Error {
    /** 正常关闭，无错误。 */
    NO_ERROR(0x0),
    /** 协议语义违规。 */
    PROTOCOL_ERROR(0x1),
    /** 内部实现错误。 */
    INTERNAL_ERROR(0x2),
    /** 流控窗口违规。 */
    FLOW_CONTROL_ERROR(0x3),
    /** SETTINGS 帧未在时限内 ACK。 */
    SETTINGS_TIMEOUT(0x4),
    /** 对已关闭流进行操作。 */
    STREAM_CLOSED(0x5),
    /** 帧尺寸超出允许范围。 */
    FRAME_SIZE_ERROR(0x6),
    /** 对端拒绝创建流（如并发流超限）。 */
    REFUSED_STREAM(0x7),
    /** 应用层取消，非协议错误。 */
    CANCEL(0x8),
    /** HPACK 解压失败。 */
    COMPRESSION_ERROR(0x9),
    /** CONNECT 方法隧道建立失败。 */
    CONNECT_ERROR(0xA),
    /** 对端发送过多空帧等，建议降速（RFC 7540 §9.1.1）。 */
    ENHANCE_YOUR_CALM(0xB),
    /** TLS 协商的安全级别不足。 */
    INADEQUATE_SECURITY(0xC),
    /** 必须使用 HTTP/1.1。 */
    HTTP_1_1_REQUIRED(0xD);

    private final long code;
    /** 按 wire 数值 O(1) 反查枚举。 */
    private static final Http2Error[] INT_TO_ENUM_MAP;
    static {
        Http2Error[] errors = values();
        Http2Error[] map = new Http2Error[errors.length];
        for (Http2Error error : errors) {
            map[(int) error.code()] = error;
        }
        INT_TO_ENUM_MAP = map;
    }

    Http2Error(long code) {
        this.code = code;
    }

    /**
     * 返回该错误在 HTTP/2 帧中使用的 32 位错误码。
     */
    public long code() {
        return code;
    }

    /**
     * 将 wire 上的数值映射为枚举；未知码返回 {@code null}。
     */
    public static Http2Error valueOf(long value) {
        return value >= INT_TO_ENUM_MAP.length || value < 0 ? null : INT_TO_ENUM_MAP[(int) value];
    }
}
