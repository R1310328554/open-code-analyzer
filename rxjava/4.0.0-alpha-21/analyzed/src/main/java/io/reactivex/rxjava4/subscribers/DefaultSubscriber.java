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

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.FlowableSubscriber;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.internal.util.EndConsumerHelper;

/**
 * {@link java.util.concurrent.Flow.Subscriber} 抽象基类：支持同步 {@link #request(long)} 与
 * {@link #cancel()}，订阅建立时调用 {@link #onStart()}。
 *
 * <p>预置 final 方法线程安全；默认 onStart 请求 {@link Long#MAX_VALUE}。
 *
 * <p>onStart 内 request 可能异步触发 onNext，须先完成初始化。
 * onNext 内 request 安全（上游 onNext 非重入）。
 *
 * <p>仅允许单次订阅；回调不应抛出未检查异常，否则用 safeSubscribe。
 *
 * @param <T> 元素类型
 *
 * <p>Example<pre><code>
 * Flowable.range(1, 5)
 *     .subscribe(new DefaultSubscriber&lt;Integer&gt;() {
 *         &#64;Override public void onStart() {
 *             System.out.println("Start!");
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
 * </code></pre>
 */
public abstract class DefaultSubscriber<T> implements FlowableSubscriber<T> {

    Subscription upstream;

    /** validate 通过后保存 upstream 并调用 onStart()。 */
    @Override
    public final void onSubscribe(Subscription s) {
        if (EndConsumerHelper.validate(this.upstream, s, getClass())) {
            this.upstream = s;
            onStart();
        }
    }

    /**
     * 向上游 {@link Subscription} 请求元素。
     * @param n 请求数量，须为正
     */
    protected final void request(long n) {
        Subscription s = this.upstream;
        if (s != null) {
            s.request(n);
        }
    }

    /** 取消上游 {@link Subscription} 并将 upstream 置 CANCELLED。 */
    protected final void cancel() {
        Subscription s = this.upstream;
        this.upstream = SubscriptionHelper.CANCELLED;
        s.cancel();
    }
    /**
     * 订阅建立后回调，可覆写做初始化或发出初始 request。
     * <p>默认请求 {@link Long#MAX_VALUE}。
     */
    protected void onStart() {
        request(Long.MAX_VALUE);
    }

}
