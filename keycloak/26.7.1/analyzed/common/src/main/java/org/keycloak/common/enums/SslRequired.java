/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.common.enums;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.keycloak.common.ClientConnection;

/**
 * Realm 是否强制 HTTPS 的策略枚举。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public enum SslRequired {

    /** 所有请求均要求 HTTPS。 */
    ALL,
    /** 仅外部（非本地）请求要求 HTTPS。 */
    EXTERNAL,
    /** 不要求 HTTPS。 */
    NONE;

    /** 根据客户端连接判断当前请求是否必须走 HTTPS。 */
    public boolean isRequired(ClientConnection connection) {
        return isRequired(connection.getRemoteAddr());
    }

    /** 根据远程主机名或 IP 判断当前请求是否必须走 HTTPS。 */
    public boolean isRequired(String host) {
        switch (this) {
            case ALL:
                return true;
            case NONE:
                return false;
            case EXTERNAL:
                // 注意：此处有时使用主机名，需要 DNS 解析；
                // 假设客户端侧解析结果一致——EXTERNAL 废弃后将移除此逻辑
                return !isLocal(host);
            default:
                return true;
        }
    }

    /** 判断地址是否为本地/内网（回环、站点本地、链路本地或 IPv6 ULA）。 */
    private boolean isLocal(String host) {
        if (host == null || host.isEmpty()) {
            return false; // InetAddress.getByName returns localhost for these
        }
        try {
            InetAddress inetAddress = InetAddress.getByName(host);
            return inetAddress.isLoopbackAddress() || inetAddress.isSiteLocalAddress() || inetAddress.isLinkLocalAddress() || isUniqueLocal(inetAddress);
        } catch (UnknownHostException e) {
            return false;
        }
    }

    /**
     * 判断地址是否属于 IPv6 唯一本地地址（ULA）范围 RFC4193。
     */
    private boolean isUniqueLocal(InetAddress address) {
        if (address instanceof java.net.Inet6Address) {
            byte[] addr = address.getAddress();
            // 检查是否在 fc00::/7 范围内
            return ((byte) (addr[0] & 0b11111110)) == (byte) 0xFC;
        }

        return false;
    }

}
