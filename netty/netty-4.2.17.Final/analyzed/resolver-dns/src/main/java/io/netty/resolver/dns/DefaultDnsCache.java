/*
 * Copyright 2016 The Netty Project
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
import io.netty.util.internal.StringUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.AbstractList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import static io.netty.util.internal.ObjectUtil.checkNotNull;
import static io.netty.util.internal.ObjectUtil.checkPositiveOrZero;

/**
 * {@link DnsCache} 的默认实现，底层使用 {@link ConcurrentMap} 与 {@link Cache}。
 * <p>若查询附带额外 {@link DnsRecord}（非空 additionals），则不进行任何缓存读写。</p>
 */
public class DefaultDnsCache implements DnsCache {

    /** 主机名到解析条目（地址或失败原因）的缓存。 */
    private final Cache<DefaultDnsCacheEntry> resolveCache = new Cache<DefaultDnsCacheEntry>() {

        @Override
        protected boolean shouldReplaceAll(DefaultDnsCacheEntry entry) {
            // 负缓存条目应替换同主机名下的全部成功记录。
            return entry.cause() != null;
        }

        @Override
        protected boolean equals(DefaultDnsCacheEntry entry, DefaultDnsCacheEntry otherEntry) {
            if (entry.address() != null) {
                return entry.address().equals(otherEntry.address());
            }
            if (otherEntry.address() != null) {
                return false;
            }
            return entry.cause().equals(otherEntry.cause());
        }
    };

    /** 成功记录的最小 TTL（秒）。 */
    private final int minTtl;
    /** 成功记录的最大 TTL（秒）。 */
    private final int maxTtl;
    /** 失败查询（负缓存）的 TTL（秒）；为 0 表示不缓存失败。 */
    private final int negativeTtl;

    /**
     * 创建尊重 DNS 服务器 TTL、且不缓存负响应的解析缓存。
     */
    public DefaultDnsCache() {
        this(0, Cache.MAX_SUPPORTED_TTL_SECS, 0);
    }

    /**
     * 创建可配置 TTL 边界的解析缓存。
     * @param minTtl the minimum TTL
     * @param maxTtl the maximum TTL
     * @param negativeTtl the TTL for failed queries
     */
    public DefaultDnsCache(int minTtl, int maxTtl, int negativeTtl) {
        this.minTtl = Math.min(Cache.MAX_SUPPORTED_TTL_SECS, checkPositiveOrZero(minTtl, "minTtl"));
        this.maxTtl = Math.min(Cache.MAX_SUPPORTED_TTL_SECS, checkPositiveOrZero(maxTtl, "maxTtl"));
        if (minTtl > maxTtl) {
            throw new IllegalArgumentException(
                    "minTtl: " + minTtl + ", maxTtl: " + maxTtl + " (expected: 0 <= minTtl <= maxTtl)");
        }
        this.negativeTtl = Math.min(Cache.MAX_SUPPORTED_TTL_SECS, checkPositiveOrZero(negativeTtl, "negativeTtl"));
    }

    /**
     * 返回成功解析记录的最小缓存 TTL（秒）。
     *
     * @see #maxTtl()
     */
    public int minTtl() {
        return minTtl;
    }

    /**
     * 返回成功解析记录的最大缓存 TTL（秒）。
     *
     * @see #minTtl()
     */
    public int maxTtl() {
        return maxTtl;
    }

    /**
     * 返回失败 DNS 查询的缓存 TTL（秒）。默认 {@code 0} 表示不缓存负结果。
     */
    public int negativeTtl() {
        return negativeTtl;
    }

    @Override
    public void clear() {
        resolveCache.clear();
    }

    @Override
    public boolean clear(String hostname) {
        checkNotNull(hostname, "hostname");
        return resolveCache.clear(appendDot(hostname));
    }

    private static boolean emptyAdditionals(DnsRecord[] additionals) {
        return additionals == null || additionals.length == 0;
    }

    @Override
    public List<? extends DnsCacheEntry> get(String hostname, DnsRecord[] additionals) {
        checkNotNull(hostname, "hostname");
        if (!emptyAdditionals(additionals)) {
            return Collections.<DnsCacheEntry>emptyList();
        }

        final List<? extends DnsCacheEntry> entries = resolveCache.get(appendDot(hostname));
        if (entries == null || entries.isEmpty()) {
            return entries;
        }
        return new DnsCacheEntryList(entries);
    }

    @Override
    public DnsCacheEntry cache(String hostname, DnsRecord[] additionals,
                               InetAddress address, long originalTtl, EventLoop loop) {
        checkNotNull(hostname, "hostname");
        checkNotNull(address, "address");
        checkNotNull(loop, "loop");
        DefaultDnsCacheEntry e = new DefaultDnsCacheEntry(hostname, address);
        if (maxTtl == 0 || !emptyAdditionals(additionals)) {
            return e;
        }
        resolveCache.cache(appendDot(hostname), e, Math.max(minTtl, (int) Math.min(maxTtl, originalTtl)), loop);
        return e;
    }

