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
 * 显式命名的异常，表示 Reactive-Streams 协议违规。
 * <p>History: 2.0.6 - experimental; 2.1 - beta
 * @since 2.2
 */
public final class ProtocolViolationException extends IllegalStateException {

    @Serial
    private static final long serialVersionUID = 1644750035281290266L;

    /**
     * 使用给定消息创建实例。
     * @param message 消息
     */
    public ProtocolViolationException(String message) {
        super(message);
    }
}
