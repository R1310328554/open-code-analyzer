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
import io.reactivex.rxjava4.core.SingleObserver;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.internal.util.EndConsumerHelper;

/**
 * 实现 {@link Disposable} 的 {@link SingleObserver} 抽象基类，支持异步取消订阅。
 *
 * <p>所有预实现的 final 方法均为线程安全。
 *
 * <p>与其他 consumer 一样，{@code DisposableSingleObserver} 仅允许订阅一次；
 * 再次订阅将抛出 {@link IllegalStateException}。
 *
 * <p>{@code #onStart()}、{@link #onSuccess(Object)} 与 {@link #onError(Throwable)}
 * 的实现不得抛出未检查异常。
 *
 * @param <T> 接收值类型
 */
public abstract class DisposableSingleObserver<T> implements SingleObserver<T>, Disposable {

    final AtomicReference<Disposable> upstream = new AtomicReference<>();

    /** setOnce 成功后调用 {@link #onStart()}。 */
    @Override
    public final void onSubscribe(@NonNull Disposable d) {
        if (EndConsumerHelper.setOnce(this.upstream, d, getClass())) {
            onStart();
        }
    }

    /** 上游 {@link Disposable} 通过 {@link #onSubscribe(Disposable)} 设置成功后调用。 */
    protected void onStart() {
    }

    /** 判断 upstream 是否已为 DISPOSED。 */
    @Override
    public final boolean isDisposed() {
        return upstream.get() == DisposableHelper.DISPOSED;
    }

    /** 通过 {@link DisposableHelper#dispose} 取消上游订阅。 */
    @Override
    public final void dispose() {
        DisposableHelper.dispose(upstream);
    }
}
