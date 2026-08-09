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

package io.reactivex.rxjava4.internal.util;

import java.io.*;

import java.util.Objects;
import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.Observer;
import io.reactivex.rxjava4.disposables.Disposable;

/**
 * 轻量级通知编码工具：将 onNext/onError/onComplete/onSubscribe
 * 统一表示为 {@link Object}，供队列与序列化场景复用。
 */
public enum NotificationLite {
    COMPLETE
    ;

    /** 包装 Throwable 的错误通知 record。 */
        record ErrorNotification(Throwable e) implements Serializable {

            @Serial
            private static final long serialVersionUID = -8759979445933046293L;

        @Override
        public String toString() {
                return "NotificationLite.Error[" + e + "]";
            }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ErrorNotification(Throwable e1)) {
                return Objects.equals(e, e1);
            }
            return false;
        }
    }

    /** 包装 {@link Subscription} 的订阅通知 record。 */
        record SubscriptionNotification(Subscription upstream) implements Serializable {

            @Serial
            private static final long serialVersionUID = -1322257508628817540L;

        @Override
            public String toString() {
                return "NotificationLite.Subscription[" + upstream + "]";
            }
        }

    /** 包装 {@link Disposable} 的订阅通知 record。 */
        record DisposableNotification(Disposable upstream) implements Serializable {

            @Serial
            private static final long serialVersionUID = -7482590109178395495L;

        @Override
            public String toString() {
                return "NotificationLite.Disposable[" + upstream + "]";
            }
        }

    /**
     * 将普通值编码为 onNext 通知（即原值本身）。
     * @param <T> 元素类型
     * @param value 待编码值
     * @return 表示 onNext 的通知对象
     */
    public static <T> Object next(T value) {
        return value;
    }

    /** @return {@link #COMPLETE} 单例，表示 onComplete */
    public static Object complete() {
        return COMPLETE;
    }

    /**
     * 将 Throwable 包装为 {@link ErrorNotification}。
     * @param e 错误
     * @return 错误通知对象
     */
    public static Object error(Throwable e) {
        return new ErrorNotification(e);
    }

    /**
     * 将 Subscription 包装为 {@link SubscriptionNotification}。
     * @param s 上游 Subscription
     * @return 订阅通知对象
     */
    public static Object subscription(Subscription s) {
        return new SubscriptionNotification(s);
    }

    /**
     * 将 Disposable 包装为 {@link DisposableNotification}。
     * @param d 上游 Disposable
     * @return 订阅通知对象
     */
    public static Object disposable(Disposable d) {
        return new DisposableNotification(d);
    }

    /** 判断 o 是否为 COMPLETE 单例。 */
    public static boolean isComplete(Object o) {
        return o == COMPLETE;
    }

    /** 判断 o 是否为 ErrorNotification。 */
    public static boolean isError(Object o) {
        return o instanceof ErrorNotification;
    }

    /** 判断 o 是否为 SubscriptionNotification。 */
    public static boolean isSubscription(Object o) {
        return o instanceof SubscriptionNotification;
    }

    /** 判断 o 是否为 DisposableNotification。 */
    public static boolean isDisposable(Object o) {
        return o instanceof DisposableNotification;
    }

    /**
     * 从 onNext 通知中取出原值（强转）。
     * @param <T> 期望类型
     * @param o 通知对象
     * @return 元素值
     */
    @SuppressWarnings("unchecked")
    public static <T> T getValue(Object o) {
        return (T)o;
    }

    /** 从 ErrorNotification 中取出 Throwable。 */
    public static Throwable getError(Object o) {
        return ((ErrorNotification)o).e;
    }

    /** 从 SubscriptionNotification 中取出 Subscription。 */
    public static Subscription getSubscription(Object o) {
        return ((SubscriptionNotification)o).upstream;
    }

    /** 从 DisposableNotification 中取出 Disposable。 */
    public static Disposable getDisposable(Object o) {
        return ((DisposableNotification)o).upstream;
    }

    /**
     * 按通知类型调用 Subscriber 的 onComplete/onError/onNext。
     * 不处理 Subscription 通知，见 {@link #acceptFull(Object, Subscriber)}。
     * @return 若为终止事件（complete 或 error）则 true
     * @see #acceptFull(Object, Subscriber)
     */
    @SuppressWarnings("unchecked")
    public static <T> boolean accept(Object o, Subscriber<? super T> s) {
        if (o == COMPLETE) {
            s.onComplete();
            return true;
        } else
        if (o instanceof ErrorNotification) {
            s.onError(((ErrorNotification)o).e);
            return true;
        }
        s.onNext((T)o);
        return false;
    }

    /**
     * 按通知类型调用 Observer 的 onComplete/onError/onNext。
     * 不处理 Disposable 订阅通知。
     * @return 若为终止事件则 true
     */
    @SuppressWarnings("unchecked")
    public static <T> boolean accept(Object o, Observer<? super T> observer) {
        if (o == COMPLETE) {
            observer.onComplete();
            return true;
        } else
        if (o instanceof ErrorNotification) {
            observer.onError(((ErrorNotification)o).e);
            return true;
        }
        observer.onNext((T)o);
        return false;
    }

    /**
     * 完整版 accept：含 onSubscribe(Subscription) 分支。
     * @return 若为 complete 或 error 则 true
     * @see #accept(Object, Subscriber)
     */
    @SuppressWarnings("unchecked")
    public static <T> boolean acceptFull(Object o, Subscriber<? super T> s) {
        if (o == COMPLETE) {
            s.onComplete();
            return true;
        } else
        if (o instanceof ErrorNotification) {
            s.onError(((ErrorNotification)o).e);
            return true;
        } else
        if (o instanceof SubscriptionNotification) {
            s.onSubscribe(((SubscriptionNotification)o).upstream);
            return false;
        }
        s.onNext((T)o);
        return false;
    }

    /**
     * 完整版 accept：含 onSubscribe(Disposable) 分支。
     * @return 若为 complete 或 error 则 true
     * @see #accept(Object, Observer)
     */
    @SuppressWarnings("unchecked")
    public static <T> boolean acceptFull(Object o, Observer<? super T> observer) {
        if (o == COMPLETE) {
            observer.onComplete();
            return true;
        } else
        if (o instanceof ErrorNotification) {
            observer.onError(((ErrorNotification)o).e);
            return true;
        } else
        if (o instanceof DisposableNotification) {
            observer.onSubscribe(((DisposableNotification)o).upstream);
            return false;
        }
        observer.onNext((T)o);
        return false;
    }

    @Override
    public String toString() {
        return "NotificationLite.Complete";
    }
}
