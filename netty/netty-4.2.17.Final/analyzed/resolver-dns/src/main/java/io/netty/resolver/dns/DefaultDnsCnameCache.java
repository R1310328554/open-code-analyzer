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
import io.netty.util.AsciiString;

import java.util.List;

import static io.netty.util.internal.ObjectUtil.*;

/**
 * {@link DnsCnameCache} 的默认实现，基于 {@link Cache} 存储 CNAME 别名映射。
 * <p>按 RFC 约定，每个查询名最多保留一条 CNAME 映射，新记录会替换旧条目。</p>
 */
public final class DefaultDnsCnameCache implements DnsCnameCache {
    /** 缓存条目允许的最小 TTL（秒）。 */
    private final int minTtl;
    /** 缓存条目允许的最大 TTL（秒）。 */
    private final int maxTtl;

    /** 主机名到 CNAME 目标名的缓存。 */
    private final Cache<String> cache = new Cache<String>() {
        @Override
        protected boolean shouldReplaceAll(String entry) {
            // RFC 规定 CNAME 与查询名为 1:1 映射，新值应替换全部旧条目。
            return true;
        }

        @Override
        protected boolean equals(String entry, String otherEntry) {
            return AsciiString.contentEqualsIgnoreCase(entry, otherEntry);
        }
    };

    /**
     * 创建尊重 DNS 服务器返回 TTL 的 CNAME 缓存。
     */
    public DefaultDnsCnameCache() {
        this(0, Cache.MAX_SUPPORTED_TTL_SECS);
    }

    /**
     * 创建可配置 TTL 边界的 CNAME 缓存。
     *
     * @param minTtl the minimum TTL
     * @param maxTtl the maximum TTL
     */
    public DefaultDnsCnameCache(int minTtl, int maxTtl) {
        this.minTtl = Math.min(Cache.MAX_SUPPORTED_TTL_SECS, checkPositiveOrZero(minTtl, "minTtl"));
        this.maxTtl = Math.min(Cache.MAX_SUPPORTED_TTL_SECS, checkPositive(maxTtl, "maxTtl"));
        if (minTtl > maxTtl) {
            throw new IllegalArgumentException(
                    "minTtl: " + minTtl + ", maxTtl: " + maxTtl + " (expected: 0 <= minTtl <= maxTtl)");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public String get(String hostname) {
        List<? extends String> cached =  cache.get(checkNotNull(hostname, "hostname"));
        if (cached == null || cached.isEmpty()) {
            return null;
        }
        // 实现上同一主机名不会缓存多条 CNAME。
        return cached.get(0);
    }

    @Override
    public void cache(String hostname, String cname, long originalTtl, EventLoop loop) {
        checkNotNull(hostname, "hostname");
        checkNotNull(cname, "cname");
        checkNotNull(loop, "loop");
        cache.cache(hostname, cname, Math.max(minTtl, (int) Math.min(maxTtl, originalTtl)), loop);
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public boolean clear(String hostname) {
        return cache.clear(checkNotNull(hostname, "hostname"));
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
