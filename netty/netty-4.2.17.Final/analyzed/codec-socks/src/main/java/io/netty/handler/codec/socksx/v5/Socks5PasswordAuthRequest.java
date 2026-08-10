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

/**
 * A SOCKS5 subnegotiation request for username-password authentication, as defined in
 * <a href="https://tools.ietf.org/html/rfc1929#section-2">the section 2, RFC1929</a>.
 *
 * <p>SOCKS5 用户名/密码子协商请求：在方法协商选定 {@link Socks5AuthMethod#PASSWORD} 后发送。
 * 报文格式为 VER(1) + ULEN + UNAME + PLEN + PASSWD，用户名与密码均为 US-ASCII 且长度不超过 255。</p>
 */
public interface Socks5PasswordAuthRequest extends Socks5Message {

    /**
     * Returns the username of this request.
     *
     * @return 用户名（US-ASCII，长度 ≤ 255）
     */
    String username();

    /**
     * Returns the password of this request.
     *
     * @return 密码（US-ASCII，长度 ≤ 255）
     */
    String password();
}
