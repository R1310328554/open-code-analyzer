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

package io.reactivex.rxjava4.subjects;

import java.io.Serial;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.annotations.*;
import io.reactivex.rxjava4.core.Observer;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.observers.DeferredScalarDisposable;
import io.reactivex.rxjava4.internal.util.ExceptionHelper;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * Subject：onComplete 时向 Observer 发射最后一项，或 onError 时发射错误。
 * <p>
 * <img width="640" height="239" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/AsyncSubject.png" alt="">
 * <p>
 * 通过 {@link #create()} 创建；onNext/onError 禁止 null（Reactive Streams 规则 2.13）。
 * <p>
 * 作为 {@link io.reactivex.rxjava4.core.Observable} 不支持背压；内部只缓存最新一项，仅在 onComplete 时发射，
 * 不宜用于无限或永不完成的源。onError 会清除最后一项，晚到 Observer 仅收到 onError。
 * <p>
 * onSubscribe 在独立作源时非必需（规则 2.12）；终止后调用会立即 dispose Disposable。
 * onNext/onError/onComplete 须串行调用，可用 {@link #toSerialized()} 防重入。
 * <p>
 * 支持 hasComplete/hasThrowable/getValue 等状态查询；默认不在特定 {@link io.reactivex.rxjava4.core.Scheduler} 上运行。
 * onError 时向当前 Observer 集合同一 Throwable 实例；取消订阅可能导致 RxJavaPlugins.onError 多次调用。
 * <dl>
 *  <dt><b>Scheduler:</b></dt>
 *  <dd>终止 onError/onComplete 所在线程通知 Observer。</dd>
 *  <dt><b>Error handling:</b></dt>
 *  <dd>无 Observer 时 onError 不触发全局错误处理器。</dd>
 * </dl>
 * @param <T> 值类型
 */
public final class AsyncSubject<T> extends Subject<T> {

    @SuppressWarnings("rawtypes")
    static final AsyncDisposable[] EMPTY = new AsyncDisposable[0];

    @SuppressWarnings("rawtypes")
    static final AsyncDisposable[] TERMINATED = new AsyncDisposable[0];

    final AtomicReference<AsyncDisposable<T>[]> subscribers;

    /** 在 subscribers 置 TERMINATED 前写入，读后可见。 */
    Throwable error;

    /** 在 subscribers 置 TERMINATED 前写入，读后可见。 */
    T value;

    /**
     * 创建新的 AsyncSubject。
     * @param <T> 接收与发射的值类型
     * @return 新的 AsyncSubject 实例
     */
    @CheckReturnValue
    @NonNull
    public static <T> AsyncSubject<T> create() {
        return new AsyncSubject<>();
    }

    /**
     * 构造 AsyncSubject。
     * @since 2.0
     */
    @SuppressWarnings("unchecked")
    AsyncSubject() {
        this.subscribers = new AtomicReference<AsyncDisposable<T>[]>(EMPTY);
    }

    @Override
    public void onSubscribe(Disposable d) {
        if (subscribers.get() == TERMINATED) {
            d.dispose();
        }
    }

    @Override
    public void onNext(T t) {
        ExceptionHelper.nullCheck(t, "onNext called with a null value.");
        if (subscribers.get() == TERMINATED) {
            return;
        }
        value = t;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onError(Throwable t) {
        ExceptionHelper.nullCheck(t, "onError called with a null Throwable.");
        if (subscribers.get() == TERMINATED) {
            RxJavaPlugins.onError(t);
            return;
        }
        value = null;
        error = t;
        for (AsyncDisposable<T> as : subscribers.getAndSet(TERMINATED)) {
            as.onError(t);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onComplete() {
        if (subscribers.get() == TERMINATED) {
            return;
        }
        T v = value;
        AsyncDisposable<T>[] array = subscribers.getAndSet(TERMINATED);
        if (v == null) {
            for (AsyncDisposable<T> as : array) {
                as.onComplete();
            }
        } else {
            for (AsyncDisposable<T> as : array) {
                as.complete(v);
            }
        }
    }

    @Override
    @CheckReturnValue
    public boolean hasObservers() {
        return subscribers.get().length != 0;
    }

    @Override
    @CheckReturnValue
    public boolean hasThrowable() {
        return subscribers.get() == TERMINATED && error != null;
    }

    @Override
    @CheckReturnValue
    public boolean hasComplete() {
        return subscribers.get() == TERMINATED && error == null;
    }

    @Override
    @CheckReturnValue
    public Throwable getThrowable() {
        return subscribers.get() == TERMINATED ? error : null;
    }

    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        AsyncDisposable<T> as = new AsyncDisposable<>(observer, this);
        observer.onSubscribe(as);
        if (add(as)) {
            if (as.isDisposed()) {
                remove(as);
            }
        } else {
            Throwable ex = error;
            if (ex != null) {
                observer.onError(ex);
            } else {
                T v = value;
                if (v != null) {
                    as.complete(v);
                } else {
                    as.onComplete();
                }
            }
        }
    }

    /**
     * 尝试将给定订阅者原子加入 subscribers 数组；subject 已终止则返回 false。
     * @param ps 要添加的订阅者
     * @return 成功为 true，已终止为 false
     */
    boolean add(AsyncDisposable<T> ps) {
        for (;;) {
            AsyncDisposable<T>[] a = subscribers.get();
            if (a == TERMINATED) {
                return false;
            }

            int n = a.length;
            @SuppressWarnings("unchecked")
            AsyncDisposable<T>[] b = new AsyncDisposable[n + 1];
            System.arraycopy(a, 0, b, 0, n);
            b[n] = ps;

            if (subscribers.compareAndSet(a, b)) {
                return true;
            }
        }
    }

    /**
     * 若已订阅则从 subject 原子移除给定订阅者。
     * @param ps 要移除的订阅者
     */
    @SuppressWarnings("unchecked")
    void remove(AsyncDisposable<T> ps) {
        for (;;) {
            AsyncDisposable<T>[] a = subscribers.get();
            int n = a.length;
            if (n == 0) {
                return;
            }

            int j = -1;
            for (int i = 0; i < n; i++) {
                if (a[i] == ps) {
                    j = i;
                    break;
                }
            }

            if (j < 0) {
                return;
            }

            AsyncDisposable<T>[] b;

            if (n == 1) {
                b = EMPTY;
            } else {
                b = new AsyncDisposable[n - 1];
                System.arraycopy(a, 0, b, 0, j);
                System.arraycopy(a, j + 1, b, j, n - j - 1);
            }
            if (subscribers.compareAndSet(a, b)) {
                return;
            }
        }
    }

    /**
     * 若 subject 持有任意值则返回 true。
     * <p>本方法线程安全。
     * @return 有值时为 true
     */
    @CheckReturnValue
    public boolean hasValue() {
        return subscribers.get() == TERMINATED && value != null;
    }

    /**
     * 返回 Subject 当前持有的单一值；无值时返回 null。
     * <p>本方法线程安全。
     * @return 当前值或 null
     */
    @Nullable
    @CheckReturnValue
    public T getValue() {
        return subscribers.get() == TERMINATED ? value : null;
    }

    static final class AsyncDisposable<T> extends DeferredScalarDisposable<T> {
        @Serial
        private static final long serialVersionUID = 5629876084736248016L;

        final AsyncSubject<T> parent;

        AsyncDisposable(Observer<? super T> actual, AsyncSubject<T> parent) {
            super(actual);
            this.parent = parent;
        }

        @Override
        public void dispose() {
            if (super.tryDispose()) {
                parent.remove(this);
            }
        }

        void onComplete() {
            if (!isDisposed()) {
                downstream.onComplete();
            }
        }

        void onError(Throwable t) {
            if (isDisposed()) {
                RxJavaPlugins.onError(t);
            } else {
                downstream.onError(t);
            }
        }
    }
}
