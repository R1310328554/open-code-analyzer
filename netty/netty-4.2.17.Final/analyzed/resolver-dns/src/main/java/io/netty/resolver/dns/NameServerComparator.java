/*
 * Copyright 2018 The Netty Project
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
package io.netty.resolver.dns;

import io.netty.util.internal.ObjectUtil;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Comparator;
import java.util.List;

/**
 * 跟随 DNS 重定向时对 nameserver 排序的专用 {@link Comparator}。
 * <p>遵循 {@link Comparator} 语义，但 {@link InetSocketAddress#equals(Object)} 与
 * {@link #compare(InetSocketAddress, InetSocketAddress)} 返回值可能不一致；仅用于排序 {@link List}。</p>
 */
public final class NameServerComparator implements Comparator<InetSocketAddress>, Serializable {

    private static final long serialVersionUID = 8372151874317596185L;

    /** 优先选用的 IP 地址类型（如 IPv4/IPv6）。 */
    private final Class<? extends InetAddress> preferredAddressType;

    /**
     * @param preferredAddressType 优先地址族对应的 {@link InetAddress} 子类
     */
    public NameServerComparator(Class<? extends InetAddress> preferredAddressType) {
        this.preferredAddressType = ObjectUtil.checkNotNull(preferredAddressType, "preferredAddressType");
    }

    @Override
    public int compare(InetSocketAddress addr1, InetSocketAddress addr2) {
        if (addr1.equals(addr2)) {
            return 0;
        }
        if (!addr1.isUnresolved() && !addr2.isUnresolved()) {
            if (addr1.getAddress().getClass() == addr2.getAddress().getClass()) {
                return 0;
            }
            return preferredAddressType.isAssignableFrom(addr1.getAddress().getClass()) ? -1 : 1;
        }
        if (addr1.isUnresolved() && addr2.isUnresolved()) {
            return 0;
        }
        return addr1.isUnresolved() ? 1 : -1;
    }
}
