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

import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.FlowableSubscriber;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.internal.util.EndConsumerHelper;

/**
 * 实现 {@link Disposable} 的 Subscriber 抽象基类，支持异步外部取消。
 *
 * <p>final 方法线程安全；默认 onStart 无界 request。
 * 在 onNext 中可用 {@link #request(long)} 与 {@link #cancel()}。
 *
 * <p>仅允许单次订阅；回调不应抛出未检查异常。

 *
 * <p>Example<pre><code>
 * Disposable d =
 *     Flowable.range(1, 5)
 *     .subscribeWith(new DisposableSubscriber&lt;Integer&gt;() {
 *         &#64;Override public void onStart() {
 *             request(1);
 *         }
 *         &#64;Override public void onNext(Integer t) {
 *             if (t == 3) {
 *                 cancel();
 *             }
 *             System.out.println(t);
 *             request(1);
 *         }
 *         &#64;Override public void onError(Throwable t) {
 *             t.printStackTrace();
 *         }
 *         &#64;Override public void onComplete() {
 *             System.out.println("Done!");
 *         }
 *     });
 * // ...
 * d.dispose();
 * </code></pre>
 *
 * @param <T> 接收的元素类型
 */
public abstract class DisposableSubscriber<T> implements FlowableSubscriber<T>, Disposable {
    final AtomicReference<Subscription> upstream = new AtomicReference<>();

    /** setOnce 成功后调用 onStart()。 */
    @Override
    public final void onSubscribe(Subscription s) {
        if (EndConsumerHelper.setOnce(this.upstream, s, getClass())) {
            onStart();
        }
    }

    /** 上游 {@link Subscription} 通过 onSubscribe 设置成功后调用。 */
    protected void onStart() {
        upstream.get().request(Long.MAX_VALUE);
    }

    /**
     * 向上游请求指定数量（须已 onSubscribe）。
     * <p>onSubscribe 前调用会 NPE；应在 onStart 或 onNext 内调用。
     * @param n 请求数量，须为正
     */
    protected final void request(long n) {
        upstream.get().request(n);
    }

    /** 取消订阅；线程安全，等价于 dispose()。 */
    protected final void cancel() {
        dispose();
    }

    /** upstream 是否为 CANCELLED。 */
    @Override
    public final boolean isDisposed() {
        return upstream.get() == SubscriptionHelper.CANCELLED;
    }

    /** SubscriptionHelper.cancel(upstream)。 */
    @Override
    public final void dispose() {
        SubscriptionHelper.cancel(upstream);
    }
}
