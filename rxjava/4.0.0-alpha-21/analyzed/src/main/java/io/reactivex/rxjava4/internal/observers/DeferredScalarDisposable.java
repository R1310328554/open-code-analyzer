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

package io.reactivex.rxjava4.internal.observers;

import io.reactivex.rxjava4.annotations.Nullable;
import io.reactivex.rxjava4.core.Observer;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

import java.io.Serial;

/**
 * 表示可融合（fuseable）的单值容器。
 *
 * @param <T> 接收并发射的值类型
 */
public class DeferredScalarDisposable<T> extends BasicIntQueueDisposable<T> {

    @Serial
    private static final long serialVersionUID = -5502432239815349361L;

    /** 事件的目标 Observer。 */
    protected final Observer<? super T> downstream;

    /** 融合模式下临时存储的值。 */
    protected T value;

    /** 表示曾调用 complete(T)。 */
    static final int TERMINATED = 2;

    /** 表示 Disposable 已被 dispose。 */
    static final int DISPOSED = 4;

    /** 表示本 Disposable 处于融合模式且当前为空。 */
    static final int FUSED_EMPTY = 8;
    /** 表示本 Disposable 处于融合模式且已有值。 */
    static final int FUSED_READY = 16;
    /** 表示本 Disposable 处于融合模式且其值已被消费。 */
    static final int FUSED_CONSUMED = 32;

    /**
     * 通过包装 Observer 构造 DeferredScalarDisposable。
     * @param downstream 要包装的 Observer，非 null（未校验）
     */
    public DeferredScalarDisposable(Observer<? super T> downstream) {
        this.downstream = downstream;
    }

    @Override
    public final int requestFusion(int mode) {
        if ((mode & ASYNC) != 0) {
            lazySet(FUSED_EMPTY);
            return ASYNC;
        }
        return NONE;
    }

    /**
     * 以单个值完成目标，或在融合模式下指示已有可用值。
     * @param value 要发出的值，非 null（未校验）
     */
    public final void complete(T value) {
        int state = get();
        if ((state & (FUSED_READY | FUSED_CONSUMED | TERMINATED | DISPOSED)) != 0) {
            return;
        }
        Observer<? super T> a = downstream;
        if (state == FUSED_EMPTY) {
            this.value = value;
            lazySet(FUSED_READY);
            a.onNext(null);
        } else {
            lazySet(TERMINATED);
            a.onNext(value);
        }
        if (get() != DISPOSED) {
            a.onComplete();
        }
    }

    /**
     * 以错误信号完成目标。
     * @param t 要发出的 Throwable，非 null（未校验）
     */
    public final void error(Throwable t) {
        int state = get();
        if ((state & (FUSED_READY | FUSED_CONSUMED | TERMINATED | DISPOSED)) != 0) {
            RxJavaPlugins.onError(t);
            return;
        }
        lazySet(TERMINATED);
        downstream.onError(t);
    }

    /**
     * 无值完成目标。
     */
    public final void complete() {
        int state = get();
        if ((state & (FUSED_READY | FUSED_CONSUMED | TERMINATED | DISPOSED)) != 0) {
            return;
        }
        lazySet(TERMINATED);
        downstream.onComplete();
    }

    @Nullable
    @Override
    public final T poll() {
        if (get() == FUSED_READY) {
            T v = value;
            value = null;
            lazySet(FUSED_CONSUMED);
            return v;
        }
        return null;
    }

    @Override
    public final boolean isEmpty() {
        return get() != FUSED_READY;
    }

    @Override
    public final void clear() {
        lazySet(FUSED_CONSUMED);
        value = null;
    }

    @Override
    public void dispose() {
        set(DISPOSED);
        value = null;
    }

    /**
     * 尝试 dispose 本 Disposable；若当前线程成功则返回 true。
     * @return 若当前线程成功则为 true
     */
    public final boolean tryDispose() {
        return getAndSet(DISPOSED) != DISPOSED;
    }

    @Override
    public final boolean isDisposed() {
        return get() == DISPOSED;
    }

}
