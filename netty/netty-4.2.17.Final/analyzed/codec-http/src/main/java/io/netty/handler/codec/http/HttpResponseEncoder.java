/*
 * Copyright 2012 The Netty Project
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
package io.netty.handler.codec.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

import static io.netty.handler.codec.http.HttpConstants.*;

/**
 * 将 {@link HttpResponse} 或 {@link HttpContent} 编码为 {@link ByteBuf}（服务端出站）。
 * <p>
 * 写入 {@code version SP status CRLF} 起始行，并按 RFC 7230 处理无正文响应的头清理。
 */
public class HttpResponseEncoder extends HttpObjectEncoder<HttpResponse> {

    @Override
    public boolean acceptOutboundMessage(Object msg) throws Exception {
        // 优先精确匹配 Default 实现类，避免 instanceof 链式检查开销；
        // 同时排除被错误实现 HttpRequest 的子类。
        // JDK type checks vs non-implemented interfaces costs O(N), where
        // N is the number of interfaces already implemented by the concrete type that's being tested.
        // !(msg instanceof HttpRequest) is supposed to always be true (and meaning that msg isn't a HttpRequest),
        // but sadly was part of the original behaviour of this method and cannot be removed.
        // We place here exact checks vs DefaultHttpResponse and DefaultFullHttpResponse because bad users can
        // extends such types and make them to implement HttpRequest (non-sense, but still possible).
        final Class<?> msgClass = msg.getClass();
        if (msgClass == DefaultFullHttpResponse.class || msgClass == DefaultHttpResponse.class) {
            return true;
        }
        return super.acceptOutboundMessage(msg) && !(msg instanceof HttpRequest);
    }

    @Override
    protected void encodeInitialLine(ByteBuf buf, HttpResponse response) throws Exception {
        response.protocolVersion().encode(buf);
        buf.writeByte(SP);
        response.status().encode(buf);
        ByteBufUtil.writeShortBE(buf, CRLF_SHORT);
    }

    @Override
    protected void sanitizeHeadersBeforeEncode(HttpResponse msg, boolean isAlwaysEmpty) {
        if (isAlwaysEmpty) {
            HttpResponseStatus status = msg.status();
            if (status.codeClass() == HttpStatusClass.INFORMATIONAL ||
                    status.code() == HttpResponseStatus.NO_CONTENT.code()) {

                // 1xx/204 等无正文响应：移除 Content-Length（RFC 7230 §3.3.2）
                msg.headers().remove(HttpHeaderNames.CONTENT_LENGTH);

                // Stripping Transfer-Encoding:
                // See https://tools.ietf.org/html/rfc7230#section-3.3.1
                msg.headers().remove(HttpHeaderNames.TRANSFER_ENCODING);
            } else if (status.code() == HttpResponseStatus.RESET_CONTENT.code()) {

                // Stripping Transfer-Encoding:
                msg.headers().remove(HttpHeaderNames.TRANSFER_ENCODING);

                // Set Content-Length: 0
                // https://httpstatuses.com/205
                msg.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, 0);
            }
        }
    }

    @Override
    protected boolean isContentAlwaysEmpty(HttpResponse msg) {
        // 按 RFC 7230 §3.3.3 判断响应是否必定无消息体（1xx/204/304/205 等）
        HttpResponseStatus status = msg.status();

        if (status.codeClass() == HttpStatusClass.INFORMATIONAL) {

            if (status.code() == HttpResponseStatus.SWITCHING_PROTOCOLS.code()) {
                // WebSocket 00 版 101 响应可能含正文，需检查 SEC-WEBSOCKET-VERSION
                // Fortunally this version should not really be used in the wild very often.
                // See https://tools.ietf.org/html/draft-ietf-hybi-thewebsocketprotocol-00#section-1.2
                return msg.headers().contains(HttpHeaderNames.SEC_WEBSOCKET_VERSION);
            }
            return true;
        }
        return status.code() == HttpResponseStatus.NO_CONTENT.code() ||
                status.code() == HttpResponseStatus.NOT_MODIFIED.code() ||
                status.code() == HttpResponseStatus.RESET_CONTENT.code();
    }
}
