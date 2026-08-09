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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.core.Observer;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Function;
import io.reactivex.rxjava4.internal.disposables.*;
import io.reactivex.rxjava4.operators.SpscLinkedArrayQueue;

/**
 * 按 keySelector 分组：为每个键创建 {@link GroupedObservable}，
 * valueSelector 映射组内元素；无订阅者的组可被 tryAbandon 回收。
 *
 * @param <T> 上游元素类型
 * @param <K> 分组键类型
 * @param <V> 组内元素类型
 */
public final class ObservableGroupBy<T, K, V> extends AbstractObservableWithUpstream<T, GroupedObservable<K, V>> {
    final Function<? super T, ? extends K> keySelector;
    final Function<? super T, ? extends V> valueSelector;
    final int bufferSize;
    final boolean delayError;

    /**
     * @param source 上游 ObservableSource
     * @param keySelector 提取分组键的函数
     * @param valueSelector 映射组内值的函数
     * @param bufferSize 每组 SPSC 队列容量
     * @param delayError 组内是否延迟错误至队列排空
     */
    public ObservableGroupBy(ObservableSource<T> source,
            Function<? super T, ? extends K> keySelector, Function<? super T, ? extends V> valueSelector,
            int bufferSize, boolean delayError) {
        super(source);
        this.keySelector = keySelector;
        this.valueSelector = valueSelector;
        this.bufferSize = bufferSize;
        this.delayError = delayError;
    }

    /** 订阅 GroupByObserver 管理分组 Map 与各 GroupedUnicast。 */
    @Override
    public void subscribeActual(Observer<? super GroupedObservable<K, V>> t) {
        source.subscribe(new GroupByObserver<>(t, keySelector, valueSelector, bufferSize, delayError));
    }

    /** 维护 groups Map，创建/完成 GroupedUnicast 并协调上游 dispose。 */
    public static final class GroupByObserver<T, K, V> extends AtomicInteger implements Observer<T>, Disposable {

        @Serial
        private static final long serialVersionUID = -3688291656102519502L;

        final Observer<? super GroupedObservable<K, V>> downstream;
        final Function<? super T, ? extends K> keySelector;
        final Function<? super T, ? extends V> valueSelector;
        final int bufferSize;
        final boolean delayError;
        final Map<Object, GroupedUnicast<K, V>> groups;

        static final Object NULL_KEY = new Object();

        Disposable upstream;

        final AtomicBoolean cancelled = new AtomicBoolean();

        public GroupByObserver(Observer<? super GroupedObservable<K, V>> actual, Function<? super T, ? extends K> keySelector,
                Function<? super T, ? extends V> valueSelector, int bufferSize, boolean delayError) {
            this.downstream = actual;
            this.keySelector = keySelector;
            this.valueSelector = valueSelector;
            this.bufferSize = bufferSize;
            this.delayError = delayError;
            this.groups = new ConcurrentHashMap<>();
            this.lazySet(1);
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                downstream.onSubscribe(this);
            }
        }

        @Override
        public void onNext(T t) {
            K key;
            try {
                key = keySelector.apply(t);
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                upstream.dispose();
                onError(e);
                return;
            }

            Object mapKey = key != null ? key : NULL_KEY;
            GroupedUnicast<K, V> group = groups.get(mapKey);
            boolean newGroup = false;
            if (group == null) {
                // 主流已 cancel 则不再创建新组并跳过该值
                if (cancelled.get()) {
                    return;
                }

                group = GroupedUnicast.createWith(key, bufferSize, this, delayError);
                groups.put(mapKey, group);

                getAndIncrement();

                newGroup = true;
            }

            V v;
            try {
                v = Objects.requireNonNull(valueSelector.apply(t), "The value supplied is null");
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                upstream.dispose();
                if (newGroup) {
                    downstream.onNext(group);
                }
                onError(e);
                return;
            }

            group.onNext(v);

            if (newGroup) {
                downstream.onNext(group);

                if (group.state.tryAbandon()) {
                    cancel(key);
                    group.onComplete();
                }
            }
        }

        @Override
        public void onError(Throwable t) {
            List<GroupedUnicast<K, V>> list = new ArrayList<>(groups.values());
            groups.clear();

            for (GroupedUnicast<K, V> e : list) {
                e.onError(t);
            }

            downstream.onError(t);
        }

        @Override
        public void onComplete() {
            List<GroupedUnicast<K, V>> list = new ArrayList<>(groups.values());
            groups.clear();

            for (GroupedUnicast<K, V> e : list) {
                e.onComplete();
            }

            downstream.onComplete();
        }

        @Override
        public void dispose() {
            // cancel 主流表示不再接受新组，但已有组仍可接收元素
            if (cancelled.compareAndSet(false, true)) {
                if (decrementAndGet() == 0) {
                    upstream.dispose();
                }
            }
        }

        @Override
        public boolean isDisposed() {
            return cancelled.get();
        }

