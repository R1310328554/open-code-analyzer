/*
 * Copyright 2017 The Netty Project
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

import io.netty.util.AsciiString;

/**
 * RFC 6455 WebSocket URI 方案常量：{@link #WS}（明文）与 {@link #WSS}（TLS）。
 * <p>各含默认端口与 {@link AsciiString} 方案名。
 */
public final class WebSocketScheme {
    /** 非加密 WebSocket，默认端口 80，方案名 {@code ws} */

    public static final WebSocketScheme WS = new WebSocketScheme(80, "ws");

    /** TLS WebSocket，默认端口 443，方案名 {@code wss} */

    public static final WebSocketScheme WSS = new WebSocketScheme(443, "wss");

    private final int port;
    private final AsciiString name;

    private WebSocketScheme(int port, String name) {
        this.port = port;
        this.name = AsciiString.cached(name);
    }

    /** 返回方案名字符串（如 {@code ws}） */
    public AsciiString name() {
        return name;
    }

    /** 返回该方案默认端口 */
    public int port() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof WebSocketScheme)) {
            return false;
        }
        WebSocketScheme other = (WebSocketScheme) o;
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
