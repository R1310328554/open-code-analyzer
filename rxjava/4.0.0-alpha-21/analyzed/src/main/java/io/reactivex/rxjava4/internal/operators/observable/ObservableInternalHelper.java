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

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.functions.*;
import io.reactivex.rxjava4.internal.functions.Functions;

/**
 * Observable 内部类与 lambda 适配的辅助工具类（record/Function 工厂）。
 */
public final class ObservableInternalHelper {

    private ObservableInternalHelper() {
        throw new IllegalStateException("No instances!");
    }

    record SimpleGenerator<T, S>(Consumer<Emitter<T>> consumer) implements BiFunction<S, Emitter<T>, S> {

        @Override
            public S apply(S t1, Emitter<T> t2) throws Throwable {
                consumer.accept(t2);
                return t1;
            }
        }

    /** 将 Consumer&lt;Emitter&gt; 包装为 generate 用的 BiFunction（状态不变）。 */
    public static <T, S> BiFunction<S, Emitter<T>, S> simpleGenerator(Consumer<Emitter<T>> consumer) {
        return new SimpleGenerator<>(consumer);
    }

    record SimpleBiGenerator<T, S>(BiConsumer<S, Emitter<T>> consumer) implements BiFunction<S, Emitter<T>, S> {

        @Override
            public S apply(S t1, Emitter<T> t2) throws Throwable {
                consumer.accept(t1, t2);
                return t1;
            }
        }

    /** 将 BiConsumer&lt;S, Emitter&gt; 包装为 generate 用的 BiFunction。 */
    public static <T, S> BiFunction<S, Emitter<T>, S> simpleBiGenerator(BiConsumer<S, Emitter<T>> consumer) {
        return new SimpleBiGenerator<>(consumer);
    }

    record ItemDelayFunction<T, U>(
            Function<? super T, ? extends ObservableSource<U>> itemDelay) implements Function<T, ObservableSource<T>> {

        @Override
            public ObservableSource<T> apply(final T v) throws Throwable {
                ObservableSource<U> o = Objects.requireNonNull(itemDelay.apply(v), "The itemDelay returned a null ObservableSource");
                return new ObservableTake<>(o, 1).map(Functions.justFunction(v)).defaultIfEmpty(v);
            }
        }

    /** 为每个元素构造 delay Observable，take(1) 后再 defaultIfEmpty 原值。 */
    public static <T, U> Function<T, ObservableSource<T>> itemDelay(final Function<? super T, ? extends ObservableSource<U>> itemDelay) {
        return new ItemDelayFunction<>(itemDelay);
    }

    record ObserverOnNext<T>(Observer<T> observer) implements Consumer<T> {

        @Override
            public void accept(T v) {
                observer.onNext(v);
            }
        }

    record ObserverOnError<T>(Observer<T> observer) implements Consumer<Throwable> {

        @Override
            public void accept(Throwable v) {
                observer.onError(v);
            }
        }

    record ObserverOnComplete<T>(Observer<T> observer) implements Action {

        @Override
            public void run() {
                observer.onComplete();
            }
        }

    /** 返回调用 observer.onNext 的 Consumer。 */
    public static <T> Consumer<T> observerOnNext(Observer<T> observer) {
        return new ObserverOnNext<>(observer);
    }

    /** 返回调用 observer.onError 的 Consumer。 */
    public static <T> Consumer<Throwable> observerOnError(Observer<T> observer) {
        return new ObserverOnError<>(observer);
    }

    /** 返回调用 observer.onComplete 的 Action。 */
    public static <T> Action observerOnComplete(Observer<T> observer) {
        return new ObserverOnComplete<>(observer);
    }

    static final class FlatMapWithCombinerInner<U, R, T> implements Function<U, R> {
        private final BiFunction<? super T, ? super U, ? extends R> combiner;
        private final T t;

        FlatMapWithCombinerInner(BiFunction<? super T, ? super U, ? extends R> combiner, T t) {
            this.combiner = combiner;
            this.t = t;
        }

        @Override
        public R apply(U w) throws Throwable {
            return combiner.apply(t, w);
        }
    }

    static final class FlatMapWithCombinerOuter<T, R, U> implements Function<T, ObservableSource<R>> {
        private final BiFunction<? super T, ? super U, ? extends R> combiner;
        private final Function<? super T, ? extends ObservableSource<? extends U>> mapper;

        FlatMapWithCombinerOuter(BiFunction<? super T, ? super U, ? extends R> combiner,
                Function<? super T, ? extends ObservableSource<? extends U>> mapper) {
            this.combiner = combiner;
            this.mapper = mapper;
        }

