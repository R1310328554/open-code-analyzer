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

package io.reactivex.rxjava4.internal.operators.single;

import java.util.*;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.functions.*;

/**
 * Single 内部工具类：提供 emptyThrower、toFlowable 及 iterableToFlowable 等辅助。
 * 不可实例化。
 */
public final class SingleInternalHelper {

    /** 工具类私有构造，禁止实例化。 */
    private SingleInternalHelper() {
        throw new IllegalStateException("No instances!");
    }

    /** 单例 Supplier，每次 get 返回新的 NoSuchElementException。 */
    enum NoSuchElementSupplier implements Supplier<NoSuchElementException> {
        INSTANCE;

        @Override
        public NoSuchElementException get() {
            return new NoSuchElementException();
        }
    }

    /** 返回用于 empty 场景抛出 NoSuchElementException 的 Supplier 单例。 */
    public static Supplier<NoSuchElementException> emptyThrower() {
        return NoSuchElementSupplier.INSTANCE;
    }

    @SuppressWarnings("rawtypes")
    /** 将 SingleSource 映射为 SingleToFlowable 的 Function 单例。 */
    enum ToFlowable implements Function<SingleSource, Publisher> {
        INSTANCE;
        @SuppressWarnings("unchecked")
        @Override
        public Publisher apply(SingleSource v) {
            return new SingleToFlowable(v);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    /** 返回将 SingleSource 转为 Publisher 的泛型 Function。 */
    public static <T> Function<SingleSource<? extends T>, Publisher<? extends T>> toFlowable() {
        return (Function)ToFlowable.INSTANCE;
    }

    /** 遍历 SingleSource 迭代器，next 时包装为 SingleToFlowable。 */
    static final class ToFlowableIterator<T> implements Iterator<Flowable<T>> {
        private final Iterator<? extends SingleSource<? extends T>> sit;

        ToFlowableIterator(Iterator<? extends SingleSource<? extends T>> sit) {
            this.sit = sit;
        }

        @Override
        public boolean hasNext() {
            return sit.hasNext();
        }

        @Override
        public Flowable<T> next() {
            return new SingleToFlowable<>(sit.next());
        }

        /** 不支持 remove，调用即抛 UnsupportedOperationException。 */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /** 将 Iterable&lt;SingleSource&gt; 转为 Iterable&lt;Flowable&gt; 的包装。 */
    static final class ToFlowableIterable<T> implements Iterable<Flowable<T>> {

        private final Iterable<? extends SingleSource<? extends T>> sources;

        ToFlowableIterable(Iterable<? extends SingleSource<? extends T>> sources) {
            this.sources = sources;
        }

        @Override
        public Iterator<Flowable<T>> iterator() {
            return new ToFlowableIterator<>(sources.iterator());
        }
    }

    /** 将 sources 包装为 ToFlowableIterable，逐项转为 Flowable。 */
    public static <T> Iterable<? extends Flowable<T>> iterableToFlowable(final Iterable<? extends SingleSource<? extends T>> sources) {
        return new ToFlowableIterable<>(sources);
    }
}
