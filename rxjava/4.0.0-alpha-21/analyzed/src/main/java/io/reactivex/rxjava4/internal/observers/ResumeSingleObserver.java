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

package io.reactivex.rxjava4.internal.observers;

import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.core.SingleObserver;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;

/**
 * 用于订阅实际 SingleSource 并在父级 {@link AtomicReference} 中
 * 替换当前 Disposable 的 {@link SingleObserver} 实现。
 *
 * @param <T> 值类型
 */
public final class ResumeSingleObserver<T> implements SingleObserver<T> {

    final AtomicReference<Disposable> parent;

    final SingleObserver<? super T> downstream;

    /**
     * @param parent 持有上游 disposable 的原子引用
     * @param downstream 下游 SingleObserver
     */
    public ResumeSingleObserver(AtomicReference<Disposable> parent, SingleObserver<? super T> downstream) {
        this.parent = parent;
        this.downstream = downstream;
    }

    /** 将 parent 中的 disposable 替换为上游订阅。 */
    @Override
    public void onSubscribe(Disposable d) {
        DisposableHelper.replace(parent, d);
    }

    /** 将成功值转发给下游。 */
    @Override
    public void onSuccess(T value) {
        downstream.onSuccess(value);
    }

    /** 将错误转发给下游。 */
    @Override
    public void onError(Throwable e) {
        downstream.onError(e);
    }
}