        @Override
        public ObservableSource<R> apply(final T t) throws Throwable {
            @SuppressWarnings("unchecked")
            ObservableSource<U> u = (ObservableSource<U>)Objects.requireNonNull(mapper.apply(t), "The mapper returned a null ObservableSource");
            return new ObservableMap<>(u, new FlatMapWithCombinerInner<U, R, T>(combiner, t));
        }
    }

    /** 将 flatMap 与 combiner 组合为 mapper -> ObservableMap 链。 */
    public static <T, U, R> Function<T, ObservableSource<R>> flatMapWithCombiner(
            final Function<? super T, ? extends ObservableSource<? extends U>> mapper,
                    final BiFunction<? super T, ? super U, ? extends R> combiner) {
        return new FlatMapWithCombinerOuter<>(combiner, mapper);
    }

    static final class FlatMapIntoIterable<T, U> implements Function<T, ObservableSource<U>> {
        private final Function<? super T, ? extends Iterable<? extends U>> mapper;

        FlatMapIntoIterable(Function<? super T, ? extends Iterable<? extends U>> mapper) {
            this.mapper = mapper;
        }

        @Override
        public ObservableSource<U> apply(T t) throws Throwable {
            return new ObservableFromIterable<>(Objects.requireNonNull(mapper.apply(t), "The mapper returned a null Iterable"));
        }
    }

    /** 将元素映射为 Iterable 并包装为 ObservableFromIterable。 */
    public static <T, U> Function<T, ObservableSource<U>> flatMapIntoIterable(final Function<? super T, ? extends Iterable<? extends U>> mapper) {
        return new FlatMapIntoIterable<>(mapper);
    }

    enum MapToInt implements Function<Object, Object> {
        INSTANCE;
        @Override
        public Object apply(Object t) {
            return 0;
        }
    }

    /** 返回调用 parent.replay() 的 Supplier。 */
    public static <T> Supplier<ConnectableObservable<T>> replaySupplier(final Observable<T> parent) {
        return new ReplaySupplier<>(parent);
    }

    /** 返回调用 parent.replay(bufferSize, eagerTruncate) 的 Supplier。 */
    public static <T> Supplier<ConnectableObservable<T>> replaySupplier(final Observable<T> parent, final int bufferSize, boolean eagerTruncate) {
        return new BufferedReplaySupplier<>(parent, bufferSize, eagerTruncate);
    }

    /** 返回带 buffer 与时间窗口的 replay Supplier。 */
    public static <T> Supplier<ConnectableObservable<T>> replaySupplier(final Observable<T> parent, final int bufferSize,
            final long time, final TimeUnit unit, final Scheduler scheduler, boolean eagerTruncate) {
        return new BufferedTimedReplaySupplier<>(parent, bufferSize, time, unit, scheduler, eagerTruncate);
    }

    /** 返回仅按时间 replay 的 Supplier。 */
    public static <T> Supplier<ConnectableObservable<T>> replaySupplier(final Observable<T> parent, final long time,
            final TimeUnit unit, final Scheduler scheduler, boolean eagerTruncate) {
        return new TimedReplayCallable<>(parent, time, unit, scheduler, eagerTruncate);
    }

    static final class ReplaySupplier<T> implements Supplier<ConnectableObservable<T>> {
        private final Observable<T> parent;

        ReplaySupplier(Observable<T> parent) {
            this.parent = parent;
        }

        @Override
        public ConnectableObservable<T> get() {
            return parent.replay();
        }
    }

    record BufferedReplaySupplier<T>(Observable<T> parent, int bufferSize,
                                     boolean eagerTruncate) implements Supplier<ConnectableObservable<T>> {

        @Override
            public ConnectableObservable<T> get() {
                return parent.replay(bufferSize, eagerTruncate);
            }
        }

    record BufferedTimedReplaySupplier<T>(Observable<T> parent, int bufferSize, long time, TimeUnit unit,
                                          Scheduler scheduler,
                                          boolean eagerTruncate) implements Supplier<ConnectableObservable<T>> {

        @Override
            public ConnectableObservable<T> get() {
                return parent.replay(bufferSize, time, unit, scheduler, eagerTruncate);
            }
        }

    record TimedReplayCallable<T>(Observable<T> parent, long time, TimeUnit unit, Scheduler scheduler,
                                  boolean eagerTruncate) implements Supplier<ConnectableObservable<T>> {

        @Override
            public ConnectableObservable<T> get() {
                return parent.replay(time, unit, scheduler, eagerTruncate);
            }
        }
}
