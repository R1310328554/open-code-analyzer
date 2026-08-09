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

package io.reactivex.rxjava4.internal.operators.observable;

import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.functions.Consumer;

/**
 * 包装 {@link ConnectableObservable}，在订阅者数量达到阈值时调用 {@code connect()}。
 *
 * @param <T> 链上元素类型
 */
public final class ObservableAutoConnect<T> extends Observable<T> {
    final ConnectableObservable<? extends T> source;
    final int numberOfObservers;
    final Consumer<? super Disposable> connection;
    final AtomicInteger clients;

    /**
     * @param source 可连接的 ConnectableObservable
     * @param numberOfObservers 触发 connect 所需的 Observer 数量
     * @param connection connect 时接收 Disposable 的回调
     */
    public ObservableAutoConnect(ConnectableObservable<? extends T> source,
            int numberOfObservers,
            Consumer<? super Disposable> connection) {
        this.source = source;
        this.numberOfObservers = numberOfObservers;
        this.connection = connection;
        this.clients = new AtomicInteger();
    }

    /** 订阅上游并在 clients 达到 numberOfObservers 时 connect。 */
    @Override
    public void subscribeActual(Observer<? super T> child) {
        source.subscribe(child);
        if (clients.incrementAndGet() == numberOfObservers) {
            source.connect(connection);
        }
    }
}
