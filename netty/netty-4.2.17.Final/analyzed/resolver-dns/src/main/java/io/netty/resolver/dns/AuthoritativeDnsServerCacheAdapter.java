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
import io.netty.handler.codec.dns.DnsRecord;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * 将 {@link AuthoritativeDnsServerCache} 的全部操作委托给底层 {@link DnsCache} 的适配器实现。
 * <p>该实现仅用于在 API 演进过程中保持向后兼容的升级路径。</p>
 */
final class AuthoritativeDnsServerCacheAdapter implements AuthoritativeDnsServerCache {

    /** 查询权威服务器缓存时不附带额外 DNS 记录。 */
    private static final DnsRecord[] EMPTY = new DnsRecord[0];
    /** 被包装的通用 DNS 解析结果缓存。 */
    private final DnsCache cache;

    AuthoritativeDnsServerCacheAdapter(DnsCache cache) {
        this.cache = checkNotNull(cache, "cache");
    }

    @Override
    public DnsServerAddressStream get(String hostname) {
        List<? extends DnsCacheEntry> entries = cache.get(hostname, EMPTY);
        if (entries == null || entries.isEmpty()) {
            return null;
        }
        // 负缓存（失败原因）不映射为名称服务器流。
        if (entries.get(0).cause() != null) {
            return null;
        }

        List<InetSocketAddress> addresses = new ArrayList<InetSocketAddress>(entries.size());

        int i = 0;
        do {
            InetAddress addr = entries.get(i).address();
            addresses.add(new InetSocketAddress(addr, DefaultDnsServerAddressStreamProvider.DNS_PORT));
        } while (++i < entries.size());
        return new SequentialDnsServerAddressStream(addresses, 0);
    }

    @Override
    public void cache(String hostname, InetSocketAddress address, long originalTtl, EventLoop loop) {
        // 仅缓存已解析的地址，未解析条目无法写入 DnsCache。
        if (!address.isUnresolved()) {
            cache.cache(hostname, EMPTY, address.getAddress(), originalTtl, loop);
        }
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public boolean clear(String hostname) {
        return cache.clear(hostname);
    }
}
