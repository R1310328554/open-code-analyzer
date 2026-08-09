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

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.annotations.NonNull;
import io.reactivex.rxjava4.core.SingleObserver;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.*;
import io.reactivex.rxjava4.internal.util.EndConsumerHelper;

/**
 * 支持异步取消订阅并管理关联资源的 {@link SingleObserver} 抽象基类。
 *
 * <p>所有预实现的 final 方法均为线程安全。
 *
 * <p>在 {@code onSuccess()} 与 {@code onError()} 中应显式调用 {@link #dispose()}；
 * 通过 {@link #add(Disposable)} 关联资源，{@link #dispose()} 时一并清理。
 *
 * <p>仅允许单次订阅；回调不得抛出未检查异常。
 *
 * @param <T> 值类型
 */
public abstract class ResourceSingleObserver<T> implements SingleObserver<T>, Disposable {
    /** 当前活跃的上游订阅。 */
    private final AtomicReference<Disposable> upstream = new AtomicReference<>();

    /** 资源复合容器，永不为 null。 */
    private final ListCompositeDisposable resources = new ListCompositeDisposable();

    /**
     * 向本 {@code ResourceSingleObserver} 添加资源。
     *
     * @param resource 要添加的资源
     *
     * @throws NullPointerException 若 resource 为 {@code null}
     */
    public final void add(@NonNull Disposable resource) {
        Objects.requireNonNull(resource, "resource is null");
        resources.add(resource);
    }

    /** setOnce 成功后调用 {@link #onStart()}。 */
    @Override
    public final void onSubscribe(@NonNull Disposable d) {
        if (EndConsumerHelper.setOnce(this.upstream, d, getClass())) {
            onStart();
        }
    }

    /**
     * 上游在本 observer 上设置 {@link Disposable} 后调用。
     * <p>可在此做初始化；默认实现为空。
     */
    protected void onStart() {
    }

    /**
     * 取消主 disposable（若有）并 dispose 本 observer 关联的所有资源。
     * <p>可在上游 {@link #onSubscribe(Disposable)} 之前调用，此时主 {@link Disposable} 会立即被 dispose。
     */
    @Override
    public final void dispose() {
        if (DisposableHelper.dispose(upstream)) {
            resources.dispose();
        }
    }

    /**
     * 判断本 {@code ResourceSingleObserver} 是否已 dispose/取消。
     * @return 若已 dispose/取消则为 true
     */
    @Override
    public final boolean isDisposed() {
        return DisposableHelper.isDisposed(upstream.get());
    }
}
