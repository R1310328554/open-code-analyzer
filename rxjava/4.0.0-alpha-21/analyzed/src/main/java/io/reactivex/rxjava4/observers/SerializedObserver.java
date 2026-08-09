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

package io.reactivex.rxjava4.observers;

import io.reactivex.rxjava4.annotations.NonNull;
import io.reactivex.rxjava4.core.Observer;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.internal.util.*;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 对另一个 {@link Observer} 的 {@link Observer#onNext(Object)}、
 * {@link Observer#onError(Throwable)} 与 {@link Observer#onComplete()} 进行串行化访问。
 *
 * <p>{@link #onSubscribe(Disposable)} 相对其他方法未串行化，
 * 须在其他方法之前以非 null {@link Disposable} 完成 onSubscribe。
 *
 * <p>假定实际 {@code Observer} 的方法不会抛出异常。
 *
 * @param <T> 值类型
 */
public final class SerializedObserver<T> implements Observer<T>, Disposable {
    final Observer<? super T> downstream;
    final boolean delayError;

    static final int QUEUE_LINK_SIZE = 4;

    Disposable upstream;

    boolean emitting;
    AppendOnlyLinkedArrayList<Object> queue;

    volatile boolean done;

    /**
     * 通过包装给定 {@link Observer} 构造 {@code SerializedObserver}。
     * @param downstream 实际 {@code Observer}，非 {@code null}（未校验）
     */
    public SerializedObserver(@NonNull Observer<? super T> downstream) {
        this(downstream, false);
    }

    /**
     * 包装给定 {@link Observer} 并可选择将错误延迟到内部缓冲中的常规值全部发出后再发出。
     * @param actual 实际 {@code Observer}，非 {@code null}（未校验）
     * @param delayError 若为 {@code true}，错误在所有常规值发出后再发出
     */
    public SerializedObserver(@NonNull Observer<? super T> actual, boolean delayError) {
        this.downstream = actual;
        this.delayError = delayError;
    }

    /** 校验 upstream 后将其设为自身并转发 onSubscribe。 */
    @Override
    public void onSubscribe(@NonNull Disposable d) {
        if (DisposableHelper.validate(this.upstream, d)) {
            this.upstream = d;

            downstream.onSubscribe(this);
        }
    }

    /** 标记 done 并 dispose 上游。 */
    @Override
    public void dispose() {
        done = true;
        upstream.dispose();
    }

    @Override
    public boolean isDisposed() {
        return upstream.isDisposed();
    }

    /** 串行化转发 onNext；若正在 emitting 则入队。 */
    @Override
    public void onNext(@NonNull T t) {
        if (done) {
            return;
        }
        if (t == null) {
            upstream.dispose();
            onError(ExceptionHelper.createNullPointerException("onNext called with a null value."));
            return;
        }
        synchronized (this) {
            if (done) {
                return;
            }
            if (emitting) {
                AppendOnlyLinkedArrayList<Object> q = queue;
                if (q == null) {
                    q = new AppendOnlyLinkedArrayList<>(QUEUE_LINK_SIZE);
                    queue = q;
                }
                q.add(NotificationLite.next(t));
                return;
            }
            emitting = true;
        }

        downstream.onNext(t);

        emitLoop();
    }

    /** 串行化转发 onError；delayError 模式下错误入队。 */
    @Override
    public void onError(@NonNull Throwable t) {
        if (done) {
            RxJavaPlugins.onError(t);
            return;
        }
        boolean reportError;
        synchronized (this) {
            if (done) {
                reportError = true;
            } else
            if (emitting) {
                done = true;
                AppendOnlyLinkedArrayList<Object> q = queue;
                if (q == null) {
                    q = new AppendOnlyLinkedArrayList<>(QUEUE_LINK_SIZE);
                    queue = q;
                }
                Object err = NotificationLite.error(t);
                if (delayError) {
                    q.add(err);
                } else {
                    q.setFirst(err);
                }
                return;
            } else {
                done = true;
                emitting = true;
                reportError = false;
            }
        }

        if (reportError) {
            RxJavaPlugins.onError(t);
            return;
        }

        downstream.onError(t);
        // no need to loop because this onError is the last event
    }

    /** 串行化转发 onComplete；若正在 emitting 则入队 complete 标记。 */
    @Override
    public void onComplete() {
        if (done) {
            return;
        }
        synchronized (this) {
            if (done) {
                return;
            }
            if (emitting) {
                AppendOnlyLinkedArrayList<Object> q = queue;
                if (q == null) {
                    q = new AppendOnlyLinkedArrayList<>(QUEUE_LINK_SIZE);
                    queue = q;
                }
                q.add(NotificationLite.complete());
                return;
            }
            done = true;
            emitting = true;
        }

        downstream.onComplete();
        // no need to loop because this onComplete is the last event
    }

    /** 排空内部队列并继续向下游发出事件。 */
    void emitLoop() {
        for (;;) {
            AppendOnlyLinkedArrayList<Object> q;
            synchronized (this) {
                q = queue;
                if (q == null) {
                    emitting = false;
                    return;
                }
                queue = null;
            }

            if (q.accept(downstream)) {
                return;
            }
        }
    }
}
