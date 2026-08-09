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

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.annotations.NonNull;

/**
 * 表示受 Reactive Streams 启发的 {@link Subscriber}，仅用于 RxJava 4，
 * 为提升性能而放宽规范 <a href='https://github.com/reactive-streams/reactive-streams-jvm#1.3'>§1.3</a>
 * 与 <a href='https://github.com/reactive-streams/reactive-streams-jvm#3.9'>§3.9</a>。
 *
 * <p>History: 2.0.7 - experimental; 2.1 - beta
 * @param <T> 值类型
 * @since 2.2
 */
public interface FlowableSubscriber<@NonNull T> extends Subscriber<T> {

    /**
     * 本方法的实现者应确保在调用 {@link Subscription#request(long)} 之前，
     * {@link #onNext(Object)} 中需要可见的一切均已就绪。实践中这意味着
     * 在 {@code request()} 调用之后不应再进行初始化，
     * 且相对于 {@code onNext} 的附加行为应为线程安全。
     *
     * {@inheritDoc}
     */
    @Override
    void onSubscribe(@NonNull Subscription s);
}
