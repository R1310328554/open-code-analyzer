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
import io.netty.util.concurrent.Promise;
import io.netty.util.internal.TypeParameterMatcher;

import java.net.SocketAddress;
import java.nio.channels.UnsupportedAddressTypeException;
import java.util.Collections;
import java.util.List;

import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * {@link AddressResolver} 的骨架实现，封装类型匹配与已解析地址的快速路径。
 */
public abstract class AbstractAddressResolver<T extends SocketAddress> implements AddressResolver<T> {

    /** 通知 Future 监听器的事件执行器 */
    private final EventExecutor executor;
    /** 地址类型匹配器 */
    private final TypeParameterMatcher matcher;

    /**
     * @param executor 用于通知 {@link #resolve(SocketAddress)} 返回的 {@link Future} 监听器的 {@link EventExecutor}
     */
    protected AbstractAddressResolver(EventExecutor executor) {
        this.executor = checkNotNull(executor, "executor");
        this.matcher = TypeParameterMatcher.find(this, AbstractAddressResolver.class, "T");
    }

    /**
     * @param executor 用于通知 {@link #resolve(SocketAddress)} 返回的 {@link Future} 监听器的 {@link EventExecutor}
     * @param addressType 本解析器支持的 {@link SocketAddress} 类型
     */
    protected AbstractAddressResolver(EventExecutor executor, Class<? extends T> addressType) {
        this.executor = checkNotNull(executor, "executor");
        this.matcher = TypeParameterMatcher.get(addressType);
    }

    /**
     * 返回用于通知 {@link #resolve(SocketAddress)} 返回的 {@link Future} 监听器的 {@link EventExecutor}。
     */
    protected EventExecutor executor() {
        return executor;
    }

    @Override
    public boolean isSupported(SocketAddress address) {
        return matcher.match(address);
    }

    @Override
    public final boolean isResolved(SocketAddress address) {
        if (!isSupported(address)) {
            throw new UnsupportedAddressTypeException();
        }

        @SuppressWarnings("unchecked")
        final T castAddress = (T) address;
        return doIsResolved(castAddress);
    }

    /**
     * 由 {@link #isResolved(SocketAddress)} 调用，检查指定 {@code address} 是否已解析。
     */
    protected abstract boolean doIsResolved(T address);

    @Override
    public final Future<T> resolve(SocketAddress address) {
        if (!isSupported(checkNotNull(address, "address"))) {
            // 地址类型不受支持
            return executor().newFailedFuture(new UnsupportedAddressTypeException());
        }

        if (isResolved(address)) {
            // 已解析，无需查询
            @SuppressWarnings("unchecked")
            final T cast = (T) address;
            return executor.newSucceededFuture(cast);
        }

        try {
            @SuppressWarnings("unchecked")
            final T cast = (T) address;
            final Promise<T> promise = executor().newPromise();
            doResolve(cast, promise);
            return promise;
        } catch (Exception e) {
            return executor().newFailedFuture(e);
        }
    }

    @Override
    public final Future<T> resolve(SocketAddress address, Promise<T> promise) {
        checkNotNull(address, "address");
        checkNotNull(promise, "promise");

        if (!isSupported(address)) {
            // 地址类型不受支持
            return promise.setFailure(new UnsupportedAddressTypeException());
        }

        if (isResolved(address)) {
            // 已解析，无需查询
            @SuppressWarnings("unchecked")
            final T cast = (T) address;
            return promise.setSuccess(cast);
        }

        try {
            @SuppressWarnings("unchecked")
            final T cast = (T) address;
            doResolve(cast, promise);
            return promise;
        } catch (Exception e) {
            return promise.setFailure(e);
        }
    }

    @Override
    public final Future<List<T>> resolveAll(SocketAddress address) {
        if (!isSupported(checkNotNull(address, "address"))) {
            // 地址类型不受支持
            return executor().newFailedFuture(new UnsupportedAddressTypeException());
        }

        if (isResolved(address)) {
            // 已解析，无需查询
            @SuppressWarnings("unchecked")
            final T cast = (T) address;
            return executor.newSucceededFuture(Collections.singletonList(cast));
        }

        try {
            @SuppressWarnings("unchecked")
            final T cast = (T) address;
            final Promise<List<T>> promise = executor().newPromise();
            doResolveAll(cast, promise);
            return promise;
        } catch (Exception e) {
            return executor().newFailedFuture(e);
        }
    }

    @Override
    public final Future<List<T>> resolveAll(SocketAddress address, Promise<List<T>> promise) {
        checkNotNull(address, "address");
        checkNotNull(promise, "promise");

        if (!isSupported(address)) {
            // 地址类型不受支持
            return promise.setFailure(new UnsupportedAddressTypeException());
        }

        if (isResolved(address)) {
            // 已解析，无需查询
            @SuppressWarnings("unchecked")
            final T cast = (T) address;
            return promise.setSuccess(Collections.singletonList(cast));
        }

        try {
            @SuppressWarnings("unchecked")
            final T cast = (T) address;
            doResolveAll(cast, promise);
            return promise;
        } catch (Exception e) {
            return promise.setFailure(e);
        }
    }

    /**
     * 由 {@link #resolve(SocketAddress)} 调用，执行实际的名称解析。
     */
    protected abstract void doResolve(T unresolvedAddress, Promise<T> promise) throws Exception;

    /**
     * 由 {@link #resolveAll(SocketAddress)} 调用，执行实际的名称解析并返回全部结果。
     */
    protected abstract void doResolveAll(T unresolvedAddress, Promise<List<T>> promise) throws Exception;

    @Override
    public void close() { }
}
