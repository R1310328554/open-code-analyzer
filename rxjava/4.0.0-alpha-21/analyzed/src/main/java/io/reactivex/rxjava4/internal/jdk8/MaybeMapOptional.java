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

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Function;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;

/**
 * 将成功值映射为 {@link Optional}，若有值则发射，否则完成。
 *
 * @param <T> 上游成功值类型
 * @param <R> 结果值类型
 * @since 3.0.0
 */
public final class MaybeMapOptional<T, R> extends Maybe<R> {

    final Maybe<T> source;

    final Function<? super T, Optional<? extends R>> mapper;

    /** @param source 上游 Maybe；@param mapper 将成功值映射为 Optional 的函数 */
    public MaybeMapOptional(Maybe<T> source, Function<? super T, Optional<? extends R>> mapper) {
        this.source = source;
        this.mapper = mapper;
    }

    @Override
    protected void subscribeActual(MaybeObserver<? super R> observer) {
        source.subscribe(new MapOptionalMaybeObserver<>(observer, mapper));
    }

    /** 将上游成功值经 Optional 映射后转发给下游的 Maybe 观察者。 */
    static final class MapOptionalMaybeObserver<T, R> implements MaybeObserver<T>, Disposable {

        final MaybeObserver<? super R> downstream;

        final Function<? super T, Optional<? extends R>> mapper;

        Disposable upstream;

        MapOptionalMaybeObserver(MaybeObserver<? super R> downstream, Function<? super T, Optional<? extends R>> mapper) {
            this.downstream = downstream;
            this.mapper = mapper;
        }

        @Override
        public void dispose() {
            Disposable d = this.upstream;
            this.upstream = DisposableHelper.DISPOSED;
            d.dispose();
        }

        @Override
        public boolean isDisposed() {
            return upstream.isDisposed();
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;

                downstream.onSubscribe(this);
            }
        }

        @Override
        public void onSuccess(T value) {
            Optional<? extends R> v;

            try {
                v = Objects.requireNonNull(mapper.apply(value), "The mapper returned a null item");
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                downstream.onError(ex);
                return;
            }

            if (v.isPresent()) {
                downstream.onSuccess(v.get());
            } else {
                downstream.onComplete();
            }
        }

        @Override
        public void onError(Throwable e) {
            downstream.onError(e);
        }

        @Override
        public void onComplete() {
            downstream.onComplete();
        }
    }
}
