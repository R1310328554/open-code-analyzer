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

import java.io.Serial;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.core.Observer;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.internal.util.QueueDrainHelper;
import io.reactivex.rxjava4.operators.QueueDisposable;
import io.reactivex.rxjava4.operators.QueueSubscription;
import io.reactivex.rxjava4.operators.SimpleQueue;

/**
 * 可与上游融合的 Subscriber，在有事件可用时回调支持接口。
 *
 * @param <T> 值类型
 */
public final class InnerQueuedObserver<T>
extends AtomicReference<Disposable>
implements Observer<T>, Disposable {

    @Serial
    private static final long serialVersionUID = -5417183359794346637L;

    final InnerQueuedObserverSupport<T> parent;

    final int prefetch;

    SimpleQueue<T> queue;

    volatile boolean done;

    int fusionMode;

    /**
     * @param parent 父级支持接口
     * @param prefetch 预取数量
     */
    public InnerQueuedObserver(InnerQueuedObserverSupport<T> parent, int prefetch) {
        this.parent = parent;
        this.prefetch = prefetch;
    }

    @Override
    public void onSubscribe(Disposable d) {
        if (DisposableHelper.setOnce(this, d)) {
            if (d instanceof QueueDisposable) {
                @SuppressWarnings("unchecked")
                QueueDisposable<T> qd = (QueueDisposable<T>) d;

                int m = qd.requestFusion(QueueDisposable.ANY);
                if (m == QueueSubscription.SYNC) {
                    fusionMode = m;
                    queue = qd;
                    done = true;
                    parent.innerComplete(this);
                    return;
                }
                if (m == QueueDisposable.ASYNC) {
                    fusionMode = m;
                    queue = qd;
                    return;
                }
            }

            queue = QueueDrainHelper.createQueue(-prefetch);
        }
    }

    @Override
    public void onNext(T t) {
        if (fusionMode == QueueDisposable.NONE) {
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
    public void dispose() {
        DisposableHelper.dispose(this);
    }

    @Override
    public boolean isDisposed() {
        return DisposableHelper.isDisposed(get());
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
