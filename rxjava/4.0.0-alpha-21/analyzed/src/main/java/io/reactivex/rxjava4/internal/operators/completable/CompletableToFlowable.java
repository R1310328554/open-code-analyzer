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

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.internal.operators.flowable.FlowableFromCompletable;

/**
 * 将 {@link CompletableSource} 包装为 {@link Flowable}；完成时 onComplete，出错时 onError，不发射元素。
 * @param <T> Flowable 元素类型
 */
public final class CompletableToFlowable<T> extends Flowable<T> {

    final CompletableSource source;

    /** @param source 上游 CompletableSource */
    public CompletableToFlowable(CompletableSource source) {
        this.source = source;
    }

    /** 通过 {@link FlowableFromCompletable.FromCompletableObserver} 订阅上游。 */
    @Override
    protected void subscribeActual(Subscriber<? super T> s) {
        source.subscribe(new FlowableFromCompletable.FromCompletableObserver<>(s));
    }
}
