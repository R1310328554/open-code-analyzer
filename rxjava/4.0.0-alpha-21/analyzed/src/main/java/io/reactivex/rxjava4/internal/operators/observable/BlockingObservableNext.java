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

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.internal.util.*;
import io.reactivex.rxjava4.observers.DisposableObserver;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 返回阻塞直到 Observable 再发射一项后才返回该项的 {@link Iterable}。
 * <p>
 * <img width="640" height="490" src="https://github.com/ReactiveX/RxJava/wiki/images/rx-operators/B.next.v3.png" alt="">
 *
 * @param <T> 元素类型
 */
public final class BlockingObservableNext<T> implements Iterable<T> {

    final ObservableSource<T> source;

    /** @param source 上游 ObservableSource */
    public BlockingObservableNext(ObservableSource<T> source) {
        this.source = source;
    }

    @Override
    public Iterator<T> iterator() {
        NextObserver<T> nextObserver = new NextObserver<>();
        return new NextIterator<>(source, nextObserver);
    }

    // 测试需访问 observer.waiting 标志
    static final class NextIterator<T> implements Iterator<T> {

        private final NextObserver<T> observer;
        private final ObservableSource<T> items;
        private T next;
        private boolean hasNext = true;
        private boolean isNextConsumed = true;
        private Throwable error;
        private boolean started;

        NextIterator(ObservableSource<T> items, NextObserver<T> observer) {
            this.items = items;
            this.observer = observer;
        }

        @Override
        public boolean hasNext() {
            if (error != null) {
                // 若已有错误则再次抛出
                throw ExceptionHelper.wrapOrThrow(error);
            }
            // 迭代器不应跨线程使用，故无需同步
            if (!hasNext) {
                // the iterator has reached the end.
                return false;
            }
            // next has not been used yet.
            return !isNextConsumed || moveToNext();
        }

        private boolean moveToNext() {
            if (!started) {
                started = true;
                // 尚未启动则立即 materialize 并订阅
                observer.setWaiting();
                new ObservableMaterialize<>(items).subscribe(observer);
            }

            Notification<T> nextNotification;

            try {
                nextNotification = observer.takeNext();
            } catch (InterruptedException e) {
                observer.dispose();
                error = e;
                throw ExceptionHelper.wrapOrThrow(e);
            }

            if (nextNotification.isOnNext()) {
                isNextConsumed = false;
                next = nextNotification.getValue();
                return true;
            }
            // Observable 完成或失败时 hasNext() 恒为 false
            hasNext = false;
            if (nextNotification.isOnComplete()) {
                return false;
            }
            error = nextNotification.getError();
            throw ExceptionHelper.wrapOrThrow(error);
        }

        @Override
        public T next() {
            if (error != null) {
                // If any error has already been thrown, throw it again.
                throw ExceptionHelper.wrapOrThrow(error);
            }
            if (hasNext()) {
                isNextConsumed = true;
                return next;
            }
            else {
                throw new NoSuchElementException("No more elements");
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Read only iterator");
        }
    }

    static final class NextObserver<T> extends DisposableObserver<Notification<T>> {
        private final BlockingQueue<Notification<T>> buf = new ArrayBlockingQueue<>(1);
        final AtomicInteger waiting = new AtomicInteger();

        @Override
        public void onComplete() {
            // materialize 流 onComplete 可忽略
        }

        @Override
        public void onError(Throwable e) {
            RxJavaPlugins.onError(e);
        }

        @Override
        public void onNext(Notification<T> args) {

            if (waiting.getAndSet(0) == 1 || !args.isOnNext()) {
                Notification<T> toOffer = args;
                while (!buf.offer(toOffer)) {
                    Notification<T> concurrentItem = buf.poll();

                    // 处理与 onComplete/onError 的竞态
                    if (concurrentItem != null && !concurrentItem.isOnNext()) {
                        toOffer = concurrentItem;
                    }
                }
            }

        }

        public Notification<T> takeNext() throws InterruptedException {
            setWaiting();
            BlockingHelper.verifyNonBlocking();
            return buf.take();
        }
        void setWaiting() {
            waiting.set(1);
        }
    }
}
