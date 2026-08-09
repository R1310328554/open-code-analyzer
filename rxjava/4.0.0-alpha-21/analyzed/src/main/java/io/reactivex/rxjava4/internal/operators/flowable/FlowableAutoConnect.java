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

package io.reactivex.rxjava4.internal.operators.flowable;

import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.functions.Consumer;

/**
 * 包装 {@link ConnectableFlowable}，在指定数量的 {@link Subscriber} 订阅后
 * 调用其 {@code connect()} 方法。
 *
 * @param <T> 链中元素类型
 */
public final class FlowableAutoConnect<T> extends Flowable<T> {
    final ConnectableFlowable<? extends T> source;
    final int numberOfSubscribers;
    final Consumer<? super Disposable> connection;
    final AtomicInteger clients;

    /**
     * @param source 可连接的 ConnectableFlowable
     * @param numberOfSubscribers 触发 connect 所需的订阅者数量
     * @param connection connect 时接收 Disposable 的回调
     */
    public FlowableAutoConnect(ConnectableFlowable<? extends T> source,
            int numberOfSubscribers,
            Consumer<? super Disposable> connection) {
        this.source = source;
        this.numberOfSubscribers = numberOfSubscribers;
        this.connection = connection;
        this.clients = new AtomicInteger();
    }

    /** 订阅 source 并在达到 numberOfSubscribers 时自动 connect。 */
    @Override
    public void subscribeActual(Subscriber<? super T> child) {
        source.subscribe(child);
        if (clients.incrementAndGet() == numberOfSubscribers) {
            source.connect(connection);
        }
    }
}
