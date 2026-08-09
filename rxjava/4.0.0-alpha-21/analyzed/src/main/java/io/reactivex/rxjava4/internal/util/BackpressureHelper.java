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

import java.util.concurrent.atomic.AtomicLong;

import io.reactivex.rxjava4.annotations.NonNull;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 背压相关工具类，用于请求聚合等操作。
 */
public final class BackpressureHelper {
    /** 工具类，禁止实例化。 */
    private BackpressureHelper() {
        throw new IllegalStateException("No instances!");
    }

    /**
     * 两 long 相加，结果上限为 {@link Long#MAX_VALUE}。
     * @param aValue 第一个值
     * @param bValue 第二个值
     * @return 上限截断后的和
     */
    public static long addCap(long aValue, long bValue) {
        long u = aValue + bValue;
        if (u < 0L) {
            return Long.MAX_VALUE;
        }
        return u;
    }

    /**
     * 两 long 相乘，结果上限为 {@link Long#MAX_VALUE}。
     * @param aValue 第一个值
     * @param bValue 第二个值
     * @return 上限截断后的积
     */
    public static long multiplyCap(long aValue, long bValue) {
        long u = aValue * bValue;
        if (((aValue | bValue) >>> 31) != 0) {
            if (u / aValue != bValue) {
                return Long.MAX_VALUE;
            }
        }
        return u;
    }

    /**
     * 原子地将正数 n 加到 {@link AtomicLong} 请求量上，结果上限 {@link Long#MAX_VALUE}，返回加之前的值。
     * @param requested 持有当前请求量的 {@code AtomicLong}
     * @param n 要加的值，应为正数（未校验）
     * @return 加法前的原值
     */
    public static long add(@NonNull AtomicLong requested, long n) {
        for (;;) {
            long r = requested.get();
            if (r == Long.MAX_VALUE) {
                return Long.MAX_VALUE;
            }
            long u = addCap(r, n);
            if (requested.compareAndSet(r, u)) {
                return r;
            }
        }
    }

    /**
     * 原子加请求量，将 {@link Long#MIN_VALUE} 视为取消标记（不再累加）。
     * @param requested 持有当前请求量的 {@code AtomicLong}
     * @param n 要加的值，应为正数（未校验）
     * @return 加法前的原值
     */
    public static long addCancel(@NonNull AtomicLong requested, long n) {
        for (;;) {
            long r = requested.get();
            if (r == Long.MIN_VALUE) {
                return Long.MIN_VALUE;
            }
            if (r == Long.MAX_VALUE) {
                return Long.MAX_VALUE;
            }
            long u = addCap(r, n);
            if (requested.compareAndSet(r, u)) {
                return r;
            }
        }
    }

    /**
     * 原子地从目标字段减去已生产数量（除非当前为 {@link Long#MAX_VALUE}）。
     * @param requested 持有当前请求量的目标字段
     * @param n 已生产元素数，应为正数（未校验）
     * @return 更新后的请求量
     */
    public static long produced(@NonNull AtomicLong requested, long n) {
        for (;;) {
            long current = requested.get();
            if (current == Long.MAX_VALUE) {
                return Long.MAX_VALUE;
            }
            long update = current - n;
            if (update < 0L) {
                RxJavaPlugins.onError(new IllegalStateException("More produced than requested: " + update));
                update = 0L;
            }
            if (requested.compareAndSet(current, update)) {
                return update;
            }
        }
    }

    /**
     * 原子减已生产数量；若当前为 {@link Long#MIN_VALUE}（已取消）或 {@link Long#MAX_VALUE}（无界）则不修改。
     * @param requested 持有当前请求量的目标字段
     * @param n 已生产元素数，应为正数（未校验）
     * @return 更新后的请求量
     */
    public static long producedCancel(@NonNull AtomicLong requested, long n) {
        for (;;) {
            long current = requested.get();
            if (current == Long.MIN_VALUE) {
                return Long.MIN_VALUE;
            }
            if (current == Long.MAX_VALUE) {
                return Long.MAX_VALUE;
            }
            long update = current - n;
            if (update < 0L) {
                RxJavaPlugins.onError(new IllegalStateException("More produced than requested: " + update));
                update = 0L;
            }
            if (requested.compareAndSet(current, update)) {
                return update;
            }
        }
    }
}
