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

package io.reactivex.rxjava4.processors;

import java.io.Serial;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.annotations.*;
import io.reactivex.rxjava4.internal.subscriptions.DeferredScalarSubscription;
import io.reactivex.rxjava4.internal.util.ExceptionHelper;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 异步处理器：onComplete 时向 Subscriber 发射最后一个 onNext 值，
 * 或 onError 时发射错误；晚订阅者也能收到缓存的最后一项。
 *
 * <p>通过 {@link #create()} 创建；onNext/onError 禁止 null。
 * <p>内部只保留最新一项，onComplete 前不向下游发射；不适合无限流。
 * <p>onXXX 须串行调用，可用 {@link #toSerialized()}；作为上游时 request(Long.MAX_VALUE)。
 * <p>支持 hasComplete/hasThrowable/getValue 等状态查询。
 *
 * @param <T> 元素类型
 */
public final class AsyncProcessor<@NonNull T> extends FlowableProcessor<T> {

    @SuppressWarnings("rawtypes")
    static final AsyncSubscription[] EMPTY = new AsyncSubscription[0];

    @SuppressWarnings("rawtypes")
    static final AsyncSubscription[] TERMINATED = new AsyncSubscription[0];

    final AtomicReference<AsyncSubscription<T>[]> subscribers;

    /** 在 subscribers 置 TERMINATED 前写入，读后可见。 */
    Throwable error;

    /** Write before updating subscribers, read after reading subscribers as TERMINATED. */
    T value;

    /** 创建空的 AsyncProcessor。 */
    @CheckReturnValue
    @NonNull
    public static <T> AsyncProcessor<T> create() {
        return new AsyncProcessor<>();
    }

    /** 包内构造：subscribers 初始为 EMPTY。 */
    @SuppressWarnings("unchecked")
    AsyncProcessor() {
        this.subscribers = new AtomicReference<AsyncSubscription<T>[]>(EMPTY);
    }

    /** 已终止则 cancel；否则无界 request(MAX_VALUE)。 */
    @Override
    public void onSubscribe(@NonNull Subscription s) {
        if (subscribers.get() == TERMINATED) {
            s.cancel();
            return;
        }
        // AsyncProcessor doesn't bother with request coordination.
        s.request(Long.MAX_VALUE);
    }

    /** 非终止时覆盖缓存 value（不立即发射）。 */
    @Override
    public void onNext(@NonNull T t) {
        ExceptionHelper.nullCheck(t, "onNext called with a null value.");
        if (subscribers.get() == TERMINATED) {
            return;
        }
        value = t;
    }

    /** 清空 value、置 error，向所有订阅者 onError；已终止则 RxJavaPlugins.onError。 */
    @SuppressWarnings("unchecked")
    @Override
    public void onError(@NonNull Throwable t) {
        ExceptionHelper.nullCheck(t, "onError called with a null Throwable.");
        if (subscribers.get() == TERMINATED) {
            RxJavaPlugins.onError(t);
            return;
        }
        value = null;
        error = t;
        for (AsyncSubscription<T> as : subscribers.getAndSet(TERMINATED)) {
            as.onError(t);
        }
    }

    /** 向订阅者 complete(最后一项) 或 onComplete（无值时）。 */
    @SuppressWarnings("unchecked")
    @Override
    public void onComplete() {
        if (subscribers.get() == TERMINATED) {
            return;
        }
        T v = value;
        AsyncSubscription<T>[] array = subscribers.getAndSet(TERMINATED);
        if (v == null) {
            for (AsyncSubscription<T> as : array) {
                as.onComplete();
            }
        } else {
            for (AsyncSubscription<T> as : array) {
                as.complete(v);
            }
        }
    }

    @Override
    @CheckReturnValue
    public boolean hasSubscribers() {
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
    @Nullable
    @CheckReturnValue
    public Throwable getThrowable() {
        return subscribers.get() == TERMINATED ? error : null;
    }

    /** 注册 AsyncSubscription；已终止则 replay 错误或最后一项。 */
    @Override
    protected void subscribeActual(@NonNull Subscriber<? super T> s) {
        AsyncSubscription<T> as = new AsyncSubscription<>(s, this);
        s.onSubscribe(as);
        if (add(as)) {
            if (as.isCancelled()) {
                remove(as);
            }
        } else {
            Throwable ex = error;
            if (ex != null) {
                s.onError(ex);
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

    /** CAS 扩展 subscribers 数组；已 TERMINATED 则 false。 */
    boolean add(AsyncSubscription<T> ps) {
        for (;;) {
            AsyncSubscription<T>[] a = subscribers.get();
            if (a == TERMINATED) {
                return false;
            }

            int n = a.length;
            @SuppressWarnings("unchecked")
            AsyncSubscription<T>[] b = new AsyncSubscription[n + 1];
            System.arraycopy(a, 0, b, 0, n);
            b[n] = ps;

            if (subscribers.compareAndSet(a, b)) {
                return true;
            }
        }
    }

    /** CAS 从 subscribers 移除指定订阅包装。 */
    @SuppressWarnings("unchecked")
    void remove(AsyncSubscription<T> ps) {
        for (;;) {
            AsyncSubscription<T>[] a = subscribers.get();
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

            AsyncSubscription<T>[] b;

            if (n == 1) {
                b = EMPTY;
            } else {
                b = new AsyncSubscription[n - 1];
                System.arraycopy(a, 0, b, 0, j);
                System.arraycopy(a, j + 1, b, j, n - j - 1);
            }
            if (subscribers.compareAndSet(a, b)) {
                return;
            }
        }
    }

    /** 已终止且 value 非 null 时 true。 */
    @CheckReturnValue
    public boolean hasValue() {
        return subscribers.get() == TERMINATED && value != null;
    }

    /** 终止后返回缓存的最后一项，否则 null。 */
    @Nullable
    @CheckReturnValue
    public T getValue() {
        return subscribers.get() == TERMINATED ? value : null;
    }

    static final class AsyncSubscription<@NonNull T> extends DeferredScalarSubscription<T> {
        @Serial
        private static final long serialVersionUID = 5629876084736248016L;

        final AsyncProcessor<T> parent;

        AsyncSubscription(Subscriber<? super T> actual, AsyncProcessor<T> parent) {
            super(actual);
            this.parent = parent;
        }

        @Override
        public void cancel() {
            if (super.tryCancel()) {
                parent.remove(this);
            }
        }

        void onComplete() {
            if (!isCancelled()) {
                downstream.onComplete();
            }
        }

        void onError(Throwable t) {
            if (isCancelled()) {
                RxJavaPlugins.onError(t);
            } else {
                downstream.onError(t);
            }
        }
    }
}
