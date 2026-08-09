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
package io.netty.handler.codec.http.websocketx;

import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.ReferenceCounted;

/**
 * WebSocket 客户端握手过程中的异常。
 *
 *  <p><b>IMPORTANT</b>: 本异常不包含 {@link ReferenceCounted} 字段（如 {@link FullHttpResponse}），
 *  无需特殊引用计数处理。
 */
public final class WebSocketClientHandshakeException extends WebSocketHandshakeException {

    private static final long serialVersionUID = 1L;

    /** 校验失败时附带的 HTTP 响应（仅含协议版本、状态码与头，不含 body）。 */
    private final HttpResponse response;

    /** 以消息构造，无附带响应。 */
    public WebSocketClientHandshakeException(String message) {
        this(message, null);
    }

    /** 以消息与可选 HTTP 响应构造。 */
    public WebSocketClientHandshakeException(String message, HttpResponse httpResponse) {
        super(message);
        if (httpResponse != null) {
            response = new DefaultHttpResponse(httpResponse.protocolVersion(),
                                               httpResponse.status(), httpResponse.headers());
        } else {
            response = null;
        }
    }

    /**
     * 返回校验失败时的 {@link HttpResponse}；非校验阶段异常时返回 {@code null}。
     */
    public HttpResponse response() {
        return response;
    }
}
