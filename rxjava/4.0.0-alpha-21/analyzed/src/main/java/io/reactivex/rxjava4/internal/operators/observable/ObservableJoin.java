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

import java.io.Serial;
import java.util.*;
import java.util.concurrent.atomic.*;

import io.reactivex.rxjava4.core.ObservableSource;
import io.reactivex.rxjava4.core.Observer;
import io.reactivex.rxjava4.disposables.*;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.*;
import io.reactivex.rxjava4.internal.operators.observable.ObservableGroupJoin.*;
import io.reactivex.rxjava4.internal.util.ExceptionHelper;
import io.reactivex.rxjava4.operators.SpscLinkedArrayQueue;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 双流 join：左/右源各元素与对侧缓存做笛卡尔 resultSelector 组合；
 * leftEnd/rightEnd 映射的窗口 Observable 终止时移除对应缓存项。
 *
 * @param <TLeft> 左源元素类型
 * @param <TRight> 右源元素类型
 * @param <TLeftEnd> 左窗口结束信号类型
 * @param <TRightEnd> 右窗口结束信号类型
 * @param <R> 组合结果类型
 */
public final class ObservableJoin<TLeft, TRight, TLeftEnd, TRightEnd, R> extends AbstractObservableWithUpstream<TLeft, R> {

    final ObservableSource<? extends TRight> other;

    final Function<? super TLeft, ? extends ObservableSource<TLeftEnd>> leftEnd;

    final Function<? super TRight, ? extends ObservableSource<TRightEnd>> rightEnd;

    final BiFunction<? super TLeft, ? super TRight, ? extends R> resultSelector;

    /**
     * @param source 左源 ObservableSource
     * @param other 右源 ObservableSource
     * @param leftEnd 左元素 -> 左窗口结束 ObservableSource
     * @param rightEnd 右元素 -> 右窗口结束 ObservableSource
     * @param resultSelector 左/右元素组合函数
     */
    public ObservableJoin(
            ObservableSource<TLeft> source,
            ObservableSource<? extends TRight> other,
            Function<? super TLeft, ? extends ObservableSource<TLeftEnd>> leftEnd,
            Function<? super TRight, ? extends ObservableSource<TRightEnd>> rightEnd,
            BiFunction<? super TLeft, ? super TRight, ? extends R> resultSelector) {
        super(source);
        this.other = other;
        this.leftEnd = leftEnd;
        this.rightEnd = rightEnd;
        this.resultSelector = resultSelector;
    }

    /** 订阅 JoinDisposable 并同时 subscribe 左/右 LeftRightObserver。 */
    @Override
    protected void subscribeActual(Observer<? super R> observer) {

        JoinDisposable<TLeft, TRight, TLeftEnd, TRightEnd, R> parent =
                new JoinDisposable<>(
                        observer, leftEnd, rightEnd, resultSelector);

        observer.onSubscribe(parent);

        LeftRightObserver left = new LeftRightObserver(parent, true);
        parent.disposables.add(left);
        LeftRightObserver right = new LeftRightObserver(parent, false);
        parent.disposables.add(right);

        source.subscribe(left);
        other.subscribe(right);
    }

