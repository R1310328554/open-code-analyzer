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
 * A response to a SOCKS5 request detail message, as defined in
 * <a href="https://tools.ietf.org/html/rfc1928#section-6">the section 6, RFC1928</a>.
 *
 * <p>SOCKS5 命令应答接口：REP 状态、绑定地址 BND.ADDR/BND.PORT（代理侧 relay 端点）。</p>
 */
public interface Socks5CommandResponse extends Socks5Message {

    /**
     * Returns the status of this response.
     *
     * <p>应答状态码 {@link Socks5CommandStatus}，0 表示成功。</p>
     */
    Socks5CommandStatus status();

    /**
     * Returns the address type of the {@code BND.ADDR} field of this response.
     *
     * <p>绑定地址类型 ATYP。</p>
     */
    Socks5AddressType bndAddrType();

    /**
     * Returns the {@code BND.ADDR} field of this response.
     *
     * <p>代理用于 relay 的地址；CONNECT 成功时常为代理出口地址。</p>
     */
    String bndAddr();

    /**
     * Returns the {@code BND.PORT} field of this response.
     *
     * <p>绑定端口。</p>
     */
    int bndPort();
}
