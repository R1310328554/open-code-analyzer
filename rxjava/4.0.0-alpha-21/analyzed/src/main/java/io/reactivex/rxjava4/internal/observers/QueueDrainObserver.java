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

import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava4.core.Observer;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.util.*;
import io.reactivex.rxjava4.operators.SimplePlainQueue;

/**
 * 持有另一 subscriber、队列并需要 queue-drain 行为的 subscriber 抽象基类。
 *
 * @param <T> 本 subscriber 订阅的源类型
 * @param <U> 队列中的值类型
 * @param <V> 子 subscriber 接受的值类型
 */
public abstract class QueueDrainObserver<T, U, V> extends QueueDrainSubscriberPad2 implements Observer<T>, ObservableQueueDrain<U, V> {
    protected final Observer<? super V> downstream;
    protected final SimplePlainQueue<U> queue;

    protected volatile boolean cancelled;

    protected volatile boolean done;
    protected Throwable error;

    /**
     * @param actual 下游 Observer
     * @param queue 内部队列
     */
    public QueueDrainObserver(Observer<? super V> actual, SimplePlainQueue<U> queue) {
        this.downstream = actual;
        this.queue = queue;
    }

    @Override
    public final boolean cancelled() {
        return cancelled;
    }

    @Override
    public final boolean done() {
        return done;
    }

    @Override
    public final boolean enter() {
        return wip.getAndIncrement() == 0;
    }

    protected final void fastPathEmit(U value, boolean delayError, Disposable dispose) {
        final Observer<? super V> observer = downstream;
        final SimplePlainQueue<U> q = queue;

        if (wip.get() == 0 && wip.compareAndSet(0, 1)) {
            accept(observer, value);
            if (leave(-1) == 0) {
                return;
            }
        } else {
            q.offer(value);
            if (!enter()) {
                return;
            }
        }
        QueueDrainHelper.drainLoop(q, observer, delayError, dispose, this);
    }

    /**
     * 确保 fast-path 按序发射。
     * @param value 要发射或入队的值
     * @param delayError 若为 true，错误延迟到源终止后再发出
     * @param disposable drain 终止时要 dispose 的资源
     */
    protected final void fastPathOrderedEmit(U value, boolean delayError, Disposable disposable) {
        final Observer<? super V> observer = downstream;
        final SimplePlainQueue<U> q = queue;

        if (wip.get() == 0 && wip.compareAndSet(0, 1)) {
            if (q.isEmpty()) {
                accept(observer, value);
                if (leave(-1) == 0) {
                    return;
                }
            } else {
                q.offer(value);
            }
        } else {
            q.offer(value);
            if (!enter()) {
                return;
            }
        }
        QueueDrainHelper.drainLoop(q, observer, delayError, disposable, this);
    }

    @Override
    public final Throwable error() {
        return error;
    }

    @Override
    public final int leave(int m) {
        return wip.addAndGet(m);
    }

    /** 默认忽略；子类可覆盖以处理队列中的值。 */
    @Override
    public void accept(Observer<? super V> a, U v) {
        // ignored by default
    }
}

// -------------------------------------------------------------------
// Padding superclasses
//-------------------------------------------------------------------

/** 在 header 与其它字段之间填充，避免伪共享。 */
class QueueDrainSubscriberPad0 {
    volatile long p1, p2, p3, p4, p5, p6, p7;
    volatile long p8, p9, p10, p11, p12, p13, p14, p15;
}

/** work-in-progress 计数器。 */
class QueueDrainSubscriberWip extends QueueDrainSubscriberPad0 {
    final AtomicInteger wip = new AtomicInteger();
}

/** 在 wip 与其它字段之间填充，避免伪共享。 */
class QueueDrainSubscriberPad2 extends QueueDrainSubscriberWip {
    volatile long p1a, p2a, p3a, p4a, p5a, p6a, p7a;
    volatile long p8a, p9a, p10a, p11a, p12a, p13a, p14a, p15a;
}

