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

package io.netty.resolver;

import io.netty.util.concurrent.EventExecutor;

import java.net.SocketAddress;

/**
 * A {@link AddressResolverGroup} of {@link NoopAddressResolver}s.
 * <p>{@link NoopAddressResolver} 的解析器组单例，禁用 Netty 默认的地址解析行为。</p>
 */
public final class NoopAddressResolverGroup extends AddressResolverGroup<SocketAddress> {

    /** 全局单例，与 {@link DefaultAddressResolverGroup#INSTANCE} 相对。 */
    public static final NoopAddressResolverGroup INSTANCE = new NoopAddressResolverGroup();

    private NoopAddressResolverGroup() { }

    @Override
    protected AddressResolver<SocketAddress> newResolver(EventExecutor executor) throws Exception {
        return new NoopAddressResolver(executor);
    }
}
