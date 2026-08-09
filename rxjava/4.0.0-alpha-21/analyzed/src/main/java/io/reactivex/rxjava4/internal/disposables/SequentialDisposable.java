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

package io.reactivex.rxjava4.internal.disposables;

import java.io.Serial;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.disposables.Disposable;

/**
 * Disposable 容器，允许原子地更新/替换 Disposable，并正确处理容器自身的 dispose。
 * <p>
 * 本类直接继承 AtomicReference，注意 API 泄漏风险！
 * @since 2.0
 */
public final class SequentialDisposable
extends AtomicReference<Disposable>
implements Disposable {

    @Serial
    private static final long serialVersionUID = -754898800686245608L;

    /**
     * 构造空的 SequentialDisposable。
     */
    public SequentialDisposable() {
        // nothing to do
    }

    /**
     * 使用给定初始 Disposable 构造 SequentialDisposable。
     * @param initial 初始 disposable，允许为 null
     */
    public SequentialDisposable(Disposable initial) {
        lazySet(initial);
    }

    /**
     * 原子操作：在本容器上设置下一个 disposable 并 dispose 前一个（若有）；
     * 若容器已被 dispose 则 dispose next。
     * @param next 要设置的 Disposable，可为 null
     * @return 操作成功则为 true；若容器已被 dispose 则为 false
     * @see #replace(Disposable)
     */
    public boolean update(Disposable next) {
        return DisposableHelper.set(this, next);
    }

    /**
     * 原子操作：在本容器上设置下一个 disposable，但不 dispose 前一个（若有）；
     * 若容器已被 dispose 则 dispose next。
     * @param next 要设置的 Disposable，可为 null
     * @return 操作成功则为 true；若容器已被 dispose 则为 false
     * @see #update(Disposable)
     */
    public boolean replace(Disposable next) {
        return DisposableHelper.replace(this, next);
    }

    @Override
    public void dispose() {
        DisposableHelper.dispose(this);
    }

    @Override
    public boolean isDisposed() {
        return DisposableHelper.isDisposed(get());
    }
}
