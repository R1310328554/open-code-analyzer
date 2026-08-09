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
import java.util.Objects;

import io.reactivex.rxjava4.annotations.Nullable;
import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.*;
import io.reactivex.rxjava4.internal.disposables.EmptyDisposable;
import io.reactivex.rxjava4.internal.observers.BasicFuseableObserver;
import io.reactivex.rxjava4.internal.util.ExceptionHelper;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 按 keySelector 提取键去重：键首次出现时转发原值，重复键静默丢弃。
 *
 * @param <T> 上游元素类型
 * @param <K> 去重键类型
 */
public final class ObservableDistinct<T, K> extends AbstractObservableWithUpstream<T, T> {

    final Function<? super T, K> keySelector;

    final Supplier<? extends Collection<? super K>> collectionSupplier;

    /**
     * @param source 上游 ObservableSource
     * @param keySelector 提取去重键的函数
     * @param collectionSupplier 提供存储已见键的 Collection 的 Supplier
     */
    public ObservableDistinct(ObservableSource<T> source, Function<? super T, K> keySelector, Supplier<? extends Collection<? super K>> collectionSupplier) {
        super(source);
        this.keySelector = keySelector;
        this.collectionSupplier = collectionSupplier;
    }

    /** 从 collectionSupplier 获取 Collection 并订阅 DistinctObserver。 */
    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        Collection<? super K> collection;

        try {
            collection = ExceptionHelper.nullCheck(collectionSupplier.get(), "The collectionSupplier returned a null Collection.");
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            EmptyDisposable.error(ex, observer);
            return;
        }

        source.subscribe(new DistinctObserver<>(observer, keySelector, collection));
    }

    /** 维护已见键集合，支持 queue fusion poll 路径去重。 */
    static final class DistinctObserver<T, K> extends BasicFuseableObserver<T, T> {

        final Collection<? super K> collection;

        final Function<? super T, K> keySelector;

        DistinctObserver(Observer<? super T> actual, Function<? super T, K> keySelector, Collection<? super K> collection) {
            super(actual);
            this.keySelector = keySelector;
            this.collection = collection;
        }

        /** collection.add(key) 成功时转发 value。 */
        @Override
        public void onNext(T value) {
            if (done) {
                return;
            }
            if (sourceMode == NONE) {
                K key;
                boolean b;

                try {
                    key = Objects.requireNonNull(keySelector.apply(value), "The keySelector returned a null key");
                    b = collection.add(key);
                } catch (Throwable ex) {
                    fail(ex);
                    return;
                }

                if (b) {
                    downstream.onNext(value);
                }
            } else {
                downstream.onNext(null);
            }
        }

        @Override
        public void onError(Throwable e) {
            if (done) {
                RxJavaPlugins.onError(e);
            } else {
                done = true;
                collection.clear();
                downstream.onError(e);
            }
        }

        @Override
        public void onComplete() {
            if (!done) {
                done = true;
                collection.clear();
                downstream.onComplete();
            }
        }

        @Override
        public int requestFusion(int mode) {
            return transitiveBoundaryFusion(mode);
        }

        @Nullable
        @Override
        public T poll() throws Throwable {
            for (;;) {
                T v = qd.poll();

                if (v == null || collection.add(Objects.requireNonNull(keySelector.apply(v), "The keySelector returned a null key"))) {
                    return v;
                }
            }
        }

        @Override
        public void clear() {
            collection.clear();
            super.clear();
        }
    }
}
