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

import java.util.concurrent.atomic.*;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.FlowableSubscriber;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.MissingBackpressureException;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.internal.util.*;
import io.reactivex.rxjava4.operators.SimplePlainQueue;

/**
 * 队列排空订阅者抽象基类：持有下游 Subscriber、内部队列，
 * 实现 {@link QueueDrain} 的 wip/request 与 fastPath 发射逻辑。
 *
 * @param <T> 上游元素类型
 * @param <U> 队列元素类型
 * @param <V> 下游接收类型
 */
public abstract class QueueDrainSubscriber<T, U, V> extends QueueDrainSubscriberPad4 implements FlowableSubscriber<T>, QueueDrain<U, V> {

    protected final Subscriber<? super V> downstream;

    protected final SimplePlainQueue<U> queue;

    protected volatile boolean cancelled;

    protected volatile boolean done;
    protected Throwable error;

    /** @param actual 下游 Subscriber；@param queue 缓冲队列 */
    public QueueDrainSubscriber(Subscriber<? super V> actual, SimplePlainQueue<U> queue) {
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

    /** CAS 将 wip 从 0 置 1，表示进入 drain 临界区。 */
    public final boolean fastEnter() {
        return wip.get() == 0 && wip.compareAndSet(0, 1);
    }

    /** 快速路径发射：有 request 则 accept，否则 dispose 并 MissingBackpressureException。 */
    protected final void fastPathEmitMax(U value, boolean delayError, Disposable dispose) {
        final Subscriber<? super V> s = downstream;
        final SimplePlainQueue<U> q = queue;

        if (fastEnter()) {
            long r = requested.get();
            if (r != 0L) {
                if (accept(s, value)) {
                    if (r != Long.MAX_VALUE) {
                        produced(1);
                    }
                }
                if (leave(-1) == 0) {
                    return;
                }
            } else {
                dispose.dispose();
                s.onError(MissingBackpressureException.createDefault());
                return;
            }
        } else {
            q.offer(value);
            if (!enter()) {
                return;
            }
        }
        QueueDrainHelper.drainMaxLoop(q, s, delayError, dispose, this);
    }

    /** 有序快速路径：队列为空时直接 accept，否则入队后 drainMaxLoop。 */
    protected final void fastPathOrderedEmitMax(U value, boolean delayError, Disposable dispose) {
        final Subscriber<? super V> s = downstream;
        final SimplePlainQueue<U> q = queue;

        if (fastEnter()) {
            long r = requested.get();
            if (r != 0L) {
                if (q.isEmpty()) {
                    if (accept(s, value)) {
                        if (r != Long.MAX_VALUE) {
                            produced(1);
                        }
                    }
                    if (leave(-1) == 0) {
                        return;
                    }
                } else {
                    q.offer(value);
                }
            } else {
                cancelled = true;
                dispose.dispose();
                s.onError(MissingBackpressureException.createDefault());
                return;
            }
        } else {
            q.offer(value);
            if (!enter()) {
                return;
            }
        }
        QueueDrainHelper.drainMaxLoop(q, s, delayError, dispose, this);
    }

    @Override
    public boolean accept(Subscriber<? super V> a, U v) {
        return false;
    }

    @Override
    public final Throwable error() {
        return error;
    }

    @Override
    public final int leave(int m) {
        return wip.addAndGet(m);
    }

    @Override
    public final long requested() {
        return requested.get();
    }

    @Override
    public final long produced(long n) {
        return requested.addAndGet(-n);
    }

    /** validate 后 BackpressureHelper.add 累加 request。 */
    public final void requested(long n) {
        if (SubscriptionHelper.validate(n)) {
            BackpressureHelper.add(requested, n);
        }
    }

}

// -------------------------------------------------------------------
// Padding superclasses
//-------------------------------------------------------------------

/** 缓存行填充：隔离 header 与其他字段，降低伪共享。 */
class QueueDrainSubscriberPad0 {
    volatile long p1, p2, p3, p4, p5, p6, p7;
    volatile long p8, p9, p10, p11, p12, p13, p14, p15;
}

/** 持有 wip 计数器（drain 重入保护）。 */
class QueueDrainSubscriberWip extends QueueDrainSubscriberPad0 {
    final AtomicInteger wip = new AtomicInteger();
}

/** 填充 wip 与 requested 之间的字段。 */
class QueueDrainSubscriberPad2 extends QueueDrainSubscriberWip {
    volatile long p1a, p2a, p3a, p4a, p5a, p6a, p7a;
    volatile long p8a, p9a, p10a, p11a, p12a, p13a, p14a, p15a;
}

/** 持有 requested 累计请求量。 */
class QueueDrainSubscriberPad3 extends QueueDrainSubscriberPad2 {
    final AtomicLong requested = new AtomicLong();
}

/** 填充 requested 与业务字段之间的区域。 */
class QueueDrainSubscriberPad4 extends QueueDrainSubscriberPad3 {
    volatile long q1, q2, q3, q4, q5, q6, q7;
    volatile long q8, q9, q10, q11, q12, q13, q14, q15;
}
