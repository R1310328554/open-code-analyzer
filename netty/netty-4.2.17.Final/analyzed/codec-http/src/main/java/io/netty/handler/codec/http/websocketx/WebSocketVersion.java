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
package io.netty.handler.codec.http.websocketx;

import io.netty.util.AsciiString;
import io.netty.util.internal.StringUtil;

/**
 * WebSocket 协议版本枚举。
 * <p>每个枚举值对应一种线协议版本；同一线协议可能被多个规范草案共用。
 */
public enum WebSocketVersion {
    /** 未知或未识别的版本。 */
    UNKNOWN(AsciiString.cached(StringUtil.EMPTY_STRING)),

    /**
     * HyBi-00 草案（<a href= "https://tools.ietf.org/html/draft-ietf-hybi-thewebsocketprotocol-00"
     * >draft-ietf-hybi-thewebsocketprotocol- 00</a>）。
     */
    V00(AsciiString.cached("0")),

    /**
     * HyBi-07 草案（<a href= "https://tools.ietf.org/html/draft-ietf-hybi-thewebsocketprotocol-07"
     * >draft-ietf-hybi-thewebsocketprotocol- 07</a>）。
     */
    V07(AsciiString.cached("7")),

    /**
     * HyBi-10 草案（<a href= "https://tools.ietf.org/html/draft-ietf-hybi-thewebsocketprotocol-10"
     * >draft-ietf-hybi-thewebsocketprotocol- 10</a>）。
     */
    V08(AsciiString.cached("8")),

    /**
     * RFC 6455（<a href="https://tools.ietf.org/html/rfc6455 ">RFC 6455</a>），
     * 源自 <a href=
     * "https://tools.ietf.org/html/draft-ietf-hybi-thewebsocketprotocol-17" >draft-ietf-hybi-thewebsocketprotocol-
     * 17</a>。
     */
    V13(AsciiString.cached("13"));

    private final AsciiString headerValue;

    WebSocketVersion(AsciiString headerValue) {
        this.headerValue = headerValue;
    }
    /**
     * @return HTTP 头 {@code Sec-WebSocket-Version} 对应的字符串值。
     */
    public String toHttpHeaderValue() {
        return toAsciiString().toString();
    }

    AsciiString toAsciiString() {
        if (this == UNKNOWN) {
            // Let's special case this to preserve behaviour
            throw new IllegalStateException("Unknown web socket version: " + this);
        }
        return headerValue;
    }
}
