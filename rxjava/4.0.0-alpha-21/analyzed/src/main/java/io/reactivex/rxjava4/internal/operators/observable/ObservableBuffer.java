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
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava4.core.ObservableSource;
import io.reactivex.rxjava4.core.Observer;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Supplier;
import io.reactivex.rxjava4.internal.disposables.*;
import io.reactivex.rxjava4.internal.util.ExceptionHelper;

/**
 * 将上游元素按 count/skip 规则收集到 {@link Collection} 中并下发。
 * count==skip 时使用非重叠窗口；否则使用滑动/跳跃缓冲。
 * @param <T> 上游元素类型
 * @param <U> 缓冲区集合类型
 */
public final class ObservableBuffer<T, U extends Collection<? super T>> extends AbstractObservableWithUpstream<T, U> {
    final int count;
    final int skip;
    final Supplier<U> bufferSupplier;

    /**
     * @param source 上游 ObservableSource
     * @param count 每个缓冲区的目标元素数
     * @param skip 启动新缓冲区的步长
     * @param bufferSupplier 创建空缓冲区的工厂
     */
    public ObservableBuffer(ObservableSource<T> source, int count, int skip, Supplier<U> bufferSupplier) {
        super(source);
        this.count = count;
        this.skip = skip;
        this.bufferSupplier = bufferSupplier;
    }

    /** count==skip 时用 BufferExactObserver，否则用 BufferSkipObserver。 */
    @Override
    protected void subscribeActual(Observer<? super U> t) {
        if (skip == count) {
            BufferExactObserver<T, U> bes = new BufferExactObserver<>(t, count, bufferSupplier);
            if (bes.createBuffer()) {
                source.subscribe(bes);
            }
        } else {
            source.subscribe(new BufferSkipObserver<>(t, count, skip, bufferSupplier));
        }
    }

    /** 非重叠固定大小缓冲：满 count 即 emit 并新建缓冲。 */
    static final class BufferExactObserver<T, U extends Collection<? super T>> implements Observer<T>, Disposable {
        final Observer<? super U> downstream;
        final int count;
        final Supplier<U> bufferSupplier;
        U buffer;

        int size;

        Disposable upstream;

        BufferExactObserver(Observer<? super U> actual, int count, Supplier<U> bufferSupplier) {
            this.downstream = actual;
            this.count = count;
            this.bufferSupplier = bufferSupplier;
        }

        /** 通过 bufferSupplier 创建新缓冲；失败时 error 下游。 */
        boolean createBuffer() {
            U b;
            try {
                b = Objects.requireNonNull(bufferSupplier.get(), "Empty buffer supplied");
            } catch (Throwable t) {
                Exceptions.throwIfFatal(t);
                buffer = null;
                if (upstream == null) {
                    EmptyDisposable.error(t, downstream);
                } else {
                    upstream.dispose();
                    downstream.onError(t);
                }
                return false;
            }

            buffer = b;

            return true;
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                downstream.onSubscribe(this);
            }
        }

        @Override
        public void dispose() {
            upstream.dispose();
        }

        @Override
        public boolean isDisposed() {
            return upstream.isDisposed();
        }

        @Override
        public void onNext(T t) {
            U b = buffer;
            if (b != null) {
                b.add(t);

                if (++size >= count) {
                    downstream.onNext(b);

                    size = 0;
                    createBuffer();
                }
            }
        }

        @Override
        public void onError(Throwable t) {
            buffer = null;
            downstream.onError(t);
        }

        @Override
        public void onComplete() {
            U b = buffer;
            if (b != null) {
                buffer = null;
                if (!b.isEmpty()) {
                    downstream.onNext(b);
                }
                downstream.onComplete();
            }
        }
    }

    /** 按 skip 步长启动新缓冲，并在各活跃缓冲中累积元素。 */
    static final class BufferSkipObserver<T, U extends Collection<? super T>>
    extends AtomicBoolean implements Observer<T>, Disposable {

        @Serial
        private static final long serialVersionUID = -8223395059921494546L;
        final Observer<? super U> downstream;
        final int count;
        final int skip;
        final Supplier<U> bufferSupplier;

        Disposable upstream;

        final ArrayDeque<U> buffers;

        long index;

        BufferSkipObserver(Observer<? super U> actual, int count, int skip, Supplier<U> bufferSupplier) {
            this.downstream = actual;
            this.count = count;
            this.skip = skip;
            this.bufferSupplier = bufferSupplier;
            this.buffers = new ArrayDeque<>();
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                downstream.onSubscribe(this);
            }
        }

        @Override
        public void dispose() {
            upstream.dispose();
        }

        @Override
        public boolean isDisposed() {
            return upstream.isDisposed();
        }

        @Override
        public void onNext(T t) {
            if (index++ % skip == 0) {
                U b;

                try {
                    b = ExceptionHelper.nullCheck(bufferSupplier.get(), "The bufferSupplier returned a null Collection.");
                } catch (Throwable e) {
                    Exceptions.throwIfFatal(e);
                    buffers.clear();
                    upstream.dispose();
                    downstream.onError(e);
                    return;
                }

                buffers.offer(b);
            }

            Iterator<U> it = buffers.iterator();
            while (it.hasNext()) {
                U b = it.next();
                b.add(t);
                if (count <= b.size()) {
                    it.remove();

                    downstream.onNext(b);
                }
            }
        }

        @Override
        public void onError(Throwable t) {
            buffers.clear();
            downstream.onError(t);
        }

        /** 排空 deque 中剩余缓冲后 onComplete。 */
        @Override
        public void onComplete() {
            while (!buffers.isEmpty()) {
                downstream.onNext(buffers.poll());
            }
            downstream.onComplete();
        }
    }
}
