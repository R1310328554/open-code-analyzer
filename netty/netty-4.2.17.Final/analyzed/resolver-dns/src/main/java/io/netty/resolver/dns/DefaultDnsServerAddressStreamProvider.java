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

import io.netty.util.NetUtil;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.SocketUtils;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.lang.reflect.Method;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.netty.resolver.dns.DnsServerAddresses.sequential;

/**
 * 使用预置默认 DNS 服务器进行解析的 {@link DnsServerAddressStreamProvider}。
 * <p>这些默认值<strong>不</strong>保证与操作系统解析配置一致；初始化时可能通过 JDK 阻塞式
 * DNS/JNDI 引导服务器列表。</p>
 */
public final class DefaultDnsServerAddressStreamProvider implements DnsServerAddressStreamProvider {
    private static final InternalLogger logger =
            InternalLoggerFactory.getInstance(DefaultDnsServerAddressStreamProvider.class);
    /** 系统属性：逗号分隔的备用名称服务器 IP 列表。 */
    private static final String DEFAULT_FALLBACK_SERVER_PROPERTY = "io.netty.resolver.dns.defaultNameServerFallback";
    /** 单例实例，供 {@link DnsNameResolverBuilder} 等默认引用。 */
    public static final DefaultDnsServerAddressStreamProvider INSTANCE = new DefaultDnsServerAddressStreamProvider();

    /** 不可变的默认名称服务器地址列表。 */
    private static final List<InetSocketAddress> DEFAULT_NAME_SERVER_LIST;
    /** 按顺序轮询上述列表的 {@link DnsServerAddresses}。 */
    private static final DnsServerAddresses DEFAULT_NAME_SERVERS;
    /** 标准 DNS 端口。 */
    static final int DNS_PORT = 53;

    static {
        final List<InetSocketAddress> defaultNameServers = new ArrayList<InetSocketAddress>(2);
        if (!PlatformDependent.isAndroid()) {
            // Android 无 /etc/resolv.conf 且无 JNDI，跳过系统探测。
            // 参见 https://github.com/netty/netty/issues/8654
            if (!PlatformDependent.isWindows()) {
                // 尝试读取 /etc/resolv.conf（Linux/macOS 常见，也可能缺失）。
                try {
                    defaultNameServers.addAll(ResolvConf.system().getNameservers());
                } catch (IllegalStateException e) {
                    String fallbackMessage = "Failed to get name servers from /etc/resolv.conf; will fall back to JNDI";
                    if (logger.isDebugEnabled()) {
                        // 始终 INFO 记录；仅 DEBUG 时附带栈。
                        logger.info(fallbackMessage, e);
                    } else {
                        logger.info(fallbackMessage);
                    }
                    DirContextUtils.addNameServers(defaultNameServers, DNS_PORT);
                }
            } else {
                DirContextUtils.addNameServers(defaultNameServers, DNS_PORT);
            }
        }

        // 仅在 Java 8 及以下尝试反射，避免 Java 9+ 非法反射访问警告。
        if (PlatformDependent.javaVersion() < 9 && defaultNameServers.isEmpty()) {
            try {
                Class<?> configClass = Class.forName("sun.net.dns.ResolverConfiguration");
                Method open = configClass.getMethod("open");
                Method nameservers = configClass.getMethod("nameservers");
                Object instance = open.invoke(null);

                @SuppressWarnings("unchecked")
                final List<String> list = (List<String>) nameservers.invoke(instance);
                for (String a: list) {
                    if (a != null) {
                        defaultNameServers.add(new InetSocketAddress(SocketUtils.addressByName(a), DNS_PORT));
                    }
                }
            } catch (Exception ignore) {
                // 反射获取系统 DNS 列表失败，后续使用属性或公共 DNS 兜底。
            }
        }

        if (!defaultNameServers.isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Default DNS servers: {} (sun.net.dns.ResolverConfiguration)", defaultNameServers);
            }
        } else {
            String defaultNameserverString = SystemPropertyUtil.get(DEFAULT_FALLBACK_SERVER_PROPERTY, null);
            if (defaultNameserverString != null) {
                for (String server : defaultNameserverString.split(",")) {
                    String dns = server.trim();
                    if (!NetUtil.isValidIpV4Address(dns) && !NetUtil.isValidIpV6Address(dns)) {
                        throw new ExceptionInInitializerError(DEFAULT_FALLBACK_SERVER_PROPERTY + " doesn't" +
                                " contain a valid list of NameServers: " + defaultNameserverString);
                    }
                    defaultNameServers.add(SocketUtils.socketAddress(server.trim(), DNS_PORT));
                }
                if (defaultNameServers.isEmpty()) {
                    throw new ExceptionInInitializerError(DEFAULT_FALLBACK_SERVER_PROPERTY + " doesn't" +
                            " contain a valid list of NameServers: " + defaultNameserverString);
                }

                if (logger.isWarnEnabled()) {
                    logger.warn(
                            "Default DNS servers: {} (Configured by {} system property)",
                            defaultNameServers, DEFAULT_FALLBACK_SERVER_PROPERTY);
                }
            } else {
                // 根据 IPv6/IPv4 偏好选择 Google Public DNS 作为最终兜底。
                // https://developers.google.com/speed/public-dns/docs/using
                // https://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html
                if (NetUtil.isIpV6AddressesPreferred() ||
                        (NetUtil.LOCALHOST instanceof Inet6Address && !NetUtil.isIpV4StackPreferred())) {
                    Collections.addAll(
                            defaultNameServers,
                            SocketUtils.socketAddress("2001:4860:4860::8888", DNS_PORT),
                            SocketUtils.socketAddress("2001:4860:4860::8844", DNS_PORT));
                } else {
                    Collections.addAll(
                            defaultNameServers,
                            SocketUtils.socketAddress("8.8.8.8", DNS_PORT),
                            SocketUtils.socketAddress("8.8.4.4", DNS_PORT));
                }

                if (logger.isWarnEnabled()) {
                    logger.warn(
                            "Default DNS servers: {} (Google Public DNS as a fallback)", defaultNameServers);
                }
            }
        }

        DEFAULT_NAME_SERVER_LIST = Collections.unmodifiableList(defaultNameServers);
        DEFAULT_NAME_SERVERS = sequential(DEFAULT_NAME_SERVER_LIST);
    }

    private DefaultDnsServerAddressStreamProvider() {
    }

    @Override
    public DnsServerAddressStream nameServerAddressStream(String hostname) {
        // 默认提供者不区分查询名，始终返回同一顺序流。
        return DEFAULT_NAME_SERVERS.stream();
    }

    /**
     * 返回系统 DNS 服务器地址列表。若无法从环境读取，则返回 Google 公共 DNS
     * {@code 8.8.8.8} 与 {@code 8.8.4.4}（或 IPv6 等价地址）。
     */
    public static List<InetSocketAddress> defaultAddressList() {
        return DEFAULT_NAME_SERVER_LIST;
    }

    /**
     * 返回按顺序轮询系统 DNS 地址的 {@link DnsServerAddresses}。环境探测失败时使用
     * Google 公共 DNS 作为兜底。
     * <p>
     * 等价于：
     * <pre>
     * DnsServerAddresses.sequential(DnsServerAddresses.defaultAddressList());
     * </pre>
     * </p>
     */
    public static DnsServerAddresses defaultAddresses() {
        return DEFAULT_NAME_SERVERS;
    }
}
