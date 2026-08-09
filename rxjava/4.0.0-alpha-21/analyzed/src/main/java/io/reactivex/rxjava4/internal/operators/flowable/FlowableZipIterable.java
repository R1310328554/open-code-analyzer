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

import java.util.Iterator;
import java.util.Objects;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.BiFunction;
import io.reactivex.rxjava4.internal.subscriptions.*;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 将上游每个元素与 {@link Iterable} 迭代器当前项经 zipper 合并；
 * 迭代器耗尽时 onComplete。
 * @param <T> 上游元素类型
 * @param <U> Iterable 元素类型
 * @param <V> 合并结果类型
 */
public final class FlowableZipIterable<T, U, V> extends AbstractFlowableWithUpstream<T, V> {
    final Iterable<U> other;
    final BiFunction<? super T, ? super U, ? extends V> zipper;

    /**
     * @param source 上游 Flowable
     * @param other 与上游 zip 的 Iterable
     * @param zipper 二元合并函数
     */
    public FlowableZipIterable(
            Flowable<T> source,
            Iterable<U> other, BiFunction<? super T, ? super U, ? extends V> zipper) {
        super(source);
        this.other = other;
        this.zipper = zipper;
    }

    /** 校验 iterator 非空且有元素后订阅 ZipIterableSubscriber。 */
    @Override
    public void subscribeActual(Subscriber<? super V> t) {
        Iterator<U> it;

        try {
            it = Objects.requireNonNull(other.iterator(), "The iterator returned by other is null");
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            EmptySubscription.error(e, t);
            return;
        }

        boolean b;

        try {
            b = it.hasNext();
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            EmptySubscription.error(e, t);
            return;
        }

        if (!b) {
            EmptySubscription.complete(t);
            return;
        }

        source.subscribe(new ZipIterableSubscriber<T, U, V>(t, it, zipper));
    }

    /** 每 onNext 取 iterator.next() 经 zipper 合并；无下一项则 complete。 */
    static final class ZipIterableSubscriber<T, U, V> implements FlowableSubscriber<T>, Subscription {
        final Subscriber<? super V> downstream;
        final Iterator<U> iterator;
        final BiFunction<? super T, ? super U, ? extends V> zipper;

        Subscription upstream;

        boolean done;

        ZipIterableSubscriber(Subscriber<? super V> actual, Iterator<U> iterator,
                BiFunction<? super T, ? super U, ? extends V> zipper) {
            this.downstream = actual;
            this.iterator = iterator;
            this.zipper = zipper;
        }

        @Override
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;
                downstream.onSubscribe(this);
            }
        }

        @Override
        public void onNext(T t) {
            if (done) {
                return;
            }

            U u;

            try {
                u = Objects.requireNonNull(iterator.next(), "The iterator returned a null value");
            } catch (Throwable e) {
                fail(e);
                return;
            }

            V v;
            try {
                v = Objects.requireNonNull(zipper.apply(t, u), "The zipper function returned a null value");
            } catch (Throwable e) {
                fail(e);
                return;
            }

            downstream.onNext(v);

            boolean b;

            try {
                b = iterator.hasNext();
            } catch (Throwable e) {
                fail(e);
                return;
            }

            if (!b) {
                done = true;
                upstream.cancel();
                downstream.onComplete();
            }
        }

        /** 标记 done、cancel 上游并 onError。 */
        void fail(Throwable e) {
            Exceptions.throwIfFatal(e);
            done = true;
            upstream.cancel();
            downstream.onError(e);
        }

        @Override
        public void onError(Throwable t) {
            if (done) {
                RxJavaPlugins.onError(t);
                return;
            }
            done = true;
            downstream.onError(t);
        }

        @Override
        public void onComplete() {
            if (done) {
                return;
            }
            done = true;
            downstream.onComplete();
        }

        @Override
        public void request(long n) {
            upstream.request(n);
        }

        @Override
        public void cancel() {
            upstream.cancel();
        }

    }
}
