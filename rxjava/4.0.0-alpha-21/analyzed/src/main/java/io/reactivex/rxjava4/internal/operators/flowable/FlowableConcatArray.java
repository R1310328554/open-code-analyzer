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
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.exceptions.CompositeException;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionArbiter;

/**
 * 顺序订阅 {@link Publisher} 数组中的各源，依次转发元素；
 * 可选 delayError 模式收集所有错误后再终止。
 * @param <T> 元素类型
 */
public final class FlowableConcatArray<T> extends Flowable<T> {

    final Publisher<? extends T>[] sources;

    final boolean delayError;

    /**
     * @param sources 要顺序拼接的 Publisher 数组
     * @param delayError 为 true 时收集所有错误后再报告
     */
    public FlowableConcatArray(Publisher<? extends T>[] sources, boolean delayError) {
        this.sources = sources;
        this.delayError = delayError;
    }

    @Override
    protected void subscribeActual(Subscriber<? super T> s) {
        ConcatArraySubscriber<T> parent = new ConcatArraySubscriber<>(sources, delayError, s);
        s.onSubscribe(parent);

        parent.onComplete();
    }

    /** 按 index 顺序订阅各 Publisher 并转发事件。 */
    static final class ConcatArraySubscriber<T> extends SubscriptionArbiter implements FlowableSubscriber<T> {

        @Serial
        private static final long serialVersionUID = -8158322871608889516L;

        final Subscriber<? super T> downstream;

        final Publisher<? extends T>[] sources;

        final boolean delayError;

        final AtomicInteger wip;

        int index;

        List<Throwable> errors;

        long produced;

        ConcatArraySubscriber(Publisher<? extends T>[] sources, boolean delayError, Subscriber<? super T> downstream) {
            super(false);
            this.downstream = downstream;
            this.sources = sources;
            this.delayError = delayError;
            this.wip = new AtomicInteger();
        }

        @Override
        public void onSubscribe(Subscription s) {
            setSubscription(s);
        }

        @Override
        public void onNext(T t) {
            produced++;
            downstream.onNext(t);
        }

        /** delayError 时收集错误并继续下一源，否则立即转发错误。 */
        @Override
        public void onError(Throwable t) {
            if (delayError) {
                List<Throwable> list = errors;
                if (list == null) {
                    list = new ArrayList<>(sources.length - index + 1);
                    errors = list;
                }
                list.add(t);
                onComplete();
            } else {
                downstream.onError(t);
            }
        }

        /** 当前源完成后订阅下一 Publisher 或终止序列。 */
        @Override
        public void onComplete() {
            if (wip.getAndIncrement() == 0) {
                Publisher<? extends T>[] sources = this.sources;
                int n = sources.length;
                int i = index;
                for (;;) {

                    if (i == n) {
                        List<Throwable> list = errors;
                        if (list != null) {
                            if (list.size() == 1) {
                                downstream.onError(list.getFirst());
                            } else {
                                downstream.onError(new CompositeException(list));
                            }
                        } else {
                            downstream.onComplete();
                        }
                        return;
                    }

                    Publisher<? extends T> p = sources[i];

                    if (p == null) {
                        Throwable ex = new NullPointerException("A Publisher entry is null");
                        if (delayError) {
                            List<Throwable> list = errors;
                            if (list == null) {
                                list = new ArrayList<>(n - i + 1);
                                errors = list;
                            }
                            list.add(ex);
                            i++;
                            continue;
                        } else {
                            downstream.onError(ex);
                            return;
                        }
                    } else {
                        long r = produced;
                        if (r != 0L) {
                            produced = 0L;
                            produced(r);
                        }
                        p.subscribe(this);
                    }

                    index = ++i;

                    if (wip.decrementAndGet() == 0) {
                        break;
                    }
                }
            }
        }
    }

}
