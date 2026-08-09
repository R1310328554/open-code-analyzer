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

package io.reactivex.rxjava4.internal.operators.maybe;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.internal.fuseable.HasUpstreamMaybeSource;
import java.util.Objects;

/**
 * 若上游发射的值与给定值 {@link Objects#equals} 相等则发射 true，
 * 否则或上游为空时发射 false。
 *
 * @param <T> 元素类型
 */
public final class MaybeContains<T> extends Single<Boolean> implements HasUpstreamMaybeSource<T> {

    final MaybeSource<T> source;

    final Object value;

    /**
     * @param source 上游 Maybe
     * @param value 待比较的目标值
     */
    public MaybeContains(MaybeSource<T> source, Object value) {
        this.source = source;
        this.value = value;
    }

    @Override
    public MaybeSource<T> source() {
        return source;
    }

    /** 订阅上游并在 onSuccess 时比较 equals。 */
    @Override
    protected void subscribeActual(SingleObserver<? super Boolean> observer) {
        source.subscribe(new ContainsMaybeObserver(observer, value));
    }

    /** onSuccess 比较 equals；onComplete 发射 false。 */
    static final class ContainsMaybeObserver implements MaybeObserver<Object>, Disposable {

        final SingleObserver<? super Boolean> downstream;

        final Object value;

        Disposable upstream;

        ContainsMaybeObserver(SingleObserver<? super Boolean> actual, Object value) {
            this.downstream = actual;
            this.value = value;
        }

        @Override
        public void dispose() {
            upstream.dispose();
            upstream = DisposableHelper.DISPOSED;
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

        /** 比较上游值与目标值并发射 Boolean 结果。 */
        @Override
        public void onSuccess(Object value) {
            upstream = DisposableHelper.DISPOSED;
            downstream.onSuccess(Objects.equals(value, this.value));
        }

        @Override
        public void onError(Throwable e) {
            upstream = DisposableHelper.DISPOSED;
            downstream.onError(e);
        }

        /** 空源视为未包含，发射 false。 */
        @Override
        public void onComplete() {
            upstream = DisposableHelper.DISPOSED;
            downstream.onSuccess(false);
        }
    }
}
