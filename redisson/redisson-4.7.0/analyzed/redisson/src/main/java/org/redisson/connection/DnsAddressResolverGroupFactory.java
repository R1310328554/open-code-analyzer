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
 * 基于 Netty DNS 的 {@link AddressResolverGroupFactory} 默认实现。
 * <p>
 * 配置 DNS 缓存、CNAME 缓存及 TCP 回退（Netty 4.1.105+）。
 *
 * @author Nikita Koksharov
 * @author hasaadon
 *
 */
public class DnsAddressResolverGroupFactory implements AddressResolverGroupFactory {

    static final Logger log = LoggerFactory.getLogger(DnsAddressResolverGroupFactory.class);

    /** 创建 DnsAddressResolverGroup，兼容不同 Netty 版本的 socketChannelType 签名。 */
    @Override
    public DnsAddressResolverGroup create(Class<? extends DatagramChannel> channelType,
                                          Class<? extends SocketChannel> socketChannelType,
                                          DnsServerAddressStreamProvider nameServerProvider) {
        DnsNameResolverBuilder dnsResolverBuilder = new DnsNameResolverBuilder();
        try {
            dnsResolverBuilder.getClass().getMethod("socketChannelType", Class.class, boolean.class);
            dnsResolverBuilder.socketChannelType(socketChannelType, true);
        } catch (NoSuchMethodException e) {
            log.warn("DNS TCP fallback on UDP query timeout disabled. Upgrade Netty to 4.1.105 or higher.");  // UDP 超时后无法 TCP 回退，建议升级 Netty
            dnsResolverBuilder.socketChannelType(socketChannelType);
        }
        dnsResolverBuilder.channelType(channelType)
                .nameServerProvider(nameServerProvider)
                .resolveCache(new DefaultDnsCache())
                .cnameCache(new DefaultDnsCnameCache());

        return new DnsAddressResolverGroup(dnsResolverBuilder);
    }

}
