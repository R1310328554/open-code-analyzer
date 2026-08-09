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

package io.reactivex.rxjava4.internal.operators.streamable;

import java.io.Serial;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import io.reactivex.rxjava4.annotations.*;

/**
 * 可复用的乒乓式通知交换：一方调用 {@link #ready()} 完成 CompletableFuture，
 * 唤醒另一方在 {@link #await()} 上等待的线程。
 * @param <T> 通知传递的元素类型
 * @since 4.0.0
 */
public final class StageResumable<T> extends AtomicReference<CompletableFuture<T>>
implements BiConsumer<T, Throwable> {

    @Serial
    private static final long serialVersionUID = -7518852864146380895L;

    /**
     * 生产者通过字段或队列准备好数据后调用；
     * 对返回的 CompletableFuture 调用 complete/completeExceptionally
     * 以唤醒当前或后续 {@link #await()} 调用方。
     * @return 待完成的 {@code CompletableFuture}
     */
    @CheckReturnValue
    @NonNull
    public CompletableFuture<T> ready() {
        CompletableFuture<T> cf;
        for (;;) {
            cf = get();
            if (cf !=  null) {
                break;
            }
            cf = new CompletableFuture<>();
            if (compareAndSet(null, cf)) {
                break;
            }
        }
        return cf;
    }

    /**
     * 消费者准备接收元素时调用；
     * 返回 whenComplete(this) 包装的 CompletableFuture 以观察完成信号。
     * @return 待观察完成值或异常的 {@code CompletableFuture}
     */
    @CheckReturnValue
    @NonNull
    public CompletableFuture<T> await() {
        CompletableFuture<T> cf;
        for (;;) {
            cf = get();
            if (cf != null) {
                break;
            }
            cf = new CompletableFuture<>();
            if (compareAndSet(null, cf)) {
                break;
            }
        }
        return cf.whenComplete(this);
    }

    /// await 完成时清除等待中的 [CompletableFuture]；用户无需调用。
    /// @param t 完成值（若有），忽略
    /// @param u 异常（若有），忽略
    @Override
    public void accept(T t, Throwable u) {
        getAndSet(null);
    }
}
