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

import java.io.Serial;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionArbiter;

/**
 * 上游 onComplete 后重新订阅，共重复 count 次（含首次订阅）。
 * count 为 {@code Long.MAX_VALUE} 时无限重复。
 * @param <T> 元素类型
 */
public final class FlowableRepeat<T> extends AbstractFlowableWithUpstream<T, T> {
    final long count;
    /**
     * @param source 上游 Flowable
     * @param count 总订阅次数（含首次）
     */
    public FlowableRepeat(Flowable<T> source, long count) {
        super(source);
        this.count = count;
    }

    /** 安装 SubscriptionArbiter 并启动 RepeatSubscriber。 */
    @Override
    public void subscribeActual(Subscriber<? super T> s) {
        SubscriptionArbiter sa = new SubscriptionArbiter(false);
        s.onSubscribe(sa);

        RepeatSubscriber<T> rs = new RepeatSubscriber<>(s, count != Long.MAX_VALUE ? count - 1 : Long.MAX_VALUE, sa, source);
        rs.subscribeNext();
    }

    /** onComplete 时若 remaining>0 则 trampolining 重新订阅。 */
    static final class RepeatSubscriber<T> extends AtomicInteger implements FlowableSubscriber<T> {

        @Serial
        private static final long serialVersionUID = -7098360935104053232L;

        final Subscriber<? super T> downstream;
        final SubscriptionArbiter sa;
        final Publisher<? extends T> source;
        long remaining;

        long produced;

        RepeatSubscriber(Subscriber<? super T> actual, long count, SubscriptionArbiter sa, Publisher<? extends T> source) {
            this.downstream = actual;
            this.sa = sa;
            this.source = source;
            this.remaining = count;
        }

        @Override
        public void onSubscribe(Subscription s) {
            sa.setSubscription(s);
        }

        @Override
        public void onNext(T t) {
            produced++;
            downstream.onNext(t);
        }

        @Override
        public void onError(Throwable t) {
            downstream.onError(t);
        }

        @Override
        public void onComplete() {
            long r = remaining;
            if (r != Long.MAX_VALUE) {
                remaining = r - 1;
            }
            if (r != 0L) {
                subscribeNext();
            } else {
                downstream.onComplete();
            }
        }

        /** 通过 trampolining 再次订阅上游。 */
        void subscribeNext() {
            if (getAndIncrement() == 0) {
                int missed = 1;
                for (;;) {
                    if (sa.isCancelled()) {
                        return;
                    }
                    long p = produced;
                    if (p != 0L) {
                        produced = 0L;
                        sa.produced(p);
                    }
                    source.subscribe(this);

                    missed = addAndGet(-missed);
                    if (missed == 0) {
                        break;
                    }
                }
            }
        }
    }
}
