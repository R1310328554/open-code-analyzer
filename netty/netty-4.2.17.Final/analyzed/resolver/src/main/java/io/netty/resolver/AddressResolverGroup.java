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
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.io.Closeable;
import java.net.SocketAddress;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * 创建并管理 {@link AddressResolver}，使每个 {@link EventExecutor} 拥有独立的解析器实例。
 */
public abstract class AddressResolverGroup<T extends SocketAddress> implements Closeable {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(AddressResolverGroup.class);

    /**
     * 此处不使用 {@link ConcurrentMap}，因为实例化解析器通常开销较大。
     */
    private final Map<EventExecutor, AddressResolver<T>> resolvers =
            new IdentityHashMap<EventExecutor, AddressResolver<T>>();

    /** EventExecutor 终止时清理对应解析器的监听器 */
    private final Map<EventExecutor, GenericFutureListener<Future<Object>>> executorTerminationListeners =
            new IdentityHashMap<EventExecutor, GenericFutureListener<Future<Object>>>();

    protected AddressResolverGroup() { }

    /**
     * 返回与指定 {@link EventExecutor} 关联的 {@link AddressResolver}。
     * 若尚未创建，则通过 {@link #newResolver(EventExecutor)} 新建并在后续调用中复用。
     */
    public AddressResolver<T> getResolver(final EventExecutor executor) {
        ObjectUtil.checkNotNull(executor, "executor");

        if (executor.isShuttingDown()) {
            throw new IllegalStateException("executor not accepting a task");
        }

        AddressResolver<T> r;
        synchronized (resolvers) {
            r = resolvers.get(executor);
            if (r == null) {
                final AddressResolver<T> newResolver;
                try {
                    newResolver = newResolver(executor);
                } catch (Exception e) {
                    throw new IllegalStateException("failed to create a new resolver", e);
                }

                resolvers.put(executor, newResolver);

                final FutureListener<Object> terminationListener = future -> {
                    synchronized (resolvers) {
                        resolvers.remove(executor);
                        executorTerminationListeners.remove(executor);
                    }
                    newResolver.close();
                };

                executorTerminationListeners.put(executor, terminationListener);
                executor.terminationFuture().addListener(terminationListener);

                r = newResolver;
            }
        }

        return r;
    }

    /**
     * 由 {@link #getResolver(EventExecutor)} 调用，创建新的 {@link AddressResolver}。
     */
    protected abstract AddressResolver<T> newResolver(EventExecutor executor) throws Exception;

    /**
     * 关闭本组创建的全部 {@link AddressResolver}。
     */
    @Override
    @SuppressWarnings({ "unchecked", "SuspiciousToArrayCall" })
    public void close() {
        final AddressResolver<T>[] rArray;
        final Map.Entry<EventExecutor, GenericFutureListener<Future<Object>>>[] listeners;

        synchronized (resolvers) {
            rArray = (AddressResolver<T>[]) resolvers.values().toArray(new AddressResolver[0]);
            resolvers.clear();
            listeners = executorTerminationListeners.entrySet().toArray(new Map.Entry[0]);
            executorTerminationListeners.clear();
        }

        for (final Map.Entry<EventExecutor, GenericFutureListener<Future<Object>>> entry : listeners) {
            entry.getKey().terminationFuture().removeListener(entry.getValue());
        }

        for (final AddressResolver<T> r: rArray) {
            try {
                r.close();
            } catch (Throwable t) {
                logger.warn("Failed to close a resolver:", t);
            }
        }
    }
}
