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
 * 表示算子尝试发射值，但下游尚未准备好接收。
 */
public final class MissingBackpressureException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 8517344746016032542L;

    /**
     * 默认错误消息。
     * <p>
     * 若下游未及时或未调用 {@link java.util.concurrent.Flow.Subscription#request(long)} 可能发生此情况。
     * @since 3.1.6
     */
    public static final String DEFAULT_MESSAGE = "Could not emit value due to lack of requests";

    /**
     * 构造无消息与 cause 的 MissingBackpressureException。
     */
    public MissingBackpressureException() {
        // no message
    }

    /**
     * 使用给定消息、无 cause 构造 MissingBackpressureException。
     * @param message 错误消息
     */
    public MissingBackpressureException(String message) {
        super(message);
    }

    /**
     * 使用默认消息 {@value #DEFAULT_MESSAGE} 构造新的 {@code MissingBackpressureException}。
     * @return 新的 {@code MissingBackpressureException} 实例。
     * @since 3.1.6
     */
    public static MissingBackpressureException createDefault() {
        return new MissingBackpressureException(DEFAULT_MESSAGE);
    }
}
