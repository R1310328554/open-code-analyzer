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

package io.netty.util.concurrent;

import io.netty.util.internal.ObjectUtil;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @deprecated Use {@link PromiseCombiner#PromiseCombiner(EventExecutor)}.
 *
 * {@link GenericFutureListener} implementation which consolidates multiple {@link Future}s
 * into one, by listening to individual {@link Future}s and producing an aggregated result
 * (success/failure) when all {@link Future}s have completed.
 *
 * @param <V> the type of value returned by the {@link Future}
 * @param <F> the type of {@link Future}
 *
 * <p>已废弃：请改用 {@link PromiseCombiner}。监听多个 {@link Promise}，全部成功时通知聚合 Promise 成功，
 * 任一失败则聚合 Promise 失败；可选是否将失败原因传播给尚未完成的 Promise。</p>
 */
@Deprecated
public class PromiseAggregator<V, F extends Future<V>> implements GenericFutureListener<F> {

    /** 全部子 Promise 完成时要通知的聚合 Promise。 */
    private final Promise<?> aggregatePromise;
    /** 子 Promise 失败时是否将其余 pending 的 Promise 也标记为失败。 */
    private final boolean failPending;
    /** 尚未完成的 Promise 集合；全部完成或失败处理后清空逻辑由 operationComplete 驱动。 */
    private Set<Promise<V>> pendingPromises;

    /**
     * Creates a new instance.
     *
     * @param aggregatePromise  the {@link Promise} to notify
     * @param failPending  {@code true} to fail pending promises, false to leave them unaffected
     *
     * <p>{@code failPending} 为 {@code true} 时，任一子 Promise 失败会将失败原因同步给其余 pending 项。</p>
     */
    public PromiseAggregator(Promise<Void> aggregatePromise, boolean failPending) {
        this.aggregatePromise = ObjectUtil.checkNotNull(aggregatePromise, "aggregatePromise");
        this.failPending = failPending;
    }

    /**
     * See {@link PromiseAggregator#PromiseAggregator(Promise, boolean)}.
     * Defaults {@code failPending} to true.
     *
     * <p>默认 {@code failPending = true}。</p>
     */
    public PromiseAggregator(Promise<Void> aggregatePromise) {
        this(aggregatePromise, true);
    }

    /**
     * Add the given {@link Promise}s to the aggregator.
     *
     * <p>注册要聚合的 Promise 并为其添加本监听器；可链式调用多次 add。</p>
     */
    @SafeVarargs
    public final PromiseAggregator<V, F> add(Promise<V>... promises) {
        ObjectUtil.checkNotNull(promises, "promises");
        if (promises.length == 0) {
            return this;
        }
        synchronized (this) {
            if (pendingPromises == null) {
                int size;
                if (promises.length > 1) {
                    size = promises.length;
                } else {
                    size = 2;
                }
                pendingPromises = new LinkedHashSet<Promise<V>>(size);
            }
            for (Promise<V> p : promises) {
                if (p == null) {
                    continue;
                }
                pendingPromises.add(p);
                p.addListener(this);
            }
        }
        return this;
    }

    @Override
    public synchronized void operationComplete(F future) throws Exception {
        if (pendingPromises == null) {
            // 未 add 任何 Promise，直接成功
            aggregatePromise.setSuccess(null);
        } else {
            pendingPromises.remove(future);
            if (!future.isSuccess()) {
                Throwable cause = future.cause();
                aggregatePromise.setFailure(cause);
                if (failPending) {
                    // 将同一失败原因传播给其余尚未完成的 Promise
                    for (Promise<V> pendingFuture : pendingPromises) {
                        pendingFuture.setFailure(cause);
                    }
                }
            } else {
                if (pendingPromises.isEmpty()) {
                    // 最后一个成功，聚合完成
                    aggregatePromise.setSuccess(null);
                }
            }
        }
    }

}
