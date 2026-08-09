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

package io.reactivex.rxjava4.core;

import io.reactivex.rxjava4.annotations.NonNull;

/**
 * 函数式接口，其 {@code subscribe()} 方法接收 {@link CompletableEmitter} 实例，
 * 以便以可安全取消的方式推送事件。
 */
@FunctionalInterface
public interface CompletableOnSubscribe {

    /**
     * 每个订阅的 {@link CompletableObserver} 调用一次。
     * @param emitter 安全发射器实例，永不为 {@code null}
     * @throws Throwable 发生错误时
     */
    void subscribe(@NonNull CompletableEmitter emitter) throws Throwable;
}
