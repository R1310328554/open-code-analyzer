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
import io.reactivex.rxjava4.internal.fuseable.*;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 上游 onComplete 时发射 true，onSuccess 时发射 false，
 * 以 {@link Single} 形式输出。
 *
 * @param <T> 元素类型
 */
public final class MaybeIsEmptySingle<T> extends Single<Boolean>
implements HasUpstreamMaybeSource<T>, FuseToMaybe<Boolean> {

    final MaybeSource<T> source;

    /** @param source 上游 Maybe */
    public MaybeIsEmptySingle(MaybeSource<T> source) {
        this.source = source;
    }

    @Override
    public MaybeSource<T> source() {
        return source;
    }

    @Override
    public Maybe<Boolean> fuseToMaybe() {
        return RxJavaPlugins.onAssembly(new MaybeIsEmpty<>(source));
    }

    /** 订阅 IsEmptyMaybeObserver 并判断上游是否为空。 */
    @Override
    protected void subscribeActual(SingleObserver<? super Boolean> observer) {
        source.subscribe(new IsEmptyMaybeObserver<>(observer));
    }

    /** onComplete 发射 true；onSuccess 发射 false。 */
    static final class IsEmptyMaybeObserver<T>
    implements MaybeObserver<T>, Disposable {

        final SingleObserver<? super Boolean> downstream;

        Disposable upstream;

        IsEmptyMaybeObserver(SingleObserver<? super Boolean> downstream) {
            this.downstream = downstream;
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

        /** 有值，发射 false。 */
        @Override
        public void onSuccess(T value) {
            upstream = DisposableHelper.DISPOSED;
            downstream.onSuccess(false);
        }

        @Override
        public void onError(Throwable e) {
            upstream = DisposableHelper.DISPOSED;
            downstream.onError(e);
        }

        /** 空源，发射 true。 */
        @Override
        public void onComplete() {
            upstream = DisposableHelper.DISPOSED;
            downstream.onSuccess(true);
        }
    }
}
