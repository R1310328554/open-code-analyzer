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

import java.util.Collection;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Supplier;
import io.reactivex.rxjava4.internal.disposables.*;
import io.reactivex.rxjava4.internal.functions.Functions;
import io.reactivex.rxjava4.internal.fuseable.FuseToObservable;
import io.reactivex.rxjava4.internal.util.ExceptionHelper;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 将 Observable 全部元素收集到 Collection，上游 onComplete 后以 Single 发射该集合。
 * 实现 {@link FuseToObservable}，可 fuse 回 {@link ObservableToList}。
 * @param <T> 上游元素类型
 * @param <U> 集合类型
 */
public final class ObservableToListSingle<T, U extends Collection<? super T>>
extends Single<U> implements FuseToObservable<U> {

    final ObservableSource<T> source;

    final Supplier<U> collectionSupplier;

    /**
     * 使用默认 ArrayList 容量提示构造集合供应器。
     * @param source 上游 ObservableSource
     * @param defaultCapacityHint ArrayList 初始容量
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ObservableToListSingle(ObservableSource<T> source, final int defaultCapacityHint) {
        this.source = source;
        this.collectionSupplier = (Supplier)Functions.createArrayList(defaultCapacityHint);
    }

    /**
     * @param source 上游 ObservableSource
     * @param collectionSupplier 可变的集合供应器
     */
    public ObservableToListSingle(ObservableSource<T> source, Supplier<U> collectionSupplier) {
        this.source = source;
        this.collectionSupplier = collectionSupplier;
    }

    /** 获取集合实例后订阅 ToListObserver。 */
    @Override
    public void subscribeActual(SingleObserver<? super U> t) {
        U coll;
        try {
            coll = ExceptionHelper.nullCheck(collectionSupplier.get(), "The collectionSupplier returned a null Collection.");
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            EmptyDisposable.error(e, t);
            return;
        }
        source.subscribe(new ToListObserver<>(t, coll));
    }

    @Override
    public Observable<U> fuseToObservable() {
        return RxJavaPlugins.onAssembly(new ObservableToList<>(source, collectionSupplier));
    }

    /** 逐项 add 至 collection；onComplete 时 onSuccess 下发集合。 */
    static final class ToListObserver<T, U extends Collection<? super T>> implements Observer<T>, Disposable {
        final SingleObserver<? super U> downstream;

        U collection;

        Disposable upstream;

        ToListObserver(SingleObserver<? super U> actual, U collection) {
            this.downstream = actual;
            this.collection = collection;
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                downstream.onSubscribe(this);
            }
        }

        @Override
        public void dispose() {
            upstream.dispose();
        }

        @Override
        public boolean isDisposed() {
            return upstream.isDisposed();
        }

        @Override
        public void onNext(T t) {
            collection.add(t);
        }

        @Override
        public void onError(Throwable t) {
            collection = null;
            downstream.onError(t);
        }

        /** 清空引用后 onSuccess 发射累积集合。 */
        @Override
        public void onComplete() {
            U c = collection;
            collection = null;
            downstream.onSuccess(c);
        }
    }
}
