/*
 * Copyright 2015 The Netty Project
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

package io.netty.resolver;

import io.netty.util.concurrent.EventExecutor;

import java.net.InetSocketAddress;

/**
 * A {@link AddressResolverGroup} of {@link DefaultNameResolver}s.
 * <p>基于 {@link DefaultNameResolver} 的默认 {@link AddressResolverGroup} 单例，
 * 为每个 {@link EventExecutor} 创建使用 JDK 域名解析的 {@link InetSocketAddress} 解析器。</p>
 */
public final class DefaultAddressResolverGroup extends AddressResolverGroup<InetSocketAddress> {

    /** 全局共享实例，Bootstrap 等组件默认使用此解析器组。 */
    public static final DefaultAddressResolverGroup INSTANCE = new DefaultAddressResolverGroup();

    private DefaultAddressResolverGroup() { }

    @Override
    protected AddressResolver<InetSocketAddress> newResolver(EventExecutor executor) throws Exception {
        // 将 InetNameResolver 包装为 AddressResolver，供 Channel 连接前解析未解析地址
        return new DefaultNameResolver(executor).asAddressResolver();
    }
}
