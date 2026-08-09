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

import java.io.Serial;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.annotations.NonNull;

/**
 * Disposable 容器基类，管理在 dispose 时需执行操作的其它类型。
 *
 * @param <T> 包含的类型
 */
abstract class ReferenceDisposable<T> extends AtomicReference<T> implements Disposable {

    @Serial
    private static final long serialVersionUID = 6537757548749041217L;

    ReferenceDisposable(T value) {
        super(Objects.requireNonNull(value, "value is null"));
    }

    protected abstract void onDisposed(@NonNull T value);

    @Override
    public final void dispose() {
        T value = get();
        if (value != null) {
            value = getAndSet(null);
            if (value != null) {
                onDisposed(value);
            }
        }
    }

    @Override
    public final boolean isDisposed() {
        return get() == null;
    }
}
