/*
 * Copyright (c) 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package io.reactivex.rxjava4.subscribers;

import java.util.Objects;
import java.util.concurrent.atomic.*;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.FlowableSubscriber;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.ListCompositeDisposable;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.internal.util.EndConsumerHelper;

/**
 * 支持异步取消订阅并管理关联资源的 Subscriber 抽象基类。
 *
 * <p>在 onError/onComplete 中应显式 {@link #dispose()}；
 * 通过 {@link #add(Disposable)} 关联资源，dispose 时一并清理。
 *
 * <p>默认 onStart 无界 request；request 支持 onSubscribe 前 deferred 累积。
 *
 * <p>仅允许单次订阅；回调不应抛出未检查异常。

 *
 * <p>Example<pre><code>
 * Disposable d =
 *     Flowable.range(1, 5)
 *     .subscribeWith(new ResourceSubscriber&lt;Integer&gt;() {
 *         &#64;Override public void onStart() {
 *             add(Schedulers.single()
 *                 .scheduleDirect(() -&gt; System.out.println("Time!"),
 *                     2, TimeUnit.SECONDS));
 *             request(1);
 *         }
 *         &#64;Override public void onNext(Integer t) {
 *             if (t == 3) {
 *                 dispose();
 *             }
 *             System.out.println(t);
 *             request(1);
 *         }
 *         &#64;Override public void onError(Throwable t) {
 *             t.printStackTrace();
 *             dispose();
 *         }
 *         &#64;Override public void onComplete() {
 *             System.out.println("Done!");
 *             dispose();
 *         }
 *     });
 * // ...
 * d.dispose();
 * </code></pre>
 *
 *
 * @param <T> 元素类型
 */
public abstract class ResourceSubscriber<T> implements FlowableSubscriber<T>, Disposable {
    /** 当前活跃的上游订阅。 */
    private final AtomicReference<Subscription> upstream = new AtomicReference<>();

    /** 资源复合容器，永不为 null。 */
    private final ListCompositeDisposable resources = new ListCompositeDisposable();

    /** 在 Subscription 到达前缓存 request(n) 计数。 */
    private final AtomicLong missedRequested = new AtomicLong();

    /**
     * 向本 {@code ResourceSubscriber} 添加资源。
     * @param resource 要添加的资源
     * @throws NullPointerException 若 resource 为 null
     */
    public final void add(Disposable resource) {
        Objects.requireNonNull(resource, "resource is null");
        resources.add(resource);
    }

    /** setOnce 后 flush missedRequested 并 onStart()。 */
    @Override
    public final void onSubscribe(Subscription s) {
        if (EndConsumerHelper.setOnce(this.upstream, s, getClass())) {
            long r = missedRequested.getAndSet(0L);
            if (r != 0L) {
                s.request(r);
            }
            onStart();
        }
    }

    /**
     * 上游设置 Subscription 后调用；默认 request(Long.MAX_VALUE)。
     */
    protected void onStart() {
        request(Long.MAX_VALUE);
    }

    /**
     * 向上游请求元素；可在 onSubscribe 前调用，订阅时一并发出。
     * @param n 请求数量，须为正
     */
    protected final void request(long n) {
        SubscriptionHelper.deferredRequest(upstream, missedRequested, n);
    }

    /** 取消订阅并 dispose 关联资源；可在 onSubscribe 前调用。 */
    @Override
    public final void dispose() {
        if (SubscriptionHelper.cancel(upstream)) {
            resources.dispose();
        }
    }

    /**
     * 是否已 dispose/取消。
     * @return 已 dispose/取消则为 true
     */
    @Override
    public final boolean isDisposed() {
        return upstream.get() == SubscriptionHelper.CANCELLED;
    }
}
