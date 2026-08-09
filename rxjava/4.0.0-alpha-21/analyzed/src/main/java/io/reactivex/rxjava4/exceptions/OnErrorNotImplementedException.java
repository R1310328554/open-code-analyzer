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

package io.reactivex.rxjava4.exceptions;

import io.reactivex.rxjava4.annotations.NonNull;

import java.io.Serial;

/**
 * 表示用于向 {@code RxJavaPlugins.onError()} 发出信号的异常：
 * 基础响应式类型的基于回调的 subscribe() 方法未指定 onError 处理器。
 * <p>History: 2.0.6 - experimental; 2.1 - beta
 * @since 2.2
 */
public final class OnErrorNotImplementedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -6298857009889503852L;

    /**
     * 使用自定义消息定制 {@code Throwable} 并包装，
     * 再以 {@code OnErrorNotImplementedException} 形式通知 {@code RxJavaPlugins.onError()} 处理器。
     *
     * @param message
     *          要赋给待通知 {@code Throwable} 的消息
     * @param e
     *          要通知的 {@code Throwable}；若为 null 则构造 NullPointerException
     */
    public OnErrorNotImplementedException(String message, @NonNull Throwable e) {
        super(message, e != null ? e : new NullPointerException());
    }

    /**
     * 在将 {@code Throwable} 以 {@code OnErrorNotImplementedException} 形式
     * 通知 {@code RxJavaPlugins.onError()} 处理器之前进行包装。
     *
     * @param e
     *          要通知的 {@code Throwable}；若为 null 则构造 NullPointerException
     */
    public OnErrorNotImplementedException(@NonNull Throwable e) {
        this("The exception was not handled due to missing onError handler in the subscribe() method call. "
                + "Further reading: https://github.com/ReactiveX/RxJava/wiki/Error-Handling | " + e, e);
    }
}
