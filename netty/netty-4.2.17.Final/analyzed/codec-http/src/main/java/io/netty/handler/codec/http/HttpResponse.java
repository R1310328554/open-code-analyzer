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

/**
 * HTTP 响应消息接口，继承 {@link HttpMessage} 并携带 {@link HttpResponseStatus}。
 * <p>
 * 与 Servlet API 不同，Cookie 解析/编码由独立的
 * {@link io.netty.handler.codec.http.cookie.ServerCookieDecoder}、
 * {@link io.netty.handler.codec.http.cookie.ClientCookieDecoder}、
 * {@link io.netty.handler.codec.http.cookie.ServerCookieEncoder}、
 * {@link io.netty.handler.codec.http.cookie.ClientCookieEncoder} 提供。
 *
 * @see HttpRequest
 * @see io.netty.handler.codec.http.cookie.ServerCookieDecoder
 * @see io.netty.handler.codec.http.cookie.ClientCookieDecoder
 * @see io.netty.handler.codec.http.cookie.ServerCookieEncoder
 * @see io.netty.handler.codec.http.cookie.ClientCookieEncoder
 */
public interface HttpResponse extends HttpMessage {

    /**
     * @deprecated Use {@link #status()} instead.
     */
    @Deprecated
    HttpResponseStatus getStatus();

    /**
     * 返回本响应的状态码与原因短语。
     *
     * @return The {@link HttpResponseStatus} of this {@link HttpResponse}
     */
    HttpResponseStatus status();

    /**
     * 设置响应状态码。
     */
    HttpResponse setStatus(HttpResponseStatus status);

    @Override
    HttpResponse setProtocolVersion(HttpVersion version);
}
