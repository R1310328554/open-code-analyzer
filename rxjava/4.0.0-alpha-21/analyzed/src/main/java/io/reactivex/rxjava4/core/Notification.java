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

package io.reactivex.rxjava4.core;

import io.reactivex.rxjava4.annotations.*;
import io.reactivex.rxjava4.internal.util.NotificationLite;
import java.util.Objects;

/**
 * 表示 reactive 信号类型：{@code onNext}、{@code onError} 与 {@code onComplete}，
 * 并持有其参数值（一个值、{@link Throwable} 或空）。
 * @param <T> 值类型
 */
public final class Notification<T> {

    final Object value;

    /** 不供外部实现。
     * @param value notification 中携带的值，不可为 {@code null}
     */
    private Notification(@Nullable Object value) {
        this.value = value;
    }

    /**
     * 若本 notification 为 {@code onComplete} 信号则返回 true。
     * @return 若本 notification 为 {@code onComplete} 信号则为 true
     */
    public boolean isOnComplete() {
        return value == null;
    }

    /**
     * 若本 notification 为 {@code onError} 信号且 {@link #getError()} 返回所含 {@link Throwable} 则返回 true。
     * @return 若本 notification 为 {@code onError} 信号则为 true
     * @see #getError()
     */
    public boolean isOnError() {
        return NotificationLite.isError(value);
    }

    /**
     * 若本 notification 为 {@code onNext} 信号且 {@link #getValue()} 返回所含值则返回 true。
     * @return 若本 notification 为 {@code onNext} 信号则为 true
     * @see #getValue()
     */
    public boolean isOnNext() {
        Object o = value;
        return o != null && !NotificationLite.isError(o);
    }

    /**
     * 若本 notification 为 {@code onNext} 信号则返回所含值，否则返回 null。
     * @return 所含值或 null
     * @see #isOnNext()
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public T getValue() {
        Object o = value;
        if (o != null && !NotificationLite.isError(o)) {
            return (T)value;
        }
        return null;
    }

    /**
     * 若本 notification 为 {@code onError} 信号则返回所含 {@link Throwable} 错误，否则返回 null。
     * @return 所含 {@code Throwable} 错误或 {@code null}
     * @see #isOnError()
     */
    @Nullable
    public Throwable getError() {
        Object o = value;
        if (NotificationLite.isError(o)) {
            return NotificationLite.getError(o);
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Notification<?> n) {
            return Objects.equals(value, n.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        Object o = value;
        return o != null ? o.hashCode() : 0;
    }

    @Override
    public String toString() {
        Object o = value;
        if (o == null) {
            return "OnCompleteNotification";
        }
        if (NotificationLite.isError(o)) {
            return "OnErrorNotification[" + NotificationLite.getError(o) + "]";
        }
        return "OnNextNotification[" + value + "]";
    }

    /**
     * 构造包含给定值的 onNext notification。
     * @param <T> 值类型
     * @param value notification 中携带的值，不可为 {@code null}
     * @return 新的 Notification 实例
     * @throws NullPointerException 若 value 为 {@code null}
     */
    @NonNull
    public static <@NonNull T> Notification<T> createOnNext(T value) {
        Objects.requireNonNull(value, "value is null");
        return new Notification<>(value);
    }

    /**
     * 构造包含错误的 onError notification。
     * @param <T> 值类型
     * @param error notification 中携带的错误 Throwable，不可为 null
     * @return 新的 Notification 实例
     * @throws NullPointerException 若 error 为 {@code null}
     */
    @NonNull
    public static <T> Notification<T> createOnError(@NonNull Throwable error) {
        Objects.requireNonNull(error, "error is null");
        return new Notification<>(NotificationLite.error(error));
    }

    /**
     * 返回表示 {@code onComplete} 信号的无状态共享空 notification 实例。
     * @param <T> 目标值类型
     * @return 表示 {@code onComplete} 信号的共享 Notification 实例
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public static <T> Notification<T> createOnComplete() {
        return (Notification<T>)COMPLETE;
    }

    /** createOnComplete 的单例实例。 */
    static final Notification<Object> COMPLETE = new Notification<>(null);
}
