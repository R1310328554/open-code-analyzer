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

import java.io.Serial;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.internal.fuseable.HasUpstreamMaybeSource;

/**
 * 主 Maybe 为空（onComplete）时订阅备用 {@link SingleSource}，
 * 以 {@link Single} 形式保证必有 onSuccess/onError。
 *
 * @param <T> 元素类型
 */
public final class MaybeSwitchIfEmptySingle<T> extends Single<T> implements HasUpstreamMaybeSource<T> {

    final MaybeSource<T> source;
    final SingleSource<? extends T> other;

    /**
     * @param source 主 MaybeSource
     * @param other 主源为空时的备用 SingleSource
     */
    public MaybeSwitchIfEmptySingle(MaybeSource<T> source, SingleSource<? extends T> other) {
        this.source = source;
        this.other = other;
    }

    /** 返回上游 MaybeSource。 */
    @Override
    public MaybeSource<T> source() {
        return source;
    }

    /** SwitchIfEmptyMaybeObserver 在 onComplete 时切换 Single。 */
    @Override
    protected void subscribeActual(SingleObserver<? super T> observer) {
        source.subscribe(new SwitchIfEmptyMaybeObserver<>(observer, other));
    }

    /** onComplete 时 CAS 并订阅 other SingleSource。 */
    static final class SwitchIfEmptyMaybeObserver<T>
    extends AtomicReference<Disposable>
    implements MaybeObserver<T>, Disposable {

        @Serial
        private static final long serialVersionUID = 4603919676453758899L;

        final SingleObserver<? super T> downstream;

        final SingleSource<? extends T> other;

        SwitchIfEmptyMaybeObserver(SingleObserver<? super T> actual, SingleSource<? extends T> other) {
            this.downstream = actual;
            this.other = other;
        }

        @Override
        public void dispose() {
            DisposableHelper.dispose(this);
        }

        @Override
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(get());
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.setOnce(this, d)) {
                downstream.onSubscribe(this);
            }
        }

        @Override
        public void onSuccess(T value) {
            downstream.onSuccess(value);
        }

        @Override
        public void onError(Throwable e) {
            downstream.onError(e);
        }

        @Override
        public void onComplete() {
            Disposable d = get();
            if (d != DisposableHelper.DISPOSED) {
                if (compareAndSet(d, null)) {
                    other.subscribe(new OtherSingleObserver<T>(downstream, this));
                }
            }
        }

        /** 备用 Single 结果 relay 到 downstream。 */
        record OtherSingleObserver<T>(SingleObserver<? super T> downstream,
                                      AtomicReference<Disposable> parent) implements SingleObserver<T> {

            @Override
                    public void onSubscribe(Disposable d) {
                        DisposableHelper.setOnce(parent, d);
                    }

                    @Override
                    public void onSuccess(T value) {
                        downstream.onSuccess(value);
                    }

                    @Override
                    public void onError(Throwable e) {
                        downstream.onError(e);
                    }
                }

    }
}
