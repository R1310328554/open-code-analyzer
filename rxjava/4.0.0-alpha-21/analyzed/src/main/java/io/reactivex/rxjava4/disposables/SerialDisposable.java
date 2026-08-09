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

package io.reactivex.rxjava4.disposables;

import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.annotations.Nullable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;

/**
 * Disposable 容器，允许原子地更新/替换所包含的 Disposable；
 * 更新时 dispose 旧实例，并在容器自身 dispose 时处理内部资源。
 */
public final class SerialDisposable implements Disposable {
    final AtomicReference<Disposable> resource;

    /**
     * 构造空的 SerialDisposable。
     */
    public SerialDisposable() {
        this.resource = new AtomicReference<>();
    }

    /**
     * 使用给定初始 Disposable 实例构造 SerialDisposable。
     * @param initialDisposable 要使用的初始 Disposable 实例，允许为 null
     */
    public SerialDisposable(@Nullable Disposable initialDisposable) {
        this.resource = new AtomicReference<>(initialDisposable);
    }

    /**
     * 原子操作：在本容器上设置下一个 disposable 并 dispose 前一个（若有）；
     * 若容器已被 dispose 则 dispose next。
     * @param next 要设置的 Disposable，可为 null
     * @return 操作成功则为 true；若容器已被 dispose 则为 false
     * @see #replace(Disposable)
     */
    public boolean set(@Nullable Disposable next) {
        return DisposableHelper.set(resource, next);
    }

    /**
     * 原子操作：在本容器上设置下一个 disposable 但不 dispose 前一个（若有）；
     * 若容器已被 dispose 则 dispose next。
     * @param next 要设置的 Disposable，可为 null
     * @return 操作成功则为 true；若容器已被 dispose 则为 false
     * @see #set(Disposable)
     */
    public boolean replace(@Nullable Disposable next) {
        return DisposableHelper.replace(resource, next);
    }

    /**
     * 返回当前包含的 Disposable；若本容器为空则返回 null。
     * @return 当前 Disposable，可为 null
     */
    @Nullable
    public Disposable get() {
        Disposable d = resource.get();
        if (d == DisposableHelper.DISPOSED) {
            return Disposable.disposed();
        }
        return d;
    }

    @Override
    public void dispose() {
        DisposableHelper.dispose(resource);
    }

    @Override
    public boolean isDisposed() {
        return DisposableHelper.isDisposed(resource.get());
    }
}
