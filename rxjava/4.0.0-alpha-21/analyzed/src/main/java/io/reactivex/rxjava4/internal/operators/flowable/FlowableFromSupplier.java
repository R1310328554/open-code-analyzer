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

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.Flowable;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Supplier;
import io.reactivex.rxjava4.internal.subscriptions.DeferredScalarSubscription;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

import java.util.Objects;

/**
 * 每次订阅时调用 {@link Supplier}，发射返回值或传播抛出的异常。
 * @param <T> Supplier 返回值及流元素类型
 * @since 3.0.0
 */
public final class FlowableFromSupplier<T> extends Flowable<T> implements Supplier<T> {

    final Supplier<? extends T> supplier;

    /** @param supplier 每次订阅时调用的 Supplier */
    public FlowableFromSupplier(Supplier<? extends T> supplier) {
        this.supplier = supplier;
    }

    /** 调用 supplier 并通过 DeferredScalarSubscription 发射单值。 */
    @Override
    public void subscribeActual(Subscriber<? super T> s) {
        DeferredScalarSubscription<T> deferred = new DeferredScalarSubscription<>(s);
        s.onSubscribe(deferred);

        T t;
        try {
            t = Objects.requireNonNull(supplier.get(), "The supplier returned a null value");
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            if (deferred.isCancelled()) {
                RxJavaPlugins.onError(ex);
            } else {
                s.onError(ex);
            }
            return;
        }

        deferred.complete(t);
    }

    /** 直接调用 supplier 并校验非 null。 */
    @Override
    public T get() throws Throwable {
        return Objects.requireNonNull(supplier.get(), "The supplier returned a null value");
    }
}
