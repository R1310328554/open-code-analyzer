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

/**
 * Different <a href="https://datatracker.ietf.org/doc/html/rfc9114#name-http-3-error-codes">HTTP3 error codes</a>.
 * <p>数值通过 {@link #code()} 写入 QUIC CONNECTION_CLOSE / RESET_STREAM 帧的应用错误码字段。
 */
public enum Http3ErrorCode {

    /**
     * Datagram or Capsule Protocol parse error
     * <a href="https://www.rfc-editor.org/rfc/rfc9297.html#name-http-3-error-code">rfc9297</a>
     * registered in IANA http3
     * <a href="https://www.iana.org/assignments/http3-parameters/http3-parameters.xhtml#http3-parameters-error-codes"
     * >
     *     IANA Http3 Error Codes</a>
     * <p>DATAGRAM 或 Capsule 协议解析失败。
     */
    H3_DATAGRAM_ERROR(0x33),

    /**
     *  No error. This is used when the connection or stream needs to be closed, but there is no error to signal.
     * <p>正常关闭，无具体错误语义。
     */
    H3_NO_ERROR(0x100),

    /**
     * Peer violated protocol requirements in a way that does not match a more specific error code,
     * or endpoint declines to use the more specific error code.
     * <p>通用协议违规，无更精确的错误码可用时使用。
     */
    H3_GENERAL_PROTOCOL_ERROR(0x101),

    /**
     * An internal error has occurred in the HTTP stack.
     * <p>HTTP 栈内部错误（如对端不应感知的实现缺陷）。
     */
    H3_INTERNAL_ERROR(0x102),

    /**
     * The endpoint detected that its peer created a stream that it will not accept.
     * <p>对端创建了本端拒绝接受的流类型。
     */
    H3_STREAM_CREATION_ERROR(0x103),

    /**
     * A stream required by the HTTP/3 connection was closed or reset.
     * <p>控制流或 QPACK 编解码流等关键单向流异常关闭。
     */
    H3_CLOSED_CRITICAL_STREAM(0x104),

    /**
     * A frame was received that was not permitted in the current state or on the current stream.
     * <p>当前状态或当前流上不允许出现的帧类型。
     */
    H3_FRAME_UNEXPECTED(0x105),

    /**
     * A frame that fails to satisfy layout requirements or with an invalid size was received.
     * <p>帧格式或长度不符合规范。
     */
    H3_FRAME_ERROR(0x106),

    /**
     * The endpoint detected that its peer is exhibiting a behavior that might be generating excessive load.
     * <p>对端行为可能导致过载（如超大帧长度）。
     */
    H3_EXCESSIVE_LOAD(0x107),

    /**
     * A Stream ID or Push ID was used incorrectly, such as exceeding a limit, reducing a limit, or being reused.
     * <p>流 ID 或 Push ID 使用不当（越界、回退上限等）。
     */
    H3_ID_ERROR(0x108),

    /**
     * An endpoint detected an error in the payload of a SETTINGS frame.
     * <p>SETTINGS 帧内容非法（重复键、保留键等）。
     */
    H3_SETTINGS_ERROR(0x109),

    /**
     * No SETTINGS frame was received at the beginning of the control stream.
     * <p>控制流首帧不是 SETTINGS。
     */
    H3_MISSING_SETTINGS(0x10a),

    /**
     * A server rejected a request without performing any application processing.
     * <p>服务端未处理即拒绝请求。
     */
    H3_REQUEST_REJECTED(0x10b),

    /**
     * The request or its response (including pushed response) is cancelled.
     * <p>请求或推送响应已被取消。
     */
    H3_REQUEST_CANCELLED(0x10c),

    /**
     * The client's stream terminated without containing a fully-formed request.
     * <p>客户端流在完整请求发出前即结束。
     */
    H3_REQUEST_INCOMPLETE(0x10d),

    /**
     * An HTTP message was malformed and cannot be processed.
     * <p>HTTP 消息格式错误（头字段、伪头等）。
     */
    H3_MESSAGE_ERROR(0x10e),

    /**
     * The TCP connection established in response to a CONNECT request was reset or abnormally closed.
     * <p>CONNECT 隧道背后的 TCP 连接异常。
     */
    H3_CONNECT_ERROR(0x10f),

    /**
     * The requested operation cannot be served over HTTP/3. The peer should retry over HTTP/1.1.
     * <p>操作无法通过 HTTP/3 完成，建议回退 HTTP/1.1。
     */
    H3_VERSION_FALLBACK(0x110),

    /**
     * The decoder failed to interpret an encoded field section and is not able to continue decoding that field section.
     * <p>QPACK 无法解压当前头部块。
     */
    QPACK_DECOMPRESSION_FAILED(0x200),

    /**
     * The decoder failed to interpret an encoder instruction received on the encoder stream.
     * <p>QPACK 解码端无法解析 encoder 流上的指令。
     */
    QPACK_ENCODER_STREAM_ERROR(0x201),

    /**
     * The encoder failed to interpret a decoder instruction received on the decoder stream.
     * <p>QPACK 编码端无法解析 decoder 流上的指令。
     */
    QPACK_DECODER_STREAM_ERROR(0x202);

    final int code;

    Http3ErrorCode(int code) {
        this.code = code;
    }

    /** 返回写入 QUIC 关闭帧的 62 位应用错误码。 */
    public int code() {
        return code;
    }
}
