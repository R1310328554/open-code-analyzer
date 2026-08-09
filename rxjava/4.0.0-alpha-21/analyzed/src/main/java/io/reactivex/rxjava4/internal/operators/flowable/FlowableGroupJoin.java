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

/* ===== [OCA 中文解析] =====
文件意图总览

GroupJoin 算子：左流元素与右流窗口配对，由 leftEnd/rightEnd 信号界定窗口生命周期并合并结果。
===== [OCA 中文解析结束] ===== */
package io.reactivex.rxjava4.internal.operators.flowable;

import java.io.Serial;
import java.util.*;
import java.util.concurrent.atomic.*;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.*;
import io.reactivex.rxjava4.exceptions.*;
import io.reactivex.rxjava4.functions.*;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.internal.util.*;
import io.reactivex.rxjava4.operators.SimpleQueue;
import io.reactivex.rxjava4.operators.SpscLinkedArrayQueue;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;
import io.reactivex.rxjava4.processors.UnicastProcessor;

/* ===== [OCA 中文解析] =====
class FlowableGroupJoin — 意图说明

协调左右流、窗口结束信号与 resultSelector 合并。

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * 协调左右流、窗口结束信号与 resultSelector 合并。
 */
public final class FlowableGroupJoin<TLeft, TRight, TLeftEnd, TRightEnd, R> extends AbstractFlowableWithUpstream<TLeft, R> {

    final Publisher<? extends TRight> other;

    final Function<? super TLeft, ? extends Publisher<TLeftEnd>> leftEnd;

    final Function<? super TRight, ? extends Publisher<TRightEnd>> rightEnd;

    final BiFunction<? super TLeft, ? super Flowable<TRight>, ? extends R> resultSelector;

    public FlowableGroupJoin(
            Flowable<TLeft> source,
            Publisher<? extends TRight> other,
            Function<? super TLeft, ? extends Publisher<TLeftEnd>> leftEnd,
            Function<? super TRight, ? extends Publisher<TRightEnd>> rightEnd,
            BiFunction<? super TLeft, ? super Flowable<TRight>, ? extends R> resultSelector) {
        super(source);
        this.other = other;
        this.leftEnd = leftEnd;
        this.rightEnd = rightEnd;
        this.resultSelector = resultSelector;
    }

    /** 组装内部 Subscriber/Observer 并订阅上游。 */


    @Override


    protected void subscribeActual(Subscriber<? super R> s) {

        GroupJoinSubscription<TLeft, TRight, TLeftEnd, TRightEnd, R> parent =
                new GroupJoinSubscription<>(s, leftEnd, rightEnd, resultSelector);

        s.onSubscribe(parent);

        LeftRightSubscriber left = new LeftRightSubscriber(parent, true);
        parent.disposables.add(left);
        LeftRightSubscriber right = new LeftRightSubscriber(parent, false);
        parent.disposables.add(right);

        source.subscribe(left);
        other.subscribe(right);
    }

    interface JoinSupport {

        /** inner 错误：按 delayError 策略合并或立即终止。 */


        void innerError(Throwable ex);

        void innerComplete(LeftRightSubscriber sender);

        void innerValue(boolean isLeft, Object o);

        void innerClose(boolean isLeft, LeftRightEndSubscriber index);

        void innerCloseError(Throwable ex);
    }

    /** 内部 GroupJoinSubscription。 */


    static final class GroupJoinSubscription<TLeft, TRight, TLeftEnd, TRightEnd, R>
    extends AtomicInteger implements Subscription, JoinSupport {

        @Serial
        private static final long serialVersionUID = -6071216598687999801L;

        final Subscriber<? super R> downstream;

        final AtomicLong requested;

        final SpscLinkedArrayQueue<Object> queue;

        final CompositeDisposable disposables;

        final Map<Integer, UnicastProcessor<TRight>> lefts;

        final Map<Integer, TRight> rights;

        final AtomicReference<Throwable> error;

        final Function<? super TLeft, ? extends Publisher<TLeftEnd>> leftEnd;

        final Function<? super TRight, ? extends Publisher<TRightEnd>> rightEnd;

        final BiFunction<? super TLeft, ? super Flowable<TRight>, ? extends R> resultSelector;

        final AtomicInteger active;

        int leftIndex;

        int rightIndex;

        volatile boolean cancelled;

        static final Integer LEFT_VALUE = 1;

        static final Integer RIGHT_VALUE = 2;

        static final Integer LEFT_CLOSE = 3;

        static final Integer RIGHT_CLOSE = 4;

        GroupJoinSubscription(Subscriber<? super R> actual, Function<? super TLeft, ? extends Publisher<TLeftEnd>> leftEnd,
                Function<? super TRight, ? extends Publisher<TRightEnd>> rightEnd,
                        BiFunction<? super TLeft, ? super Flowable<TRight>, ? extends R> resultSelector) {
            this.downstream = actual;
            this.requested = new AtomicLong();
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

        /** 处理下游背压 request。 */


        @Override


        public void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                BackpressureHelper.add(requested, n);
            }
        }

