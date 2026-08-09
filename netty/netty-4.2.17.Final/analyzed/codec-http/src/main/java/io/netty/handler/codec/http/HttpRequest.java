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
 * HTTP 请求接口，包含方法、URI 与协议版本。
 * <p>
 * 查询串请用 {@link QueryStringEncoder}/{@link QueryStringDecoder}；
 * Cookie 请用 {@code io.netty.handler.codec.http.cookie} 包下编解码器。
 *
 * @see HttpResponse
 * @see io.netty.handler.codec.http.cookie.ServerCookieDecoder
 * @see io.netty.handler.codec.http.cookie.ClientCookieDecoder
 * @see io.netty.handler.codec.http.cookie.ServerCookieEncoder
 * @see io.netty.handler.codec.http.cookie.ClientCookieEncoder
 */
public interface HttpRequest extends HttpMessage {

    /**
     * @deprecated Use {@link #method()} instead.
     */
    @Deprecated
    HttpMethod getMethod();

    /**
     * 返回本请求的 {@link HttpMethod}。
     * @return The {@link HttpMethod} of this {@link HttpRequest}
     */
    HttpMethod method();

    /**
     * 设置本请求的 {@link HttpMethod}。
     */
    HttpRequest setMethod(HttpMethod method);

    /**
     * @deprecated Use {@link #uri()} instead.
     */
    @Deprecated
    String getUri();

    /**
     * 返回请求 URI（或路径）。
     * @return The URI being requested
     */
    String uri();

    /**
     * 设置请求 URI（或路径）。
     */
    HttpRequest setUri(String uri);

    @Override
    HttpRequest setProtocolVersion(HttpVersion version);
}
