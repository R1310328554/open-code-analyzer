/*
 * Copyright 2020 The Netty Project
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
package io.netty.util;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.InjectAccessors;
import com.oracle.svm.core.annotate.TargetClass;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collection;

/**
 * GraalVM Native Image 下 {@link NetUtil} 的替代实现。
 *
 * <p>通过 {@link InjectAccessors} 将静态字段访问重定向到按需初始化的 Lazy Holder，
 * 避免在镜像构建期执行网络探测，改在运行时懒加载 localhost 与网卡列表。</p>
 */
@TargetClass(NetUtil.class)
final class NetUtilSubstitutions {
    private NetUtilSubstitutions() {
    }

    /** 懒加载的 IPv4 回环地址。 */
    @Alias
    @InjectAccessors(NetUtilLocalhost4Accessor.class)
    public static Inet4Address LOCALHOST4;

    /** 懒加载的 IPv6 回环地址。 */
    @Alias
    @InjectAccessors(NetUtilLocalhost6Accessor.class)
    public static Inet6Address LOCALHOST6;

    /** 懒加载的首选回环地址。 */
    @Alias
    @InjectAccessors(NetUtilLocalhostAccessor.class)
    public static InetAddress LOCALHOST;

    /** 懒加载的本机网卡集合。 */
    @Alias
    @InjectAccessors(NetUtilNetworkInterfacesAccessor.class)
    public static Collection<NetworkInterface> NETWORK_INTERFACES;

    /** LOCALHOST4 的 getter/setter 注入器。 */
    private static final class NetUtilLocalhost4Accessor {
        static Inet4Address get() {
            // 使用 Initialization-on-demand holder 惯用法按需初始化
            // using https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom
            return NetUtilLocalhost4LazyHolder.LOCALHOST4;
        }

        static void set(Inet4Address ignored) {
            // 空 setter，避免运行时初始化 NetUtil 时因缺少 setter 抛异常
            // a no-op setter to avoid exceptions when NetUtil is initialized at run-time
        }
    }

    /** LOCALHOST4 的懒加载持有者。 */
    private static final class NetUtilLocalhost4LazyHolder {
        private static final Inet4Address LOCALHOST4 = NetUtilInitializations.createLocalhost4();
    }

    /** LOCALHOST6 的 getter/setter 注入器。 */
    private static final class NetUtilLocalhost6Accessor {
        static Inet6Address get() {
            // using https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom
            return NetUtilLocalhost6LazyHolder.LOCALHOST6;
        }

        static void set(Inet6Address ignored) {
            // a no-op setter to avoid exceptions when NetUtil is initialized at run-time
        }
    }

    /** LOCALHOST6 的懒加载持有者。 */
    private static final class NetUtilLocalhost6LazyHolder {
        private static final Inet6Address LOCALHOST6 = NetUtilInitializations.createLocalhost6();
    }

    /** LOCALHOST 的 getter/setter 注入器。 */
    private static final class NetUtilLocalhostAccessor {
        static InetAddress get() {
            // using https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom
            return NetUtilLocalhostLazyHolder.LOCALHOST;
        }

        static void set(InetAddress ignored) {
            // a no-op setter to avoid exceptions when NetUtil is initialized at run-time
        }
    }

    /** LOCALHOST 的懒加载持有者，依赖网卡与 v4/v6 回环地址。 */
    private static final class NetUtilLocalhostLazyHolder {
        private static final InetAddress LOCALHOST = NetUtilInitializations
                .determineLoopback(NetUtilNetworkInterfacesLazyHolder.NETWORK_INTERFACES,
                        NetUtilLocalhost4LazyHolder.LOCALHOST4, NetUtilLocalhost6LazyHolder.LOCALHOST6)
                .address();
    }

    /** NETWORK_INTERFACES 的 getter/setter 注入器。 */
    private static final class NetUtilNetworkInterfacesAccessor {
        static Collection<NetworkInterface> get() {
            // using https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom
            return NetUtilNetworkInterfacesLazyHolder.NETWORK_INTERFACES;
        }

        static void set(Collection<NetworkInterface> ignored) {
            // a no-op setter to avoid exceptions when NetUtil is initialized at run-time
        }
    }

    /** 网卡列表的懒加载持有者。 */
    private static final class NetUtilNetworkInterfacesLazyHolder {
        private static final Collection<NetworkInterface> NETWORK_INTERFACES =
                NetUtilInitializations.networkInterfaces();
    }
}
