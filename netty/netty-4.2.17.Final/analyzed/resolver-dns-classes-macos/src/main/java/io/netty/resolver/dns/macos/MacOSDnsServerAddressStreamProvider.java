/*
 * Copyright 2019 The Netty Project
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
package io.netty.resolver.dns.macos;

import io.netty.resolver.dns.DnsServerAddressStream;
import io.netty.resolver.dns.DnsServerAddressStreamProvider;
import io.netty.resolver.dns.DnsServerAddressStreamProviders;
import io.netty.resolver.dns.DnsServerAddresses;
import io.netty.util.internal.ClassInitializerUtil;
import io.netty.util.internal.NativeLibraryLoader;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.ThrowableUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 基于 macOS 系统 DNS 配置（与 Apple
 * <a href="https://opensource.apple.com/tarballs/mDNSResponder/">mDNSResponder</a> 同源机制）
 * 的 {@link DnsServerAddressStreamProvider} 实现。
 * <p>按搜索域后缀匹配 hostname，并定期（默认 10 秒）刷新系统解析器列表。</p>
 */
public final class MacOSDnsServerAddressStreamProvider implements DnsServerAddressStreamProvider {

    /** 按 {@link DnsResolver#searchOrder()} 降序排序，使低序号（高优先级）覆盖高序号条目 */
    private static final Comparator<DnsResolver> RESOLVER_COMPARATOR =
            new Comparator<DnsResolver>() {
                @Override
                public int compare(DnsResolver r1, DnsResolver r2) {
                    // 降序：searchOrder 越小优先级越高，应覆盖较大值的配置
                    return r1.searchOrder() < r2.searchOrder() ? 1 : r1.searchOrder() == r2.searchOrder() ? 0 : -1;
                }
            };

    /** 原生库加载失败时的根因；{@code null} 表示可用 */
    private static final Throwable UNAVAILABILITY_CAUSE;

    private static final InternalLogger logger =
            InternalLoggerFactory.getInstance(MacOSDnsServerAddressStreamProvider.class);

    /** 系统 DNS 映射刷新间隔（纳秒），默认 10 秒 */
    private static final long REFRESH_INTERVAL = TimeUnit.SECONDS.toNanos(10);

    static {
        // 预加载 JNI OnLoad 所需类，避免类加载器死锁（见 netty#11209）
        ClassInitializerUtil.tryLoadClasses(MacOSDnsServerAddressStreamProvider.class,
                // netty_resolver_dns_macos
                byte[].class, String.class
        );

        Throwable cause = null;
        try {
            loadNativeLibrary();
        } catch (Throwable error) {
            cause = error;
        }
        UNAVAILABILITY_CAUSE = cause;
    }

    /** 加载 macOS 专用 DNS 解析原生库 */
    private static void loadNativeLibrary() {
        if (!PlatformDependent.isOsx()) {
            throw new IllegalStateException("Only supported on MacOS/OSX");
        }
        String staticLibName = "netty_resolver_dns_native_macos";
        String sharedLibName = staticLibName + '_' + PlatformDependent.normalizedArch();
        ClassLoader cl = PlatformDependent.getClassLoader(MacOSDnsServerAddressStreamProvider.class);
        try {
            NativeLibraryLoader.load(sharedLibName, cl);
        } catch (UnsatisfiedLinkError e1) {
            try {
                NativeLibraryLoader.load(staticLibName, cl);
                logger.debug("Failed to load {}", sharedLibName, e1);
            } catch (UnsatisfiedLinkError e2) {
                ThrowableUtil.addSuppressed(e1, e2);
                throw e1;
            }
        }
    }

    /** @return 原生实现是否已成功加载 */
    public static boolean isAvailable() {
        return UNAVAILABILITY_CAUSE == null;
    }

    /** 若不可用则抛出 {@link UnsatisfiedLinkError} */
    public static void ensureAvailability() {
        if (UNAVAILABILITY_CAUSE != null) {
            throw (Error) new UnsatisfiedLinkError(
                    "failed to load the required native library").initCause(UNAVAILABILITY_CAUSE);
        }
    }

    /** @return 不可用时的失败原因，可用时为 {@code null} */
    public static Throwable unavailabilityCause() {
        return UNAVAILABILITY_CAUSE;
    }

    /** 构造时立即读取当前系统 DNS 映射并记录刷新时间戳 */
    public MacOSDnsServerAddressStreamProvider() {
        ensureAvailability();
        currentMappings = retrieveCurrentMappings();
        lastRefresh = new AtomicLong(System.nanoTime());
    }

    /** 域名 → 顺序 nameserver 列表的缓存映射 */
    private volatile Map<String, DnsServerAddresses> currentMappings;
    /** 上次刷新映射的纳秒时间戳 */
    private final AtomicLong lastRefresh;

    /** 通过 JNI 读取系统解析器并构建域名映射表 */
    private static Map<String, DnsServerAddresses> retrieveCurrentMappings() {
        DnsResolver[] resolvers = resolvers();

        if (resolvers == null || resolvers.length == 0) {
            return Collections.emptyMap();
        }
        Arrays.sort(resolvers, RESOLVER_COMPARATOR);
        Map<String, DnsServerAddresses> resolverMap = new HashMap<String, DnsServerAddresses>(resolvers.length);
        for (DnsResolver resolver: resolvers) {
            // 跳过 mDNS 专用解析器
            if ("mdns".equalsIgnoreCase(resolver.options())) {
                continue;
            }
            InetSocketAddress[] nameservers = resolver.nameservers();
            if (nameservers == null || nameservers.length == 0) {
                continue;
            }
            String domain = resolver.domain();
            if (domain == null) {
                // 默认（全局）映射
                domain = StringUtil.EMPTY_STRING;
            }
            InetSocketAddress[] servers = resolver.nameservers();
            for (int a = 0; a < servers.length; a++) {
                InetSocketAddress address = servers[a];
                // 端口为 0 时使用解析器配置的端口，仍为 0 则回退 53
                if (address.getPort() == 0) {
                    int port = resolver.port();
                    if (port == 0) {
                        port = 53;
                    }
                    servers[a] = new InetSocketAddress(address.getAddress(), port);
                }
            }

            resolverMap.put(domain, DnsServerAddresses.sequential(servers));
        }
        return resolverMap;
    }

    @Override
    public DnsServerAddressStream nameServerAddressStream(String hostname) {
        long last = lastRefresh.get();
        Map<String, DnsServerAddresses> resolverMap = currentMappings;
        if (System.nanoTime() - last > REFRESH_INTERVAL) {
            // 轻微竞态：刷新窗口内可能仍用旧配置，可接受
            if (lastRefresh.compareAndSet(last, System.nanoTime())) {
                resolverMap = currentMappings = retrieveCurrentMappings();
            }
        }

        final String originalHostname = hostname;
        for (;;) {
            int i = hostname.indexOf('.', 1);
            if (i < 0 || i == hostname.length() - 1) {
                // 无更长后缀，尝试默认映射
                DnsServerAddresses addresses = resolverMap.get(StringUtil.EMPTY_STRING);
                if (addresses != null) {
                    return addresses.stream();
                }
                return DnsServerAddressStreamProviders.unixDefault().nameServerAddressStream(originalHostname);
            }

            DnsServerAddresses addresses = resolverMap.get(hostname);
            if (addresses != null) {
                return addresses.stream();
            }

            // 剥掉最左侧标签，继续匹配父域
            hostname = hostname.substring(i + 1);
        }
    }

    /** JNI：返回当前系统 {@link DnsResolver} 数组 */
    private static native DnsResolver[] resolvers();
}
