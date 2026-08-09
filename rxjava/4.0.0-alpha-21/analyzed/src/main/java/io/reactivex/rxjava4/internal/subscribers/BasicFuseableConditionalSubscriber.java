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

package io.reactivex.rxjava4.internal.subscribers;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.operators.ConditionalSubscriber;
import io.reactivex.rxjava4.operators.QueueSubscription;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 可融合（fuseable）中间 subscriber 的基类。
 * @param <T> 上游值类型
 * @param <R> 下游值类型
 */
public abstract class BasicFuseableConditionalSubscriber<T, R> implements ConditionalSubscriber<T>, QueueSubscription<R> {

    /** 下游 subscriber。 */
    protected final ConditionalSubscriber<? super R> downstream;

    /** 上游 subscription。 */
    protected Subscription upstream;

    /** 上游的 QueueSubscription（非 null 时）。 */
    protected QueueSubscription<T> qs;

    /** 标志：不再接受 onXXX 事件。 */
    protected boolean done;

    /** 保存上游已建立的融合模式。 */
    protected int sourceMode;

    /**
     * 通过包装给定 subscriber 构造 BasicFuseableConditionalSubscriber。
     * @param downstream subscriber，不可为 null（未校验）
     */
    public BasicFuseableConditionalSubscriber(ConditionalSubscriber<? super R> downstream) {
        this.downstream = downstream;
    }

    // final: fixed protocol steps to support fuseable and non-fuseable upstream
    @SuppressWarnings("unchecked")
    @Override
    public final void onSubscribe(Subscription s) {
        if (SubscriptionHelper.validate(this.upstream, s)) {

            this.upstream = s;
            if (s instanceof QueueSubscription) {
                this.qs = (QueueSubscription<T>)s;
            }

            if (beforeDownstream()) {

                downstream.onSubscribe(this);

                afterDownstream();
            }

        }
    }

    /**
     * 在调用 {@code actual.onSubscribe(this)} 之前执行操作，可覆盖本方法。
     * @return 若应继续 onSubscribe 调用则为 true
     */
    protected boolean beforeDownstream() {
        return true;
    }

    /**
     * 在 {@code actual.onSubscribe(this)} 调用完成后执行操作，可覆盖本方法。
     */
    protected void afterDownstream() {
        // default no-op
    }

    // -----------------------------------
    // Convenience and state-aware methods
    // -----------------------------------

    @Override
    public void onError(Throwable t) {
        if (done) {
            RxJavaPlugins.onError(t);
            return;
        }
        done = true;
        downstream.onError(t);
    }

    /**
     * 若为致命异常则重新抛出，否则调用 {@link #onError(Throwable)}。
     * @param t 要重新抛出或向实际 subscriber 发出的异常
     */
    protected final void fail(Throwable t) {
        Exceptions.throwIfFatal(t);
        upstream.cancel();
        onError(t);
    }

    @Override
    public void onComplete() {
        if (done) {
            return;
        }
        done = true;
        downstream.onComplete();
    }

    /**
     * 以给定 mode 调用上游 QueueSubscription.requestFusion，
     * 若该 mode 未设置 {@link QueueSubscription#BOUNDARY} 标志，
     * 则将已建立的模式保存到 {@link #sourceMode}。
     * <p>
     * 若上游不支持融合（{@link #qs} 为 null），返回 {@link QueueSubscription#NONE}。
     * @param mode 请求的融合模式
     * @return 已建立的融合模式
     */
    protected final int transitiveBoundaryFusion(int mode) {
        QueueSubscription<T> qs = this.qs;
        if (qs != null) {
            if ((mode & BOUNDARY) == 0) {
                int m = qs.requestFusion(mode);
                if (m != NONE) {
                    sourceMode = m;
                }
                return m;
            }
        }
        return NONE;
    }

    // --------------------------------------------------------------
    // Default implementation of the RS and QS protocol (can be overridden)
    // --------------------------------------------------------------

    @Override
    public void request(long n) {
        upstream.request(n);
    }

    @Override
    public void cancel() {
        upstream.cancel();
    }

    @Override
    public boolean isEmpty() {
        return qs.isEmpty();
    }

    @Override
    public void clear() {
        qs.clear();
    }

    // -----------------------------------------------------------
    // The rest of the Queue interface methods shouldn't be called
    // -----------------------------------------------------------

    @Override
    public final boolean offer(R e) {
        throw new UnsupportedOperationException("Should not be called!");
    }

    @Override
    public final boolean offer(R v1, R v2) {
        throw new UnsupportedOperationException("Should not be called!");
    }
}
