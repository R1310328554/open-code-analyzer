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

package io.reactivex.rxjava4.internal.operators.completable;

import java.io.Serial;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.*;
import io.reactivex.rxjava4.functions.Function;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;

/**
 * 上游 {@link CompletableSource} 出错时，通过 {@link Function} 映射为
 * 备用 CompletableSource 并继续订阅。
 */
public final class CompletableResumeNext extends Completable {

    final CompletableSource source;

    final Function<? super Throwable, ? extends CompletableSource> errorMapper;

    /**
     * @param source 上游 CompletableSource
     * @param errorMapper 将错误映射为备用 CompletableSource 的函数
     */
    public CompletableResumeNext(CompletableSource source,
            Function<? super Throwable, ? extends CompletableSource> errorMapper) {
        this.source = source;
        this.errorMapper = errorMapper;
    }

    /** 订阅 source；错误时通过 errorMapper 恢复并订阅备用源。 */
    @Override
    protected void subscribeActual(final CompletableObserver observer) {
        ResumeNextObserver parent = new ResumeNextObserver(observer, errorMapper);
        observer.onSubscribe(parent);
        source.subscribe(parent);
    }

    /** 出错时映射并订阅备用 CompletableSource 的内部 observer。 */
    static final class ResumeNextObserver
    extends AtomicReference<Disposable>
    implements CompletableObserver, Disposable {

        @Serial
        private static final long serialVersionUID = 5018523762564524046L;

        final CompletableObserver downstream;

        final Function<? super Throwable, ? extends CompletableSource> errorMapper;

        boolean once;

        ResumeNextObserver(CompletableObserver observer, Function<? super Throwable, ? extends CompletableSource> errorMapper) {
            this.downstream = observer;
            this.errorMapper = errorMapper;
        }

        @Override
        public void onSubscribe(Disposable d) {
            DisposableHelper.replace(this, d);
        }

        @Override
        public void onComplete() {
            downstream.onComplete();
        }

        /** 首次错误时调用 errorMapper 并订阅返回的 CompletableSource。 */
        @Override
        public void onError(Throwable e) {
            if (once) {
                downstream.onError(e);
                return;
            }
            once = true;

            CompletableSource c;

            try {
                c = Objects.requireNonNull(errorMapper.apply(e), "The errorMapper returned a null CompletableSource");
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                downstream.onError(new CompositeException(e, ex));
                return;
            }

            c.subscribe(this);
        }

        @Override
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(get());
        }

        @Override
        public void dispose() {
            DisposableHelper.dispose(this);
        }
    }
}
