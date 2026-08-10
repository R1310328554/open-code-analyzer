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

import java.net.InetAddress;
import java.util.Collections;
import java.util.List;

/**
 * 空实现的 {@link DnsCache}，从不持久化解析结果（仅返回临时条目）。
 */
public final class NoopDnsCache implements DnsCache {

    /** 单例实例。 */
    public static final NoopDnsCache INSTANCE = new NoopDnsCache();

    /**
     * 私有单例构造器。
     */
    private NoopDnsCache() {
    }

    @Override
    public void clear() {
    }

    @Override
    public boolean clear(String hostname) {
        return false;
    }

    @Override
    public List<? extends DnsCacheEntry> get(String hostname, DnsRecord[] additionals) {
        return Collections.emptyList();
    }

    @Override
    public DnsCacheEntry cache(String hostname, DnsRecord[] additional,
                               InetAddress address, long originalTtl, EventLoop loop) {
        return new NoopDnsCacheEntry(address);
    }

    @Override
    public DnsCacheEntry cache(String hostname, DnsRecord[] additional, Throwable cause, EventLoop loop) {
        return null;
    }

    @Override
    public String toString() {
        return NoopDnsCache.class.getSimpleName();
    }

    /** 不写入全局缓存、仅持有地址引用的 {@link DnsCacheEntry}。 */
    private static final class NoopDnsCacheEntry implements DnsCacheEntry {
        private final InetAddress address;

        NoopDnsCacheEntry(InetAddress address) {
            this.address = address;
        }

        @Override
        public InetAddress address() {
            return address;
        }

        @Override
        public Throwable cause() {
            return null;
        }

        @Override
        public String toString() {
            return address.toString();
        }
    }
}
