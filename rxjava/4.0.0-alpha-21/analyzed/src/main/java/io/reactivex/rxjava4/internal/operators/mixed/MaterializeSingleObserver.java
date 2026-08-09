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

package io.reactivex.rxjava4.internal.operators.mixed;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;

/**
 * 同时实现 Maybe、Single、Completable 的 Observer 接口，
 * 将各信号转为 {@link Notification} 并以 {@link SingleObserver#onSuccess} 向下游发射。
 * <p>History: 2.2.4 - experimental
 * @param <T> 源元素类型
 * @since 3.0.0
 */
public final class MaterializeSingleObserver<T>
implements SingleObserver<T>, MaybeObserver<T>, CompletableObserver, Disposable {

    final SingleObserver<? super Notification<T>> downstream;

    Disposable upstream;

    /** @param downstream 接收 Notification 的 SingleObserver */
    public MaterializeSingleObserver(SingleObserver<? super Notification<T>> downstream) {
        this.downstream = downstream;
    }

    @Override
    public void onSubscribe(Disposable d) {
        if (DisposableHelper.validate(upstream, d)) {
            this.upstream = d;
            downstream.onSubscribe(this);
        }
    }

    /** onComplete 转为 Notification.createOnComplete 并 onSuccess。 */
    @Override
    public void onComplete() {
        downstream.onSuccess(Notification.createOnComplete());
    }

    /** onSuccess 转为 Notification.createOnNext 并 onSuccess。 */
    @Override
    public void onSuccess(T t) {
        downstream.onSuccess(Notification.createOnNext(t));
    }

    /** onError 转为 Notification.createOnError 并 onSuccess。 */
    @Override
    public void onError(Throwable e) {
        downstream.onSuccess(Notification.createOnError(e));
    }

    @Override
    public boolean isDisposed() {
        return upstream.isDisposed();
    }

    @Override
    public void dispose() {
        upstream.dispose();
    }
}
