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
import java.util.ArrayDeque;
import java.util.concurrent.atomic.*;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.internal.util.BackpressureHelper;

/**
 * 缓存上游最后 count 个元素，上游 onComplete 后按背压依次发射。
 * @param <T> 元素类型
 */
public final class FlowableTakeLast<T> extends AbstractFlowableWithUpstream<T, T> {
    final int count;

    /**
     * @param source 上游 Flowable
     * @param count 保留的末尾元素个数
     */
    public FlowableTakeLast(Flowable<T> source, int count) {
        super(source);
        this.count = count;
    }

    /** 订阅 TakeLastSubscriber（ArrayDeque 环形缓冲）。 */
    @Override
    protected void subscribeActual(Subscriber<? super T> s) {
        source.subscribe(new TakeLastSubscriber<>(s, count));
    }

    /** 以 deque 滑动窗口缓存；done 后 drain 发射。 */
    static final class TakeLastSubscriber<T> extends ArrayDeque<T> implements FlowableSubscriber<T>, Subscription {

        @Serial
        private static final long serialVersionUID = 7240042530241604978L;
        final Subscriber<? super T> downstream;
        final int count;

        Subscription upstream;
        volatile boolean done;
        volatile boolean cancelled;

        final AtomicLong requested = new AtomicLong();

        final AtomicInteger wip = new AtomicInteger();

        TakeLastSubscriber(Subscriber<? super T> actual, int count) {
            this.downstream = actual;
            this.count = count;
        }

        @Override
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;
                downstream.onSubscribe(this);
                s.request(Long.MAX_VALUE);
            }
        }

        /** 队列满时 poll 队首再 offer 新元素。 */
        @Override
        public void onNext(T t) {
            if (count == size()) {
                poll();
            }
            offer(t);
        }

        @Override
        public void onError(Throwable t) {
            downstream.onError(t);
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
            cancelled = true;
            upstream.cancel();
        }

        /** 上游完成后按 requested 从 deque poll 并 onNext。 */
        void drain() {
            if (wip.getAndIncrement() == 0) {
                Subscriber<? super T> a = downstream;
                long r = requested.get();
                do {
                    if (cancelled) {
                        return;
                    }
                    if (done) {
                        long e = 0L;

                        while (e != r) {
                            if (cancelled) {
                                return;
                            }
                            T v = poll();
                            if (v == null) {
                                a.onComplete();
                                return;
                            }
                            a.onNext(v);
                            e++;
                        }
                        if (isEmpty()) {
                            a.onComplete();
                            return;
                        }
                        if (e != 0L) {
                            r = BackpressureHelper.produced(requested, e);
                        }
                    }
                } while (wip.decrementAndGet() != 0);
            }
        }
    }
}
