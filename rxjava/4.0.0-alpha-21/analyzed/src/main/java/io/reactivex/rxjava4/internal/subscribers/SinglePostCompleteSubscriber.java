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
import java.util.concurrent.atomic.AtomicLong;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.FlowableSubscriber;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.internal.util.BackpressureHelper;

/**
 * 单值后置完成订阅者：按下游 request 转发上游信号，
 * 支持在背压感知下发射最终值后 onComplete（如 reduce 末元素）。
 *
 * @param <T> 上游输入类型
 * @param <R> 下游输出类型
 */
public abstract class SinglePostCompleteSubscriber<T, R> extends AtomicLong implements FlowableSubscriber<T>, Subscription {
    @Serial
    private static final long serialVersionUID = 7917814472626990048L;

    /** 下游 Subscriber。 */
    protected final Subscriber<? super R> downstream;

    /** 上游 Subscription。 */
    protected Subscription upstream;

    /** 尚无 request 时暂存的最终值。 */
    protected R value;

    /** 已向上游产生的元素计数（用于 produced 扣减）。 */
    protected long produced;

    /** 掩码：最高位表示 complete 状态。 */
    static final long COMPLETE_MASK = Long.MIN_VALUE;
    /** 掩码：低 63 位为当前 request 累计量。 */
    static final long REQUEST_MASK = Long.MAX_VALUE;

    public SinglePostCompleteSubscriber(Subscriber<? super R> downstream) {
        this.downstream = downstream;
    }

    @Override
    public void onSubscribe(Subscription s) {
        if (SubscriptionHelper.validate(this.upstream, s)) {
            this.upstream = s;
            downstream.onSubscribe(this);
        }
    }

    /**
     * 信号最终值：有 request 则立即 onNext+onComplete，否则暂存 value 并置 COMPLETE_MASK。
     * @param n 待发射的最终值
     */
    protected final void complete(R n) {
        long p = produced;
        if (p != 0) {
            BackpressureHelper.produced(this, p);
        }

        for (;;) {
            long r = get();
            if ((r & COMPLETE_MASK) != 0) {
                onDrop(n);
                return;
            }
            if ((r & REQUEST_MASK) != 0) {
                lazySet(COMPLETE_MASK + 1);
                downstream.onNext(n);
                downstream.onComplete();
                return;
            }
            value = n;
            if (compareAndSet(0, COMPLETE_MASK)) {
                return;
            }
            value = null;
        }
    }

    /**
     * 多次 complete 时丢弃的值回调（默认无操作）。
     * @param n 被丢弃的值
     */
    protected void onDrop(R n) {
        // default is no-op
    }

    /** 合并 request 至 AtomicLong；若已 complete 则补发 value+onComplete。 */
    @Override
    public final void request(long n) {
        if (SubscriptionHelper.validate(n)) {
            for (;;) {
                long r = get();
                if ((r & COMPLETE_MASK) != 0) {
                    if (compareAndSet(COMPLETE_MASK, COMPLETE_MASK + 1)) {
                        downstream.onNext(value);
                        downstream.onComplete();
                    }
                    break;
                }
                long u = BackpressureHelper.addCap(r, n);
                if (compareAndSet(r, u)) {
                    upstream.request(n);
                    break;
                }
            }
        }
    }

    /** 取消上游 Subscription。 */
    @Override
    public void cancel() {
        upstream.cancel();
    }
}
