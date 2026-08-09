/*
 * Copyright 2015 The Netty Project
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

import io.netty.util.AsciiString;

/**
 * HTTP URI 方案常量（RFC 7230），封装方案名与默认端口。
 * <p>
 * {@link #HTTP} 对应 80 端口，{@link #HTTPS} 对应 443 端口。
 */
public final class HttpScheme {

    /** 明文 HTTP 方案（{@code http}，默认端口 80） */

    public static final HttpScheme HTTP = new HttpScheme(80, "http");

    /** 安全 HTTP 方案（{@code https}，默认端口 443） */

    public static final HttpScheme HTTPS = new HttpScheme(443, "https");

    private final int port;
    private final AsciiString name;

    private HttpScheme(int port, String name) {
        this.port = port;
        this.name = AsciiString.cached(name);
    }

    /** 返回方案名（{@link AsciiString}，如 {@code http}） */
    public AsciiString name() {
        return name;
    }

    /** 返回该方案的 IANA 默认端口 */
    public int port() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HttpScheme)) {
            return false;
        }
        HttpScheme other = (HttpScheme) o;
        return other.port() == port && other.name().equals(name);
    }

    @Override
    public int hashCode() {
        return port * 31 + name.hashCode();
    }

    @Override
    public String toString() {
        return name.toString();
    }
}
