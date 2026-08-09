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
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Supplier;
import io.reactivex.rxjava4.internal.fuseable.CancellableQueueFuseable;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 执行 {@link Runnable}：成功则 onComplete，异常则 onError；不发射 onNext。
 * 同时实现 {@link Supplier} 供标量融合路径使用。
 *
 * @param <T> 元素类型（本算子不发射元素）
 * @since 3.0.0
 */
public final class ObservableFromRunnable<T> extends Observable<T> implements Supplier<T> {

    final Runnable run;

    /** @param run 订阅时执行的 Runnable */
    public ObservableFromRunnable(Runnable run) {
        this.run = run;
    }

    /** 经 CancellableQueueFuseable 执行 run，异常或正常完成对应终止信号。 */
    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        CancellableQueueFuseable<T> qs = new CancellableQueueFuseable<>();
        observer.onSubscribe(qs);

        if (!qs.isDisposed()) {

            try {
                run.run();
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                if (!qs.isDisposed()) {
                    observer.onError(ex);
                } else {
                    RxJavaPlugins.onError(ex);
                }
                return;
            }

            if (!qs.isDisposed()) {
                observer.onComplete();
            }
        }
    }

    /** Supplier 路径：执行 run 并返回 null（视为 onComplete）。 */
    @Override
    public T get() throws Throwable {
        run.run();
        return null; // 视为 onComplete()
    }
}
