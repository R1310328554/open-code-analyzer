/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.adapter.reactor;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.Exceptions;
import reactor.core.publisher.Operators;
import reactor.core.publisher.SignalType;

/**
 * <p>
 * 自 reactor-core 的 {@link reactor.core.publisher.BaseSubscriber} 复制而来，
 * 允许子类重写 {@code onSubscribe}、{@code onNext}、{@code onError} 与 {@code onComplete} 以定制行为。
 * </p>
 * <p>该基类还为 {@code onErrorDropped} 钩子提供谓词，作为 Sentinel 流控场景的变通方案。</p>
 */
abstract class InheritableBaseSubscriber<T> implements CoreSubscriber<T>, Subscription, Disposable {

    volatile Subscription subscription;

    static AtomicReferenceFieldUpdater<InheritableBaseSubscriber, Subscription> S =
        AtomicReferenceFieldUpdater.newUpdater(InheritableBaseSubscriber.class, Subscription.class,
            "subscription");

    /**
     * 返回当前上游 {@link Subscription}。
     *
     * @return 当前 {@link Subscription}
     */
    protected Subscription upstream() {
        return subscription;
    }

    @Override
    public boolean isDisposed() {
        return subscription == Operators.cancelledSubscription();
    }

    /**
     * 通过 {@link Subscription#cancel()} 取消订阅，等效于 {@link Disposable#dispose()}。
     */
    @Override
    public void dispose() {
        cancel();
    }

    /**
     * {@code onSubscribe} 时进一步处理上游 {@link Subscription} 的钩子。
     * 可在此调用 {@link #request(long)} 作为初始请求；若初始请求不是无界的 {@code Long.MAX_VALUE}，
     * 则通常还需在 {@link #hookOnNext(Object)} 中继续 request。
     * <p>默认行为同 {@link #requestUnbounded()}，请求无界 {@code Long.MAX_VALUE}。
     *
     * @param subscription 待处理的上游订阅
     */
    protected void hookOnSubscribe(Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
    }

    /**
     * 处理 {@code onNext} 值的钩子。若 {@link #hookOnSubscribe(Subscription) 初始请求} 非无界，
     * 可在此调用 {@link #request(long)} 向上游 {@code org.reactivestreams.Publisher} 继续拉取数据。
     * <p>默认为空实现。
     *
     * @param value 下游收到的元素
     */
    protected void hookOnNext(T value) {
        // 空操作
    }

    /**
     * 可选的完成处理钩子，默认为空实现。
     */
    protected void hookOnComplete() {
        // 空操作
    }

    /**
     * 可选的错误处理钩子，默认调用 {@link Exceptions#errorCallbackNotImplemented(Throwable)}。
     *
     * @param throwable 待处理的错误
     */
    protected void hookOnError(Throwable throwable) {
        throw Exceptions.errorCallbackNotImplemented(throwable);
    }

    /**
     * 调用本 Subscriber 的 {@link #cancel()} 取消订阅时执行的可选钩子，默认为空实现。
     */
    protected void hookOnCancel() {
        // 空操作
    }

    /**
     * 任意终止事件（onError、onComplete、cancel）之后执行的可选钩子。
     * 该钩子在 {@link #hookOnError(Throwable)}、{@link #hookOnComplete()} 与 {@link #hookOnCancel()} 之后执行，
     * 即使上述回调失败也会调用。默认为空实现；若钩子自身失败，将由
     * {@code Operators#onErrorDropped(Throwable, reactor.util.context.Context)} 捕获。
     *
     * @param type 触发该钩子的终止事件类型
     *             （{@link SignalType#ON_ERROR}、{@link SignalType#ON_COMPLETE} 或 {@link SignalType#CANCEL}）
     */
    protected void hookFinally(SignalType type) {
        // 空操作
    }

    @Override
    public void onSubscribe(Subscription s) {
        if (Operators.setOnce(S, this, s)) {
            try {
                hookOnSubscribe(s);
            } catch (Throwable throwable) {
                onError(Operators.onOperatorError(s, throwable, currentContext()));
            }
        }
    }

    @Override
    public void onNext(T value) {
        Objects.requireNonNull(value, "onNext");
        try {
            hookOnNext(value);
        } catch (Throwable throwable) {
            onError(Operators.onOperatorError(subscription, throwable, value, currentContext()));
        }
    }

    protected boolean shouldCallErrorDropHook() {
        return true;
    }

    @Override
    public void onError(Throwable t) {
        Objects.requireNonNull(t, "onError");

        if (S.getAndSet(this, Operators.cancelledSubscription()) == Operators
            .cancelledSubscription()) {
            // 订阅已被并发取消

            // Sentinel BlockException 变通处理：
            // 通过谓词方法决定异常是被静默丢弃，还是调用 {@code onErrorDropped} 钩子。
            if (shouldCallErrorDropHook()) {
                Operators.onErrorDropped(t, currentContext());
            }

            return;
        }

        try {
            hookOnError(t);
        } catch (Throwable e) {
            e = Exceptions.addSuppressed(e, t);
            Operators.onErrorDropped(e, currentContext());
        } finally {
            safeHookFinally(SignalType.ON_ERROR);
        }
    }

    @Override
    public void onComplete() {
        if (S.getAndSet(this, Operators.cancelledSubscription()) != Operators
            .cancelledSubscription()) {
            // 确认未被并发取消
            try {
                hookOnComplete();
            } catch (Throwable throwable) {
                // 上方已置为 CancelledSubscription，hookOnError 将短路
                hookOnError(Operators.onOperatorError(throwable, currentContext()));
            } finally {
                safeHookFinally(SignalType.ON_COMPLETE);
            }
        }
    }

    @Override
    public final void request(long n) {
        if (Operators.validate(n)) {
            Subscription s = this.subscription;
            if (s != null) {
                s.request(n);
            }
        }
    }

    /**
     * 以无界方式 {@link #request(long) 请求} 上游数据。
     */
    public final void requestUnbounded() {
        request(Long.MAX_VALUE);
    }

    @Override
    public final void cancel() {
        if (Operators.terminate(S, this)) {
            try {
                hookOnCancel();
            } catch (Throwable throwable) {
                hookOnError(Operators.onOperatorError(subscription, throwable, currentContext()));
            } finally {
                safeHookFinally(SignalType.CANCEL);
            }
        }
    }

    void safeHookFinally(SignalType type) {
        try {
            hookFinally(type);
        } catch (Throwable finallyFailure) {
            Operators.onErrorDropped(finallyFailure, currentContext());
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
