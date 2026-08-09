/*
 * Copyright 2013 The Netty Project
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
package io.netty.handler.codec.spdy;

import io.netty.util.internal.ObjectUtil;

/**
 * SPDY 流状态码及其语义描述，用于 {@link SpdyRstStreamFrame} 说明流被重置的原因。
 * <p>与 HTTP 状态码不同，这些是协议层错误/终止码，由 RST_STREAM 帧携带。
 */
public class SpdyStreamStatus implements Comparable<SpdyStreamStatus> {

    /** 1 — 协议错误：帧格式或语义不符合 SPDY 规范。 */
    public static final SpdyStreamStatus PROTOCOL_ERROR =
        new SpdyStreamStatus(1, "PROTOCOL_ERROR");

    /** 2 — 无效流：引用的 stream ID 不存在或不可用。 */
    public static final SpdyStreamStatus INVALID_STREAM =
        new SpdyStreamStatus(2, "INVALID_STREAM");

    /** 3 — 拒绝流：服务端拒绝创建该流（如超出并发限制）。 */
    public static final SpdyStreamStatus REFUSED_STREAM =
        new SpdyStreamStatus(3, "REFUSED_STREAM");

    /** 4 — 不支持的版本：对端 SPDY 版本不匹配。 */
    public static final SpdyStreamStatus UNSUPPORTED_VERSION =
        new SpdyStreamStatus(4, "UNSUPPORTED_VERSION");

    /** 5 — 取消：应用层主动取消，非错误场景。 */
    public static final SpdyStreamStatus CANCEL =
        new SpdyStreamStatus(5, "CANCEL");

    /** 6 — 内部错误：端点内部故障导致无法继续处理该流。 */
    public static final SpdyStreamStatus INTERNAL_ERROR =
        new SpdyStreamStatus(6, "INTERNAL_ERROR");

    /** 7 — 流控错误：违反 WINDOW_UPDATE 或初始窗口约定。 */
    public static final SpdyStreamStatus FLOW_CONTROL_ERROR =
        new SpdyStreamStatus(7, "FLOW_CONTROL_ERROR");

    /** 8 — 流 ID 已被占用：SYN_STREAM 使用了已激活的 stream ID。 */
    public static final SpdyStreamStatus STREAM_IN_USE =
        new SpdyStreamStatus(8, "STREAM_IN_USE");

    /** 9 — 流已关闭：对已 half-closed 或 fully-closed 的流发送数据。 */
    public static final SpdyStreamStatus STREAM_ALREADY_CLOSED =
        new SpdyStreamStatus(9, "STREAM_ALREADY_CLOSED");

    /** 10 — 凭证无效：客户端认证信息被拒绝。 */
    public static final SpdyStreamStatus INVALID_CREDENTIALS =
        new SpdyStreamStatus(10, "INVALID_CREDENTIALS");

    /** 11 — 帧过大：单帧 payload 超出 SETTINGS 允许的上限。 */
    public static final SpdyStreamStatus FRAME_TOO_LARGE =
        new SpdyStreamStatus(11, "FRAME_TOO_LARGE");

    /**
     * 按数值解析 {@link SpdyStreamStatus}。
     * 标准码（1–11）返回预置单例；未知码构造 {@code UNKNOWN (n)} 实例。
     */
    public static SpdyStreamStatus valueOf(int code) {
        if (code == 0) {
            throw new IllegalArgumentException(
                    "0 is not a valid status code for a RST_STREAM");
        }

        switch (code) {
        case 1:
            return PROTOCOL_ERROR;
        case 2:
            return INVALID_STREAM;
        case 3:
            return REFUSED_STREAM;
        case 4:
            return UNSUPPORTED_VERSION;
        case 5:
            return CANCEL;
        case 6:
            return INTERNAL_ERROR;
        case 7:
            return FLOW_CONTROL_ERROR;
        case 8:
            return STREAM_IN_USE;
        case 9:
            return STREAM_ALREADY_CLOSED;
        case 10:
            return INVALID_CREDENTIALS;
        case 11:
            return FRAME_TOO_LARGE;
        }

        return new SpdyStreamStatus(code, "UNKNOWN (" + code + ')');
    }

    private final int code;

    private final String statusPhrase;

    /**
     * 构造自定义状态码实例。
     * @param code 非零状态码（0 对 RST_STREAM 非法）
     * @param statusPhrase 可读描述，通常为大写枚举名
     */
    public SpdyStreamStatus(int code, String statusPhrase) {
        if (code == 0) {
            throw new IllegalArgumentException(
                    "0 is not a valid status code for a RST_STREAM");
        }

        this.statusPhrase = ObjectUtil.checkNotNull(statusPhrase, "statusPhrase");
        this.code = code;
    }

    /** 返回数值状态码。 */
    public int code() {
        return code;
    }

    /** 返回状态短语（如 {@code PROTOCOL_ERROR}）。 */
    public String statusPhrase() {
        return statusPhrase;
    }

    @Override
    public int hashCode() {
        return code();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SpdyStreamStatus)) {
            return false;
        }

        return code() == ((SpdyStreamStatus) o).code();
    }

    @Override
    public String toString() {
        return statusPhrase();
    }

    @Override
    public int compareTo(SpdyStreamStatus o) {
        return code() - o.code();
    }
}
