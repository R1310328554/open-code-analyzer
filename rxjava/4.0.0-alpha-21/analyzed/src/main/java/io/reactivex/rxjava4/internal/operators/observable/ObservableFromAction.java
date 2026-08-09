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
import io.reactivex.rxjava4.functions.*;
import io.reactivex.rxjava4.internal.fuseable.CancellableQueueFuseable;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 执行 {@link Action} 并在无异常时 onComplete；
 * 异常经 onError 转发。实现 {@link Supplier} 时 get 返回 null 表示完成。
 *
 * @param <T> 元素类型（不会发射 onNext）
 * @since 3.0.0
 */
public final class ObservableFromAction<T> extends Observable<T> implements Supplier<T> {

    final Action action;

    /** @param action 订阅时执行的 Action */
    public ObservableFromAction(Action action) {
        this.action = action;
    }

    /** 经 CancellableQueueFuseable 订阅后 run action 并 onComplete/onError。 */
    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        CancellableQueueFuseable<T> qs = new CancellableQueueFuseable<>();
        observer.onSubscribe(qs);

        if (!qs.isDisposed()) {

            try {
                action.run();
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

    /** 标量路径：run action 后返回 null 表示 onComplete。 */
    @Override
    public T get() throws Throwable {
        action.run();
        return null; // considered as onComplete()
    }
}
