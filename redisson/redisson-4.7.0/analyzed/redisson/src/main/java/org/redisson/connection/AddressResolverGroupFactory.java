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
import io.netty.resolver.AddressResolverGroup;
import io.netty.resolver.dns.DnsServerAddressStreamProvider;

import java.net.InetSocketAddress;

/**
 * 地址解析器组工厂接口，用于创建 Netty {@link AddressResolverGroup}。
 * <p>
 * 默认实现为 {@link DnsAddressResolverGroupFactory}；
 * 可替换以支持自定义 DNS 或静态地址映射。
 *
 * @author hasaadon
 */
public interface AddressResolverGroupFactory {

    /**
     * 创建 {@link InetSocketAddress} 地址解析器组。
     *
     * @param channelType UDP 通道类型（DNS 查询）
     * @param socketChannelType TCP 通道类型（DNS TCP 回退）
     * @param nameServerProvider DNS 服务器地址流提供者
     * @return 地址解析器组实例
     */

}
