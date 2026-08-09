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
import io.reactivex.rxjava4.functions.*;
import io.reactivex.rxjava4.internal.observers.DisposableLambdaObserver;

/**
 * 在订阅与 dispose 生命周期注入回调：
 * onSubscribe 收到 Disposable 时、dispose 时分别执行。
 *
 * @param <T> 元素类型
 */
public final class ObservableDoOnLifecycle<T> extends AbstractObservableWithUpstream<T, T> {
    private final Consumer<? super Disposable> onSubscribe;
    private final Action onDispose;

    /**
     * @param upstream 上游 Observable
     * @param onSubscribe 收到 Disposable 时执行的 Consumer
     * @param onDispose dispose 时执行的 Action
     */
    public ObservableDoOnLifecycle(Observable<T> upstream, Consumer<? super Disposable> onSubscribe,
            Action onDispose) {
        super(upstream);
        this.onSubscribe = onSubscribe;
        this.onDispose = onDispose;
    }

    /** 经 DisposableLambdaObserver 包装生命周期回调。 */
    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        source.subscribe(new DisposableLambdaObserver<>(observer, onSubscribe, onDispose));
    }
}