    /** 维护 lefts/rights 缓存、窗口订阅与 drain 队列，驱动 join 组合。 */
    static final class JoinDisposable<TLeft, TRight, TLeftEnd, TRightEnd, R>
    extends AtomicInteger implements Disposable, JoinSupport {

        @Serial
        private static final long serialVersionUID = -6071216598687999801L;

        final Observer<? super R> downstream;

        final SpscLinkedArrayQueue<Object> queue;

        final CompositeDisposable disposables;

        final Map<Integer, TLeft> lefts;

        final Map<Integer, TRight> rights;

        final AtomicReference<Throwable> error;

        final Function<? super TLeft, ? extends ObservableSource<TLeftEnd>> leftEnd;

        final Function<? super TRight, ? extends ObservableSource<TRightEnd>> rightEnd;

        final BiFunction<? super TLeft, ? super TRight, ? extends R> resultSelector;

        final AtomicInteger active;

        int leftIndex;

        int rightIndex;

        volatile boolean cancelled;

        /** 队列标记：左源新值。 */
        static final Integer LEFT_VALUE = 1;

        /** 队列标记：右源新值。 */
        static final Integer RIGHT_VALUE = 2;

        /** 队列标记：左窗口关闭。 */
        static final Integer LEFT_CLOSE = 3;

        /** 队列标记：右窗口关闭。 */
        static final Integer RIGHT_CLOSE = 4;

        JoinDisposable(Observer<? super R> actual,
                Function<? super TLeft, ? extends ObservableSource<TLeftEnd>> leftEnd,
                Function<? super TRight, ? extends ObservableSource<TRightEnd>> rightEnd,
                        BiFunction<? super TLeft, ? super TRight, ? extends R> resultSelector) {
            this.downstream = actual;
            this.disposables = new CompositeDisposable();
            this.queue = new SpscLinkedArrayQueue<>(bufferSize());
            this.lefts = new LinkedHashMap<>();
            this.rights = new LinkedHashMap<>();
            this.error = new AtomicReference<>();
            this.leftEnd = leftEnd;
            this.rightEnd = rightEnd;
            this.resultSelector = resultSelector;
            this.active = new AtomicInteger(2);
        }

        @Override
        public void dispose() {
            if (!cancelled) {
                cancelled = true;
                cancelAll();
                if (getAndIncrement() == 0) {
                    queue.clear();
                }
            }
        }

        @Override
        public boolean isDisposed() {
            return cancelled;
        }

        void cancelAll() {
            disposables.dispose();
        }

        void errorAll(Observer<?> a) {
            Throwable ex = ExceptionHelper.terminate(error);

            lefts.clear();
            rights.clear();

            a.onError(ex);
        }

        void fail(Throwable exc, Observer<?> a, SpscLinkedArrayQueue<?> q) {
            Exceptions.throwIfFatal(exc);
            ExceptionHelper.addThrowable(error, exc);
            q.clear();
            cancelAll();
            errorAll(a);
        }

        /** 从 queue 取标记/值，更新缓存并 apply resultSelector 发射组合结果。 */
        void drain() {
            if (getAndIncrement() != 0) {
                return;
            }

            int missed = 1;
            SpscLinkedArrayQueue<Object> q = queue;
            Observer<? super R> a = downstream;

            for (;;) {
                for (;;) {
                    if (cancelled) {
                        q.clear();
                        return;
                    }

                    Throwable ex = error.get();
                    if (ex != null) {
                        q.clear();
                        cancelAll();
                        errorAll(a);
                        return;
                    }

                    boolean d = active.get() == 0;

                    Integer mode = (Integer)q.poll();

                    boolean empty = mode == null;

                    if (d && empty) {

                        lefts.clear();
                        rights.clear();
                        disposables.dispose();

                        a.onComplete();
                        return;
                    }

                    if (empty) {
                        break;
                    }

                    Object val = q.poll();

                    if (mode == LEFT_VALUE) {
                        @SuppressWarnings("unchecked")
                        TLeft left = (TLeft)val;

                        int idx = leftIndex++;
                        lefts.put(idx, left);

                        ObservableSource<TLeftEnd> p;

                        try {
                            p = Objects.requireNonNull(leftEnd.apply(left), "The leftEnd returned a null ObservableSource");
                        } catch (Throwable exc) {
                            fail(exc, a, q);
                            return;
                        }

                        LeftRightEndObserver end = new LeftRightEndObserver(this, true, idx);
                        disposables.add(end);

                        p.subscribe(end);

                        ex = error.get();
                        if (ex != null) {
                            q.clear();
                            cancelAll();
                            errorAll(a);
                            return;
                        }

                        for (TRight right : rights.values()) {

                            R w;

                            try {
                                w = Objects.requireNonNull(resultSelector.apply(left, right), "The resultSelector returned a null value");
                            } catch (Throwable exc) {
                                fail(exc, a, q);
                                return;
                            }

                            a.onNext(w);
                        }
                    }
                    else if (mode == RIGHT_VALUE) {
                        @SuppressWarnings("unchecked")
                        TRight right = (TRight)val;

                        int idx = rightIndex++;

                        rights.put(idx, right);

                        ObservableSource<TRightEnd> p;

                        try {
                            p = Objects.requireNonNull(rightEnd.apply(right), "The rightEnd returned a null ObservableSource");
                        } catch (Throwable exc) {
                            fail(exc, a, q);
                            return;
                        }

                        LeftRightEndObserver end = new LeftRightEndObserver(this, false, idx);
                        disposables.add(end);

                        p.subscribe(end);

                        ex = error.get();
                        if (ex != null) {
                            q.clear();
                            cancelAll();
                            errorAll(a);
                            return;
                        }

                        for (TLeft left : lefts.values()) {

                            R w;

                            try {
                                w = Objects.requireNonNull(resultSelector.apply(left, right), "The resultSelector returned a null value");
                            } catch (Throwable exc) {
                                fail(exc, a, q);
                                return;
                            }

                            a.onNext(w);
                        }
                    }
                    else if (mode == LEFT_CLOSE) {
                        LeftRightEndObserver end = (LeftRightEndObserver)val;

                        lefts.remove(end.index);
                        disposables.remove(end);
                    } else {
                        LeftRightEndObserver end = (LeftRightEndObserver)val;

                        rights.remove(end.index);
                        disposables.remove(end);
                    }
                }

                missed = addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }

        @Override
        public void innerError(Throwable ex) {
            if (ExceptionHelper.addThrowable(error, ex)) {
                active.decrementAndGet();
                drain();
            } else {
                RxJavaPlugins.onError(ex);
            }
        }

        @Override
        public void innerComplete(LeftRightObserver sender) {
            disposables.delete(sender);
            active.decrementAndGet();
            drain();
        }

        /** 左/右新值入队并触发 drain。 */
        @Override
        public void innerValue(boolean isLeft, Object o) {
            synchronized (this) {
                queue.offer(isLeft ? LEFT_VALUE : RIGHT_VALUE, o);
            }
            drain();
        }

        /** 窗口结束：从 lefts/rights 移除对应索引并 drain。 */
        @Override
        public void innerClose(boolean isLeft, LeftRightEndObserver index) {
            synchronized (this) {
                queue.offer(isLeft ? LEFT_CLOSE : RIGHT_CLOSE, index);
            }
            drain();
        }

        @Override
        public void innerCloseError(Throwable ex) {
            if (ExceptionHelper.addThrowable(error, ex)) {
                drain();
            } else {
                RxJavaPlugins.onError(ex);
            }
        }
    }
}
