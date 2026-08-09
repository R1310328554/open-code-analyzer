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

import java.io.Serial;

/**
 * 发往 {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable) RxJavaPlugins.onError} 的
 * Throwable 错误包装类。
 * <p>History: 2.0.6 - experimental; 2.1 - beta
 * @since 2.2
 */
public final class UndeliverableException extends IllegalStateException {

    @Serial
    private static final long serialVersionUID = 1644750035281290266L;

    /**
     * 通过包装给定、非 null 的 cause Throwable 构造实例。
     * @param cause 原因，不可为 null
     */
    public UndeliverableException(Throwable cause) {
        super("The exception could not be delivered to the consumer because it has already cancelled/disposed the flow "
                + "or the exception has nowhere to go to begin with. "
                + "Further reading: https://github.com/ReactiveX/RxJava/wiki/What's-different-in-2.0#error-handling | " + cause, cause);
    }
}
