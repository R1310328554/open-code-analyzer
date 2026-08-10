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
package io.netty.handler.codec.socksx.v5;

import io.netty.handler.codec.DecoderResult;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

/**
 * The default {@link Socks5PasswordAuthRequest}.
 *
 * <p>RFC 1929 用户名/密码子协商请求：VER(1) + ULEN + UNAME + PLEN + PASSWD。
 * 用户名与密码长度均不得超过 255 字节；{@link #toString()} 隐藏密码明文。</p>
 */
public class DefaultSocks5PasswordAuthRequest extends AbstractSocks5Message implements Socks5PasswordAuthRequest {

    private final String username;
    private final String password;

    public DefaultSocks5PasswordAuthRequest(String username, String password) {
        ObjectUtil.checkNotNull(username, "username");
        ObjectUtil.checkNotNull(password, "password");

        // RFC 1929：单字节长度前缀，故字符串 UTF-8 长度上限 255
        if (username.length() > 255) {
            throw new IllegalArgumentException("username: **** (expected: less than 256 chars)");
        }
        if (password.length() > 255) {
            throw new IllegalArgumentException("password: **** (expected: less than 256 chars)");
        }

        this.username = username;
        this.password = password;
    }

    @Override
    public String username() {
        return username;
    }

    @Override
    public String password() {
        return password;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(StringUtil.simpleClassName(this));

        DecoderResult decoderResult = decoderResult();
        if (!decoderResult.isSuccess()) {
            buf.append("(decoderResult: ");
            buf.append(decoderResult);
            buf.append(", username: ");
        } else {
            buf.append("(username: ");
        }
        buf.append(username());
        buf.append(", password: ****)");

        return buf.toString();
    }
}
