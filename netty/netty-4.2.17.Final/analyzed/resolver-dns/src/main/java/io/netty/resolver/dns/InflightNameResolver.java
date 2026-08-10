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

import io.netty.resolver.NameResolver;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;
import io.netty.util.internal.StringUtil;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

import static io.netty.util.internal.ObjectUtil.checkNotNull;

// FIXME(trustin): Find a better name and move it to the 'resolver' module.
/**
 * 合并同一主机名并发解析请求的 {@link NameResolver} 装饰器。
 * <p>若某主机名已在解析中，后续请求复用同一 {@link Promise}，避免重复 DNS 查询。</p>
 */
final class InflightNameResolver<T> implements NameResolver<T> {

    /** 执行 resolve 回调的 EventExecutor。 */
    private final EventExecutor executor;
    /** 被装饰的实际解析器。 */
    private final NameResolver<T> delegate;
    /** 进行中的单地址 resolve，键为主机名。 */
    private final ConcurrentMap<String, Promise<T>> resolvesInProgress;
    /** 进行中的 resolveAll，键为主机名。 */
    private final ConcurrentMap<String, Promise<List<T>>> resolveAllsInProgress;

    InflightNameResolver(EventExecutor executor, NameResolver<T> delegate,
                         ConcurrentMap<String, Promise<T>> resolvesInProgress,
                         ConcurrentMap<String, Promise<List<T>>> resolveAllsInProgress) {

        this.executor = checkNotNull(executor, "executor");
        this.delegate = checkNotNull(delegate, "delegate");
        this.resolvesInProgress = checkNotNull(resolvesInProgress, "resolvesInProgress");
        this.resolveAllsInProgress = checkNotNull(resolveAllsInProgress, "resolveAllsInProgress");
    }

    @Override
    public Future<T> resolve(String inetHost) {
        return resolve(inetHost, executor.<T>newPromise());
    }

    @Override
    public Future<List<T>> resolveAll(String inetHost) {
        return resolveAll(inetHost, executor.<List<T>>newPromise());
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public Promise<T> resolve(String inetHost, Promise<T> promise) {
        return resolve(resolvesInProgress, inetHost, promise, false);
    }

    @Override
    public Promise<List<T>> resolveAll(String inetHost, Promise<List<T>> promise) {
        return resolve(resolveAllsInProgress, inetHost, promise, true);
    }

    private <U> Promise<U> resolve(
            final ConcurrentMap<String, Promise<U>> resolveMap,
            final String inetHost, final Promise<U> promise, boolean resolveAll) {

        final Promise<U> earlyPromise = resolveMap.putIfAbsent(inetHost, promise);
        if (earlyPromise != null) {
            // 该主机名已有解析在进行，复用已有 Promise
            if (earlyPromise.isDone()) {
                transferResult(earlyPromise, promise);
            } else {
                earlyPromise.addListener((FutureListener<U>) f -> transferResult(f, promise));
            }
        } else {
            try {
                if (resolveAll) {
                    @SuppressWarnings("unchecked")
                    final Promise<List<T>> castPromise = (Promise<List<T>>) promise; // U is List<T>
                    delegate.resolveAll(inetHost, castPromise);
                } else {
                    @SuppressWarnings("unchecked")
                    final Promise<T> castPromise = (Promise<T>) promise; // U is T
                    delegate.resolve(inetHost, castPromise);
                }
            } finally {
                if (promise.isDone()) {
                    resolveMap.remove(inetHost);
                } else {
                    promise.addListener((FutureListener<U>) f -> resolveMap.remove(inetHost));
                }
            }
        }

        return promise;
    }

    private static <T> void transferResult(Future<T> src, Promise<T> dst) {
        if (src.isSuccess()) {
            dst.trySuccess(src.getNow());
        } else {
            dst.tryFailure(src.cause());
        }
    }

    @Override
    public String toString() {
        return StringUtil.simpleClassName(this) + '(' + delegate + ')';
    }
}
