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
import java.util.concurrent.atomic.*;

import io.reactivex.rxjava4.annotations.*;
import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.util.ExceptionHelper;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 类似 Subject 的热 Single 式事件源与消费者。
 * <p>
 * <img width="640" height="236" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/SingleSubject.png" alt="">
 * <p>
 * 通过 {@link #create()} 创建；onSuccess/onError 禁止 null；任一调用即原子终止。
 * 方法均线程安全；不支持清除 onSuccess 缓存；支持 hasValue/getValue 等状态查询。
 * <dl>
 *  <dt><b>Scheduler:</b></dt>
 *  <dd>onSuccess/onError 调用线程通知 SingleObserver。</dd>
 *  <dt><b>Error handling:</b></dt>
 *  <dd>无 Observer 时 onError 不触发全局错误处理器。</dd>
 * </dl>
 * <p>History: 2.0.5 - experimental
 * @param <T> 接收与发射的值类型
 * @since 2.1
 */
public final class SingleSubject<T> extends Single<T> implements SingleObserver<T> {

    final AtomicReference<SingleDisposable<T>[]> observers;

    @SuppressWarnings("rawtypes")
    static final SingleDisposable[] EMPTY = new SingleDisposable[0];

    @SuppressWarnings("rawtypes")
    static final SingleDisposable[] TERMINATED = new SingleDisposable[0];

    final AtomicBoolean once;
    T value;
    Throwable error;

    /**
     * 创建新的 SingleSubject。
     * @param <T> 接收与发射的值类型
     * @return 新的 SingleSubject 实例
     */
    @CheckReturnValue
    @NonNull
    public static <T> SingleSubject<T> create() {
        return new SingleSubject<>();
    }

    @SuppressWarnings("unchecked")
    SingleSubject() {
        once = new AtomicBoolean();
        observers = new AtomicReference<SingleDisposable<T>[]>(EMPTY);
    }

    @Override
    public void onSubscribe(@NonNull Disposable d) {
        if (observers.get() == TERMINATED) {
            d.dispose();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onSuccess(@NonNull T value) {
        ExceptionHelper.nullCheck(value, "onSuccess called with a null value.");
        if (once.compareAndSet(false, true)) {
            this.value = value;
            for (SingleDisposable<T> md : observers.getAndSet(TERMINATED)) {
                md.downstream.onSuccess(value);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onError(@NonNull Throwable e) {
        ExceptionHelper.nullCheck(e, "onError called with a null Throwable.");
        if (once.compareAndSet(false, true)) {
            this.error = e;
            for (SingleDisposable<T> md : observers.getAndSet(TERMINATED)) {
                md.downstream.onError(e);
            }
        } else {
            RxJavaPlugins.onError(e);
        }
    }

    @Override
    protected void subscribeActual(@NonNull SingleObserver<? super T> observer) {
        SingleDisposable<T> md = new SingleDisposable<>(observer, this);
        observer.onSubscribe(md);
        if (add(md)) {
            if (md.isDisposed()) {
                remove(md);
            }
        } else {
            Throwable ex = error;
            if (ex != null) {
                observer.onError(ex);
            } else {
                observer.onSuccess(value);
            }
        }
    }

    boolean add(@NonNull SingleDisposable<T> inner) {
        for (;;) {
            SingleDisposable<T>[] a = observers.get();
            if (a == TERMINATED) {
                return false;
            }

            int n = a.length;
            @SuppressWarnings("unchecked")
            SingleDisposable<T>[] b = new SingleDisposable[n + 1];
            System.arraycopy(a, 0, b, 0, n);
            b[n] = inner;
            if (observers.compareAndSet(a, b)) {
                return true;
            }
        }
    }

    @SuppressWarnings("unchecked")
    void remove(@NonNull SingleDisposable<T> inner) {
        for (;;) {
            SingleDisposable<T>[] a = observers.get();
            int n = a.length;
            if (n == 0) {
                return;
            }

            int j = -1;

            for (int i = 0; i < n; i++) {
                if (a[i] == inner) {
                    j = i;
                    break;
                }
            }

            if (j < 0) {
                return;
            }
            SingleDisposable<T>[] b;
            if (n == 1) {
                b = EMPTY;
            } else {
                b = new SingleDisposable[n - 1];
                System.arraycopy(a, 0, b, 0, j);
                System.arraycopy(a, j + 1, b, j, n - j - 1);
            }

            if (observers.compareAndSet(a, b)) {
                return;
            }
        }
    }

    /**
     * 若 SingleSubject 以 success 终止则返回成功值。
     * @return 成功值或 null
     */
    @Nullable
    public T getValue() {
        if (observers.get() == TERMINATED) {
            return value;
        }
        return null;
    }

    /**
     * 若 SingleSubject 以 success 终止则返回 true。
     * @return 以 success 终止时为 true
     */
    public boolean hasValue() {
        return observers.get() == TERMINATED && value != null;
    }

    /**
     * 若 SingleSubject 以 error 终止则返回该错误，否则 null。
     * @return 终止错误或 null
     */
    @Nullable
    public Throwable getThrowable() {
        if (observers.get() == TERMINATED) {
            return error;
        }
        return null;
    }

    /**
     * 若 SingleSubject 以 error 终止则返回 true。
     * @return 以 error 终止时为 true
     */
    public boolean hasThrowable() {
        return observers.get() == TERMINATED && error != null;
    }

    /**
     * 若 SingleSubject 有 Observer 则返回 true。
     * @return 有 Observer 时为 true
     */
    public boolean hasObservers() {
        return observers.get().length != 0;
    }

    /**
     * 返回当前 Observer 数量。
     * @return 当前 Observer 数量
     */
    /* test */ int observerCount() {
        return observers.get().length;
    }

    static final class SingleDisposable<T>
    extends AtomicReference<SingleSubject<T>> implements Disposable {
        @Serial
        private static final long serialVersionUID = -7650903191002190468L;

        final SingleObserver<? super T> downstream;

        SingleDisposable(SingleObserver<? super T> actual, SingleSubject<T> parent) {
            this.downstream = actual;
            lazySet(parent);
        }

        @Override
        public void dispose() {
            SingleSubject<T> parent = getAndSet(null);
            if (parent != null) {
                parent.remove(this);
            }
        }

        @Override
        public boolean isDisposed() {
            return get() == null;
        }
    }
}
