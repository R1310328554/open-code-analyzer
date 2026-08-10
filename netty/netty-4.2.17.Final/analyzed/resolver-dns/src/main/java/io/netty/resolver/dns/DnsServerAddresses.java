/*
 * Copyright 2014 The Netty Project
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

import static io.netty.util.internal.ObjectUtil.checkNotNull;
import static io.netty.util.internal.ObjectUtil.checkNonEmpty;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 为 {@link DnsNameResolver} 提供无限循环的 DNS 服务器地址序列。
 */
@SuppressWarnings("IteratorNextCanNotThrowNoSuchElementException")
public abstract class DnsServerAddresses {
    /**
     * @deprecated Use {@link DefaultDnsServerAddressStreamProvider#defaultAddressList()}.
     * <p>
     * 返回系统 DNS 服务器地址列表；若读取失败则回退到 Google 公共 DNS（8.8.8.8、8.8.4.4）。
     */
    @Deprecated
    public static List<InetSocketAddress> defaultAddressList() {
        return DefaultDnsServerAddressStreamProvider.defaultAddressList();
    }

    /**
     * @deprecated Use {@link DefaultDnsServerAddressStreamProvider#defaultAddresses()}.
     * <p>
     * 按顺序返回系统 DNS 服务器地址；读取失败时使用 Google 公共 DNS。
     * <p>
     * 等价于：
     * <pre>
     * DnsServerAddresses.sequential(DnsServerAddresses.defaultAddressList());
     * </pre>
     * </p>
     */
    @Deprecated
    public static DnsServerAddresses defaultAddresses() {
        return DefaultDnsServerAddressStreamProvider.defaultAddresses();
    }

    /**
     * 按顺序循环返回指定 {@code addresses}；到达末尾后从首个地址重新开始。
     */
    public static DnsServerAddresses sequential(Iterable<? extends InetSocketAddress> addresses) {
        return sequential0(sanitize(addresses));
    }

    /**
     * 按顺序循环返回指定 {@code addresses}；到达末尾后从首个地址重新开始。
     */
    public static DnsServerAddresses sequential(InetSocketAddress... addresses) {
        return sequential0(sanitize(addresses));
    }

    private static DnsServerAddresses sequential0(final List<InetSocketAddress> addresses) {
        if (addresses.size() == 1) {
            return singleton(addresses.get(0));
        }

        return new DefaultDnsServerAddresses("sequential", addresses) {
            @Override
            public DnsServerAddressStream stream() {
                return new SequentialDnsServerAddressStream(addresses, 0);
            }
        };
    }

    /**
     * 以随机顺序循环返回指定 {@code addresses}；一轮耗尽后重新洗牌。
     */
    public static DnsServerAddresses shuffled(Iterable<? extends InetSocketAddress> addresses) {
        return shuffled0(sanitize(addresses));
    }

    /**
     * 以随机顺序循环返回指定 {@code addresses}；一轮耗尽后重新洗牌。
     */
    public static DnsServerAddresses shuffled(InetSocketAddress... addresses) {
        return shuffled0(sanitize(addresses));
    }

    private static DnsServerAddresses shuffled0(List<InetSocketAddress> addresses) {
        if (addresses.size() == 1) {
            return singleton(addresses.get(0));
        }

        return new DefaultDnsServerAddresses("shuffled", addresses) {
            @Override
            public DnsServerAddressStream stream() {
                return new ShuffledDnsServerAddressStream(addresses);
            }
        };
    }

    /**
     * 以轮转顺序返回 {@code addresses}：类似 {@link #sequential(Iterable)}，但每次 {@link #stream()}
     * 的起始位置不同（第一次从首地址、第二次从次地址，依此类推）。
     */
    public static DnsServerAddresses rotational(Iterable<? extends InetSocketAddress> addresses) {
        return rotational0(sanitize(addresses));
    }

    /**
     * 以轮转顺序返回 {@code addresses}：类似 {@link #sequential(Iterable)}，但每次 {@link #stream()}
     * 的起始位置不同。
     */
    public static DnsServerAddresses rotational(InetSocketAddress... addresses) {
        return rotational0(sanitize(addresses));
    }

    private static DnsServerAddresses rotational0(List<InetSocketAddress> addresses) {
        if (addresses.size() == 1) {
            return singleton(addresses.get(0));
        }

        return new RotationalDnsServerAddresses(addresses);
    }

    /**
     * 仅包含单个 {@code address} 的 {@link DnsServerAddresses}。
     */
    public static DnsServerAddresses singleton(final InetSocketAddress address) {
        checkNotNull(address, "address");
        if (address.isUnresolved()) {
            throw new IllegalArgumentException("cannot use an unresolved DNS server address: " + address);
        }

        return new SingletonDnsServerAddresses(address);
    }

    private static List<InetSocketAddress> sanitize(Iterable<? extends InetSocketAddress> addresses) {
        checkNotNull(addresses, "addresses");

        final List<InetSocketAddress> list;
        if (addresses instanceof Collection) {
            list = new ArrayList<InetSocketAddress>(((Collection<?>) addresses).size());
        } else {
            list = new ArrayList<InetSocketAddress>(4);
        }

        for (InetSocketAddress a : addresses) {
            if (a == null) {
                break;
            }
            if (a.isUnresolved()) {
                throw new IllegalArgumentException("cannot use an unresolved DNS server address: " + a);
            }
            list.add(a);
        }

        return checkNonEmpty(list, "list");
    }

    private static List<InetSocketAddress> sanitize(InetSocketAddress[] addresses) {
        checkNotNull(addresses, "addresses");

        List<InetSocketAddress> list = new ArrayList<InetSocketAddress>(addresses.length);
        for (InetSocketAddress a: addresses) {
            if (a == null) {
                break;
            }
            if (a.isUnresolved()) {
                throw new IllegalArgumentException("cannot use an unresolved DNS server address: " + a);
            }
            list.add(a);
        }

        if (list.isEmpty()) {
            return DefaultDnsServerAddressStreamProvider.defaultAddressList();
        }

        return list;
    }

    /**
     * 启动新的无限 DNS 服务器地址流；每次未缓存的 {@link DnsNameResolver#resolve(String)} 或
     * {@link DnsNameResolver#resolveAll(String)} 时由 {@link DnsNameResolver} 调用。
     */
    public abstract DnsServerAddressStream stream();
}
