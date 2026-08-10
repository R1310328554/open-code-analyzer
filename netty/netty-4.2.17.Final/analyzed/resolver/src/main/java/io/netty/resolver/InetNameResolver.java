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
import io.netty.util.concurrent.Future;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * A skeletal {@link NameResolver} implementation that resolves {@link InetAddress}.
 * <p>解析 {@link InetAddress} 的 {@link NameResolver} 骨架基类，
 * 提供 {@link #asAddressResolver()} 将自身包装为 {@link InetSocketAddress} 解析器。</p>
 */
public abstract class InetNameResolver extends SimpleNameResolver<InetAddress> {
    /** 懒加载缓存的地址解析器，与当前 NameResolver 共享同一 executor。 */
    private volatile AddressResolver<InetSocketAddress> addressResolver;

    /**
     * @param executor the {@link EventExecutor} which is used to notify the listeners of the {@link Future} returned
     *                 by {@link #resolve(String)}
     */
    protected InetNameResolver(EventExecutor executor) {
        super(executor);
    }

    /**
     * Return a {@link AddressResolver} that will use this name resolver underneath.
     * It's cached internally, so the same instance is always returned.
     * <p>返回基于本解析器的 {@link AddressResolver}；双重检查锁定保证单例缓存。</p>
     */
    public AddressResolver<InetSocketAddress> asAddressResolver() {
        AddressResolver<InetSocketAddress> result = addressResolver;
        if (result == null) {
            synchronized (this) {
                result = addressResolver;
                if (result == null) {
                    addressResolver = result = new InetSocketAddressResolver(executor(), this);
                }
            }
        }
        return result;
    }
}
