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

/**
 * {@code CNAME} 别名映射缓存接口。
 * <p>记录主机名到规范名称的映射，避免重复跟随 CNAME 链查询。</p>
 */
public interface DnsCnameCache {

    /**
     * 返回给定主机名已缓存的 CNAME 目标。
     *
     * @param hostname the hostname
     * @return the cached entries or an {@code null} if none.
     */
    String get(String hostname);

    /**
     * 缓存主机名到 CNAME 的映射。
     *
     * @param hostname the hostname
     * @param cname the cname mapping.
     * @param originalTtl the TTL as returned by the DNS server
     * @param loop the {@link EventLoop} used to register the TTL timeout
     */
    void cache(String hostname, String cname, long originalTtl, EventLoop loop);

    /**
     * 清除全部已缓存的 CNAME 条目。
     *
     * @see #clear(String)
     */
    void clear();

    /**
     * 清除指定主机名的 CNAME 缓存。
     *
     * @return {@code true} if and only if there was an entry for the specified host name in the cache and
     *         it has been removed by this method
     */
    boolean clear(String hostname);
}
