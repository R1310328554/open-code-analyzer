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

import java.io.Serial;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.core.Observer;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;

/**
 * 包装 Observer 并持有额外 Disposable 资源；
 * onError/onComplete/dispose 时一并释放 upstream 与 resource。
 * @param <T> 元素类型
 */
public final class ObserverResourceWrapper<T> extends AtomicReference<Disposable> implements Observer<T>, Disposable {

    @Serial
    private static final long serialVersionUID = -8612022020200669122L;

    final Observer<? super T> downstream;

    final AtomicReference<Disposable> upstream = new AtomicReference<>();

    public ObserverResourceWrapper(Observer<? super T> downstream) {
        this.downstream = downstream;
    }

    @Override
    public void onSubscribe(Disposable d) {
        if (DisposableHelper.setOnce(upstream, d)) {
            downstream.onSubscribe(this);
        }
    }

    @Override
    public void onNext(T t) {
        downstream.onNext(t);
    }

    /** dispose 后转发 onError。 */
    @Override
    public void onError(Throwable t) {
        dispose();
        downstream.onError(t);
    }

    @Override
    public void onComplete() {
        dispose();
        downstream.onComplete();
    }

    /** 同时 dispose upstream 与本体 AtomicReference 中的 resource。 */
    @Override
    public void dispose() {
        DisposableHelper.dispose(upstream);

        DisposableHelper.dispose(this);
    }

    @Override
    public boolean isDisposed() {
        return upstream.get() == DisposableHelper.DISPOSED;
    }

    /** 设置需随 Observer 生命周期释放的额外 Disposable。 */
    public void setResource(Disposable resource) {
        DisposableHelper.set(this, resource);
    }
}
