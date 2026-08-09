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
import io.reactivex.rxjava4.core.Observer;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.internal.util.EndConsumerHelper;

/**
 * 实现 {@link Disposable} 的 {@link Observer} 抽象基类：
 * 在 onNext 中可调用 {@link #dispose()} 异步取消。
 *
 * <p>仅允许单次订阅；final 方法线程安全。
 *
 * <p>回调不应抛出未检查异常，否则用 safeSubscribe。
 *
 * @param <T> 接收值类型
 */
public abstract class DisposableObserver<T> implements Observer<T>, Disposable {

    final AtomicReference<Disposable> upstream = new AtomicReference<>();

    /** setOnce 后 onStart()。 */
    @Override
    public final void onSubscribe(@NonNull Disposable d) {
        if (EndConsumerHelper.setOnce(this.upstream, d, getClass())) {
            onStart();
        }
    }

    /** 上游 Disposable 设置成功后调用。 */
    protected void onStart() {
    }

    /** upstream 是否为 DISPOSED。 */
    @Override
    public final boolean isDisposed() {
        return upstream.get() == DisposableHelper.DISPOSED;
    }

    /** DisposableHelper.dispose(upstream)。 */
    @Override
    public final void dispose() {
        DisposableHelper.dispose(upstream);
    }
}
