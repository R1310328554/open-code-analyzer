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

package io.reactivex.rxjava4.internal.disposables;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.ProtocolViolationException;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 原子操作 Disposable 的工具方法。
 */
public enum DisposableHelper implements Disposable {
    /**
     * 表示终态、已 dispose 状态的单例实例，请勿泄漏。
     */
    DISPOSED
    ;

    /**
     * 检查给定 Disposable 是否为公共 {@link #DISPOSED} 枚举值。
     * @param d 要检查的 disposable
     * @return 若 d 为 {@link #DISPOSED} 则为 true
     */
    public static boolean isDisposed(Disposable d) {
        return d == DISPOSED;
    }

    /**
     * 原子设置字段并 dispose 旧内容。
     * @param field 目标字段
     * @param d 要设置的新 Disposable
     * @return 成功则为 true；若字段包含 {@link #DISPOSED} 实例则为 false
     */
    public static boolean set(AtomicReference<Disposable> field, Disposable d) {
        for (;;) {
            Disposable current = field.get();
            if (current == DISPOSED) {
                if (d != null) {
                    d.dispose();
                }
                return false;
            }
            if (field.compareAndSet(current, d)) {
                if (current != null) {
                    current.dispose();
                }
                return true;
            }
        }
    }

    /**
     * 原子地将字段设置为给定非 null Disposable 并返回 true，
     * 若字段非 null 则返回 false。
     * 若目标字段包含公共 DISPOSED 实例，则 dispose 提供的 disposable。
     * 若字段包含其它非 null Disposable，则向 RxJavaPlugins.onError 钩子报告 IllegalStateException。
     *
     * @param field 目标字段
     * @param d 要设置的 disposable，不可为 null
     * @return 操作成功则为 true，否则为 false
     */
    public static boolean setOnce(AtomicReference<Disposable> field, Disposable d) {
        Objects.requireNonNull(d, "d is null");
        if (!field.compareAndSet(null, d)) {
            d.dispose();
            if (field.get() != DISPOSED) {
                reportDisposableSet();
            }
            return false;
        }
        return true;
    }

    /**
     * 原子地将字段中的 Disposable 替换为给定新 Disposable，但不 dispose 旧实例。
     * @param field 要更改的目标字段
     * @param d 新 disposable，允许为 null
     * @return 操作成功则为 true；若目标字段包含公共 DISPOSED 实例且给定 disposable（若非 null）已被 dispose 则为 false
     */
    public static boolean replace(AtomicReference<Disposable> field, Disposable d) {
        for (;;) {
            Disposable current = field.get();
            if (current == DISPOSED) {
                if (d != null) {
                    d.dispose();
                }
                return false;
            }
            if (field.compareAndSet(current, d)) {
                return true;
            }
        }
    }

    /**
     * 若字段中的 Disposable 尚未 dispose，则原子 dispose 它。
     * @param field 目标字段
     * @return 若当前线程成功 dispose Disposable 则为 true
     */
    public static boolean dispose(AtomicReference<Disposable> field) {
        Disposable current = field.get();
        Disposable d = DISPOSED;
        if (current != d) {
            current = field.getAndSet(d);
            if (current != d) {
                if (current != null) {
                    current.dispose();
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 验证 current 为 null 且 next 非 null，否则向 RxJavaPlugins 报告错误并返回 false。
     * @param current 当前 Disposable，期望为 null
     * @param next 下一个 Disposable，期望非 null
     * @return 验证成功则为 true
     */
    public static boolean validate(Disposable current, Disposable next) {
        if (next == null) {
            RxJavaPlugins.onError(new NullPointerException("next is null"));
            return false;
        }
        if (current != null) {
            next.dispose();
            reportDisposableSet();
            return false;
        }
        return true;
    }

    /**
     * 向 RxJavaPlugins 错误处理器报告 disposable 已被设置。
     */
    public static void reportDisposableSet() {
        RxJavaPlugins.onError(new ProtocolViolationException("Disposable already set!"));
    }

    /**
     * 若字段为 null 则原子尝试设置给定 Disposable；若字段包含 {@link #DISPOSED} 则 dispose 它。
     * @param field 目标字段
     * @param d 要设置的 disposable
     * @return 成功则为 true，否则为 false
     */
    public static boolean trySet(AtomicReference<Disposable> field, Disposable d) {
        if (!field.compareAndSet(null, d)) {
            if (field.get() == DISPOSED) {
                d.dispose();
            }
            return false;
        }
        return true;
    }

    /** 故意无操作。 */
    @Override
    public void dispose() {
        // deliberately no-op
    }

    /** 始终返回 true，表示已 dispose。 */
    @Override
    public boolean isDisposed() {
        return true;
    }
}
