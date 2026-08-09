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
 * HTTP 消息接口，为 {@link HttpRequest} 与 {@link HttpResponse} 提供公共属性。
 * <p>
 * 包含协议版本与头集合；不含正文（正文由 {@link HttpContent} 承载）。
 *
 * @see HttpResponse
 * @see HttpRequest
 * @see HttpHeaders
 */
public interface HttpMessage extends HttpObject {

    /**
     * @deprecated Use {@link #protocolVersion()} instead.
     */
    @Deprecated
    HttpVersion getProtocolVersion();

    /** 返回本消息的 HTTP 协议版本。 */

    HttpVersion protocolVersion();

    /** 设置本消息的 HTTP 协议版本。 */

    HttpMessage setProtocolVersion(HttpVersion version);

    /** 返回本消息的头集合。 */

    HttpHeaders headers();
}
