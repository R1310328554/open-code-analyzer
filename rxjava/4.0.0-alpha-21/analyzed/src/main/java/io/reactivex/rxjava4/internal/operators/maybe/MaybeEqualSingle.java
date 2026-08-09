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

package io.reactivex.rxjava4.internal.operators.maybe;

import java.io.Serial;
import java.util.concurrent.atomic.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.BiPredicate;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 比较两个 {@link MaybeSource}：均 empty 则 true；
 * 均有值则用 {@link BiPredicate} 判定是否相等。
 *
 * @param <T> 两源公共元素类型
 */
public final class MaybeEqualSingle<T> extends Single<Boolean> {
    final MaybeSource<? extends T> source1;

    final MaybeSource<? extends T> source2;

    final BiPredicate<? super T, ? super T> isEqual;

    /**
     * @param source1 第一个 MaybeSource
     * @param source2 第二个 MaybeSource
     * @param isEqual 值相等判定
     */
    public MaybeEqualSingle(MaybeSource<? extends T> source1, MaybeSource<? extends T> source2,
            BiPredicate<? super T, ? super T> isEqual) {
        this.source1 = source1;
        this.source2 = source2;
        this.isEqual = isEqual;
    }

    /** EqualCoordinator 计数两源终止后比较并 onSuccess。 */
    @Override
    protected void subscribeActual(SingleObserver<? super Boolean> observer) {
        EqualCoordinator<T> parent = new EqualCoordinator<>(observer, isEqual);
        observer.onSubscribe(parent);
        parent.subscribe(source1, source2);
    }

    @SuppressWarnings("serial")
    /** 初始计数 2，两源均 done 后比较 value。 */
    static final class EqualCoordinator<T>
    extends AtomicInteger
    implements Disposable {
        final SingleObserver<? super Boolean> downstream;

        final EqualObserver<T> observer1;

        final EqualObserver<T> observer2;

        final BiPredicate<? super T, ? super T> isEqual;

        EqualCoordinator(SingleObserver<? super Boolean> actual, BiPredicate<? super T, ? super T> isEqual) {
            super(2);
            this.downstream = actual;
            this.isEqual = isEqual;
            this.observer1 = new EqualObserver<>(this);
            this.observer2 = new EqualObserver<>(this);
        }

        void subscribe(MaybeSource<? extends T> source1, MaybeSource<? extends T> source2) {
            source1.subscribe(observer1);
            source2.subscribe(observer2);
        }

        @Override
        public void dispose() {
            observer1.dispose();
            observer2.dispose();
        }

        @Override
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(observer1.get());
        }

        @SuppressWarnings("unchecked")
        /** 两源均终止：双 null 为 true，双非 null 用 isEqual。 */
        void done() {
            if (decrementAndGet() == 0) {
                Object o1 = observer1.value;
                Object o2 = observer2.value;

                if (o1 != null && o2 != null) {
                    boolean b;

                    try {
                        b = isEqual.test((T)o1, (T)o2);
                    } catch (Throwable ex) {
                        Exceptions.throwIfFatal(ex);
                        downstream.onError(ex);
                        return;
                    }

                    downstream.onSuccess(b);
                } else {
                    downstream.onSuccess(o1 == null && o2 == null);
                }
            }
        }

        /** 首个 error 取消另一源并向下游 onError。 */
        void error(EqualObserver<T> sender, Throwable ex) {
            if (getAndSet(0) > 0) {
                if (sender == observer1) {
                    observer2.dispose();
                } else {
                    observer1.dispose();
                }
                downstream.onError(ex);
            } else {
                RxJavaPlugins.onError(ex);
            }
        }
    }

    /** 缓存 onSuccess 值或 onComplete 后通知 parent.done()。 */
    static final class EqualObserver<T>
    extends AtomicReference<Disposable>
    implements MaybeObserver<T> {

        @Serial
        private static final long serialVersionUID = -3031974433025990931L;

        final EqualCoordinator<T> parent;

        Object value;

        EqualObserver(EqualCoordinator<T> parent) {
            this.parent = parent;
        }

        public void dispose() {
            DisposableHelper.dispose(this);
        }

        @Override
        public void onSubscribe(Disposable d) {
            DisposableHelper.setOnce(this, d);
        }

        @Override
        public void onSuccess(T value) {
            this.value = value;
            parent.done();
        }

        @Override
        public void onError(Throwable e) {
            parent.error(this, e);
        }

        @Override
        public void onComplete() {
            parent.done();
        }

    }
}
