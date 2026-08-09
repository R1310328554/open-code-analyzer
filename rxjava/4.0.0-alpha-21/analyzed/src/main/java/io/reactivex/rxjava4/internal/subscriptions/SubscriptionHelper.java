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

package io.reactivex.rxjava4.internal.subscriptions;

import java.util.Objects;
import java.util.concurrent.atomic.*;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.exceptions.ProtocolViolationException;
import io.reactivex.rxjava4.internal.util.BackpressureHelper;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * Subscription 校验与原子设置工具：validate/set/setOnce/replace/cancel
 * 及 deferredRequest/deferredSetOnce 等背压辅助方法。
 */
public enum SubscriptionHelper implements Subscription {
    /**
     * 表示已取消的 Subscription 哨兵实例。
     * <p>勿向外泄漏此单例！
     */
    CANCELLED
    ;

    @Override
    public void request(long n) {
        // deliberately ignored
    }

    @Override
    public void cancel() {
        // deliberately ignored
    }

    /**
     * 校验 current 为 null 且 next 非 null；否则上报错误。
     * @param current 当前 Subscription，应为 null
     * @param next 新 Subscription，应非 null
     * @return 校验通过则 true
     */
    public static boolean validate(Subscription current, Subscription next) {
        if (next == null) {
            RxJavaPlugins.onError(new NullPointerException("next is null"));
            return false;
        }
        if (current != null) {
            next.cancel();
            reportSubscriptionSet();
            return false;
        }
        return true;
    }

    /** 上报“Subscription already set”协议违规。 */
    public static void reportSubscriptionSet() {
        RxJavaPlugins.onError(new ProtocolViolationException("Subscription already set!"));
    }

    /**
     * 校验 request 量 n 为正数。
     * @param n request 量
     * @return n 非正则 false
     */
    public static boolean validate(long n) {
        if (n <= 0) {
            RxJavaPlugins.onError(new IllegalArgumentException("n > 0 required but it was " + n));
            return false;
        }
        return true;
    }

    /**
     * 上报生产量超过 request 的协议违规。
     * @param n 超产量
     */
    public static void reportMoreProduced(long n) {
        RxJavaPlugins.onError(new ProtocolViolationException("More produced than requested: " + n));
    }

    /**
     * CAS 设置 Subscription 并 cancel 旧值。
     * @param field 目标 AtomicReference
     * @param s 新 Subscription
     * @return 成功 true；field 已为 CANCELLED 则 false
     * @see #replace(AtomicReference, Subscription)
     */
    public static boolean set(AtomicReference<Subscription> field, Subscription s) {
        for (;;) {
            Subscription current = field.get();
            if (current == CANCELLED) {
                if (s != null) {
                    s.cancel();
                }
                return false;
            }
            if (field.compareAndSet(current, s)) {
                if (current != null) {
                    current.cancel();
                }
                return true;
            }
        }
    }

    /**
     * 仅在 field 为 null 时 CAS 设置；重复设置则 reportSubscriptionSet。
     * @param field 目标 AtomicReference
     * @param s 新 Subscription
     * @return 首次设置成功则 true
     */
    public static boolean setOnce(AtomicReference<Subscription> field, Subscription s) {
        Objects.requireNonNull(s, "s is null");
        if (!field.compareAndSet(null, s)) {
            s.cancel();
            if (field.get() != CANCELLED) {
                reportSubscriptionSet();
            }
            return false;
        }
        return true;
    }

    /**
     * CAS 替换 Subscription，不 cancel 旧值。
     * @param field 目标 AtomicReference
     * @param s 新 Subscription
     * @return 成功 true；已为 CANCELLED 则 false
     * @see #set(AtomicReference, Subscription)
     */
    public static boolean replace(AtomicReference<Subscription> field, Subscription s) {
        for (;;) {
            Subscription current = field.get();
            if (current == CANCELLED) {
                if (s != null) {
                    s.cancel();
                }
                return false;
            }
            if (field.compareAndSet(current, s)) {
                return true;
            }
        }
    }

    /**
     * getAndSet(CANCELLED) 并 cancel 原 Subscription。
     * @param field 目标 AtomicReference
     * @return 由调用线程完成 swap 则 true
     */
    public static boolean cancel(AtomicReference<Subscription> field) {
        Subscription current = field.get();
        if (current != CANCELLED) {
            current = field.getAndSet(CANCELLED);
            if (current != CANCELLED) {
                if (current != null) {
                    current.cancel();
                }
                return true;
            }
        }
        return false;
    }

    /**
     * setOnce 后 flush requested 中积压的 request。
     * @param field Subscription 字段
     * @param requested 暂存 request 的 AtomicLong
     * @param s 新 Subscription，非 null
     * @return 首次设置成功则 true
     */
    public static boolean deferredSetOnce(AtomicReference<Subscription> field, AtomicLong requested,
            Subscription s) {
        if (SubscriptionHelper.setOnce(field, s)) {
            long r = requested.getAndSet(0L);
            if (r != 0L) {
                s.request(r);
            }
            return true;
        }
        return false;
    }

    /**
     * field 有 Subscription 则直接 request，否则累加至 requested。
     * @param field 可能已持有 Subscription 的字段
     * @param requested 暂存 request
     * @param n 本次 request 量，为正
     */
    public static void deferredRequest(AtomicReference<Subscription> field, AtomicLong requested, long n) {
        Subscription s = field.get();
        if (s != null) {
            s.request(n);
        } else {
            if (SubscriptionHelper.validate(n)) {
                BackpressureHelper.add(requested, n);

                s = field.get();
                if (s != null) {
                    long r = requested.getAndSet(0L);
                    if (r != 0L) {
                        s.request(r);
                    }
                }
            }
        }
    }

    /**
     * setOnce 后立即 request 指定数量。
     * @param field 目标字段
     * @param s 新 Subscription
     * @param request 初始 request 量
     * @return 首次设置成功则 true
     * @since 2.1.11
     */
    public static boolean setOnce(AtomicReference<Subscription> field, Subscription s, long request) {
        if (setOnce(field, s)) {
            s.request(request);
            return true;
        }
        return false;
    }
}