    @Override
    public DnsCacheEntry cache(String hostname, DnsRecord[] additionals, Throwable cause, EventLoop loop) {
        checkNotNull(hostname, "hostname");
        checkNotNull(cause, "cause");
        checkNotNull(loop, "loop");

        DefaultDnsCacheEntry e = new DefaultDnsCacheEntry(hostname, cause);
        if (negativeTtl == 0 || !emptyAdditionals(additionals)) {
            return e;
        }

        resolveCache.cache(appendDot(hostname), e, negativeTtl, loop);
        return e;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("DefaultDnsCache(minTtl=")
                .append(minTtl).append(", maxTtl=")
                .append(maxTtl).append(", negativeTtl=")
                .append(negativeTtl).append(", cached resolved hostname=")
                .append(resolveCache.size()).append(')')
                .toString();
    }

    /** 单条 DNS 缓存条目，可为成功地址或失败 Throwable。 */
    private static final class DefaultDnsCacheEntry implements DnsCacheEntry {
        private final String hostname;
        private final InetAddress address;
        private final Throwable cause;
        private final int hash;

        DefaultDnsCacheEntry(String hostname, InetAddress address) {
            this.hostname = hostname;
            this.address = address;
            cause = null;
            hash = System.identityHashCode(this);
        }

        DefaultDnsCacheEntry(String hostname, Throwable cause) {
            this.hostname = hostname;
            this.cause = cause;
            address = null;
            hash = System.identityHashCode(this);
        }

        private DefaultDnsCacheEntry(DefaultDnsCacheEntry entry) {
            this.hostname = entry.hostname;
            if (entry.cause == null) {
                this.address = entry.address;
                this.cause = null;
            } else {
                this.address = null;
                this.cause = copyThrowable(entry.cause);
            }
            this.hash = entry.hash;
        }

        @Override
        public InetAddress address() {
            return address;
        }

        @Override
        public Throwable cause() {
            return cause;
        }

        String hostname() {
            return hostname;
        }

        @Override
        public String toString() {
            if (cause != null) {
                return hostname + '/' + cause;
            } else {
                return address.toString();
            }
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof DefaultDnsCacheEntry) && ((DefaultDnsCacheEntry) obj).hash == hash;
        }

        /** 负缓存条目返回副本，避免调用方持有带完整栈的异常直至 TTL 过期。 */
        DnsCacheEntry copyIfNeeded() {
            if (cause == null) {
                return this;
            }
            return new DefaultDnsCacheEntry(this);
        }
    }

    /** 与 {@link DnsCache} 查询键一致：FQDN 以 trailing dot 结尾。 */
    private static String appendDot(String hostname) {
        return StringUtil.endsWith(hostname, '.') ? hostname : hostname + '.';
    }

    private static Throwable copyThrowable(Throwable error) {
        if (error.getClass() == UnknownHostException.class) {
            // 快速路径：本实现仅将 UnknownHostException 写入负缓存。
            UnknownHostException copy = new UnknownHostException(error.getMessage()) {
                @Override
                public Throwable fillInStackTrace() {
                    // 不填充栈，减小副本开销。
                    return this;
                }
            };
            copy.initCause(error.getCause());
            copy.setStackTrace(error.getStackTrace());
            return copy;
        }

        try {
            // Throwable 可序列化，通过深拷贝复制任意异常类型。
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(error);
            }

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            try (ObjectInputStream ois = new ObjectInputStream(bais)) {
                return (Throwable) ois.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 对外暴露的条目列表视图：读取负缓存时按需复制异常，防止内存长期占用。
     */
    private static final class DnsCacheEntryList extends AbstractList<DnsCacheEntry> {
        private final List<? extends DnsCacheEntry> entries;

        DnsCacheEntryList(List<? extends DnsCacheEntry> entries) {
            this.entries = entries;
        }

        @Override
        public DnsCacheEntry get(int index) {
            DefaultDnsCacheEntry entry = (DefaultDnsCacheEntry) entries.get(index);
            // 调用方可能对返回的异常调用 addSuppressed 等，复制可避免持有大栈直至过期。
            return entry.copyIfNeeded();
        }

        @Override
        public int size() {
            return entries.size();
        }

        @Override
        public int hashCode() {
            // 委托 super 以满足 checkstyle
            return super.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof DnsCacheEntryList) {
                // 快速路径：比较底层列表。
                return entries.equals(((DnsCacheEntryList) o).entries);
            }
            return super.equals(o);
        }
    };
}
