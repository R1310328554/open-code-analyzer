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

import io.reactivex.rxjava4.core.Flowable;
import io.reactivex.rxjava4.internal.util.*;
import io.reactivex.rxjava4.subscribers.DefaultSubscriber;

/**
 * 返回始终给出 Observable 最近发射项的 Iterable；若尚未发射则返回种子值。
 * <p>
 * <img width="640" height="490" src="https://github.com/ReactiveX/RxJava/wiki/images/rx-operators/B.mostRecent.v3.png" alt="">
 *
 * @param <T> 值类型
 */
public final class BlockingFlowableMostRecent<T> implements Iterable<T> {

    final Flowable<T> source;

    final T initialValue;

    /**
     * @param source 上游 Flowable
     * @param initialValue 尚无发射时的初始值
     */
    public BlockingFlowableMostRecent(Flowable<T> source, T initialValue) {
        this.source = source;
        this.initialValue = initialValue;
    }

    @Override
    public Iterator<T> iterator() {
        MostRecentSubscriber<T> mostRecentSubscriber = new MostRecentSubscriber<>(initialValue);

        source.subscribe(mostRecentSubscriber);

        return mostRecentSubscriber.getIterable();
    }

    static final class MostRecentSubscriber<T> extends DefaultSubscriber<T> {
        volatile Object value;

        MostRecentSubscriber(T value) {
            this.value = NotificationLite.next(value);
        }

        @Override
        public void onComplete() {
            value = NotificationLite.complete();
        }

        @Override
        public void onError(Throwable e) {
            value = NotificationLite.error(e);
        }

        @Override
        public void onNext(T args) {
            value = NotificationLite.next(args);
        }

        /**
         * 返回的 {@link Iterator} 非线程安全；勿在一线程调用 {@link Iterator#hasNext()} 而在另一线程调用 {@link Iterator#next()}。
         * @return Iterator
         */
        public Iterator getIterable() {
            return new Iterator();
        }

        final class Iterator implements java.util.Iterator<T> {
            /** 缓冲，保证 hasNext() 与 next() 之间迭代器状态不变。 */
            private Object buf;

            @Override
            public boolean hasNext() {
                buf = value;
                return !NotificationLite.isComplete(buf);
            }

            @Override
            public T next() {
                try {
                    // if hasNext wasn't called before calling next.
                    if (buf == null) {
                        buf = value;
                    }
                    if (NotificationLite.isComplete(buf)) {
                        throw new NoSuchElementException();
                    }
                    if (NotificationLite.isError(buf)) {
                        throw ExceptionHelper.wrapOrThrow(NotificationLite.getError(buf));
                    }
                    return NotificationLite.getValue(buf);
                }
                finally {
                    buf = null;
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Read only iterator");
            }
        }
    }
}
