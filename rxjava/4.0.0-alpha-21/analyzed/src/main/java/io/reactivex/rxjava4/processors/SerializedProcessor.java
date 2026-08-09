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

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.annotations.Nullable;
import io.reactivex.rxjava4.internal.util.*;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 串行化对 Subscriber 方法的调用。
 * <p>其余 Publisher 与 Subject 方法按设计已为线程安全。
 *
 * @param <T> 元素值类型
 */
/* public */ final class SerializedProcessor<T> extends FlowableProcessor<T> {
    /** 实际接收串行化 Subscriber 调用的处理器。 */
    final FlowableProcessor<T> actual;
    /** 表示正在发射，由本对象监视器保护。 */
    boolean emitting;
    /** 非 null 时保存错过的 NotificationLite 事件。 */
    AppendOnlyLinkedArrayList<Object> queue;
    /** 已收到终止事件，后续事件将被丢弃。 */
    volatile boolean done;

    /**
     * 包装实际 subject 的构造器。
     * @param actual 被包装的 subject
     */
    SerializedProcessor(final FlowableProcessor<T> actual) {
        this.actual = actual;
    }

    @Override
    protected void subscribeActual(Subscriber<? super T> s) {
        actual.subscribe(s);
    }

    @Override
    public void onSubscribe(Subscription s) {
        boolean cancel;
        if (!done) {
            synchronized (this) {
                if (done) {
                    cancel = true;
                } else {
                    if (emitting) {
                        AppendOnlyLinkedArrayList<Object> q = queue;
                        if (q == null) {
                            q = new AppendOnlyLinkedArrayList<>(4);
                            queue = q;
                        }
                        q.add(NotificationLite.subscription(s));
                        return;
                    }
                    emitting = true;
                    cancel = false;
                }
            }
        } else {
            cancel = true;
        }
        if (cancel) {
            s.cancel();
        } else {
            actual.onSubscribe(s);
            emitLoop();
        }
    }

    @Override
    public void onNext(T t) {
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
                    q = new AppendOnlyLinkedArrayList<>(4);
                    queue = q;
                }
                q.add(NotificationLite.next(t));
                return;
            }
            emitting = true;
        }
        actual.onNext(t);
        emitLoop();
    }

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
            } else {
                done = true;
                if (emitting) {
                    AppendOnlyLinkedArrayList<Object> q = queue;
                    if (q == null) {
                        q = new AppendOnlyLinkedArrayList<>(4);
                        queue = q;
                    }
                    q.setFirst(NotificationLite.error(t));
                    return;
                }
                reportError = false;
                emitting = true;
            }
        }
        if (reportError) {
            RxJavaPlugins.onError(t);
            return;
        }
        actual.onError(t);
    }

    @Override
    public void onComplete() {
        if (done) {
            return;
        }
        synchronized (this) {
            if (done) {
                return;
            }
            done = true;
            if (emitting) {
                AppendOnlyLinkedArrayList<Object> q = queue;
                if (q == null) {
                    q = new AppendOnlyLinkedArrayList<>(4);
                    queue = q;
                }
                q.add(NotificationLite.complete());
                return;
            }
            emitting = true;
        }
        actual.onComplete();
    }

    /** 循环处理队列中所有通知直至清空。 */
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

            q.accept(actual);
        }
    }

    @Override
    public boolean hasSubscribers() {
        return actual.hasSubscribers();
    }

    @Override
    public boolean hasThrowable() {
        return actual.hasThrowable();
    }

    @Override
    @Nullable
    public Throwable getThrowable() {
        return actual.getThrowable();
    }

    @Override
    public boolean hasComplete() {
        return actual.hasComplete();
    }
}
