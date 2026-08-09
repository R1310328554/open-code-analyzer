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

import io.reactivex.rxjava4.annotations.NonNull;

import java.io.Serial;

/**
 * 管理 {@link Runnable} 实例的 disposable 容器。
 */
final class RunnableDisposable extends ReferenceDisposable<Runnable> {

    @Serial
    private static final long serialVersionUID = -8219729196779211169L;

    /** @param value 要在 dispose 时执行的 Runnable */
    RunnableDisposable(Runnable value) {
        super(value);
    }

    /** dispose 时运行所持有的 Runnable。 */
    @Override
    protected void onDisposed(@NonNull Runnable value) {
        value.run();
    }

    /** 返回包含 disposed 状态与 Runnable 的调试字符串。 */
    @Override
    public String toString() {
        return "RunnableDisposable(disposed=" + isDisposed() + ", " + get() + ")";
    }
}
