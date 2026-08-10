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

/**
 * 缓存用于解析特定主机名时应使用的权威名称服务器（nameserver）。
 * <p>DNS 解析过程中，NS 记录或委派链可能指示某域名应使用特定权威服务器；
 * 本接口负责存储并在 TTL 到期前复用这些服务器地址，避免重复查询委派信息。</p>
 */
public interface AuthoritativeDnsServerCache {

    /**
     * 返回缓存中用于解析给定主机名的名称服务器流。
     * <p>返回的 {@link DnsServerAddressStream} 可能包含尚未解析的 {@link InetSocketAddress}，
     * 在后续解析其他域名时再按需解析。</p>
     *
     * @param hostname the hostname
     * @return the cached entries or an {@code null} if none.
     */
    DnsServerAddressStream get(String hostname);

    /**
     * 缓存用于解析给定主机名的名称服务器地址。
     *
     * @param hostname the hostname
     * @param address the nameserver address (which may be unresolved).
     * @param originalTtl the TTL as returned by the DNS server
     * @param loop the {@link EventLoop} used to register the TTL timeout
     */
    void cache(String hostname, InetSocketAddress address, long originalTtl, EventLoop loop);

    /**
     * 清除所有已缓存的名称服务器条目。
     *
     * @see #clear(String)
     */
    void clear();

    /**
     * 清除指定主机名对应的已缓存名称服务器。
     *
     * @return {@code true} if and only if there was an entry for the specified host name in the cache and
     *         it has been removed by this method
     */
    boolean clear(String hostname);
}
