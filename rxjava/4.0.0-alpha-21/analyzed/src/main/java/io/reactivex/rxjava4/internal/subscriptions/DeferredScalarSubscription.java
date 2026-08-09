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

package io.reactivex.rxjava4.internal.subscriptions;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.annotations.*;

import java.io.Serial;

/**
 * 延迟标量 Subscription：最终在 request 后发射单个值。
 * <p>
 * 通过 {@link #complete(Object)} 设置值；AtomicInteger 位域表示
 * 有无 value/request/cancel 及 fusion 状态（FUSED_*）。
 * @param <T> 元素类型
 */
public class DeferredScalarSubscription<@NonNull T> extends BasicIntQueueSubscription<T> {

    @Serial
    private static final long serialVersionUID = -2151279923272604993L;

    /** 接收单值的下游 Subscriber。 */
    protected final Subscriber<? super T> downstream;

    /** 尚无 request 或 fusion 模式下暂存的值。 */
    protected T value;

    /** 状态：无 value、无 request。 */
    static final int NO_REQUEST_NO_VALUE = 0;
    /** 状态：有 value、无 request。 */
    static final int NO_REQUEST_HAS_VALUE = 1;
    /** 状态：有 request、无 value。 */
    static final int HAS_REQUEST_NO_VALUE = 2;
    /** 状态：有 request 且有 value，可立即发射。 */
    static final int HAS_REQUEST_HAS_VALUE = 3;

    /** 状态：已 cancel。 */
    static final int CANCELLED = 4;

    /** fusion：空队列，等待 complete。 */
    static final int FUSED_EMPTY = 8;
    /** fusion：已有值，poll 可取。 */
    static final int FUSED_READY = 16;
    /** fusion：值已被 poll 消费。 */
    static final int FUSED_CONSUMED = 32;

    /**
     * 包装下游 Subscriber。
     * @param downstream 目标 Subscriber（未校验非 null）
     */
    public DeferredScalarSubscription(Subscriber<? super T> downstream) {
        this.downstream = downstream;
    }

    /** validate 后按状态机发射 value+onComplete 或置 HAS_REQUEST。 */
    @Override
    public final void request(long n) {
        if (SubscriptionHelper.validate(n)) {
            for (;;) {
                int state = get();
                // if any bits 1-31 are set, we are either in fusion mode (FUSED_*)
                // or request has been called (HAS_REQUEST_*)
                if ((state & ~NO_REQUEST_HAS_VALUE) != 0) {
                    return;
                }
                if (state == NO_REQUEST_HAS_VALUE) {
                    if (compareAndSet(NO_REQUEST_HAS_VALUE, HAS_REQUEST_HAS_VALUE)) {
                        T v = value;
                        if (v != null) {
                            value = null;
                            Subscriber<? super T> a = downstream;
                            a.onNext(v);
                            if (get() != CANCELLED) {
                                a.onComplete();
                            }
                        }
                    }
                    return;
                }
                if (compareAndSet(NO_REQUEST_NO_VALUE, HAS_REQUEST_NO_VALUE)) {
                    return;
                }
            }
        }
    }

    /**
     * 设置单值；有 request 则立即 onNext+onComplete，否则暂存。
     * <p>应仅调用一次。
     * @param v 待发射的值（未校验非 null）
     */
    public final void complete(T v) {
        int state = get();
        for (;;) {
            if (state == FUSED_EMPTY) {
                value = v;
                lazySet(FUSED_READY);

                Subscriber<? super T> a = downstream;
                a.onNext(null);
                if (get() != CANCELLED) {
                    a.onComplete();
                }
                return;
            }

            // if state is >= CANCELLED or bit zero is set (*_HAS_VALUE) case, return
            if ((state & ~HAS_REQUEST_NO_VALUE) != 0) {
                return;
            }

            if (state == HAS_REQUEST_NO_VALUE) {
                lazySet(HAS_REQUEST_HAS_VALUE);
                Subscriber<? super T> a = downstream;
                a.onNext(v);
                if (get() != CANCELLED) {
                    a.onComplete();
                }
                return;
            }
            value = v;
            if (compareAndSet(NO_REQUEST_NO_VALUE, NO_REQUEST_HAS_VALUE)) {
                return;
            }
            state = get();
            if (state == CANCELLED) {
                value = null;
                return;
            }
        }
    }

    /** 支持 ASYNC fusion 时置 FUSED_EMPTY。 */
    @Override
    public final int requestFusion(int mode) {
        if ((mode & ASYNC) != 0) {
            lazySet(FUSED_EMPTY);
            return ASYNC;
        }
        return NONE;
    }

    /** FUSED_READY 时取出 value 并置 FUSED_CONSUMED。 */
    @Nullable
    @Override
    public final T poll() {
        if (get() == FUSED_READY) {
            lazySet(FUSED_CONSUMED);
            T v = value;
            value = null;
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

    /** 置 CANCELLED 并清空 value。 */
    @Override
    public void cancel() {
        set(CANCELLED);
        value = null;
    }

    /**
     * Returns true if this Subscription has been cancelled.
     * @return true if this Subscription has been cancelled
     */
    public final boolean isCancelled() {
        return get() == CANCELLED;
    }

    /**
     * 原子置 cancel 并返回是否由当前线程首次取消。
     * @return 当前线程成功取消则 true
     */
    public final boolean tryCancel() {
        return getAndSet(CANCELLED) != CANCELLED;
    }
}
