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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Function;
import io.reactivex.rxjava4.internal.disposables.*;
import io.reactivex.rxjava4.subjects.PublishSubject;

/**
 * 在 selector 执行期间通过 {@link PublishSubject} 共享上游 Observable：
 * selector 返回的 ObservableSource 与上游并行订阅，上游事件经 subject 转发。
 * @param <T> 上游元素类型
 * @param <R> selector 输出类型
 */
public final class ObservablePublishSelector<T, R> extends AbstractObservableWithUpstream<T, R> {

    final Function<? super Observable<T>, ? extends ObservableSource<R>> selector;

    /**
     * @param source 上游 ObservableSource
     * @param selector 接收共享 Observable 并返回目标 ObservableSource 的函数
     */
    public ObservablePublishSelector(final ObservableSource<T> source,
                                              final Function<? super Observable<T>, ? extends ObservableSource<R>> selector) {
        super(source);
        this.selector = selector;
    }

    /** 创建 PublishSubject，应用 selector 后并行订阅 target 与 source。 */
    @Override
    protected void subscribeActual(Observer<? super R> observer) {
        PublishSubject<T> subject = PublishSubject.create();

        ObservableSource<? extends R> target;

        try {
            target = Objects.requireNonNull(selector.apply(subject), "The selector returned a null ObservableSource");
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            EmptyDisposable.error(ex, observer);
            return;
        }

        TargetObserver<R> o = new TargetObserver<>(observer);

        target.subscribe(o);

        source.subscribe(new SourceObserver<>(subject, o));
    }

    /** 将上游事件转发至 subject，onSubscribe 绑定 target 的 Disposable。 */
    record SourceObserver<T>(PublishSubject<T> subject, AtomicReference<Disposable> target) implements Observer<T> {

        @Override
            public void onSubscribe(Disposable d) {
                DisposableHelper.setOnce(target, d);
            }

            @Override
            public void onNext(T value) {
                subject.onNext(value);
            }

            @Override
            public void onError(Throwable e) {
                subject.onError(e);
            }

            @Override
            public void onComplete() {
                subject.onComplete();
            }
        }

    /** selector 侧下游 Observer：终止时 dispose 自身并转发信号。 */
    static final class TargetObserver<R>
    extends AtomicReference<Disposable> implements Observer<R>, Disposable {
        @Serial
        private static final long serialVersionUID = 854110278590336484L;

        final Observer<? super R> downstream;

        Disposable upstream;

        TargetObserver(Observer<? super R> downstream) {
            this.downstream = downstream;
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;

                downstream.onSubscribe(this);
            }
        }

        @Override
        public void onNext(R value) {
            downstream.onNext(value);
        }

        /** onError 时 dispose 自身再转发错误。 */
        @Override
        public void onError(Throwable e) {
            DisposableHelper.dispose(this);
            downstream.onError(e);
        }

        /** onComplete 时 dispose 自身再转发完成。 */
        @Override
        public void onComplete() {
            DisposableHelper.dispose(this);
            downstream.onComplete();
        }

        @Override
        public void dispose() {
            upstream.dispose();
            DisposableHelper.dispose(this);
        }

        @Override
        public boolean isDisposed() {
            return upstream.isDisposed();
        }
    }
}
