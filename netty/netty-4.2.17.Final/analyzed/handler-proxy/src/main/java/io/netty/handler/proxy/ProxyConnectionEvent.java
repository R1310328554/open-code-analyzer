/*
 * Copyright 2014 The Netty Project
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

package io.netty.handler.proxy;

import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

import java.net.SocketAddress;

/**
 * 代理隧道建立成功后通过 userEvent 下发的连接事件。
 * <p>包含协议名、认证方式、代理地址与目标地址，便于日志与监控。</p>
 */
public final class ProxyConnectionEvent {

    private final String protocol;
    private final String authScheme;
    private final SocketAddress proxyAddress;
    private final SocketAddress destinationAddress;
    /** 缓存的 toString 结果 */
    private String strVal;

    /**
     * 创建表示已成功连到目标地址的事件。
     */
    public ProxyConnectionEvent(
            String protocol, String authScheme, SocketAddress proxyAddress, SocketAddress destinationAddress) {
        this.protocol = ObjectUtil.checkNotNull(protocol, "protocol");
        this.authScheme = ObjectUtil.checkNotNull(authScheme, "authScheme");
        this.proxyAddress = ObjectUtil.checkNotNull(proxyAddress, "proxyAddress");
        this.destinationAddress = ObjectUtil.checkNotNull(destinationAddress, "destinationAddress");
    }

    /**
     * 返回代理协议名称（如 {@code http}、{@code socks5}）。
     */
    public String protocol() {
        return protocol;
    }

    /**
     * 返回认证方案名称（如 {@code basic}、{@code none}）。
     */
    public String authScheme() {
        return authScheme;
    }

    /**
     * 返回代理服务器地址。
     */
    @SuppressWarnings("unchecked")
    public <T extends SocketAddress> T proxyAddress() {
        return (T) proxyAddress;
    }

    /**
     * 返回目标（远端）地址。
     */
    @SuppressWarnings("unchecked")
    public <T extends SocketAddress> T destinationAddress() {
        return (T) destinationAddress;
    }

    @Override
    public String toString() {
        if (strVal != null) {
            return strVal;
        }

        StringBuilder buf = new StringBuilder(128)
            .append(StringUtil.simpleClassName(this))
            .append('(')
            .append(protocol)
            .append(", ")
            .append(authScheme)
            .append(", ")
            .append(proxyAddress)
            .append(" => ")
            .append(destinationAddress)
            .append(')');

        return strVal = buf.toString();
    }
}
