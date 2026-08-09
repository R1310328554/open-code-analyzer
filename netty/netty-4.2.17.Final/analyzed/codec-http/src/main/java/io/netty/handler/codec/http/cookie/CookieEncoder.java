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
package io.netty.handler.codec.http.cookie;

import static io.netty.handler.codec.http.cookie.CookieUtil.firstInvalidCookieNameOctet;
import static io.netty.handler.codec.http.cookie.CookieUtil.firstInvalidCookieValueOctet;
import static io.netty.handler.codec.http.cookie.CookieUtil.unwrapValue;

/**
 * 客户端与服务端 Cookie 编码器的抽象基类。
 * <p>
 * 严格模式下通过 {@link #validateCookie} 校验 name/value。
 */
public abstract class CookieEncoder {

    /** 是否严格校验 name/value 字符。 */
    protected final boolean strict;

    /** @param strict 是否启用 RFC6265 严格校验 */
    protected CookieEncoder(boolean strict) {
        this.strict = strict;
    }

    /** 严格模式下校验 Cookie name/value 字符与引号平衡。 */
    protected void validateCookie(String name, String value) {
        if (strict) {
            int pos;

            if ((pos = firstInvalidCookieNameOctet(name)) >= 0) {
                throw new IllegalArgumentException("Cookie name contains an invalid char: " + name.charAt(pos));
            }

            CharSequence unwrappedValue = unwrapValue(value);
            if (unwrappedValue == null) {
                throw new IllegalArgumentException("Cookie value wrapping quotes are not balanced: " + value);
            }

            if ((pos = firstInvalidCookieValueOctet(unwrappedValue)) >= 0) {
                throw new IllegalArgumentException("Cookie value contains an invalid char: " +
                                                   unwrappedValue.charAt(pos));
            }
        }
    }
}
