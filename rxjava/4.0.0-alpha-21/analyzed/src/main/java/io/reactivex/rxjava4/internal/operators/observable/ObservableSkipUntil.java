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

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.*;
import io.reactivex.rxjava4.observers.SerializedObserver;

/**
 * 在 other 发出首个信号（onNext 或 onComplete）之前丢弃主序列 onNext；
 * 之后原样转发。other 的 onError 会终止整个流。
 *
 * @param <T> 主序列元素类型
 * @param <U> 门控序列元素类型
 */
public final class ObservableSkipUntil<T, U> extends AbstractObservableWithUpstream<T, T> {
    final ObservableSource<U> other;
    public ObservableSkipUntil(ObservableSource<T> source, ObservableSource<U> other) {
        super(source);
        this.other = other;
    }

    /** 用 SerializedObserver 包装下游，ArrayCompositeDisposable 管理双路订阅。 */
    @Override
    public void subscribeActual(Observer<? super T> child) {

        final SerializedObserver<T> serial = new SerializedObserver<>(child);

        final ArrayCompositeDisposable frc = new ArrayCompositeDisposable(2);

        serial.onSubscribe(frc);

        final SkipUntilObserver<T> sus = new SkipUntilObserver<>(serial, frc);

        other.subscribe(new SkipUntil(frc, sus, serial));

        source.subscribe(sus);
    }

    /** notSkipping 由 other 置 true；notSkippingLocal 保证首个放行项也下发。 */
    static final class SkipUntilObserver<T> implements Observer<T> {

        final Observer<? super T> downstream;
        final ArrayCompositeDisposable frc;

        Disposable upstream;

        volatile boolean notSkipping;
        boolean notSkippingLocal;

        SkipUntilObserver(Observer<? super T> actual, ArrayCompositeDisposable frc) {
            this.downstream = actual;
            this.frc = frc;
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                frc.setResource(0, d);
            }
        }

        @Override
        public void onNext(T t) {
            if (notSkippingLocal) {
                downstream.onNext(t);
            } else
            if (notSkipping) {
                notSkippingLocal = true;
                downstream.onNext(t);
            }
        }

        @Override
        public void onError(Throwable t) {
            frc.dispose();
            downstream.onError(t);
        }

        @Override
        public void onComplete() {
            frc.dispose();
            downstream.onComplete();
        }
    }

    /** other 的 onNext 后 dispose 自身并打开主序列；onComplete 仅打开不 dispose。 */
    final class SkipUntil implements Observer<U> {
        final ArrayCompositeDisposable frc;
        final SkipUntilObserver<T> sus;
        final SerializedObserver<T> serial;
        Disposable upstream;

        SkipUntil(ArrayCompositeDisposable frc, SkipUntilObserver<T> sus, SerializedObserver<T> serial) {
            this.frc = frc;
            this.sus = sus;
            this.serial = serial;
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                frc.setResource(1, d);
            }
        }

        @Override
        public void onNext(U t) {
            upstream.dispose();
            sus.notSkipping = true;
        }

        @Override
        public void onError(Throwable t) {
            frc.dispose();
            serial.onError(t);
        }

        @Override
        public void onComplete() {
            sus.notSkipping = true;
        }
    }
}
