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
 * 类似 Subject 的热 Maybe 式事件源与消费者。
 * <p>
 * <img width="640" height="164" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/MaybeSubject.png" alt="">
 * <p>
 * 通过 {@link #create()} 创建；onSuccess/onError 禁止 null。
 * onSuccess/onError/onComplete 任一调用即原子进入终止态；方法均线程安全。
 * 不支持清除已缓存的 onSuccess 值；支持 hasValue/getValue 等状态查询。
 * <dl>
 *  <dt><b>Scheduler:</b></dt>
 *  <dd>终止信号在调用线程通知 MaybeObserver。</dd>
 *  <dt><b>Error handling:</b></dt>
 *  <dd>无 Observer 时 onError 不触发全局错误处理器。</dd>
 * </dl>
 * <p>History: 2.0.5 - experimental
 * @param <T> 接收与发射的值类型
 * @since 2.1
 */
public final class MaybeSubject<T> extends Maybe<T> implements MaybeObserver<T> {

    final AtomicReference<MaybeDisposable<T>[]> observers;

    @SuppressWarnings("rawtypes")
    static final MaybeDisposable[] EMPTY = new MaybeDisposable[0];

    @SuppressWarnings("rawtypes")
    static final MaybeDisposable[] TERMINATED = new MaybeDisposable[0];

    final AtomicBoolean once;
    T value;
    Throwable error;

    /**
     * 创建新的 MaybeSubject。
     * @param <T> 接收与发射的值类型
     * @return 新的 MaybeSubject 实例
     */
    @CheckReturnValue
    @NonNull
    public static <T> MaybeSubject<T> create() {
        return new MaybeSubject<>();
    }

    @SuppressWarnings("unchecked")
    MaybeSubject() {
        once = new AtomicBoolean();
        observers = new AtomicReference<MaybeDisposable<T>[]>(EMPTY);
    }

    @Override
    public void onSubscribe(Disposable d) {
        if (observers.get() == TERMINATED) {
            d.dispose();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onSuccess(T value) {
        ExceptionHelper.nullCheck(value, "onSuccess called with a null value.");
        if (once.compareAndSet(false, true)) {
            this.value = value;
            for (MaybeDisposable<T> md : observers.getAndSet(TERMINATED)) {
                md.downstream.onSuccess(value);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onError(Throwable e) {
        ExceptionHelper.nullCheck(e, "onError called with a null Throwable.");
        if (once.compareAndSet(false, true)) {
            this.error = e;
            for (MaybeDisposable<T> md : observers.getAndSet(TERMINATED)) {
                md.downstream.onError(e);
            }
        } else {
            RxJavaPlugins.onError(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onComplete() {
        if (once.compareAndSet(false, true)) {
            for (MaybeDisposable<T> md : observers.getAndSet(TERMINATED)) {
                md.downstream.onComplete();
            }
        }
    }

    @Override
    protected void subscribeActual(MaybeObserver<? super T> observer) {
        MaybeDisposable<T> md = new MaybeDisposable<>(observer, this);
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
                T v = value;
                if (v == null) {
                    observer.onComplete();
                } else {
                    observer.onSuccess(v);
                }
            }
        }
    }

    boolean add(MaybeDisposable<T> inner) {
        for (;;) {
            MaybeDisposable<T>[] a = observers.get();
            if (a == TERMINATED) {
                return false;
            }

            int n = a.length;
            @SuppressWarnings("unchecked")
            MaybeDisposable<T>[] b = new MaybeDisposable[n + 1];
            System.arraycopy(a, 0, b, 0, n);
            b[n] = inner;
            if (observers.compareAndSet(a, b)) {
                return true;
            }
        }
    }

    @SuppressWarnings("unchecked")
    void remove(MaybeDisposable<T> inner) {
        for (;;) {
            MaybeDisposable<T>[] a = observers.get();
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
            MaybeDisposable<T>[] b;
            if (n == 1) {
                b = EMPTY;
            } else {
                b = new MaybeDisposable[n - 1];
                System.arraycopy(a, 0, b, 0, j);
                System.arraycopy(a, j + 1, b, j, n - j - 1);
            }

            if (observers.compareAndSet(a, b)) {
                return;
            }
        }
    }

    /**
     * 若 MaybeSubject 以 success 终止则返回成功值。
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
     * 若 MaybeSubject 以 success 终止则返回 true。
     * @return 以 success 终止时为 true
     */
    public boolean hasValue() {
        return observers.get() == TERMINATED && value != null;
    }

    /**
     * 若 MaybeSubject 以 error 终止则返回该错误，否则 null。
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
     * 若 MaybeSubject 以 error 终止则返回 true。
     * @return 以 error 终止时为 true
     */
    public boolean hasThrowable() {
        return observers.get() == TERMINATED && error != null;
    }

    /**
     * 若 MaybeSubject 已完成（无 success 无 error）则返回 true。
     * @return 已完成时为 true
     */
    public boolean hasComplete() {
        return observers.get() == TERMINATED && value == null && error == null;
    }

    /**
     * 若 MaybeSubject 有 Observer 则返回 true。
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

    static final class MaybeDisposable<T>
    extends AtomicReference<MaybeSubject<T>> implements Disposable {
        @Serial
        private static final long serialVersionUID = -7650903191002190468L;

        final MaybeObserver<? super T> downstream;

        MaybeDisposable(MaybeObserver<? super T> actual, MaybeSubject<T> parent) {
            this.downstream = actual;
            lazySet(parent);
        }

        @Override
        public void dispose() {
            MaybeSubject<T> parent = getAndSet(null);
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
