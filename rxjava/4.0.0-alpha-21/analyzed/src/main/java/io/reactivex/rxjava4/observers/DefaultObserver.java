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

import io.reactivex.rxjava4.annotations.NonNull;
import io.reactivex.rxjava4.core.Observer;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.internal.util.EndConsumerHelper;

/**
 * 带同步 {@link #cancel()} 的 Observer 抽象基类：
 * onSubscribe 校验单次订阅后调用 {@link #onStart()}。
 *
 * <p>预置 final 方法线程安全；仅允许订阅一次。
 *
 * <p>在 onNext 中可调用 protected {@link #cancel()} 取消上游。
 *
 * <p>onStart/onNext/onError/onComplete 不应抛出未检查异常；
 * 否则请用 {@link io.reactivex.rxjava4.core.Observable#safeSubscribe(io.reactivex.rxjava4.core.Observer)}。
 *
 * @param <T> 元素类型
 */
public abstract class DefaultObserver<T> implements Observer<T> {

    private Disposable upstream;

    /** EndConsumerHelper.validate 通过后保存 upstream 并 onStart()。 */
    @Override
    public final void onSubscribe(@NonNull Disposable d) {
        if (EndConsumerHelper.validate(this.upstream, d, getClass())) {
            this.upstream = d;
            onStart();
        }
    }

    /** 同步 dispose 上游并将 upstream 置 DISPOSED。 */
    protected final void cancel() {
        Disposable upstream = this.upstream;
        this.upstream = DisposableHelper.DISPOSED;
        upstream.dispose();
    }
    /** 订阅建立后回调，子类可覆写做初始化。 */
    protected void onStart() {
    }

}
