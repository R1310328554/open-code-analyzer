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

import io.reactivex.rxjava4.core.Observer;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;

import java.io.Serial;

/**
 * 可生成 0 或 1 个结果值的可融合 Observer。
 * @param <T> 输入值类型
 * @param <R> 输出值类型
 */
public abstract class DeferredScalarObserver<T, R>
extends DeferredScalarDisposable<R>
implements Observer<T> {

    @Serial
    private static final long serialVersionUID = -266195175408988651L;

    /** 上游 disposable。 */
    protected Disposable upstream;

    /**
     * 创建 DeferredScalarObserver 实例并包装下游 Observer。
     * @param downstream 下游 subscriber，非 null（未校验）
     */
    public DeferredScalarObserver(Observer<? super R> downstream) {
        super(downstream);
    }

    @Override
    public void onSubscribe(Disposable d) {
        if (DisposableHelper.validate(this.upstream, d)) {
            this.upstream = d;

            downstream.onSubscribe(this);
        }
    }

    @Override
    public void onError(Throwable t) {
        value = null;
        error(t);
    }

    @Override
    public void onComplete() {
        R v = value;
        if (v != null) {
            value = null;
            complete(v);
        } else {
            complete();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        upstream.dispose();
    }
}
