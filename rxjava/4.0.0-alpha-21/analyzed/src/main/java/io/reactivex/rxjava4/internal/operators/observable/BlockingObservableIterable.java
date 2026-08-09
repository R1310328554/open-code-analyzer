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

package io.reactivex.rxjava4.internal.operators.observable;

import java.io.Serial;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.*;

import io.reactivex.rxjava4.core.ObservableSource;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.internal.util.*;
import io.reactivex.rxjava4.operators.SpscLinkedArrayQueue;

/**
 * 将 {@link ObservableSource} 转为阻塞 {@link Iterable}，
 * 通过 SPSC 队列与条件变量在 hasNext/next 中等待上游事件。
 *
 * @param <T> 元素类型
 */
public final class BlockingObservableIterable<T> implements Iterable<T> {
    final ObservableSource<? extends T> source;

    final int bufferSize;

    /**
     * @param source 上游 ObservableSource
     * @param bufferSize 内部 SPSC 队列容量
     */
    public BlockingObservableIterable(ObservableSource<? extends T> source, int bufferSize) {
        this.source = source;
        this.bufferSize = bufferSize;
    }

    /** 创建 BlockingObservableIterator 并立即订阅上游。 */
    @Override
    public Iterator<T> iterator() {
        BlockingObservableIterator<T> it = new BlockingObservableIterator<>(bufferSize);
        source.subscribe(it);
        return it;
    }

    /** 阻塞迭代器：Observer 入队，Iterator 出队；hasNext 可阻塞等待。 */
    static final class BlockingObservableIterator<T>
    extends AtomicReference<Disposable>
    implements io.reactivex.rxjava4.core.Observer<T>, Iterator<T>, Disposable {

        @Serial
        private static final long serialVersionUID = 6695226475494099826L;

        final SpscLinkedArrayQueue<T> queue;

        final Lock lock;

        final Condition condition;

        volatile boolean done;
        volatile Throwable error;

        BlockingObservableIterator(int batchSize) {
            this.queue = new SpscLinkedArrayQueue<>(batchSize);
            this.lock = new ReentrantLock();
            this.condition = lock.newCondition();
        }

        @Override
        public boolean hasNext() {
            for (;;) {
                if (isDisposed()) {
                    Throwable e = error;
                    if (e != null) {
                        throw ExceptionHelper.wrapOrThrow(e);
                    }
                    return false;
                }
                boolean d = done;
                boolean empty = queue.isEmpty();
                if (d) {
                    Throwable e = error;
                    if (e != null) {
                        throw ExceptionHelper.wrapOrThrow(e);
                    } else
                    if (empty) {
                        return false;
                    }
                }
                if (empty) {
                    try {
                        BlockingHelper.verifyNonBlocking();
                        lock.lock();
                        try {
                            while (!done && queue.isEmpty() && !isDisposed()) {
                                condition.await();
                            }
                        } finally {
                            lock.unlock();
                        }
                    } catch (InterruptedException ex) {
                        DisposableHelper.dispose(this);
                        signalConsumer();
                        throw ExceptionHelper.wrapOrThrow(ex);
                    }
                } else {
                    return true;
                }
            }
        }

        @Override
        public T next() {
            if (hasNext()) {
                return queue.poll();
            }
            throw new NoSuchElementException();
        }

        @Override
        public void onSubscribe(Disposable d) {
            DisposableHelper.setOnce(this, d);
        }

        @Override
        public void onNext(T t) {
            queue.offer(t);
            signalConsumer();
        }

        @Override
        public void onError(Throwable t) {
            error = t;
            done = true;
            signalConsumer();
        }

        @Override
        public void onComplete() {
            done = true;
            signalConsumer();
        }

        /** 唤醒在 condition 上等待的 hasNext 线程。 */
        void signalConsumer() {
            lock.lock();
            try {
                condition.signalAll();
            } finally {
                lock.unlock();
            }
        }

        @Override // otherwise default method which isn't available in Java 7
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }

        @Override
        public void dispose() {
            DisposableHelper.dispose(this);
            signalConsumer(); // 以防当前正阻塞在 hasNext 中
        }

        @Override
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(get());
        }
    }
}
