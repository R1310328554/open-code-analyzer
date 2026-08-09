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

package io.reactivex.rxjava4.internal.operators.flowable;

import io.reactivex.rxjava4.core.FlowableSubscriber;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.internal.util.BackpressureHelper;
import static java.util.concurrent.Flow.*;

import java.io.Serial;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 当下游 {@link Subscriber} 未就绪时，对上游过量更新进行节流的抽象基类。
 *
 * @param <T> 上游值类型
 * @param <R> 下游值类型
 */
abstract class AbstractBackpressureThrottlingSubscriber<T, R> extends AtomicInteger implements FlowableSubscriber<T>, Subscription {

    @Serial
    private static final long serialVersionUID = -5050301752721603566L;

    final Subscriber<? super R> downstream;

    Subscription upstream;

    volatile boolean done;
    Throwable error;

    volatile boolean cancelled;

    final AtomicLong requested = new AtomicLong();

    final AtomicReference<R> current = new AtomicReference<>();

    /** @param downstream 下游 Subscriber */
    AbstractBackpressureThrottlingSubscriber(Subscriber<? super R> downstream) {
        this.downstream = downstream;
    }

    /** 验证上游订阅，向下游传递自身并请求 Long.MAX_VALUE。 */
    @Override
    public void onSubscribe(Subscription s) {
        if (SubscriptionHelper.validate(this.upstream, s)) {
            this.upstream = s;
            downstream.onSubscribe(this);
            s.request(Long.MAX_VALUE);
        }
    }

    @Override
    public abstract void onNext(T t);

    @Override
    public void onError(Throwable t) {
        error = t;
        done = true;
        drain();
    }

    @Override
    public void onComplete() {
        done = true;
        drain();
    }

    @Override
    public void request(long n) {
        if (SubscriptionHelper.validate(n)) {
            BackpressureHelper.add(requested, n);
            drain();
        }
    }

    @Override
    public void cancel() {
        if (!cancelled) {
            cancelled = true;
            upstream.cancel();

            if (getAndIncrement() == 0) {
                current.lazySet(null);
            }
        }
    }

    /** 按背压请求从 current 取出最新值并向下游发射。 */
    void drain() {
        if (getAndIncrement() != 0) {
            return;
        }
        final Subscriber<? super R> a = downstream;
        int missed = 1;
        final AtomicLong r = requested;
        final AtomicReference<R> q = current;

        for (;;) {
            long e = 0L;

            while (e != r.get()) {
                boolean d = done;
                R v = q.getAndSet(null);
                boolean empty = v == null;

                if (checkTerminated(d, empty, a, q)) {
                    return;
                }

                if (empty) {
                    break;
                }

                a.onNext(v);

                e++;
            }

            if (e == r.get() && checkTerminated(done, q.get() == null, a, q)) {
                return;
            }

            if (e != 0L) {
                BackpressureHelper.produced(r, e);
            }

            missed = addAndGet(-missed);
            if (missed == 0) {
                break;
            }
        }
    }

    /** 检查取消/完成/错误状态并向下游发出相应信号。 */
    boolean checkTerminated(boolean d, boolean empty, Subscriber<?> a, AtomicReference<R> q) {
        if (cancelled) {
            q.lazySet(null);
            return true;
        }

        if (d) {
            Throwable e = error;
            if (e != null) {
                q.lazySet(null);
                a.onError(e);
                return true;
            } else
            if (empty) {
                a.onComplete();
                return true;
            }
        }

        return false;
    }
}
