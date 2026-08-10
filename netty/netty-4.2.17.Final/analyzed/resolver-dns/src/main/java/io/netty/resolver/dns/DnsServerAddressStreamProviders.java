/*
 * Copyright 2017 The Netty Project
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

import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * {@link DnsServerAddressStreamProvider} 相关工具方法。
 */
public final class DnsServerAddressStreamProviders {

    private static final InternalLogger LOGGER =
            InternalLoggerFactory.getInstance(DnsServerAddressStreamProviders.class);
    private static final Constructor<? extends DnsServerAddressStreamProvider> STREAM_PROVIDER_CONSTRUCTOR;
    private static final String MACOS_PROVIDER_CLASS_NAME =
            "io.netty.resolver.dns.macos.MacOSDnsServerAddressStreamProvider";

    static {
        Constructor<? extends DnsServerAddressStreamProvider> constructor = null;
        if (PlatformDependent.isOsx()) {
            try {
                // MacOS 实现在独立 jar 中，通过反射加载以避免硬依赖
                Object maybeProvider = AccessController.doPrivileged(new PrivilegedAction<Object>() {
                    @Override
                    public Object run() {
                        try {
                            return Class.forName(
                                    MACOS_PROVIDER_CLASS_NAME,
                                    true,
                                    DnsServerAddressStreamProviders.class.getClassLoader());
                        } catch (Throwable cause) {
                            return cause;
                        }
                    }
                });
                if (maybeProvider instanceof Class) {
                    @SuppressWarnings("unchecked")
                    Class<? extends DnsServerAddressStreamProvider> providerClass =
                            (Class<? extends DnsServerAddressStreamProvider>) maybeProvider;
                    constructor = providerClass.getConstructor();
                    constructor.newInstance();  // ctor ensures availability
                    LOGGER.debug("{}: available", MACOS_PROVIDER_CLASS_NAME);
                } else {
                    throw (Throwable) maybeProvider;
                }
            } catch (ClassNotFoundException cause) {
                LOGGER.warn("Can not find {} in the classpath, fallback to system defaults. This may result in "
                        + "incorrect DNS resolutions on MacOS. Check whether you have a dependency on "
                        + "'io.netty:netty-resolver-dns-native-macos'", MACOS_PROVIDER_CLASS_NAME);
            } catch (Throwable cause) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.error("Unable to load {}, fallback to system defaults. This may result in "
                            + "incorrect DNS resolutions on MacOS. Check whether you have a dependency on "
                            + "'io.netty:netty-resolver-dns-native-macos'", MACOS_PROVIDER_CLASS_NAME, cause);
                } else {
                    LOGGER.error("Unable to load {}, fallback to system defaults. This may result in "
                            + "incorrect DNS resolutions on MacOS. Check whether you have a dependency on "
                            + "'io.netty:netty-resolver-dns-native-macos'. Use DEBUG level to see the full stack: {}",
                            MACOS_PROVIDER_CLASS_NAME,
                            cause.getCause() != null ? cause.getCause().toString() : cause.toString());
                }
                constructor = null;
            }
        }
        STREAM_PROVIDER_CONSTRUCTOR = constructor;
    }

    private DnsServerAddressStreamProviders() {
    }

    /**
     * 继承本机 DNS 服务器配置的 {@link DnsServerAddressStreamProvider}。
     * <p>当前仅支持 macOS 与 Linux。</p>
     * @return A {@link DnsServerAddressStreamProvider} which inherits the DNS servers from your local host's
     * configuration.
     */
    public static DnsServerAddressStreamProvider platformDefault() {
        if (STREAM_PROVIDER_CONSTRUCTOR != null) {
            try {
                return STREAM_PROVIDER_CONSTRUCTOR.newInstance();
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                // ignore
            }
        }
        return unixDefault();
    }

    /** 返回 Unix 风格默认 provider（解析 resolv.conf 等）。 */
    public static DnsServerAddressStreamProvider unixDefault() {
        return DefaultProviderHolder.DEFAULT_DNS_SERVER_ADDRESS_STREAM_PROVIDER;
    }

    // 延迟初始化默认 provider，仅在需要时加载
    private static final class DefaultProviderHolder {
        // 与 OpenJDK sun.net.dns.ResolverConfigurationImpl 相同，每 5 分钟刷新
        private static final long REFRESH_INTERVAL = TimeUnit.MINUTES.toNanos(5);

        // TODO(scott): how is this done on Windows? This may require a JNI call to GetNetworkParams
        // https://msdn.microsoft.com/en-us/library/aa365968(VS.85).aspx.
        static final DnsServerAddressStreamProvider DEFAULT_DNS_SERVER_ADDRESS_STREAM_PROVIDER =
                new DnsServerAddressStreamProvider() {
                    private volatile DnsServerAddressStreamProvider currentProvider = provider();
                    private final AtomicLong lastRefresh = new AtomicLong(System.nanoTime());

                    @Override
                    public DnsServerAddressStream nameServerAddressStream(String hostname) {
                        long last = lastRefresh.get();
                        DnsServerAddressStreamProvider current = currentProvider;
                        if (System.nanoTime() - last > REFRESH_INTERVAL) {
                            // 存在轻微竞态，旧配置可能仍被短暂使用
                            if (lastRefresh.compareAndSet(last, System.nanoTime())) {
                                current = currentProvider = provider();
                            }
                        }
                        return current.nameServerAddressStream(hostname);
                    }

                    private DnsServerAddressStreamProvider provider() {
                        // Windows 直接使用默认 provider，避免无意义的错误日志
                        return PlatformDependent.isWindows() ? DefaultDnsServerAddressStreamProvider.INSTANCE :
                                UnixResolverDnsServerAddressStreamProvider.parseSilently();
                    }
                };
    }
}
