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
 * 表示因上游完全无视背压，或从多个线程并发、未同步地调用
 * {@link java.util.concurrent.Flow.Subscriber#onNext(Object)} 而发生队列溢出。
 * 极少数情况下表示算子内部存在 bug。
 * @since 3.1.6
 */
public final class QueueOverflowException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 8517344746016032542L;

    /**
     * 队列溢出时的默认消息。
     * <p>
     * 若上游完全无视背压，或从多个线程并发、未同步地调用
     * {@link java.util.concurrent.Flow.Subscriber#onNext(Object)} 可能发生此情况。
     * 极少数情况下表示算子内部存在 bug。
     */
    private static final String DEFAULT_MESSAGE = "Queue overflow due to illegal concurrent onNext calls or a bug in an operator";

    /**
     * 使用默认消息构造 QueueOverflowException。
     */
    public QueueOverflowException() {
        this(DEFAULT_MESSAGE);
    }

    /**
     * 使用给定消息、无 cause 构造 QueueOverflowException。
     * @param message 错误消息
     */
    public QueueOverflowException(String message) {
        super(message);
    }
}
