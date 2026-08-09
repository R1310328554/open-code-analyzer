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

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.*;
import io.reactivex.rxjava4.functions.Predicate;

/**
 * 订阅上游 {@link CompletableSource}；当错误满足 {@link Predicate} 时
 * 转为正常完成而非转发错误。
 */
public final class CompletableOnErrorComplete extends Completable {

    final CompletableSource source;

    final Predicate<? super Throwable> predicate;

    /**
     * @param source 上游 CompletableSource
     * @param predicate 返回 true 时将错误转为完成
     */
    public CompletableOnErrorComplete(CompletableSource source, Predicate<? super Throwable> predicate) {
        this.source = source;
        this.predicate = predicate;
    }

    /** 订阅 source 并按 predicate 过滤错误。 */
    @Override
    protected void subscribeActual(final CompletableObserver observer) {

        source.subscribe(new OnError(observer, predicate));
    }

    /** 根据 predicate 决定转发错误或转为完成的内部 observer。 */
    static final class OnError implements CompletableObserver {

        private final CompletableObserver downstream;
        private final Predicate<? super Throwable> predicate;

        OnError(CompletableObserver observer,
                Predicate<? super Throwable> predicate) {
            this.downstream = observer;
            this.predicate = predicate;
        }

        @Override
        public void onComplete() {
            downstream.onComplete();
        }

        /** predicate 为 true 时通知完成，否则转发错误。 */
        @Override
        public void onError(Throwable e) {
            boolean b;

            try {
                b = predicate.test(e);
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                downstream.onError(new CompositeException(e, ex));
                return;
            }

            if (b) {
                downstream.onComplete();
            } else {
                downstream.onError(e);
            }
        }

        @Override
        public void onSubscribe(Disposable d) {
            downstream.onSubscribe(d);
        }

    }
}
