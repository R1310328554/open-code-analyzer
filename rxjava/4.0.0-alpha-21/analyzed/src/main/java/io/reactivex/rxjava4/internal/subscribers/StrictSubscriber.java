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

package io.reactivex.rxjava4.internal.subscribers;

import java.io.Serial;
import java.util.concurrent.atomic.*;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.FlowableSubscriber;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.internal.util.*;

/**
 * 严格 Reactive-Streams 合规包装：强制执行标准算子为性能省略的附加规则。
 * <ul>
 * <li>§1.3：onSubscribe 返回前不得并发 onNext</li>
 * <li>§2.3：onError/onComplete 内不得 cancel</li>
 * <li>§3.9：负 request 触发 onError(IllegalArgumentException)</li>
 * </ul>
 * 违反 §2.12（onSubscribe 至多一次）时 cancel 并 onError(IllegalStateException)。
 * @param <T> 元素类型
 * @since 2.0.7
 */
public class StrictSubscriber<T>
extends AtomicInteger
implements FlowableSubscriber<T>, Subscription {

    @Serial
    private static final long serialVersionUID = -4945028590049415624L;

    final Subscriber<? super T> downstream;

    final AtomicThrowable error;

    final AtomicLong requested;

    final AtomicReference<Subscription> upstream;

    final AtomicBoolean once;

    volatile boolean done;

    public StrictSubscriber(Subscriber<? super T> downstream) {
        this.downstream = downstream;
        this.error = new AtomicThrowable();
        this.requested = new AtomicLong();
        this.upstream = new AtomicReference<>();
        this.once = new AtomicBoolean();
    }

    /** n<=0 时 cancel 并 onError；否则 deferredRequest 转发。 */
    @Override
    public void request(long n) {
        if (n <= 0) {
            cancel();
            onError(new IllegalArgumentException("§3.9 violated: positive request amount required but it was " + n));
        } else {
            SubscriptionHelper.deferredRequest(upstream, requested, n);
        }
    }

    /** 未完成时 cancel 上游。 */
    @Override
    public void cancel() {
        if (!done) {
            SubscriptionHelper.cancel(upstream);
        }
    }

    /** once CAS 成功则 deferredSetOnce；重复 onSubscribe 违反 §2.12。 */
    @Override
    public void onSubscribe(Subscription s) {
        if (once.compareAndSet(false, true)) {

            downstream.onSubscribe(this);

            SubscriptionHelper.deferredSetOnce(this.upstream, requested, s);
        } else {
            s.cancel();
            cancel();
            onError(new IllegalStateException("§2.12 violated: onSubscribe must be called at most once"));
        }
    }

    /** 通过 HalfSerializer 串行化向下游 onNext。 */
    @Override
    public void onNext(T t) {
        HalfSerializer.onNext(downstream, t, this, error);
    }

    /** 置 done 后 HalfSerializer.onError（不调用 cancel）。 */
    @Override
    public void onError(Throwable t) {
        done = true;
        HalfSerializer.onError(downstream, t, this, error);
    }

    /** 置 done 后 HalfSerializer.onComplete。 */
    @Override
    public void onComplete() {
        done = true;
        HalfSerializer.onComplete(downstream, this, error);
    }
}
