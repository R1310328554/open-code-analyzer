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

package io.reactivex.rxjava4.subscribers;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.annotations.NonNull;
import io.reactivex.rxjava4.core.FlowableSubscriber;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.internal.util.*;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 对另一个 {@link Subscriber} 的 onNext/onError/onComplete 进行串行化访问。
 *
 * <p>onSubscribe 相对其他方法未串行化，须先以非 null Subscription 完成 onSubscribe。
 *
 * <p>假定实际 Subscriber 的方法不会抛出异常。
 *
 * @param <T> 元素类型
 */
public final class SerializedSubscriber<T> implements FlowableSubscriber<T>, Subscription {
    final Subscriber<? super T> downstream;
    final boolean delayError;

    static final int QUEUE_LINK_SIZE = 4;

    Subscription upstream;

    boolean emitting;
    AppendOnlyLinkedArrayList<Object> queue;

    volatile boolean done;

    /**
     * 包装给定 {@link Subscriber} 构造 SerializedSubscriber。
     * @param downstream 实际 Subscriber（未校验非 null）
     */
    public SerializedSubscriber(Subscriber<? super T> downstream) {
        this(downstream, false);
    }

    /**
     * 包装 Subscriber 并可选择将错误延迟到缓冲中常规值全部发出后。
     * @param actual 实际 Subscriber
     * @param delayError 为 true 时错误在常规值之后发出
     */
    public SerializedSubscriber(@NonNull Subscriber<? super T> actual, boolean delayError) {
        this.downstream = actual;
        this.delayError = delayError;
    }

    /** 校验 upstream 后以自身转发 onSubscribe。 */
    @Override
    public void onSubscribe(@NonNull Subscription s) {
        if (SubscriptionHelper.validate(this.upstream, s)) {
            this.upstream = s;
            downstream.onSubscribe(this);
        }
    }

    /** 串行化转发 onNext；emitting 时入队。 */
    @Override
    public void onNext(@NonNull T t) {
        if (done) {
            return;
        }
        if (t == null) {
            upstream.cancel();
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
    public void onError(Throwable t) {
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

    /** 串行化转发 onComplete；emitting 时入队 complete 标记。 */
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

    /** 委托 upstream.request。 */
    @Override
    public void request(long n) {
        upstream.request(n);
    }

    /** 委托 upstream.cancel。 */
    @Override
    public void cancel() {
        upstream.cancel();
    }
}
