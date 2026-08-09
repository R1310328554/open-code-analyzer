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

import java.util.Collection;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Supplier;
import io.reactivex.rxjava4.internal.disposables.EmptyDisposable;
import io.reactivex.rxjava4.internal.fuseable.FuseToFlowable;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.internal.util.*;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 将 Flowable 全部元素收集为 {@link Single} 结果（默认 {@link ArrayList}）。
 * @param <T> 元素类型
 * @param <U> 集合类型
 */
public final class FlowableToListSingle<T, U extends Collection<? super T>> extends Single<U> implements FuseToFlowable<U> {

    final Flowable<T> source;

    final Supplier<U> collectionSupplier;

    /** 使用默认 ArrayList Supplier。 */
    @SuppressWarnings("unchecked")
    public FlowableToListSingle(Flowable<T> source) {
        this(source, (Supplier<U>)ArrayListSupplier.asSupplier());
    }

    /**
     * @param source 上游 Flowable
     * @param collectionSupplier 创建空集合的 Supplier
     */
    public FlowableToListSingle(Flowable<T> source, Supplier<U> collectionSupplier) {
        this.source = source;
        this.collectionSupplier = collectionSupplier;
    }

    /** 收集完成后 onSuccess(collection)。 */
    @Override
    protected void subscribeActual(SingleObserver<? super U> observer) {
        U coll;
        try {
            coll = ExceptionHelper.nullCheck(collectionSupplier.get(), "The collectionSupplier returned a null Collection.");
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            EmptyDisposable.error(e, observer);
            return;
        }
        source.subscribe(new ToListSubscriber<>(observer, coll));
    }

    /** 转为 {@link FlowableToList} 以便 fuse。 */
    @Override
    public Flowable<U> fuseToFlowable() {
        return RxJavaPlugins.onAssembly(new FlowableToList<>(source, collectionSupplier));
    }

    /** request 全部元素；onComplete 时 onSuccess。 */
    static final class ToListSubscriber<T, U extends Collection<? super T>>
    implements FlowableSubscriber<T>, Disposable {

        final SingleObserver<? super U> downstream;

        Subscription upstream;

        U value;

        ToListSubscriber(SingleObserver<? super U> actual, U collection) {
            this.downstream = actual;
            this.value = collection;
        }

        @Override
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;
                downstream.onSubscribe(this);
                s.request(Long.MAX_VALUE);
            }
        }

        @Override
        public void onNext(T t) {
            value.add(t);
        }

        @Override
        public void onError(Throwable t) {
            value = null;
            upstream = SubscriptionHelper.CANCELLED;
            downstream.onError(t);
        }

        @Override
        public void onComplete() {
            upstream = SubscriptionHelper.CANCELLED;
            downstream.onSuccess(value);
        }

        @Override
        public void dispose() {
            upstream.cancel();
            upstream = SubscriptionHelper.CANCELLED;
        }

        @Override
        public boolean isDisposed() {
            return upstream == SubscriptionHelper.CANCELLED;
        }
    }
}
