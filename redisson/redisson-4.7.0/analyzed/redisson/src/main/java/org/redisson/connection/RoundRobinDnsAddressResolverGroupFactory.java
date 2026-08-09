/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.connection;

import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.resolver.dns.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 轮询 DNS 地址解析器组工厂。
 * <p>
 * 对同一主机名解析出的多个 IP 按轮询方式分配，适用于多 A 记录负载均衡场景。
 * 兼容 Netty 4.1.105+ 的 DNS TCP 回退特性。
 *
 * @author Nikita Koksharov
 * @author hasaadon
 *
 */
public class RoundRobinDnsAddressResolverGroupFactory implements AddressResolverGroupFactory {

    /** 工厂日志记录器。 */
    static final Logger log = LoggerFactory.getLogger(RoundRobinDnsAddressResolverGroupFactory.class);

    /** 构建带轮询策略的 {@link DnsAddressResolverGroup}。 */
    @Override
    public DnsAddressResolverGroup create(Class<? extends DatagramChannel> channelType,
                                          Class<? extends SocketChannel> socketChannelType,
                                          DnsServerAddressStreamProvider nameServerProvider) {
        DnsNameResolverBuilder dnsResolverBuilder = new DnsNameResolverBuilder();
        try {
            dnsResolverBuilder.getClass().getMethod("socketChannelType", Class.class, boolean.class);
            dnsResolverBuilder.socketChannelType(socketChannelType, true);
        } catch (NoSuchMethodException e) {
            // Netty 版本过低，无法启用 DNS UDP 超时后的 TCP 回退
            log.warn("DNS TCP fallback on UDP query timeout disabled. Upgrade Netty to 4.1.105 or higher.");
            dnsResolverBuilder.socketChannelType(socketChannelType);
        }
        dnsResolverBuilder.channelType(channelType)
                .nameServerProvider(nameServerProvider)
                .resolveCache(new DefaultDnsCache())
                .cnameCache(new DefaultDnsCnameCache());

        return new RoundRobinDnsAddressResolverGroup(dnsResolverBuilder);
    }
    
}
