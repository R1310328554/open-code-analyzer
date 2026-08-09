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

package io.reactivex.rxjava4.internal.operators.flowable;

import java.io.Serial;
import java.util.concurrent.Flow.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.functions.Consumer;
import io.reactivex.rxjava4.internal.disposables.*;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 将 {@link ConnectableFlowable} 转为普通 {@link Flowable}：
 * 只要有订阅者就保持连接，订阅数归零后断开（可配置延迟）。
 *
 * @param <T> 元素类型
 */
public final class FlowableRefCount<T> extends Flowable<T> {

    final ConnectableFlowable<T> source;

    final int n;

    final long timeout;

    final TimeUnit unit;

    final Scheduler scheduler;

    RefConnection connection;

    /** @param source 待 refCount 的 ConnectableFlowable */
    public FlowableRefCount(ConnectableFlowable<T> source) {
        this(source, 1, 0L, TimeUnit.NANOSECONDS, null);
    }

    /**
     * @param source ConnectableFlowable 源
     * @param n 达到该订阅数时触发 connect
     * @param timeout 末位订阅者取消后的断开延迟
     * @param unit timeout 时间单位
     * @param scheduler 调度 timeout 的 Scheduler
     */
    public FlowableRefCount(ConnectableFlowable<T> source, int n, long timeout, TimeUnit unit,
            Scheduler scheduler) {
        this.source = source;
        this.n = n;
        this.timeout = timeout;
        this.unit = unit;
        this.scheduler = scheduler;
    }

    /** 维护 RefConnection 订阅计数，必要时 connect 上游。 */
    @Override
    protected void subscribeActual(Subscriber<? super T> s) {

        RefConnection conn;

        boolean connect = false;
        synchronized (this) {
            conn = connection;
            if (conn == null) {
                conn = new RefConnection(this);
                connection = conn;
            }

            long c = conn.subscriberCount;
            if (c == 0L && conn.timer != null) {
                conn.timer.dispose();
            }
            conn.subscriberCount = c + 1;
            if (!conn.connected && c + 1 == n) {
                connect = true;
                conn.connected = true;
            }
        }

        source.subscribe(new RefCountSubscriber<>(s, this, conn));

        if (connect) {
            source.connect(conn);
        }
    }

    /** 订阅者取消时递减计数；归零后调度或立即 timeout 断开。 */
    void cancel(RefConnection rc) {
        SequentialDisposable sd;
        synchronized (this) {
            if (connection == null || connection != rc) {
                return;
            }
            long c = rc.subscriberCount - 1;
            rc.subscriberCount = c;
            if (c != 0L || !rc.connected) {
                return;
            }
            if (timeout == 0L) {
                timeout(rc);
                return;
            }
            sd = new SequentialDisposable();
            rc.timer = sd;
        }

        sd.replace(scheduler.scheduleDirect(rc, timeout, unit));
    }

    /** 上游终止时清理 timer 并在计数归零时 reset 源。 */
    void terminated(RefConnection rc) {
        synchronized (this) {
            if (connection == rc) {
                if (rc.timer != null) {
                    rc.timer.dispose();
                    rc.timer = null;
                }
                if (--rc.subscriberCount == 0) {
                    connection = null;
                    source.reset();
                }
            }
        }
    }

    /** 延迟到期且无订阅者时 reset ConnectableFlowable。 */
    void timeout(RefConnection rc) {
        synchronized (this) {
            if (rc.subscriberCount == 0 && rc == connection) {
                connection = null;
                Disposable connectionObject = rc.get();
                DisposableHelper.dispose(rc);
                if (connectionObject == null) {
                    rc.disconnectedEarly = true;
                } else {
                    source.reset();
                }
            }
        }
    }

    /** 共享连接状态：订阅计数、connect 标志与断开 timer。 */
    static final class RefConnection extends AtomicReference<Disposable>
    implements Runnable, Consumer<Disposable> {

        @Serial
        private static final long serialVersionUID = -4552101107598366241L;

        final FlowableRefCount<?> parent;

        Disposable timer;

        long subscriberCount;

        boolean connected;

        boolean disconnectedEarly;

        RefConnection(FlowableRefCount<?> parent) {
            this.parent = parent;
        }

        /** timer 到期回调，触发 parent.timeout。 */
        @Override
        public void run() {
            parent.timeout(this);
        }

        /** connect 回调：保存 Disposable；若已 early disconnect 则 reset。 */
        @Override
        public void accept(Disposable t) {
            DisposableHelper.replace(this, t);
            synchronized (parent) {
                if (disconnectedEarly) {
                    parent.source.reset();
                }
            }
        }
    }

    /** 包装下游 Subscription，cancel/终止时通知 parent 更新连接状态。 */
    static final class RefCountSubscriber<T>
    extends AtomicBoolean implements FlowableSubscriber<T>, Subscription {

        @Serial
        private static final long serialVersionUID = -7419642935409022375L;

        final Subscriber<? super T> downstream;

        final FlowableRefCount<T> parent;

        final RefConnection connection;

        Subscription upstream;

        RefCountSubscriber(Subscriber<? super T> actual, FlowableRefCount<T> parent, RefConnection connection) {
            this.downstream = actual;
            this.parent = parent;
            this.connection = connection;
        }

        @Override
        public void onNext(T t) {
            downstream.onNext(t);
        }

        @Override
        public void onError(Throwable t) {
            if (compareAndSet(false, true)) {
                parent.terminated(connection);
                downstream.onError(t);
            } else {
                RxJavaPlugins.onError(t);
            }
        }

        @Override
        public void onComplete() {
            if (compareAndSet(false, true)) {
                parent.terminated(connection);
                downstream.onComplete();
            }
        }

        @Override
        public void request(long n) {
            upstream.request(n);
        }

        @Override
        public void cancel() {
            upstream.cancel();
            if (compareAndSet(false, true)) {
                parent.cancel(connection);
            }
        }

        @Override
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(upstream, s)) {
                this.upstream = s;

                downstream.onSubscribe(this);
            }
        }
    }
}
