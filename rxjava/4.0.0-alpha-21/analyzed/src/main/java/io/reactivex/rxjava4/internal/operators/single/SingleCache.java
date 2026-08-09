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

import java.io.Serial;
import java.util.concurrent.atomic.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;

/**
 * 缓存 Single 的首个 onSuccess/onError 结果，
 * 后续订阅者直接重放缓存值（wip 门控仅订阅一次上游）。
 *
 * @param <T> 元素类型
 */
public final class SingleCache<T> extends Single<T> implements SingleObserver<T> {

    @SuppressWarnings("rawtypes")
    static final CacheDisposable[] EMPTY = new CacheDisposable[0];
    @SuppressWarnings("rawtypes")
    static final CacheDisposable[] TERMINATED = new CacheDisposable[0];

    final SingleSource<? extends T> source;

    final AtomicInteger wip;

    final AtomicReference<CacheDisposable<T>[]> observers;

    T value;

    Throwable error;

    /** @param source 被缓存的 SingleSource */
    @SuppressWarnings("unchecked")
    public SingleCache(SingleSource<? extends T> source) {
        this.source = source;
        this.wip = new AtomicInteger();
        this.observers = new AtomicReference<CacheDisposable<T>[]>(EMPTY);
    }

    /** add 观察者；wip==0 时首次 subscribe 上游，已终止则直接重放缓存。 */
    @Override
    protected void subscribeActual(final SingleObserver<? super T> observer) {
        CacheDisposable<T> d = new CacheDisposable<>(observer, this);
        observer.onSubscribe(d);

        if (add(d)) {
            if (d.isDisposed()) {
                remove(d);
            }
        } else {
            Throwable ex = error;
            if (ex != null) {
                observer.onError(ex);
            } else {
                observer.onSuccess(value);
            }
            return;
        }

        if (wip.getAndIncrement() == 0) {
            source.subscribe(this);
        }
    }

    /** CAS 将 observer 追加至 observers 数组。 */
    boolean add(CacheDisposable<T> observer) {
        for (;;) {
            CacheDisposable<T>[] a = observers.get();
            if (a == TERMINATED) {
                return false;
            }
            int n = a.length;
            @SuppressWarnings("unchecked")
            CacheDisposable<T>[] b = new CacheDisposable[n + 1];
            System.arraycopy(a, 0, b, 0, n);
            b[n] = observer;
            if (observers.compareAndSet(a, b)) {
                return true;
            }
        }
    }

    /** dispose 时 CAS 从 observers 数组移除 observer。 */
    @SuppressWarnings("unchecked")
    void remove(CacheDisposable<T> observer) {
        for (;;) {
            CacheDisposable<T>[] a = observers.get();
            int n = a.length;
            if (n == 0) {
                return;
            }

            int j = -1;
            for (int i = 0; i < n; i++) {
                if (a[i] == observer) {
                    j = i;
                    break;
                }
            }

            if (j < 0) {
                return;
            }

            CacheDisposable<T>[] b;

            if (n == 1) {
                b = EMPTY;
            } else {
                b = new CacheDisposable[n - 1];
                System.arraycopy(a, 0, b, 0, j);
                System.arraycopy(a, j + 1, b, j, n - j - 1);
            }
            if (observers.compareAndSet(a, b)) {
                return;
            }
        }
    }

    @Override
    public void onSubscribe(Disposable d) {
        // not supported by this operator
    }

    /** 缓存 value 并向 TERMINATED 前全部 observer 广播 onSuccess。 */
    @SuppressWarnings("unchecked")
    @Override
    public void onSuccess(T value) {
        this.value = value;

        for (CacheDisposable<T> d : observers.getAndSet(TERMINATED)) {
            if (!d.isDisposed()) {
                d.downstream.onSuccess(value);
            }
        }
    }

    /** 缓存 error 并向全部 observer 广播 onError。 */
    @SuppressWarnings("unchecked")
    @Override
    public void onError(Throwable e) {
        this.error = e;

        for (CacheDisposable<T> d : observers.getAndSet(TERMINATED)) {
            if (!d.isDisposed()) {
                d.downstream.onError(e);
            }
        }
    }

    /** 下游 Disposable 包装：dispose 时从 parent 移除自身。 */
    static final class CacheDisposable<T>
    extends AtomicBoolean
    implements Disposable {

        @Serial
        private static final long serialVersionUID = 7514387411091976596L;

        final SingleObserver<? super T> downstream;

        final SingleCache<T> parent;

        CacheDisposable(SingleObserver<? super T> actual, SingleCache<T> parent) {
            this.downstream = actual;
            this.parent = parent;
        }

        @Override
        public boolean isDisposed() {
            return get();
        }

        @Override
        public void dispose() {
            if (compareAndSet(false, true)) {
                parent.remove(this);
            }
        }
    }
}
