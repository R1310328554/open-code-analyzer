/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.common.util;

import java.util.Date;

/**
 * 可注入偏移量的“当前时间”工具，便于测试与模拟时钟。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class Time {

    /** 全局时间偏移（秒），volatile 保证多线程可见性。 */
    private static volatile int offset;

    /**
     * 返回加上 {@link #offset} 秒偏移后的当前时间（秒，int）。
     *
     * @return see description
     * @deprecated Use {@link #currentTimeSeconds()} to avoid integer overflow beyond year 2038.
     */
    @Deprecated
    public static int currentTime() {
        return ((int) (System.currentTimeMillis() / 1000)) + offset;
    }

    /**
     * 返回加上 {@link #offset} 秒偏移后的当前时间（秒，long）。
     * 与 {@link #currentTime()} 不同，使用 long 避免 2038 年后 int 溢出。
     *
     * @return see description
     */
    public static long currentTimeSeconds() {
        return (System.currentTimeMillis() / 1000) + offset;
    }

    /**
     * 返回加上 {@link #offset} 秒偏移后的当前时间（毫秒）。
     *
     * @return see description
     */
    public static long currentTimeMillis() {
        return System.currentTimeMillis() + (offset * 1000L);
    }

    /**
     * 由秒级 epoch 时间构造 {@link Date}。
     *
     * @param time Time in milliseconds since the epoch
     * @return see description
     */
    public static Date toDate(int time) {
        return new Date(time * 1000L);
    }

    /**
     * 由毫秒级 epoch 时间构造 {@link Date}。
     *
     * @param time Time in milliseconds since the epoch
     * @return see description
     */
    public static Date toDate(long time) {
        return new Date(time);
    }

    /**
     * 将秒级 epoch 时间转换为毫秒（不做偏移调整）。
     *
     * @param time Time in seconds since the epoch
     * @return Time in milliseconds
     */
    public static long toMillis(long time) {
        return time * 1000L;
    }

    /**
     * @return Time offset in seconds that will be added to {@link #currentTime()} and {@link #currentTimeMillis()}.
     */
    public static int getOffset() {
        return offset;
    }

    /**
     * 设置将叠加到 {@link #currentTime()} 与 {@link #currentTimeMillis()} 的时间偏移（秒）。
     *
     * @param offset Offset (in seconds)
     */
    public static void setOffset(int offset) {
        Time.offset = offset;
    }

}