        /** 从 groups 移除键；引用计数归零时 dispose 上游。 */
        public void cancel(K key) {
            Object mapKey = key != null ? key : NULL_KEY;
            groups.remove(mapKey);
            if (decrementAndGet() == 0) {
                upstream.dispose();
            }
        }
    }

    /** 单键分组 Observable，委托 State 管理队列与 drain。 */
    static final class GroupedUnicast<K, T> extends GroupedObservable<K, T> {

        final State<T, K> state;

        public static <T, K> GroupedUnicast<K, T> createWith(K key, int bufferSize, GroupByObserver<?, K, T> parent, boolean delayError) {
            State<T, K> state = new State<>(bufferSize, parent, key, delayError);
            return new GroupedUnicast<>(key, state);
        }

        protected GroupedUnicast(K key, State<T, K> state) {
            super(key);
            this.state = state;
        }

        @Override
        protected void subscribeActual(Observer<? super T> observer) {
            state.subscribe(observer);
        }

        public void onNext(T t) {
            state.onNext(t);
        }

        public void onError(Throwable e) {
            state.onError(e);
        }

        public void onComplete() {
            state.onComplete();
        }
    }

    static final class State<T, K> extends AtomicInteger implements Disposable, ObservableSource<T> {

        @Serial
        private static final long serialVersionUID = -3852313036005250360L;

        final K key;
        final SpscLinkedArrayQueue<T> queue;
        final GroupByObserver<?, K, T> parent;
        final boolean delayError;

        volatile boolean done;
        Throwable error;

        final AtomicBoolean cancelled = new AtomicBoolean();

        final AtomicReference<Observer<? super T>> actual = new AtomicReference<>();

        final AtomicInteger once = new AtomicInteger();

        /** 尚无订阅者。 */
        static final int FRESH = 0;
        /** 已有订阅者。 */
        static final int HAS_SUBSCRIBER = 1;
        /** 已放弃（无订阅者时被回收）。 */
        static final int ABANDONED = 2;
        /** 已放弃且曾有订阅者。 */
        static final int ABANDONED_HAS_SUBSCRIBER = ABANDONED | HAS_SUBSCRIBER;

        State(int bufferSize, GroupByObserver<?, K, T> parent, K key, boolean delayError) {
            this.queue = new SpscLinkedArrayQueue<>(bufferSize);
            this.parent = parent;
            this.key = key;
            this.delayError = delayError;
        }

        @Override
        public void dispose() {
            if (cancelled.compareAndSet(false, true)) {
                if (getAndIncrement() == 0) {
                    actual.lazySet(null);
                    cancelParent();
                }
            }
        }

        @Override
        public boolean isDisposed() {
            return cancelled.get();
        }

        @Override
        public void subscribe(Observer<? super T> observer) {
            for (;;) {
                int s = once.get();
                if ((s & HAS_SUBSCRIBER) != 0) {
                    break;
                }
                int u = s | HAS_SUBSCRIBER;
                if (once.compareAndSet(s, u)) {
                    observer.onSubscribe(this);
                    actual.lazySet(observer);
                    if (cancelled.get()) {
                        actual.lazySet(null);
                    } else {
                        drain();
                    }
                    return;
                }
            }
            EmptyDisposable.error(new IllegalStateException("Only one Observer allowed!"), observer);
        }

        public void onNext(T t) {
            queue.offer(t);
            drain();
        }

        public void onError(Throwable e) {
            error = e;
            done = true;
            drain();
        }

        public void onComplete() {
            done = true;
            drain();
        }

        /** 从 queue poll 并转发至 actual observer，处理终止与错误。 */
        void drain() {
            if (getAndIncrement() != 0) {
                return;
            }
            int missed = 1;

            final SpscLinkedArrayQueue<T> q = queue;
            final boolean delayError = this.delayError;
            Observer<? super T> a = actual.get();
            for (;;) {
                if (a != null) {
                    for (;;) {
                        boolean d = done;
                        T v = q.poll();
                        boolean empty = v == null;

                        if (checkTerminated(d, empty, a, delayError)) {
                            return;
                        }

                        if (empty) {
                            break;
                        }

                        a.onNext(v);
                    }
                }

                missed = addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
                if (a == null) {
                    a = actual.get();
                }
            }
        }

        void cancelParent() {
            if ((once.get() & ABANDONED) == 0) {
                parent.cancel(key);
            }
        }

        /** FRESH 时 CAS 为 ABANDONED，表示无订阅者的组可立即完成。 */
        boolean tryAbandon() {
            return once.get() == FRESH && once.compareAndSet(FRESH, ABANDONED);
        }

        boolean checkTerminated(boolean d, boolean empty, Observer<? super T> a, boolean delayError) {
            if (cancelled.get()) {
                queue.clear();
                actual.lazySet(null);
                cancelParent();
                return true;
            }

            if (d) {
                if (delayError) {
                    if (empty) {
                        Throwable e = error;
                        actual.lazySet(null);
                        if (e != null) {
                            a.onError(e);
                        } else {
                            a.onComplete();
                        }
                        return true;
                    }
                } else {
                    Throwable e = error;
                    if (e != null) {
                        queue.clear();
                        actual.lazySet(null);
                        a.onError(e);
                        return true;
                    } else
                    if (empty) {
                        actual.lazySet(null);
                        a.onComplete();
                        return true;
                    }
                }
            }

            return false;
        }
    }
}
