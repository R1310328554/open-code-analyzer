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
import java.util.List;

/**
 * DNS 解析结果缓存接口。
 * <p>存储主机名到 {@link InetAddress} 或解析失败原因的映射，支持按 TTL 过期与按主机名清除。</p>
 */
public interface DnsCache {

    /**
     * 清除本解析器缓存的全部已解析地址。
     *
     * @see #clear(String)
     */
    void clear();

    /**
     * 从缓存中清除指定主机名的已解析地址。
     *
     * @return {@code true} if and only if there was an entry for the specified host name in the cache and
     *         it has been removed by this method
     */
    boolean clear(String hostname);

    /**
     * 返回给定主机名的缓存条目列表。
     * @param hostname the hostname
     * @param additionals the additional records
     * @return the cached entries
     */
    List<? extends DnsCacheEntry> get(String hostname, DnsRecord[] additionals);

    /**
     * 创建 {@link DnsCacheEntry} 并缓存成功解析的地址。
     * @param hostname the hostname
     * @param additionals the additional records
     * @param address the resolved address
     * @param originalTtl the TTL as returned by the DNS server
     * @param loop the {@link EventLoop} used to register the TTL timeout
     * @return The {@link DnsCacheEntry} corresponding to this cache entry.
     */
    DnsCacheEntry cache(String hostname, DnsRecord[] additionals, InetAddress address, long originalTtl,
                        EventLoop loop);

    /**
     * 缓存给定主机名的解析失败结果（负缓存）。
     * Be aware this <strong>won't</strong> be called with timeout / cancel / transport exceptions.
      *
     * @param hostname the hostname
     * @param additionals the additional records
     * @param cause the resolution failure
     * @param loop the {@link EventLoop} used to register the TTL timeout
     * @return The {@link DnsCacheEntry} corresponding to this cache entry, or {@code null} if this cache doesn't
     * support caching failed responses.
     */
    DnsCacheEntry cache(String hostname, DnsRecord[] additionals, Throwable cause, EventLoop loop);
}
