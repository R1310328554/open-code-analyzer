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
import java.util.*;
import java.util.concurrent.atomic.*;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.exceptions.*;
import io.reactivex.rxjava4.functions.*;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.internal.util.BackpressureHelper;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 以有界双端队列缓冲上游元素，并在溢出时按 {@link BackpressureOverflowStrategy} 处理。
 *
 * @param <T> 输入与输出值类型
 */
public final class FlowableOnBackpressureBufferStrategy<T> extends AbstractFlowableWithUpstream<T, T> {

    final long bufferSize;

    final BackpressureOverflowStrategy strategy;

    final Consumer<? super T> onDropped;

    /**
     * @param source 上游 Flowable
     * @param bufferSize 缓冲区最大容量
     * @param strategy 溢出策略（丢弃最新/最旧或报错）
     * @param onDropped 被丢弃元素的回调
     */
    public FlowableOnBackpressureBufferStrategy(Flowable<T> source,
            long bufferSize,
            BackpressureOverflowStrategy strategy,
            Consumer<? super T> onDropped) {
        super(source);
        this.bufferSize = bufferSize;
        this.strategy = strategy;
        this.onDropped = onDropped;
    }

    /** 订阅上游并按策略缓冲过量元素。 */
    @Override
    protected void subscribeActual(Subscriber<? super T> s) {
        source.subscribe(new OnBackpressureBufferStrategySubscriber<>(s, strategy, bufferSize, onDropped));
    }

    /** 按策略维护 Deque 并按 request drain 的 subscriber。 */
    static final class OnBackpressureBufferStrategySubscriber<T>
    extends AtomicInteger
    implements FlowableSubscriber<T>, Subscription {

        @Serial
        private static final long serialVersionUID = 3240706908776709697L;

        final Subscriber<? super T> downstream;

        final Consumer<? super T> onDropped;

        final BackpressureOverflowStrategy strategy;

        final long bufferSize;

        final AtomicLong requested;

        final Deque<T> deque;

        Subscription upstream;

        volatile boolean cancelled;

        volatile boolean done;
        Throwable error;

        OnBackpressureBufferStrategySubscriber(Subscriber<? super T> actual,
                BackpressureOverflowStrategy strategy, long bufferSize,
                Consumer<? super T> onDropped) {
            this.downstream = actual;
            this.strategy = strategy;
            this.bufferSize = bufferSize;
            this.requested = new AtomicLong();
            this.deque = new ArrayDeque<>();
            this.onDropped = onDropped;
        }

        @Override
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;

                downstream.onSubscribe(this);

                s.request(Long.MAX_VALUE);
            }
        }

        @Override
        public void onNext(T t) {
            if (done) {
                return;
            }
            boolean callError = false;
            boolean callDrain = false;
            Deque<T> dq = deque;
            T toDrop = null;
            synchronized (dq) {
                if (dq.size() == bufferSize) {
                   switch (strategy) {
                   case DROP_LATEST:
                       toDrop = dq.pollLast();
                       dq.offer(t);
                       break;
                   case DROP_OLDEST:
                       toDrop = dq.poll();
                       dq.offer(t);
                       break;
                   default:
                       // 溢出时报 MissingBackpressureException
                       toDrop = t;
                       callError = true;
                       break;
                   }
                } else {
                   dq.offer(t);
                   callDrain = true;
                }
            }

            if (onDropped != null && toDrop != null) {
                try {
                    onDropped.accept(toDrop);
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    upstream.cancel();
                    onError(ex);
                }
            }

            if (callError) {
                upstream.cancel();
                onError(MissingBackpressureException.createDefault());
            }

            if (callDrain) {
                drain();
            }
        }

        @Override
        public void onError(Throwable t) {
            if (done) {
                RxJavaPlugins.onError(t);
                return;
            }
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
            cancelled = true;
            upstream.cancel();

            if (getAndIncrement() == 0) {
                clear(deque);
            }
        }

        void clear(Deque<T> dq) {
            synchronized (dq) {
                dq.clear();
            }
        }

        /** 同步 poll Deque 并按 requested 向下游发射。 */
        void drain() {
            if (getAndIncrement() != 0) {
                return;
            }

            int missed = 1;
            Deque<T> dq = deque;
            Subscriber<? super T> a = downstream;
            for (;;) {
                long r = requested.get();
                long e = 0L;
                while (e != r) {
                    if (cancelled) {
                        clear(dq);
                        return;
                    }

                    boolean d = done;

                    T v;

                    synchronized (dq) {
                        v = dq.poll();
                    }

                    boolean empty = v == null;

                    if (d) {
                        Throwable ex = error;
                        if (ex != null) {
                            clear(dq);
                            a.onError(ex);
                            return;
                        }
                        if (empty) {
                            a.onComplete();
                            return;
                        }
                    }

                    if (empty) {
                        break;
                    }

                    a.onNext(v);

                    e++;
                }

                if (e == r) {
                    if (cancelled) {
                        clear(dq);
                        return;
                    }

                    boolean d = done;

                    boolean empty;

                    synchronized (dq) {
                        empty = dq.isEmpty();
                    }

                    if (d) {
                        Throwable ex = error;
                        if (ex != null) {
                            clear(dq);
                            a.onError(ex);
                            return;
                        }
                        if (empty) {
                            a.onComplete();
                            return;
                        }
                    }
                }

                if (e != 0L) {
                    BackpressureHelper.produced(requested, e);
                }

                missed = addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }
    }
}
