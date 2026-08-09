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

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.ProtocolViolationException;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 辅助报告同一 consumer 类型的重复订阅，
 * 替代内部 "Disposable already set!" 消息（该消息主要留给内部算子 bug）。
 */
public final class EndConsumerHelper {

    /**
     * 工具类，禁止实例化。
     */
    private EndConsumerHelper() {
        throw new IllegalStateException("No instances!");
    }

    /**
     * 确保上游 Disposable 为 null；否则 dispose next 并在非共享 disposed 实例时报告重复订阅。
     * @param upstream 上游当前值
     * @param next 待设置的 Disposable
     * @param observer consumer 类，用于个性化错误消息
     * @return 上游为 null 时 true
     */
    public static boolean validate(Disposable upstream, Disposable next, Class<?> observer) {
        Objects.requireNonNull(next, "next is null");
        if (upstream != null) {
            next.dispose();
            if (upstream != DisposableHelper.DISPOSED) {
                reportDoubleSubscription(observer);
            }
            return false;
        }
        return true;
    }

    /**
     * 原子地将 null 更新为 next Disposable；失败则 dispose next 并报告协议违规。
     * @param upstream 目标 AtomicReference
     * @param next 要设置的 Disposable
     * @param observer consumer 类，用于个性化错误消息
     * @return CAS 成功为 true
     */
    public static boolean setOnce(AtomicReference<Disposable> upstream, Disposable next, Class<?> observer) {
        Objects.requireNonNull(next, "next is null");
        if (!upstream.compareAndSet(null, next)) {
            next.dispose();
            if (upstream.get() != DisposableHelper.DISPOSED) {
                reportDoubleSubscription(observer);
            }
            return false;
        }
        return true;
    }

    /**
     * 确保上游 Subscription 为 null；否则 cancel next 并在非共享 cancelled 实例时报告重复订阅。
     * @param upstream 上游当前值
     * @param next 待设置的 Subscription
     * @param subscriber consumer 类，用于个性化错误消息
     * @return 上游为 null 时 true
     */
    public static boolean validate(Subscription upstream, Subscription next, Class<?> subscriber) {
        Objects.requireNonNull(next, "next is null");
        if (upstream != null) {
            next.cancel();
            if (upstream != SubscriptionHelper.CANCELLED) {
                reportDoubleSubscription(subscriber);
            }
            return false;
        }
        return true;
    }

    /**
     * 原子地将 null 更新为 next Subscription；失败则 cancel next 并报告协议违规。
     * @param upstream 目标 AtomicReference
     * @param next 要设置的 Subscription
     * @param subscriber consumer 类，用于个性化错误消息
     * @return CAS 成功为 true
     */
    public static boolean setOnce(AtomicReference<Subscription> upstream, Subscription next, Class<?> subscriber) {
        Objects.requireNonNull(next, "next is null");
        if (!upstream.compareAndSet(null, next)) {
            next.cancel();
            if (upstream.get() != SubscriptionHelper.CANCELLED) {
                reportDoubleSubscription(subscriber);
            }
            return false;
        }
        return true;
    }

    /**
     * 根据 consumer 类名构建错误消息。
     * @param consumer consumer 类名
     * @return 错误消息字符串
     */
    public static String composeMessage(String consumer) {
        return "It is not allowed to subscribe with a(n) " + consumer + " multiple times. "
                + "Please create a fresh instance of " + consumer + " and subscribe that to the target source instead.";
    }

    /**
     * 报告带个性化消息的 {@link ProtocolViolationException}，并通过 RxJavaPlugins.onError 上报。
     * @param consumer consumer 类
     */
    public static void reportDoubleSubscription(Class<?> consumer) {
        RxJavaPlugins.onError(new ProtocolViolationException(composeMessage(consumer.getName())));
    }
}
