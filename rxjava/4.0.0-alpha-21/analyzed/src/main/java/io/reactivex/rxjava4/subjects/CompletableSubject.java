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
 * 类似 Subject 的热 Completable 式事件源与消费者。
 * <p>
 * <img width="640" height="243" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/CompletableSubject.png" alt="">
 * <p>
 * 通过 {@link #create()} 创建；{@link #onError(Throwable)} 禁止 null。
 * onSubscribe 独立作源时非必需；终止后调用会立即 dispose Disposable。
 * 所有方法线程安全；重复 onComplete 无效果，重复 onError 交由 RxJavaPlugins 处理。
 * <p>
 * 支持 hasComplete/hasThrowable/getThrowable/hasObservers；默认不在特定 {@link io.reactivex.rxjava4.core.Scheduler} 上运行。
 * <dl>
 *  <dt><b>Scheduler:</b></dt>
 *  <dd>终止事件在调用线程通知 CompletableObserver。</dd>
 *  <dt><b>Error handling:</b></dt>
 *  <dd>无 Observer 时 onError 不触发全局错误处理器。</dd>
 * </dl>
 * <p>History: 2.0.5 - experimental
 * @since 2.1
 */
public final class CompletableSubject extends Completable implements CompletableObserver {

    final AtomicReference<CompletableDisposable[]> observers;

    static final CompletableDisposable[] EMPTY = new CompletableDisposable[0];

    static final CompletableDisposable[] TERMINATED = new CompletableDisposable[0];

    final AtomicBoolean once;
    Throwable error;

    /**
     * 创建新的 CompletableSubject。
     * @return 新的 CompletableSubject 实例
     */
    @CheckReturnValue
    @NonNull
    public static CompletableSubject create() {
        return new CompletableSubject();
    }

    CompletableSubject() {
        once = new AtomicBoolean();
        observers = new AtomicReference<>(EMPTY);
    }

    @Override
    public void onSubscribe(Disposable d) {
        if (observers.get() == TERMINATED) {
            d.dispose();
        }
    }

    @Override
    public void onError(Throwable e) {
        ExceptionHelper.nullCheck(e, "onError called with a null Throwable.");
        if (once.compareAndSet(false, true)) {
            this.error = e;
            for (CompletableDisposable md : observers.getAndSet(TERMINATED)) {
                md.downstream.onError(e);
            }
        } else {
            RxJavaPlugins.onError(e);
        }
    }

    @Override
    public void onComplete() {
        if (once.compareAndSet(false, true)) {
            for (CompletableDisposable md : observers.getAndSet(TERMINATED)) {
                md.downstream.onComplete();
            }
        }
    }

    @Override
    protected void subscribeActual(CompletableObserver observer) {
        CompletableDisposable md = new CompletableDisposable(observer, this);
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
                observer.onComplete();
            }
        }
    }

    boolean add(CompletableDisposable inner) {
        for (;;) {
            CompletableDisposable[] a = observers.get();
            if (a == TERMINATED) {
                return false;
            }

            int n = a.length;

            CompletableDisposable[] b = new CompletableDisposable[n + 1];
            System.arraycopy(a, 0, b, 0, n);
            b[n] = inner;
            if (observers.compareAndSet(a, b)) {
                return true;
            }
        }
    }

    void remove(CompletableDisposable inner) {
        for (;;) {
            CompletableDisposable[] a = observers.get();
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
            CompletableDisposable[] b;
            if (n == 1) {
                b = EMPTY;
            } else {
                b = new CompletableDisposable[n - 1];
                System.arraycopy(a, 0, b, 0, j);
                System.arraycopy(a, j + 1, b, j, n - j - 1);
            }

            if (observers.compareAndSet(a, b)) {
                return;
            }
        }
    }

    /**
     * 若 CompletableSubject 以 error 终止则返回该错误，否则 null。
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
     * 若 CompletableSubject 以 error 终止则返回 true。
     * @return 以 error 终止时为 true
     */
    public boolean hasThrowable() {
        return observers.get() == TERMINATED && error != null;
    }

    /**
     * 若 CompletableSubject 已完成则返回 true。
     * @return 已完成时为 true
     */
    public boolean hasComplete() {
        return observers.get() == TERMINATED && error == null;
    }

    /**
     * 若 CompletableSubject 有 Observer 则返回 true。
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

    static final class CompletableDisposable
    extends AtomicReference<CompletableSubject> implements Disposable {
        @Serial
        private static final long serialVersionUID = -7650903191002190468L;

        final CompletableObserver downstream;

        CompletableDisposable(CompletableObserver actual, CompletableSubject parent) {
            this.downstream = actual;
            lazySet(parent);
        }

        @Override
        public void dispose() {
            CompletableSubject parent = getAndSet(null);
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
