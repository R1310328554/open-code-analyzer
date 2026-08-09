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
 * 具有 {@code subscribe()} 方法的函数式接口，接收 {@link FlowableEmitter} 实例，
 * 以背压安全且取消安全的方式推送事件。
 *
 * @param <T> 推送的值类型
 */
@FunctionalInterface
public interface FlowableOnSubscribe<@NonNull T> {

    /**
     * 对每个订阅的 {@link java.util.concurrent.Flow.Subscriber Subscriber} 调用。
     * @param emitter 安全的 emitter 实例，永不为 {@code null}
     * @throws Throwable 出错时抛出
     */
    void subscribe(@NonNull FlowableEmitter<T> emitter) throws Throwable;
}

