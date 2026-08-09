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

package io.reactivex.rxjava4.observers;

import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.annotations.NonNull;
import io.reactivex.rxjava4.core.MaybeObserver;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.internal.util.EndConsumerHelper;

/**
 * 实现 {@link Disposable} 的 {@link MaybeObserver} 抽象基类。
 *
 * <p>onSuccess/onError/onComplete 互斥，onSuccess 后不会 onComplete。
 *
 * <p>仅允许单次订阅；final 方法线程安全。
 *
 * @param <T> 接收值类型
 */
public abstract class DisposableMaybeObserver<T> implements MaybeObserver<T>, Disposable {

    final AtomicReference<Disposable> upstream = new AtomicReference<>();

    /** setOnce 成功后 onStart()。 */
    @Override
    public final void onSubscribe(@NonNull Disposable d) {
        if (EndConsumerHelper.setOnce(this.upstream, d, getClass())) {
            onStart();
        }
    }

    /** 上游 Disposable 就绪后回调。 */
    protected void onStart() {
    }

    /** 是否已 dispose。 */
    @Override
    public final boolean isDisposed() {
        return upstream.get() == DisposableHelper.DISPOSED;
    }

    /** 取消上游订阅。 */
    @Override
    public final void dispose() {
        DisposableHelper.dispose(upstream);
    }
}
