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

import java.nio.CharBuffer;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

/**
 * 客户端与服务端 Cookie 解码器的抽象基类。
 * <p>
 * 提供 {@link #initCookie} 解析 name=value 并校验 RFC6265 字符范围。
 */
public abstract class CookieDecoder {

    private final InternalLogger logger = InternalLoggerFactory.getInstance(getClass());

    /** 是否严格校验 name/value 字符。 */
    private final boolean strict;

    /** @param strict 是否启用 RFC6265 严格校验 */
    protected CookieDecoder(boolean strict) {
        this.strict = strict;
    }

    /** 从头片段解析 Cookie name/value；无效时返回 {@code null} 并打 debug 日志。 */
    protected DefaultCookie initCookie(String header, int nameBegin, int nameEnd, int valueBegin, int valueEnd) {
        if (nameBegin == -1 || nameBegin == nameEnd) {
            logger.debug("Skipping cookie with null name");
            return null;
        }

        if (valueBegin == -1) {
            logger.debug("Skipping cookie with null value");
            return null;
        }

        CharSequence wrappedValue = CharBuffer.wrap(header, valueBegin, valueEnd);
        CharSequence unwrappedValue = unwrapValue(wrappedValue);
        if (unwrappedValue == null) {
            logger.debug("Skipping cookie because starting quotes are not properly balanced in '{}'",
                    wrappedValue);
            return null;
        }

        final String name = header.substring(nameBegin, nameEnd);

        int invalidOctetPos;
        if (strict && (invalidOctetPos = firstInvalidCookieNameOctet(name)) >= 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("Skipping cookie because name '{}' contains invalid char '{}'",
                        name, name.charAt(invalidOctetPos));
            }
            return null;
        }

        final boolean wrap = unwrappedValue.length() != valueEnd - valueBegin;

        if (strict && (invalidOctetPos = firstInvalidCookieValueOctet(unwrappedValue)) >= 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("Skipping cookie because value '{}' contains invalid char '{}'",
                        unwrappedValue, unwrappedValue.charAt(invalidOctetPos));
            }
            return null;
        }

        DefaultCookie cookie = new DefaultCookie(name, unwrappedValue.toString());
        cookie.setWrap(wrap);
        return cookie;
    }
}
