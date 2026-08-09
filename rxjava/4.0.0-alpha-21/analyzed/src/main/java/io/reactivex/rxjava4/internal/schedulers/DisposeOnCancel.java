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

package io.reactivex.rxjava4.internal.schedulers;

import java.util.concurrent.*;

import io.reactivex.rxjava4.annotations.NonNull;
import io.reactivex.rxjava4.disposables.Disposable;

/**
 * 实现 {@link Future}：{@link #cancel} 时调用 upstream.dispose()，
 * 其余 Future 方法为占位实现（始终未完成/未取消）。
 */
/** @param upstream cancel 时要 dispose 的 Disposable */
record DisposeOnCancel(Disposable upstream) implements Future<Object> {

    /** 调用 upstream.dispose()；Future 语义上仍返回 false。 */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        upstream.dispose();
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public Object get() {
        return null;
    }

    @Override
    public Object get(long timeout, @NonNull TimeUnit unit) {
        return null;
    }
}
