/*
 * Copyright 2015 The Netty Project
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

import io.netty.util.NetUtil;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * 按 DNS 服务器地址管理进行中的 {@link DnsQueryContext}，分配与回收 16 位查询 ID。
 * <p>IPv4/IPv6 兼容地址会共享同一上下文映射，避免双栈环境下 ID 空间分裂。</p>
 */
final class DnsQueryContextManager {

    /** 键为 DNS 服务器地址，值为该服务器上 queryId → {@link DnsQueryContext} 的映射。 */
    private final Map<InetSocketAddress, DnsQueryContextMap> map =
            new HashMap<InetSocketAddress, DnsQueryContextMap>();

    /**
     * 注册 {@link DnsQueryContext} 并返回应使用的查询 ID；ID 耗尽时返回 {@code -1} 且不存储上下文。
     *
     * @param nameServerAddr    The {@link InetSocketAddress} of the nameserver to query.
     * @param qCtx              The {@link {@link DnsQueryContext} to store.
     * @return                  the ID that should be used or {@code -1} if none could be generated.
     */
    int add(InetSocketAddress nameServerAddr, DnsQueryContext qCtx) {
        assert !nameServerAddr.isUnresolved();
        final DnsQueryContextMap contexts = getOrCreateContextMap(nameServerAddr);
        return contexts.add(qCtx);
    }

    /**
     * 按服务器地址与 query ID 查找 {@link DnsQueryContext}，未找到返回 {@code null}。
     *
     * @param nameServerAddr    The {@link InetSocketAddress} of the nameserver.
     * @param id                The id that identifies the {@link DnsQueryContext} and was used for the query.
     * @return                  The context or {@code null} if none could be found.
     */
    DnsQueryContext get(InetSocketAddress nameServerAddr, int id) {
        assert !nameServerAddr.isUnresolved();
        final DnsQueryContextMap contexts = getContextMap(nameServerAddr);
        if (contexts == null) {
            return null;
        }
        return contexts.get(id);
    }

    /**
     * 移除并返回指定服务器与 ID 的 {@link DnsQueryContext}，同时将 ID 归还 ID 空间。
     *
     * @param nameServerAddr    The {@link InetSocketAddress} of the nameserver.
     * @param id                The id that identifies the {@link DnsQueryContext} and was used for the query.
     * @return                  The context or {@code null} if none could be removed.
     */
    DnsQueryContext remove(InetSocketAddress nameServerAddr, int id) {
        assert !nameServerAddr.isUnresolved();
        final DnsQueryContextMap contexts = getContextMap(nameServerAddr);
        if (contexts == null) {
            return null;
        }
        return contexts.remove(id);
    }

    private DnsQueryContextMap getContextMap(InetSocketAddress nameServerAddr) {
        synchronized (map) {
            return map.get(nameServerAddr);
        }
    }

    private DnsQueryContextMap getOrCreateContextMap(InetSocketAddress nameServerAddr) {
        synchronized (map) {
            final DnsQueryContextMap contexts = map.get(nameServerAddr);
            if (contexts != null) {
                return contexts;
            }

            final DnsQueryContextMap newContexts = new DnsQueryContextMap();
            final InetAddress a = nameServerAddr.getAddress();
            final int port = nameServerAddr.getPort();
            DnsQueryContextMap old = map.put(nameServerAddr, newContexts);
            // Assert that we didn't replace an existing mapping.
            assert old == null : "DnsQueryContextMap already exists for " + nameServerAddr;

            InetSocketAddress extraAddress = null;
            if (a instanceof Inet4Address) {
                // 同时为 IPv4 映射 IPv6 地址注册相同映射
                // Also add the mapping for the IPv4-compatible IPv6 address.
                final Inet4Address a4 = (Inet4Address) a;
                if (a4.isLoopbackAddress()) {
                    extraAddress = new InetSocketAddress(NetUtil.LOCALHOST6, port);
                } else {
                    extraAddress = new InetSocketAddress(toCompactAddress(a4), port);
                }
            } else if (a instanceof Inet6Address) {
                // IPv4 兼容 IPv6 地址时同步注册 IPv4 键
                // Also add the mapping for the IPv4 address if this IPv6 address is compatible.
                final Inet6Address a6 = (Inet6Address) a;
                if (a6.isLoopbackAddress()) {
                    extraAddress = new InetSocketAddress(NetUtil.LOCALHOST4, port);
                } else if (a6.isIPv4CompatibleAddress()) {
                    extraAddress = new InetSocketAddress(toIPv4Address(a6), port);
                }
            }
            if (extraAddress != null) {
                old = map.put(extraAddress, newContexts);
                // Assert that we didn't replace an existing mapping.
                assert old == null : "DnsQueryContextMap already exists for " + extraAddress;
            }

            return newContexts;
        }
    }

    private static Inet6Address toCompactAddress(Inet4Address a4) {
        byte[] b4 = a4.getAddress();
        byte[] b6 = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, b4[0], b4[1], b4[2], b4[3] };
        try {
            return (Inet6Address) InetAddress.getByAddress(b6);
        } catch (UnknownHostException e) {
            throw new Error(e);
        }
    }

    private static Inet4Address toIPv4Address(Inet6Address a6) {
        assert a6.isIPv4CompatibleAddress();

        byte[] b6 = a6.getAddress();
        byte[] b4 = { b6[12], b6[13], b6[14], b6[15] };
        try {
            return (Inet4Address) InetAddress.getByAddress(b4);
        } catch (UnknownHostException e) {
            throw new Error(e);
        }
    }

    private static final class DnsQueryContextMap {

        private final DnsQueryIdSpace idSpace = new DnsQueryIdSpace();

        // 首次分配从 0 开始（内部计数自 -1 递增）
        // We increment on every usage so start with -1, this will ensure we start with 0 as first id.
        private final IntObjectMap<DnsQueryContext> map = new IntObjectHashMap<DnsQueryContext>();

        synchronized int add(DnsQueryContext ctx) {
            int id = idSpace.nextId();
            if (id == -1) {
                // ID 空间耗尽（-1），不写入 map
                // -1 means that we couldn't reserve an id to use. In this case return early and not store the
                // context in the map.
                return -1;
            }
            DnsQueryContext oldCtx = map.put(id, ctx);
            assert oldCtx == null;
            return id;
        }

        synchronized DnsQueryContext get(int id) {
            return map.get(id);
        }

        synchronized DnsQueryContext remove(int id) {
            DnsQueryContext result = map.remove(id);
            if (result != null) {
                idSpace.pushId(id);
            }
            assert result != null : "DnsQueryContext not found, id: "  + id;
            return result;
        }
    }
}
