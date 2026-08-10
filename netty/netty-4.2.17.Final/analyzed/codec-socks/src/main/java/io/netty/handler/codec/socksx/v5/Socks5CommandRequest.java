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
 * A SOCKS5 request detail message, as defined in
 * <a href="https://tools.ietf.org/html/rfc1928#section-4">the section 4, RFC1928</a>.
 *
 * <p>SOCKS5 命令请求（CONNECT / BIND / UDP ASSOCIATE）的语义接口。
 * 字段对应 VER、CMD、RSV、ATYP、DST.ADDR、DST.PORT。</p>
 */
public interface Socks5CommandRequest extends Socks5Message {

    /**
     * Returns the type of this request.
     *
     * <p>命令类型：{@link Socks5CommandType#CONNECT} 等。</p>
     */
    Socks5CommandType type();

    /**
     * Returns the type of the {@code DST.ADDR} field of this request.
     *
     * <p>目标地址类型 ATYP。</p>
     */
    Socks5AddressType dstAddrType();

    /**
     * Returns the {@code DST.ADDR} field of this request.
     *
     * <p>目标主机名或 IP 字符串。</p>
     */
    String dstAddr();

    /**
     * Returns the {@code DST.PORT} field of this request.
     *
     * <p>目标端口，0–65535。</p>
     */
    int dstPort();
}
