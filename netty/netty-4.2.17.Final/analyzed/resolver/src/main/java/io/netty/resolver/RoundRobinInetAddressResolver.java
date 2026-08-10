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
package io.netty.resolver;

import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A {@link NameResolver} that resolves {@link InetAddress} and force Round Robin by choosing a single address
 * randomly in {@link #resolve(String)} and {@link #resolve(String, Promise)}
 * if multiple are returned by the {@link NameResolver}.
 * Use {@link #asAddressResolver()} to create a {@link InetSocketAddress} resolver
 * <p>包装底层 {@link NameResolver}，在 {@link #resolve} 时从多地址中随机选取一条实现客户端负载均衡；
 * {@link #resolveAll} 则随机旋转列表顺序以分散连接分布。
 * 可通过 {@link #asAddressResolver()} 获得 {@link InetSocketAddress} 解析器。</p>
 */
public class RoundRobinInetAddressResolver extends InetNameResolver {
    private final NameResolver<InetAddress> nameResolver;

    /**
     * @param executor the {@link EventExecutor} which is used to notify the listeners of the {@link Future} returned by
     * {@link #resolve(String)}
     * @param nameResolver the {@link NameResolver} used for name resolution
     */
    public RoundRobinInetAddressResolver(EventExecutor executor, NameResolver<InetAddress> nameResolver) {
        super(executor);
        this.nameResolver = nameResolver;
    }

    @Override
    protected void doResolve(final String inetHost, final Promise<InetAddress> promise) throws Exception {
        // 对外暴露 resolve，内部调用 resolveAll 再随机挑选一个地址
        nameResolver.resolveAll(inetHost).addListener((FutureListener<List<InetAddress>>) future -> {
            if (future.isSuccess()) {
                List<InetAddress> inetAddresses = future.getNow();
                int numAddresses = inetAddresses.size();
                if (numAddresses > 0) {
                    // 多地址时随机索引，实现轮询式客户端负载均衡
                    promise.setSuccess(inetAddresses.get(randomIndex(numAddresses)));
                } else {
                    promise.setFailure(new UnknownHostException(inetHost));
                }
            } else {
                promise.setFailure(future.cause());
            }
        });
    }

    @Override
    protected void doResolveAll(String inetHost, final Promise<List<InetAddress>> promise) throws Exception {
        nameResolver.resolveAll(inetHost).addListener((FutureListener<List<InetAddress>>) future -> {
            if (future.isSuccess()) {
                List<InetAddress> inetAddresses = future.getNow();
                if (!inetAddresses.isEmpty()) {
                    // 复制为可变列表，每次随机旋转不同步长以打散顺序
                    List<InetAddress> result = new ArrayList<InetAddress>(inetAddresses);
                    Collections.rotate(result, randomIndex(inetAddresses.size()));
                    promise.setSuccess(result);
                } else {
                    promise.setSuccess(inetAddresses);
                }
            } else {
                promise.setFailure(future.cause());
            }
        });
    }

    /** 单地址时固定返回 0，多地址时使用 ThreadLocalRandom 选取索引。 */
    private static int randomIndex(int numAddresses) {
        return numAddresses == 1 ? 0 : ThreadLocalRandom.current().nextInt(numAddresses);
    }

    @Override
    public void close() {
        nameResolver.close();
    }
}
