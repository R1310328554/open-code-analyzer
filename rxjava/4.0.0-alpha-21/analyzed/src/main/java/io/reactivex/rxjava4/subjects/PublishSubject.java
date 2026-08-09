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
import io.reactivex.rxjava4.core.Observer;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.util.ExceptionHelper;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * Subject：向当前订阅的 {@link Observer} 多播项，向当前或晚到 Observer 发送终止事件。
 * <p>
 * <img width="640" height="281" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/PublishSubject.png" alt="">
 * <p>
 * 通过 {@link #create()} 创建；onNext/onError 禁止 null；作为 Observable 不支持背压，不缓存历史项。
 * 终止后晚到 Observer 仅收到对应终止事件。onXXX 须串行，可用 {@link #toSerialized()}。
 * <dl>
 *  <dt><b>Scheduler:</b></dt>
 *  <dd>在各自 onXXX 调用线程通知 Observer。</dd>
 *  <dt><b>Error handling:</b></dt>
 *  <dd>无 Observer 时 onError 不触发全局错误处理器。</dd>
 * </dl>
 *
 * @param <T> Subject 观察与发射的项类型
 */
public final class PublishSubject<T> extends Subject<T> {
    /** subscribers 数组的已终止标记。 */
    @SuppressWarnings("rawtypes")
    static final PublishDisposable[] TERMINATED = new PublishDisposable[0];
    /** 空 subscribers 数组，避免重复分配。 */
    @SuppressWarnings("rawtypes")
    static final PublishDisposable[] EMPTY = new PublishDisposable[0];

    /** 当前已订阅的 subscribers 数组。 */
    final AtomicReference<PublishDisposable<T>[]> subscribers;

    /** 错误；终止前写入，检查 subscribers 后读取。 */
    Throwable error;

    /**
     * 构造 PublishSubject。
     * @param <T> 值类型
     * @return 新的 PublishSubject
     */
    @CheckReturnValue
    @NonNull
    public static <T> PublishSubject<T> create() {
        return new PublishSubject<>();
    }

    /**
     * 构造 PublishSubject。
     * @since 2.0
     */
    @SuppressWarnings("unchecked")
    PublishSubject() {
        subscribers = new AtomicReference<PublishDisposable<T>[]>(EMPTY);
    }

    @Override
    protected void subscribeActual(Observer<? super T> t) {
        PublishDisposable<T> ps = new PublishDisposable<>(t, this);
        t.onSubscribe(ps);
        if (add(ps)) {
            // if cancellation happened while a successful add, the remove() didn't work
            // so we need to do it again
            if (ps.isDisposed()) {
                remove(ps);
            }
        } else {
            Throwable ex = error;
            if (ex != null) {
                t.onError(ex);
            } else {
                t.onComplete();
            }
        }
    }

    /**
     * 尝试将给定订阅者原子加入 subscribers 数组；subject 已终止则返回 false。
     * @param ps 要添加的订阅者
     * @return 成功为 true，已终止为 false
     */
    boolean add(PublishDisposable<T> ps) {
        for (;;) {
            PublishDisposable<T>[] a = subscribers.get();
            if (a == TERMINATED) {
                return false;
            }

            int n = a.length;
            @SuppressWarnings("unchecked")
            PublishDisposable<T>[] b = new PublishDisposable[n + 1];
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
    void remove(PublishDisposable<T> ps) {
        for (;;) {
            PublishDisposable<T>[] a = subscribers.get();
            if (a == TERMINATED || a == EMPTY) {
                return;
            }

            int n = a.length;
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

            PublishDisposable<T>[] b;

            if (n == 1) {
                b = EMPTY;
            } else {
                b = new PublishDisposable[n - 1];
                System.arraycopy(a, 0, b, 0, j);
                System.arraycopy(a, j + 1, b, j, n - j - 1);
            }
            if (subscribers.compareAndSet(a, b)) {
                return;
            }
        }
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
        for (PublishDisposable<T> pd : subscribers.get()) {
            pd.onNext(t);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onError(Throwable t) {
        ExceptionHelper.nullCheck(t, "onError called with a null Throwable.");
        if (subscribers.get() == TERMINATED) {
            RxJavaPlugins.onError(t);
            return;
        }
        error = t;

        for (PublishDisposable<T> pd : subscribers.getAndSet(TERMINATED)) {
            pd.onError(t);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onComplete() {
        if (subscribers.get() == TERMINATED) {
            return;
        }
        for (PublishDisposable<T> pd : subscribers.getAndSet(TERMINATED)) {
            pd.onComplete();
        }
    }

    @Override
    @CheckReturnValue
    public boolean hasObservers() {
        return subscribers.get().length != 0;
    }

    @Override
    @Nullable
    @CheckReturnValue
    public Throwable getThrowable() {
        if (subscribers.get() == TERMINATED) {
            return error;
        }
        return null;
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

    /**
     * 包装实际订阅者，跟踪请求，取消时从 subscribers 数组移除自身。
     *
     * @param <T> 值类型
     */
    static final class PublishDisposable<T> extends AtomicBoolean implements Disposable {

        @Serial
        private static final long serialVersionUID = 3562861878281475070L;
        /** 实际订阅者。 */
        final Observer<? super T> downstream;
        /** subject 状态。 */
        final PublishSubject<T> parent;

        /**
         * 构造 PublishDisposable，包装实际订阅者与父 subject。
         * @param actual 实际订阅者
         * @param parent 父 PublishSubject
         */
        PublishDisposable(Observer<? super T> actual, PublishSubject<T> parent) {
            this.downstream = actual;
            this.parent = parent;
        }

        public void onNext(T t) {
            if (!get()) {
                downstream.onNext(t);
            }
        }

        public void onError(Throwable t) {
            if (get()) {
                RxJavaPlugins.onError(t);
            } else {
                downstream.onError(t);
            }
        }

        public void onComplete() {
            if (!get()) {
                downstream.onComplete();
            }
        }

        @Override
        public void dispose() {
            if (compareAndSet(false, true)) {
                parent.remove(this);
            }
        }

        @Override
        public boolean isDisposed() {
            return get();
        }
    }
}
