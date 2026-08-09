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

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.annotations.Nullable;
import io.reactivex.rxjava4.core.Flowable;
import io.reactivex.rxjava4.functions.Predicate;
import io.reactivex.rxjava4.internal.subscribers.*;
import io.reactivex.rxjava4.operators.ConditionalSubscriber;
import io.reactivex.rxjava4.operators.QueueSubscription;

/**
 * 仅向下游转发满足 {@link Predicate} 的上游元素。
 * @param <T> 元素类型
 */
public final class FlowableFilter<T> extends AbstractFlowableWithUpstream<T, T> {
    final Predicate<? super T> predicate;
    /**
     * @param source 上游 Flowable
     * @param predicate 过滤谓词
     */
    public FlowableFilter(Flowable<T> source, Predicate<? super T> predicate) {
        super(source);
        this.predicate = predicate;
    }

    /** ConditionalSubscriber 走融合路径，否则使用 FilterSubscriber。 */
    @Override
    protected void subscribeActual(Subscriber<? super T> s) {
        if (s instanceof ConditionalSubscriber) {
            source.subscribe(new FilterConditionalSubscriber<>(
                    (ConditionalSubscriber<? super T>) s, predicate));
        } else {
            source.subscribe(new FilterSubscriber<>(s, predicate));
        }
    }

    /** 对非 Conditional 下游应用 predicate 过滤。 */
    static final class FilterSubscriber<T> extends BasicFuseableSubscriber<T, T>
    implements ConditionalSubscriber<T> {
        final Predicate<? super T> filter;

        FilterSubscriber(Subscriber<? super T> actual, Predicate<? super T> filter) {
            super(actual);
            this.filter = filter;
        }

        @Override
        public void onNext(T t) {
            if (!tryOnNext(t)) {
                upstream.request(1);
            }
        }

        /** predicate 为 true 时转发元素；融合模式下直接转发 null。 */
        @Override
        public boolean tryOnNext(T t) {
            if (done) {
                return false;
            }
            if (sourceMode != NONE) {
                downstream.onNext(null);
                return true;
            }
            boolean b;
            try {
                b = filter.test(t);
            } catch (Throwable e) {
                fail(e);
                return true;
            }
            if (b) {
                downstream.onNext(t);
            }
            return b;
        }

        @Override
        public int requestFusion(int mode) {
            return transitiveBoundaryFusion(mode);
        }

        /** 从队列 poll 直至 predicate 匹配或队列为空。 */
        @Nullable
        @Override
        public T poll() throws Throwable {
            QueueSubscription<T> qs = this.qs;
            Predicate<? super T> f = filter;

            for (;;) {
                T t = qs.poll();
                if (t == null) {
                    return null;
                }

                if (f.test(t)) {
                    return t;
                }

                if (sourceMode == ASYNC) {
                    qs.request(1);
                }
            }
        }
    }

    /** 对 ConditionalSubscriber 下游应用 predicate 过滤。 */
    static final class FilterConditionalSubscriber<T> extends BasicFuseableConditionalSubscriber<T, T> {
        final Predicate<? super T> filter;

        FilterConditionalSubscriber(ConditionalSubscriber<? super T> actual, Predicate<? super T> filter) {
            super(actual);
            this.filter = filter;
        }

        @Override
        public void onNext(T t) {
            if (!tryOnNext(t)) {
                upstream.request(1);
            }
        }

        @Override
        public boolean tryOnNext(T t) {
            if (done) {
                return false;
            }

            if (sourceMode != NONE) {
                return downstream.tryOnNext(null);
            }

            boolean b;
            try {
                b = filter.test(t);
            } catch (Throwable e) {
                fail(e);
                return true;
            }
            return b && downstream.tryOnNext(t);
        }

        @Override
        public int requestFusion(int mode) {
            return transitiveBoundaryFusion(mode);
        }

        @Nullable
        @Override
        public T poll() throws Throwable {
            QueueSubscription<T> qs = this.qs;
            Predicate<? super T> f = filter;

            for (;;) {
                T t = qs.poll();
                if (t == null) {
                    return null;
                }

                if (f.test(t)) {
                    return t;
                }

                if (sourceMode == ASYNC) {
                    qs.request(1);
                }
            }
        }
    }
}
