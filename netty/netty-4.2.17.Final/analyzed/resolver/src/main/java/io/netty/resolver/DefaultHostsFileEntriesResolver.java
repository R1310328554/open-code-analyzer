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
package io.netty.resolver;

import io.netty.util.CharsetUtil;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Default {@link HostsFileEntriesResolver} that resolves hosts file entries only once.
 * <p>默认 hosts 文件解析器：启动时加载 {@code /etc/hosts}（或 Windows 等价路径），
 * 可按系统属性 {@code io.netty.hostsFileRefreshInterval} 定期刷新缓存。</p>
 */
public final class DefaultHostsFileEntriesResolver implements HostsFileEntriesResolver {

    private static final InternalLogger logger =
            InternalLoggerFactory.getInstance(DefaultHostsFileEntriesResolver.class);
    /** 默认刷新间隔（纳秒）；0 表示仅加载一次、不自动刷新。 */
    private static final long DEFAULT_REFRESH_INTERVAL;

    /** 两次刷新之间的最小间隔（纳秒）。 */
    private final long refreshInterval;
    /** 上次成功刷新的单调时钟时间戳。 */
    private final AtomicLong lastRefresh = new AtomicLong(System.nanoTime());
    private final HostsFileEntriesProvider.Parser hostsFileParser;
    /** IPv4 主机名到地址列表的 volatile 快照。 */
    private volatile Map<String, List<InetAddress>> inet4Entries;
    /** IPv6 主机名到地址列表的 volatile 快照。 */
    private volatile Map<String, List<InetAddress>> inet6Entries;

    static {
        DEFAULT_REFRESH_INTERVAL = SystemPropertyUtil.getLong(
                "io.netty.hostsFileRefreshInterval", /*nanos*/0);

        if (logger.isDebugEnabled()) {
            logger.debug("-Dio.netty.hostsFileRefreshInterval: {}", DEFAULT_REFRESH_INTERVAL);
        }
    }

    public DefaultHostsFileEntriesResolver() {
        this(HostsFileEntriesProvider.parser(), DEFAULT_REFRESH_INTERVAL);
    }

    // for testing purpose only
    DefaultHostsFileEntriesResolver(HostsFileEntriesProvider.Parser hostsFileParser, long refreshInterval) {
        this.hostsFileParser = hostsFileParser;
        this.refreshInterval = ObjectUtil.checkPositiveOrZero(refreshInterval, "refreshInterval");
        HostsFileEntriesProvider entries = parseEntries(hostsFileParser);
        inet4Entries = entries.ipv4Entries();
        inet6Entries = entries.ipv6Entries();
    }

    @Override
    public InetAddress address(String inetHost, ResolvedAddressTypes resolvedAddressTypes) {
        return firstAddress(addresses(inetHost, resolvedAddressTypes));
    }

    /**
     * Resolves all addresses of a hostname against the entries in a hosts file, depending on the specified
     * {@link ResolvedAddressTypes}.
     *
     * @param inetHost the hostname to resolve
     * @param resolvedAddressTypes the address types to resolve
     * @return all matching addresses or {@code null} in case the hostname cannot be resolved
     */
    public List<InetAddress> addresses(String inetHost, ResolvedAddressTypes resolvedAddressTypes) {
        String normalized = normalize(inetHost);
        ensureHostsFileEntriesAreFresh();

        switch (resolvedAddressTypes) {
            case IPV4_ONLY:
                return inet4Entries.get(normalized);
            case IPV6_ONLY:
                return inet6Entries.get(normalized);
            case IPV4_PREFERRED:
                List<InetAddress> allInet4Addresses = inet4Entries.get(normalized);
                return allInet4Addresses != null ? allAddresses(allInet4Addresses, inet6Entries.get(normalized)) :
                        inet6Entries.get(normalized);
            case IPV6_PREFERRED:
                List<InetAddress> allInet6Addresses = inet6Entries.get(normalized);
                return allInet6Addresses != null ? allAddresses(allInet6Addresses, inet4Entries.get(normalized)) :
                        inet4Entries.get(normalized);
            default:
                throw new IllegalArgumentException("Unknown ResolvedAddressTypes " + resolvedAddressTypes);
        }
    }

    /** 若配置了刷新间隔且已过期，则 CAS 竞争成功后重新解析 hosts 文件。 */
    private void ensureHostsFileEntriesAreFresh() {
        long interval = refreshInterval;
        if (interval == 0) {
            return;
        }
        long last = lastRefresh.get();
        long currentTime = System.nanoTime();
        if (currentTime - last > interval) {
            if (lastRefresh.compareAndSet(last, currentTime)) {
                HostsFileEntriesProvider entries = parseEntries(hostsFileParser);
                inet4Entries = entries.ipv4Entries();
                inet6Entries = entries.ipv6Entries();
            }
        }
    }

    // package-private for testing purposes
    String normalize(String inetHost) {
        return inetHost.toLowerCase(Locale.ENGLISH);
    }

    private static List<InetAddress> allAddresses(List<InetAddress> a, List<InetAddress> b) {
        List<InetAddress> result = new ArrayList<InetAddress>(a.size() + (b == null ? 0 : b.size()));
        result.addAll(a);
        if (b != null) {
            result.addAll(b);
        }
        return result;
    }

    private static InetAddress firstAddress(List<InetAddress> addresses) {
        return addresses != null && !addresses.isEmpty() ? addresses.get(0) : null;
    }

    private static HostsFileEntriesProvider parseEntries(HostsFileEntriesProvider.Parser parser) {
        if (PlatformDependent.isWindows()) {
            // Windows 上 hosts 文件编码无统一标准，依次尝试系统默认、UTF-16、UTF-8
            return parser.parseSilently(Charset.defaultCharset(), CharsetUtil.UTF_16, CharsetUtil.UTF_8);
        }
        return parser.parseSilently();
    }
}
