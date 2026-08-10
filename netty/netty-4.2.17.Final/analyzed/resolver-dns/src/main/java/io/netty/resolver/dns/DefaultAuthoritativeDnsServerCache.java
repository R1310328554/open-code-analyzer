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

import io.netty.channel.EventLoop;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import static io.netty.util.internal.ObjectUtil.checkNotNull;
import static io.netty.util.internal.ObjectUtil.checkPositive;
import static io.netty.util.internal.ObjectUtil.checkPositiveOrZero;

/**
 * {@link AuthoritativeDnsServerCache} 的默认实现，底层使用 {@link ConcurrentMap} 与 {@link Cache} 管理 TTL。
 */
public class DefaultAuthoritativeDnsServerCache implements AuthoritativeDnsServerCache {

    /** 缓存条目允许的最小 TTL（秒）。 */
    private final int minTtl;
    /** 缓存条目允许的最大 TTL（秒）。 */
    private final int maxTtl;
    /** 同一主机名下多个名称服务器地址的排序比较器，可为 {@code null} 表示保持插入顺序。 */
    private final Comparator<InetSocketAddress> comparator;
    /** 按主机名索引的权威名称服务器地址缓存。 */
    private final Cache<InetSocketAddress> resolveCache = new Cache<InetSocketAddress>() {
        @Override
        protected boolean shouldReplaceAll(InetSocketAddress entry) {
            return false;
        }

        @Override
        protected boolean equals(InetSocketAddress entry, InetSocketAddress otherEntry) {
            return entry.getHostString().equalsIgnoreCase(otherEntry.getHostString());
        }

        @Override
        protected void sortEntries(String hostname, List<InetSocketAddress> entries) {
            if (comparator != null) {
                Collections.sort(entries, comparator);
            }
        }
    };

    /**
     * 创建尊重 DNS 服务器返回 TTL 的缓存。
     */
    public DefaultAuthoritativeDnsServerCache() {
        this(0, Cache.MAX_SUPPORTED_TTL_SECS, null);
    }

    /**
     * 创建可配置 TTL 边界与地址排序的缓存。
     *
     * @param minTtl the minimum TTL
     * @param maxTtl the maximum TTL
     * @param comparator the {@link Comparator} to order the {@link InetSocketAddress} for a hostname or {@code null}
     *                   if insertion order should be used.
     */
    public DefaultAuthoritativeDnsServerCache(int minTtl, int maxTtl, Comparator<InetSocketAddress> comparator) {
        this.minTtl = Math.min(Cache.MAX_SUPPORTED_TTL_SECS, checkPositiveOrZero(minTtl, "minTtl"));
        this.maxTtl = Math.min(Cache.MAX_SUPPORTED_TTL_SECS, checkPositive(maxTtl, "maxTtl"));
        if (minTtl > maxTtl) {
            throw new IllegalArgumentException(
                    "minTtl: " + minTtl + ", maxTtl: " + maxTtl + " (expected: 0 <= minTtl <= maxTtl)");
        }
        this.comparator = comparator;
    }

    @SuppressWarnings("unchecked")
    @Override
    public DnsServerAddressStream get(String hostname) {
        checkNotNull(hostname, "hostname");

        List<? extends InetSocketAddress> addresses = resolveCache.get(hostname);
        if (addresses == null || addresses.isEmpty()) {
            return null;
        }
        return new SequentialDnsServerAddressStream(addresses, 0);
    }

    @Override
    public void cache(String hostname, InetSocketAddress address, long originalTtl, EventLoop loop) {
        checkNotNull(hostname, "hostname");
        checkNotNull(address, "address");
        checkNotNull(loop, "loop");

        if (address.getHostString() == null) {
            // 必须能提取主机名字符串，以便后续替换缓存中的未解析条目。
            return;
        }

        resolveCache.cache(hostname, address, Math.max(minTtl, (int) Math.min(maxTtl, originalTtl)), loop);
    }

    @Override
    public void clear() {
        resolveCache.clear();
    }

    @Override
    public boolean clear(String hostname) {
        return resolveCache.clear(checkNotNull(hostname, "hostname"));
    }

    @Override
    public String toString() {
        return "DefaultAuthoritativeDnsServerCache(minTtl=" + minTtl + ", maxTtl=" + maxTtl + ", cached nameservers=" +
                resolveCache.size() + ')';
    }

    // 包级可见，供单元测试读取 minTtl。
    int minTtl() {
        return minTtl;
    }

    // 包级可见，供单元测试读取 maxTtl。
    int maxTtl() {
        return maxTtl;
    }
}