        /** 取消订阅并释放资源。 */


        @Override


        public void cancel() {
            if (cancelled) {
                return;
            }
            cancelled = true;
            cancelAll();
            if (getAndIncrement() == 0) {
                queue.clear();
            }
        }

        void cancelAll() {
            disposables.dispose();
        }

        void errorAll(Subscriber<?> a) {
            Throwable ex = ExceptionHelper.terminate(error);

            for (UnicastProcessor<TRight> up : lefts.values()) {
                up.onError(ex);
            }

            lefts.clear();
            rights.clear();

            a.onError(ex);
        }

        void fail(Throwable exc, Subscriber<?> a, SimpleQueue<?> q) {
            Exceptions.throwIfFatal(exc);
            ExceptionHelper.addThrowable(error, exc);
            q.clear();
            cancelAll();
            errorAll(a);
        }

        /** drain 循环：按 request 从队列取元素发射。 */


        void drain() {
            if (getAndIncrement() != 0) {
                return;
            }

            int missed = 1;
            SpscLinkedArrayQueue<Object> q = queue;
            Subscriber<? super R> a = downstream;

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
                        for (UnicastProcessor<?> up : lefts.values()) {
                            up.onComplete();
                        }

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

                        UnicastProcessor<TRight> up = UnicastProcessor.create();
                        int idx = leftIndex++;
                        lefts.put(idx, up);

                        Publisher<TLeftEnd> p;

                        try {
                            p = Objects.requireNonNull(leftEnd.apply(left), "The leftEnd returned a null Publisher");
                        } catch (Throwable exc) {
                            fail(exc, a, q);
                            return;
                        }

                        LeftRightEndSubscriber end = new LeftRightEndSubscriber(this, true, idx);
                        disposables.add(end);

                        p.subscribe(end);

                        ex = error.get();
                        if (ex != null) {
                            q.clear();
                            cancelAll();
                            errorAll(a);
                            return;
                        }

                        R w;

                        try {
                            w = Objects.requireNonNull(resultSelector.apply(left, up), "The resultSelector returned a null value");
                        } catch (Throwable exc) {
                            fail(exc, a, q);
                            return;
                        }

                        // TODO since only left emission calls the actual, it is possible to link downstream backpressure with left's source and not error out
                        if (requested.get() != 0L) {
                            a.onNext(w);
                            BackpressureHelper.produced(requested, 1);
                        } else {
                            fail(MissingBackpressureException.createDefault(), a, q);
                            return;
                        }

                        for (TRight right : rights.values()) {
                            up.onNext(right);
                        }
                    }
                    else if (mode == RIGHT_VALUE) {
                        @SuppressWarnings("unchecked")
                        TRight right = (TRight)val;

                        int idx = rightIndex++;

                        rights.put(idx, right);

                        Publisher<TRightEnd> p;

                        try {
                            p = Objects.requireNonNull(rightEnd.apply(right), "The rightEnd returned a null Publisher");
                        } catch (Throwable exc) {
                            fail(exc, a, q);
                            return;
                        }

                        LeftRightEndSubscriber end = new LeftRightEndSubscriber(this, false, idx);
                        disposables.add(end);

                        p.subscribe(end);

                        ex = error.get();
                        if (ex != null) {
                            q.clear();
                            cancelAll();
                            errorAll(a);
                            return;
                        }

                        for (UnicastProcessor<TRight> up : lefts.values()) {
                            up.onNext(right);
                        }
                    }
                    else if (mode == LEFT_CLOSE) {
                        LeftRightEndSubscriber end = (LeftRightEndSubscriber)val;

                        UnicastProcessor<TRight> up = lefts.remove(end.index);
                        disposables.remove(end);
                        if (up != null) {
                            up.onComplete();
                        }
                    }
                    else {
                        LeftRightEndSubscriber end = (LeftRightEndSubscriber)val;

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
        public /** inner 错误：按 delayError 策略合并或立即终止。 */
 void innerError(Throwable ex) {
            if (ExceptionHelper.addThrowable(error, ex)) {
                active.decrementAndGet();
                drain();
            } else {
                RxJavaPlugins.onError(ex);
            }
        }

        @Override
        public void innerComplete(LeftRightSubscriber sender) {
            disposables.delete(sender);
            active.decrementAndGet();
            drain();
        }

        @Override
        public void innerValue(boolean isLeft, Object o) {
            synchronized (this) {
                queue.offer(isLeft ? LEFT_VALUE : RIGHT_VALUE, o);
            }
            drain();
        }

        @Override
        public void innerClose(boolean isLeft, LeftRightEndSubscriber index) {
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

    /** 内部 LeftRightSubscriber。 */


    static final class LeftRightSubscriber
    extends AtomicReference<Subscription>
    implements FlowableSubscriber<Object>, Disposable {

        @Serial
        private static final long serialVersionUID = 1883890389173668373L;

        final JoinSupport parent;

        final boolean isLeft;

        LeftRightSubscriber(JoinSupport parent, boolean isLeft) {
            this.parent = parent;
            this.isLeft = isLeft;
        }

        /** dispose 连接/inner 并清理状态。 */


        @Override


        public void dispose() {
            SubscriptionHelper.cancel(this);
        }

        /** 返回是否已 dispose。 */


        @Override


        public boolean isDisposed() {
            return get() == SubscriptionHelper.CANCELLED;
        }

        /** 校验 Subscription 并初始化内部状态。 */


        @Override


        public void onSubscribe(Subscription s) {
            SubscriptionHelper.setOnce(this, s, Long.MAX_VALUE);
        }

        /** 处理上游 onNext 并转发或缓存。 */


        @Override


        public void onNext(Object t) {
            parent.innerValue(isLeft, t);
        }

        /** 处理上游/onError 并按策略终止或延迟错误。 */


        @Override


        public void onError(Throwable t) {
            parent.innerError(t);
        }

        /** 上游完成：清理资源并向下游发送 onComplete。 */


        @Override


        public void onComplete() {
            parent.innerComplete(this);
        }

    }

    /** 内部 LeftRightEndSubscriber。 */


    static final class LeftRightEndSubscriber
    extends AtomicReference<Subscription>
    implements FlowableSubscriber<Object>, Disposable {

        @Serial
        private static final long serialVersionUID = 1883890389173668373L;

        final JoinSupport parent;

        final boolean isLeft;

        final int index;

        LeftRightEndSubscriber(JoinSupport parent,
                boolean isLeft, int index) {
            this.parent = parent;
            this.isLeft = isLeft;
            this.index = index;
        }

        /** dispose 连接/inner 并清理状态。 */


        @Override


        public void dispose() {
            SubscriptionHelper.cancel(this);
        }

        /** 返回是否已 dispose。 */


        @Override


        public boolean isDisposed() {
            return get() == SubscriptionHelper.CANCELLED;
        }

        /** 校验 Subscription 并初始化内部状态。 */


        @Override


        public void onSubscribe(Subscription s) {
            SubscriptionHelper.setOnce(this, s, Long.MAX_VALUE);
        }

        /** 处理上游 onNext 并转发或缓存。 */


        @Override


        public void onNext(Object t) {
            if (SubscriptionHelper.cancel(this)) {
                parent.innerClose(isLeft, this);
            }
        }

        /** 处理上游/onError 并按策略终止或延迟错误。 */


        @Override


        public void onError(Throwable t) {
            parent.innerCloseError(t);
        }

        /** 上游完成：清理资源并向下游发送 onComplete。 */


        @Override


        public void onComplete() {
            parent.innerClose(isLeft, this);
        }

    }

}
