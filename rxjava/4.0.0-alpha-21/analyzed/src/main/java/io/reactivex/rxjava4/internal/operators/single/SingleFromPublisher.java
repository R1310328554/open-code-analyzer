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

package io.reactivex.rxjava4.internal.operators.single;

import java.util.NoSuchElementException;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 订阅 Publisher 并取首个 onNext 元素作为 Single 成功值；
 * 多于一个元素则 IndexOutOfBoundsException，空序列则 NoSuchElementException。
 *
 * @param <T> 元素类型
 */
public final class SingleFromPublisher<T> extends Single<T> {

    final Publisher<? extends T> publisher;

    /** @param publisher 上游 Publisher（期望 0 或 1 个元素） */
    public SingleFromPublisher(Publisher<? extends T> publisher) {
        this.publisher = publisher;
    }

    /** 订阅 ToSingleObserver，request(MAX) 收集唯一元素。 */
    @Override
    protected void subscribeActual(final SingleObserver<? super T> observer) {
        publisher.subscribe(new ToSingleObserver<T>(observer));
    }

    /** Publisher→Single 适配：缓存首个 onNext，onComplete 时 onSuccess 或 onError。 */
    static final class ToSingleObserver<T> implements FlowableSubscriber<T>, Disposable {
        final SingleObserver<? super T> downstream;

        Subscription upstream;

        T value;

        boolean done;

        volatile boolean disposed;

        ToSingleObserver(SingleObserver<? super T> downstream) {
            this.downstream = downstream;
        }

        @Override
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;

                downstream.onSubscribe(this);

                s.request(Long.MAX_VALUE);
            }
        }

        /** 首元素缓存；第二个元素 cancel 并 IndexOutOfBoundsException。 */
        @Override
        public void onNext(T t) {
            if (done) {
                return;
            }
            if (value != null) {
                upstream.cancel();
                done = true;
                this.value = null;
                downstream.onError(new IndexOutOfBoundsException("Too many elements in the Publisher"));
            } else {
                value = t;
            }
        }

        @Override
        public void onError(Throwable t) {
            if (done) {
                RxJavaPlugins.onError(t);
                return;
            }
            done = true;
            this.value = null;
            downstream.onError(t);
        }

        /** 有缓存值则 onSuccess；否则 NoSuchElementException。 */
        @Override
        public void onComplete() {
            if (done) {
                return;
            }
            done = true;
            T v = this.value;
            this.value = null;
            if (v == null) {
                downstream.onError(new NoSuchElementException("The source Publisher is empty"));
            } else {
                downstream.onSuccess(v);
            }
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }

        @Override
        public void dispose() {
            disposed = true;
            upstream.cancel();
        }
    }
}
