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

import java.io.Serial;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.FlowableSubscriber;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.internal.util.QueueDrainHelper;
import io.reactivex.rxjava4.operators.QueueSubscription;
import io.reactivex.rxjava4.operators.SimpleQueue;

/**
 * 可与上游融合的 Subscriber，在有事件可用时回调支持接口。
 *
 * @param <T> 值类型
 */
public final class InnerQueuedSubscriber<T>
extends AtomicReference<Subscription>
implements FlowableSubscriber<T>, Subscription {

    @Serial
    private static final long serialVersionUID = 22876611072430776L;

    final InnerQueuedSubscriberSupport<T> parent;

    final int prefetch;

    final int limit;

    volatile SimpleQueue<T> queue;

    volatile boolean done;

    long produced;

    int fusionMode;

    /**
     * @param parent 父级支持接口
     * @param prefetch 预取数量
     */
    public InnerQueuedSubscriber(InnerQueuedSubscriberSupport<T> parent, int prefetch) {
        this.parent = parent;
        this.prefetch = prefetch;
        this.limit = prefetch - (prefetch >> 2);
    }

    /** 尝试融合；SYNC 模式直接 innerComplete，否则创建队列并预取。 */
    @Override
    public void onSubscribe(Subscription s) {
        if (SubscriptionHelper.setOnce(this, s)) {
            if (s instanceof QueueSubscription) {
                @SuppressWarnings("unchecked")
                QueueSubscription<T> qs = (QueueSubscription<T>) s;

                int m = qs.requestFusion(QueueSubscription.ANY);
                if (m == QueueSubscription.SYNC) {
                    fusionMode = m;
                    queue = qs;
                    done = true;
                    parent.innerComplete(this);
                    return;
                }
                if (m == QueueSubscription.ASYNC) {
                    fusionMode = m;
                    queue = qs;
                    QueueDrainHelper.request(s, prefetch);
                    return;
                }
            }

            queue = QueueDrainHelper.createQueue(prefetch);

            QueueDrainHelper.request(s, prefetch);
        }
    }

    @Override
    public void onNext(T t) {
        if (fusionMode == QueueSubscription.NONE) {
            parent.innerNext(this, t);
        } else {
            parent.drain();
        }
    }

    @Override
    public void onError(Throwable t) {
        parent.innerError(this, t);
    }

    @Override
    public void onComplete() {
        parent.innerComplete(this);
    }

    @Override
    public void request(long n) {
        if (fusionMode != QueueSubscription.SYNC) {
            long p = produced + n;
            if (p >= limit) {
                produced = 0L;
                get().request(p);
            } else {
                produced = p;
            }
        }
    }

    @Override
    public void cancel() {
        SubscriptionHelper.cancel(this);
    }

    /** 若内部序列已完成则返回 true。 */
    public boolean isDone() {
        return done;
    }

    /** 将内部序列标记为已完成。 */
    public void setDone() {
        this.done = true;
    }

    /** @return 内部队列 */
    public SimpleQueue<T> queue() {
        return queue;
    }
}
