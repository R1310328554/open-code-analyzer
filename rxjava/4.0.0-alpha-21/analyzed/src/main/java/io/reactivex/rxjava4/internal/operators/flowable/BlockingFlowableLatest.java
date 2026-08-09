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

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.internal.util.*;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;
import io.reactivex.rxjava4.subscribers.DisposableSubscriber;

/**
 * 阻塞等待并迭代上游最新值；上游快于迭代器时可能跳过中间值，但不会跳过 {@code onError} 或 {@code onComplete}。
 * @param <T> 发射的值类型
 */
public final class BlockingFlowableLatest<T> implements Iterable<T> {

    final Publisher<? extends T> source;

    /** @param source 上游 Publisher */
    public BlockingFlowableLatest(Publisher<? extends T> source) {
        this.source = source;
    }

    @Override
    public Iterator<T> iterator() {
        LatestSubscriberIterator<T> lio = new LatestSubscriberIterator<>();
        Flowable.<T>fromPublisher(source).materialize().subscribe(lio);
        return lio;
    }

    /** 上游 Subscriber，对外作为 Iterator 输出。 */
    static final class LatestSubscriberIterator<T> extends DisposableSubscriber<Notification<T>> implements Iterator<T> {
        final Semaphore notify = new Semaphore(0);
        // observer's notification
        final AtomicReference<Notification<T>> value = new AtomicReference<>();

        // iterator's notification
        Notification<T> iteratorNotification;

        @Override
        public void onNext(Notification<T> args) {
            boolean wasNotAvailable = value.getAndSet(args) == null;
            if (wasNotAvailable) {
                notify.release();
            }
        }

        @Override
        public void onError(Throwable e) {
            RxJavaPlugins.onError(e);
        }

        @Override
        public void onComplete() {
            // not expected
        }

        /** 阻塞等待下一个 materialized 通知。 */
        @Override
        public boolean hasNext() {
            if (iteratorNotification != null && iteratorNotification.isOnError()) {
                throw ExceptionHelper.wrapOrThrow(iteratorNotification.getError());
            }
            if (iteratorNotification == null || iteratorNotification.isOnNext()) {
                if (iteratorNotification == null) {
                    try {
                        BlockingHelper.verifyNonBlocking();
                        notify.acquire();
                    } catch (InterruptedException ex) {
                        dispose();
                        iteratorNotification = Notification.createOnError(ex);
                        throw ExceptionHelper.wrapOrThrow(ex);
                    }

                    Notification<T> n = value.getAndSet(null);
                    iteratorNotification = n;
                    if (n.isOnError()) {
                        throw ExceptionHelper.wrapOrThrow(n.getError());
                    }
                }
            }
            return iteratorNotification.isOnNext();
        }

        /** 返回当前 onNext 通知中的值。 */
        @Override
        public T next() {
            if (hasNext()) {
                if (iteratorNotification.isOnNext()) {
                    T v = iteratorNotification.getValue();
                    iteratorNotification = null;
                    return v;
                }
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Read-only iterator.");
        }

    }
}
