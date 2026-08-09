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

package io.reactivex.rxjava4.subscribers;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.annotations.NonNull;
import io.reactivex.rxjava4.core.FlowableSubscriber;
import io.reactivex.rxjava4.exceptions.*;
import io.reactivex.rxjava4.internal.subscriptions.*;
import io.reactivex.rxjava4.internal.util.ExceptionHelper;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 包装另一个 {@link Subscriber}，确保 onXXX 符合协议（序列化要求除外）。
 *
 * @param <T> 元素类型
 */
public final class SafeSubscriber<@NonNull T> implements FlowableSubscriber<T>, Subscription {
    /** 实际下游 Subscriber。 */
    final Subscriber<? super T> downstream;
    /** 上游 Subscription。 */
    Subscription upstream;
    /** 是否已进入终止状态。 */
    boolean done;

    /**
     * 包装给定 {@link Subscriber} 构造 SafeSubscriber。
     * @param downstream 实际 Subscriber（未校验非 null）
     */
    public SafeSubscriber(@NonNull Subscriber<? super T> downstream) {
        this.downstream = downstream;
    }

    /** 校验 upstream 后以自身转发 onSubscribe；异常时 cancel 并上报。 */
    @Override
    public void onSubscribe(@NonNull Subscription s) {
        if (SubscriptionHelper.validate(this.upstream, s)) {
            this.upstream = s;
            try {
                downstream.onSubscribe(this);
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                done = true;
                // can't call onError because the actual's state may be corrupt at this point
                try {
                    s.cancel();
                } catch (Throwable e1) {
                    Exceptions.throwIfFatal(e1);
                    RxJavaPlugins.onError(new CompositeException(e, e1));
                    return;
                }
                RxJavaPlugins.onError(e);
            }
        }
    }

    /** 校验 null 与订阅状态后安全转发 onNext。 */
    @Override
    public void onNext(@NonNull T t) {
        if (done) {
            return;
        }
        if (upstream == null) {
            onNextNoSubscription();
            return;
        }

        if (t == null) {
            Throwable ex = ExceptionHelper.createNullPointerException("onNext called with a null Throwable.");
            try {
                upstream.cancel();
            } catch (Throwable e1) {
                Exceptions.throwIfFatal(e1);
                onError(new CompositeException(ex, e1));
                return;
            }
            onError(ex);
            return;
        }

        try {
            downstream.onNext(t);
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            try {
                upstream.cancel();
            } catch (Throwable e1) {
                Exceptions.throwIfFatal(e1);
                onError(new CompositeException(e, e1));
                return;
            }
            onError(e);
        }
    }

    /** 未设置 Subscription 时以 EmptySubscription 订阅并向下游 onError。 */
    void onNextNoSubscription() {
        done = true;
        Throwable ex = new NullPointerException("Subscription not set!");

        try {
            downstream.onSubscribe(EmptySubscription.INSTANCE);
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            // can't call onError because the actual's state may be corrupt at this point
            RxJavaPlugins.onError(new CompositeException(ex, e));
            return;
        }
        try {
            downstream.onError(ex);
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            // if onError failed, all that's left is to report the error to plugins
            RxJavaPlugins.onError(new CompositeException(ex, e));
        }
    }

    /** 安全转发 onError；已终止或未订阅时上报 RxJavaPlugins。 */
    @Override
    public void onError(@NonNull Throwable t) {
        if (done) {
            RxJavaPlugins.onError(t);
            return;
        }
        done = true;

        if (upstream == null) {
            Throwable npe = new NullPointerException("Subscription not set!");

            try {
                downstream.onSubscribe(EmptySubscription.INSTANCE);
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                // can't call onError because the actual's state may be corrupt at this point
                RxJavaPlugins.onError(new CompositeException(t, npe, e));
                return;
            }
            try {
                downstream.onError(new CompositeException(t, npe));
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                // if onError failed, all that's left is to report the error to plugins
                RxJavaPlugins.onError(new CompositeException(t, npe, e));
            }
            return;
        }

        if (t == null) {
            t = ExceptionHelper.createNullPointerException("onError called with a null Throwable.");
        }

        try {
            downstream.onError(t);
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);

            RxJavaPlugins.onError(new CompositeException(t, ex));
        }
    }

    /** 安全转发 onComplete；未订阅时走 onCompleteNoSubscription。 */
    @Override
    public void onComplete() {
        if (done) {
            return;
        }
        done = true;

        if (upstream == null) {
            onCompleteNoSubscription();
            return;
        }

        try {
            downstream.onComplete();
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            RxJavaPlugins.onError(e);
        }
    }

    /** 未设置 Subscription 时以 EmptySubscription 订阅并向下游 onError。 */
    void onCompleteNoSubscription() {

        Throwable ex = new NullPointerException("Subscription not set!");

        try {
            downstream.onSubscribe(EmptySubscription.INSTANCE);
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            // can't call onError because the actual's state may be corrupt at this point
            RxJavaPlugins.onError(new CompositeException(ex, e));
            return;
        }
        try {
            downstream.onError(ex);
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            // if onError failed, all that's left is to report the error to plugins
            RxJavaPlugins.onError(new CompositeException(ex, e));
        }
    }

    /** 安全转发 request；异常时 cancel 并上报。 */
    @Override
    public void request(long n) {
        try {
            upstream.request(n);
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            try {
                upstream.cancel();
            } catch (Throwable e1) {
                Exceptions.throwIfFatal(e1);
                RxJavaPlugins.onError(new CompositeException(e, e1));
                return;
            }
            RxJavaPlugins.onError(e);
        }
    }

    /** 安全转发 cancel；异常上报 RxJavaPlugins。 */
    @Override
    public void cancel() {
        try {
            upstream.cancel();
        } catch (Throwable e1) {
            Exceptions.throwIfFatal(e1);
            RxJavaPlugins.onError(e1);
        }
    }
}
