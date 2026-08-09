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
import io.reactivex.rxjava4.disposables.*;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Action;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 订阅时同步执行 {@link Action}，成功则 onComplete，异常则 onError。
 */
public final class CompletableFromAction extends Completable {

    final Action run;

    /** @param run 要执行的 action */
    public CompletableFromAction(Action run) {
        this.run = run;
    }

    /** 执行 action 并根据结果完成或出错。 */
    @Override
    protected void subscribeActual(CompletableObserver observer) {
        Disposable d = Disposable.empty();
        observer.onSubscribe(d);
        if (!d.isDisposed()) {
            try {
                run.run();
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                if (!d.isDisposed()) {
                    observer.onError(e);
                } else {
                    RxJavaPlugins.onError(e);
                }
                return;
            }
            if (!d.isDisposed()) {
                observer.onComplete();
            }
        }
    }

}
