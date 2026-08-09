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
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 取消 {@link Future} 实例的 Disposable 容器。
 */
final class FutureDisposable extends AtomicReference<Future<?>> implements Disposable {

    @Serial
    private static final long serialVersionUID = 6545242830671168775L;

    private final boolean allowInterrupt;

    /** @param run 要管理的 Future；@param allowInterrupt 取消时是否允许中断运行中的线程 */
    FutureDisposable(Future<?> run, boolean allowInterrupt) {
        super(run);
        this.allowInterrupt = allowInterrupt;
    }

    /** 若 Future 为 null 或已完成则视为已 dispose。 */
    @Override
    public boolean isDisposed() {
        Future<?> f = get();
        return f == null || f.isDone();
    }

    /** 原子取出 Future 并取消，仅执行一次。 */
    @Override
    public void dispose() {
        Future<?> f = getAndSet(null);
        if (f != null) {
            f.cancel(allowInterrupt);
        }
    }
}
