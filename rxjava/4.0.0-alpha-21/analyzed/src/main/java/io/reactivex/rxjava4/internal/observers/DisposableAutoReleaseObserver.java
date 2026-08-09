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

/*
 * Copyright 2016-2019 David Karnok
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.reactivex.rxjava4.internal.observers;

import io.reactivex.rxjava4.core.Observer;
import io.reactivex.rxjava4.disposables.DisposableContainer;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.*;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;

import java.io.Serial;

/**
 * 包装 lambda 回调；当上游终止或本 observer 被 dispose 时，
 * 从 {@link io.reactivex.rxjava4.disposables.CompositeDisposable} 中移除自身。
 * <p>History: 0.18.0 @ RxJavaExtensions
 * @param <T> 消费的元素类型
 * @since 3.1.0
 */
public final class DisposableAutoReleaseObserver<T>
extends AbstractDisposableAutoRelease
implements Observer<T> {

    @Serial
    private static final long serialVersionUID = 8924480688481408726L;

    final Consumer<? super T> onNext;

    /**
     * @param composite 要从中移除自身的复合容器
     * @param onNext 下一项回调
     * @param onError 错误回调
     * @param onComplete 完成回调
     */
    public DisposableAutoReleaseObserver(
            DisposableContainer composite,
            Consumer<? super T> onNext,
            Consumer<? super Throwable> onError,
            Action onComplete
    ) {
        super(composite, onError, onComplete);
        this.onNext = onNext;
    }

    /** 调用 onNext 回调；若回调抛出异常则 dispose 并转发 onError。 */
    @Override
    public void onNext(T t) {
        if (get() != DisposableHelper.DISPOSED) {
            try {
                onNext.accept(t);
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                get().dispose();
                onError(e);
            }
        }
    }

}
