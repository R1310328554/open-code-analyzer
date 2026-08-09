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

package io.reactivex.rxjava4.internal.jdk8;

import java.util.*;
import java.util.stream.Stream;

import io.reactivex.rxjava4.annotations.*;
import io.reactivex.rxjava4.core.Observable;
import io.reactivex.rxjava4.core.Observer;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.internal.disposables.EmptyDisposable;
import io.reactivex.rxjava4.operators.QueueDisposable;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 包装 {@link Stream}，将其值作为 {@link Observable} 序列发射。
 * @param <T> Stream 的元素类型
 * @since 3.0.0
 */
public final class ObservableFromStream<T> extends Observable<T> {

    final Stream<T> stream;

    /** @param stream 要包装的 Stream */
    public ObservableFromStream(Stream<T> stream) {
        this.stream = stream;
    }

    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        subscribeStream(observer, stream);
    }

    /**
     * 订阅 Stream。
     * @param <T> 流的元素类型
     * @param observer 要驱动的观察者
     * @param stream 要消费的序列
     */
    public static <T> void subscribeStream(Observer<? super T> observer, Stream<T> stream) {
        Iterator<T> iterator;
        try {
            iterator = stream.iterator();

            if (!iterator.hasNext()) {
                EmptyDisposable.complete(observer);
                closeSafely(stream);
                return;
            }
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            EmptyDisposable.error(ex, observer);
            closeSafely(stream);
            return;
        }

        StreamDisposable<T> disposable = new StreamDisposable<>(observer, iterator, stream);
        observer.onSubscribe(disposable);
        disposable.run();
    }

    /** 安全关闭 AutoCloseable，异常经 RxJavaPlugins 上报。 */
    static void closeSafely(AutoCloseable c) {
        try {
            c.close();
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            RxJavaPlugins.onError(ex);
        }
    }

    /** 从 Stream 迭代器拉取元素并向下游发射的 Disposable。 */
    static final class StreamDisposable<T> implements QueueDisposable<T> {

        final Observer<? super T> downstream;

        Iterator<T> iterator;

        AutoCloseable closeable;

        volatile boolean disposed;

        boolean once;

        boolean outputFused;

        StreamDisposable(Observer<? super T> downstream, Iterator<T> iterator, AutoCloseable closeable) {
            this.downstream = downstream;
            this.iterator = iterator;
            this.closeable = closeable;
        }

        @Override
        public void dispose() {
            disposed = true;
            run();
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }

        @Override
        public int requestFusion(int mode) {
            if ((mode & SYNC) != 0) {
                outputFused = true;
                return SYNC;
            }
            return NONE;
        }

        @Override
        public boolean offer(@NonNull T value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean offer(@NonNull T v1, @NonNull T v2) {
            throw new UnsupportedOperationException();
        }

        @Nullable
        @Override
        public T poll() {
            if (iterator == null) {
                return null;
            }
            if (!once) {
                once = true;
            } else {
                if (!iterator.hasNext()) {
                    clear();
                    return null;
                }
            }
            return Objects.requireNonNull(iterator.next(), "The Stream's Iterator.next() returned a null value");
        }

        @Override
        public boolean isEmpty() {
            Iterator<T> it = iterator;
            if (it != null) {
                if (!once || it.hasNext()) {
                    return false;
                }
                clear();
            }
            return true;
        }

        @Override
        public void clear() {
            iterator = null;
            AutoCloseable c = closeable;
            closeable = null;
            if (c != null) {
                closeSafely(c);
            }
        }

        /** 非融合模式下逐元素迭代 Stream 并通知下游。 */
        public void run() {
            if (outputFused) {
                return;
            }
            Iterator<T> iterator = this.iterator;
            Observer<? super T> downstream = this.downstream;

            for (;;) {
                if (disposed) {
                    clear();
                    break;
                }

                T next;
                try {
                    next = Objects.requireNonNull(iterator.next(), "The Stream's Iterator.next returned a null value");
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    downstream.onError(ex);
                    disposed = true;
                    continue;
                }

                if (disposed) {
                    continue;
                }

                downstream.onNext(next);

                if (disposed) {
                    continue;
                }

                try {
                    if (iterator.hasNext()) {
                        continue;
                    }
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    downstream.onError(ex);
                    disposed = true;
                    continue;
                }

                downstream.onComplete();
                disposed = true;
            }
        }
    }
}
