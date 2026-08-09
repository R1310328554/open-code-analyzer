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

package io.reactivex.rxjava4.internal.operators.maybe;

import java.io.Serial;
import java.util.concurrent.atomic.*;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.SequentialDisposable;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.internal.util.*;

/**
 * 依次串联数组中每个 {@link MaybeSource} 发射的值，
 * 以 {@link Flowable} 形式向下游输出并支持背压。
 *
 * @param <T> 元素类型
 */
public final class MaybeConcatArray<T> extends Flowable<T> {

    final MaybeSource<? extends T>[] sources;

    /** @param sources Maybe 源数组 */
    public MaybeConcatArray(MaybeSource<? extends T>[] sources) {
        this.sources = sources;
    }

    /** 创建 ConcatMaybeObserver 并启动 drain 串联。 */
    @Override
    protected void subscribeActual(Subscriber<? super T> s) {
        ConcatMaybeObserver<T> parent = new ConcatMaybeObserver<>(s, sources);
        s.onSubscribe(parent);
        parent.drain();
    }

    /** 逐个订阅 Maybe 源，将 onSuccess 值经背压检查后转发为 onNext。 */
    static final class ConcatMaybeObserver<T>
    extends AtomicInteger
    implements MaybeObserver<T>, Subscription {

        @Serial
        private static final long serialVersionUID = 3520831347801429610L;

        final Subscriber<? super T> downstream;

        final AtomicLong requested;

        final AtomicReference<Object> current;

        final SequentialDisposable disposables;

        final MaybeSource<? extends T>[] sources;

        int index;

        long produced;

        ConcatMaybeObserver(Subscriber<? super T> actual, MaybeSource<? extends T>[] sources) {
            this.downstream = actual;
            this.sources = sources;
            this.requested = new AtomicLong();
            this.disposables = new SequentialDisposable();
            this.current = new AtomicReference<>(NotificationLite.COMPLETE); // 模拟前一个 Maybe 已完成
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
            disposables.dispose();
        }

        @Override
        public void onSubscribe(Disposable d) {
            disposables.replace(d);
        }

        /** 缓存成功值并触发 drain。 */
        @Override
        public void onSuccess(T value) {
            current.lazySet(value);
            drain();
        }

        @Override
        public void onError(Throwable e) {
            downstream.onError(e);
        }

        @Override
        public void onComplete() {
            current.lazySet(NotificationLite.COMPLETE);
            drain();
        }

        /** 背压感知地发射缓存值并订阅下一个 Maybe 源。 */
        @SuppressWarnings("unchecked")
        void drain() {
            if (getAndIncrement() != 0) {
                return;
            }

            AtomicReference<Object> c = current;
            Subscriber<? super T> a = downstream;
            Disposable cancelled = disposables;

            for (;;) {
                if (cancelled.isDisposed()) {
                    c.lazySet(null);
                    return;
                }

                Object o = c.get();

                if (o != null) {
                    boolean goNextSource;
                    if (o != NotificationLite.COMPLETE) {
                        long p = produced;
                        if (p != requested.get()) {
                            produced = p + 1;
                            c.lazySet(null);
                            goNextSource = true;

                            a.onNext((T)o);
                        } else {
                            goNextSource = false;
                        }
                    } else {
                        goNextSource = true;
                        c.lazySet(null);
                    }

                    if (goNextSource && !cancelled.isDisposed()) {
                        int i = index;
                        if (i == sources.length) {
                            a.onComplete();
                            return;
                        }
                        index = i + 1;

                        sources[i].subscribe(this);
                    }
                }

                if (decrementAndGet() == 0) {
                    break;
                }
            }
        }
    }
}
