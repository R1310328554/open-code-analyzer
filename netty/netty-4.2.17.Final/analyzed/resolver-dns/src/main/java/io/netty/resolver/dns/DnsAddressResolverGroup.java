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

import io.netty.channel.ChannelFactory;
import io.netty.channel.EventLoop;
import io.netty.channel.socket.DatagramChannel;
import io.netty.resolver.AddressResolver;
import io.netty.resolver.AddressResolverGroup;
import io.netty.resolver.InetSocketAddressResolver;
import io.netty.resolver.NameResolver;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import io.netty.util.internal.StringUtil;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 基于 {@link DnsNameResolver} 的 {@link AddressResolverGroup}{@code <InetSocketAddress>}。
 * <p>组内各 {@link EventLoop} 共享同一套 DNS 解析/CNAME/权威服务器缓存，并对进行中的
 * resolve/resolveAll 请求去重（{@link InflightNameResolver}）。</p>
 */
public class DnsAddressResolverGroup extends AddressResolverGroup<InetSocketAddress> {

    /** 构建 {@link DnsNameResolver} 的配置模板（含共享缓存）。 */
    private final DnsNameResolverBuilder dnsResolverBuilder;

    /** 按主机名合并进行中的单次 resolve。 */
    private final ConcurrentMap<String, Promise<InetAddress>> resolvesInProgress = new ConcurrentHashMap<>();
    /** 按主机名合并进行中的 resolveAll。 */
    private final ConcurrentMap<String, Promise<List<InetAddress>>> resolveAllsInProgress = new ConcurrentHashMap<>();

    public DnsAddressResolverGroup(DnsNameResolverBuilder dnsResolverBuilder) {
        this.dnsResolverBuilder = withSharedCaches(dnsResolverBuilder.copy());
    }

    public DnsAddressResolverGroup(
            Class<? extends DatagramChannel> channelType,
            DnsServerAddressStreamProvider nameServerProvider) {
        this.dnsResolverBuilder = withSharedCaches(new DnsNameResolverBuilder());
        dnsResolverBuilder.datagramChannelType(channelType).nameServerProvider(nameServerProvider);
    }

    public DnsAddressResolverGroup(
            ChannelFactory<? extends DatagramChannel> channelFactory,
            DnsServerAddressStreamProvider nameServerProvider) {
        this.dnsResolverBuilder = withSharedCaches(new DnsNameResolverBuilder());
        dnsResolverBuilder.datagramChannelFactory(channelFactory).nameServerProvider(nameServerProvider);
    }

    /**
     * 确保组内成员共享同一套缓存实例，避免每个 EventLoop 独立缓存导致命中率下降。
     */
    private static DnsNameResolverBuilder withSharedCaches(DnsNameResolverBuilder dnsResolverBuilder) {
        return dnsResolverBuilder.resolveCache(dnsResolverBuilder.getOrNewCache())
                .cnameCache(dnsResolverBuilder.getOrNewCnameCache())
                .authoritativeDnsServerCache(dnsResolverBuilder.getOrNewAuthoritativeDnsServerCache());
    }

    @SuppressWarnings("deprecation")
    @Override
    protected final AddressResolver<InetSocketAddress> newResolver(EventExecutor executor) throws Exception {
        if (!(executor instanceof EventLoop)) {
            throw new IllegalStateException(
                    "unsupported executor type: " + StringUtil.simpleClassName(executor) +
                    " (expected: " + StringUtil.simpleClassName(EventLoop.class));
        }

        // 为兼容可能被覆写的 deprecated 方法，仍分别传递 factory 与 nameServerProvider。
        EventLoop loop = dnsResolverBuilder.eventLoop;
        return newResolver(loop == null ? (EventLoop) executor : loop,
                dnsResolverBuilder.datagramChannelFactory(),
                dnsResolverBuilder.nameServerProvider());
    }

    /**
     * @deprecated Override {@link #newNameResolver(EventLoop, ChannelFactory, DnsServerAddressStreamProvider)}.
     */
    @Deprecated
    protected AddressResolver<InetSocketAddress> newResolver(
            EventLoop eventLoop, ChannelFactory<? extends DatagramChannel> channelFactory,
            DnsServerAddressStreamProvider nameServerProvider) throws Exception {

        final NameResolver<InetAddress> resolver = new InflightNameResolver<InetAddress>(
                eventLoop,
                newNameResolver(eventLoop, channelFactory, nameServerProvider),
                resolvesInProgress,
                resolveAllsInProgress);

        return newAddressResolver(eventLoop, resolver);
    }

    /**
     * 创建 {@link NameResolver}。子类可覆盖以替换实现或调整默认配置。
     */
    protected NameResolver<InetAddress> newNameResolver(EventLoop eventLoop,
                                                        ChannelFactory<? extends DatagramChannel> channelFactory,
                                                        DnsServerAddressStreamProvider nameServerProvider)
            throws Exception {
        DnsNameResolverBuilder builder = dnsResolverBuilder.copy();

        // builder 中通常已设置 channelFactory 与 nameServerProvider，再次赋值以防子类覆写遗漏。
        return builder.eventLoop(eventLoop)
                .datagramChannelFactory(channelFactory)
                .nameServerProvider(nameServerProvider)
                .build();
    }

    /**
     * 创建 {@link AddressResolver}。子类可覆盖以替换实现或调整默认配置。
     */
    protected AddressResolver<InetSocketAddress> newAddressResolver(EventLoop eventLoop,
                                                                    NameResolver<InetAddress> resolver)
            throws Exception {
        return new InetSocketAddressResolver(eventLoop, resolver);
    }
}
