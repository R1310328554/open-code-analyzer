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
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Cancellable;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 包装 {@link Cancellable} 实例的 disposable 容器。
 * <p>
 * 注意 {@link AtomicReference} API 泄漏问题！
 */
public final class CancellableDisposable extends AtomicReference<Cancellable>
implements Disposable {

    @Serial
    private static final long serialVersionUID = 5718521705281392066L;

    /** @param cancellable 要包装的 Cancellable */
    public CancellableDisposable(Cancellable cancellable) {
        super(cancellable);
    }

    /** 若内部引用已为 null 则视为已 dispose。 */
    @Override
    public boolean isDisposed() {
        return get() == null;
    }

    /** 原子取出 Cancellable 并调用 cancel，仅执行一次。 */
    @Override
    public void dispose() {
        if (get() != null) {
            Cancellable c = getAndSet(null);
            if (c != null) {
                try {
                    c.cancel();
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    RxJavaPlugins.onError(ex);
                }
            }
        }
    }
}
