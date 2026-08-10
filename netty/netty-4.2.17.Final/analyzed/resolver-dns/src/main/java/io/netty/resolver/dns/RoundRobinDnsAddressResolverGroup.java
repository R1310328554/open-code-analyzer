/*
 * Copyright 2016 The Netty Project
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
import io.netty.resolver.NameResolver;
import io.netty.resolver.RoundRobinInetAddressResolver;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * 支持对解析出的多个目标地址轮询选择的 {@link DnsNameResolver} {@link AddressResolverGroup}。
 * <p>适用于连接池等场景：若始终连向单一解析地址会导致负载不均。</p>
 */
public class RoundRobinDnsAddressResolverGroup extends DnsAddressResolverGroup {

    public RoundRobinDnsAddressResolverGroup(DnsNameResolverBuilder dnsResolverBuilder) {
        super(dnsResolverBuilder);
    }

    public RoundRobinDnsAddressResolverGroup(
            Class<? extends DatagramChannel> channelType,
            DnsServerAddressStreamProvider nameServerProvider) {
        super(channelType, nameServerProvider);
    }

    public RoundRobinDnsAddressResolverGroup(
            ChannelFactory<? extends DatagramChannel> channelFactory,
            DnsServerAddressStreamProvider nameServerProvider) {
        super(channelFactory, nameServerProvider);
    }

    /**
     * 必须重写本方法而非 {@link #newNameResolver(EventLoop, ChannelFactory, DnsServerAddressStreamProvider)}，
     * 以避免 {@link #newResolver(EventLoop, ChannelFactory, DnsServerAddressStreamProvider)} 中
     * {@link InflightNameResolver} 对 {@link NameResolver#resolve} 结果的缓存，从而保证轮询生效。
     */
    @Override
    protected final AddressResolver<InetSocketAddress> newAddressResolver(EventLoop eventLoop,
                                                                          NameResolver<InetAddress> resolver)
            throws Exception {
        return new RoundRobinInetAddressResolver(eventLoop, resolver).asAddressResolver();
    }
}
