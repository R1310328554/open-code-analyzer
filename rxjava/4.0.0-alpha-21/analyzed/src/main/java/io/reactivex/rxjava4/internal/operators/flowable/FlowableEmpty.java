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
import io.reactivex.rxjava4.internal.subscriptions.EmptySubscription;
import io.reactivex.rxjava4.operators.ScalarSupplier;

/**
 * 仅发出 onSubscribe 与 onComplete 的空 {@link Flowable} 源。
 */
public final class FlowableEmpty extends Flowable<Object> implements ScalarSupplier<Object> {

    public static final Flowable<Object> INSTANCE = new FlowableEmpty();

    private FlowableEmpty() {
    }

    /** 通过 {@link EmptySubscription#complete} 立即完成。 */
    @Override
    public void subscribeActual(Subscriber<? super Object> s) {
        EmptySubscription.complete(s);
    }

    @Override
    public Object get() {
        return null; // null 标量值表示空序列
    }
}
