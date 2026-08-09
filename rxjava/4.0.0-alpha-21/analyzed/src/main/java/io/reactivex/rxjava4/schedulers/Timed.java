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

package io.reactivex.rxjava4.schedulers;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava4.annotations.NonNull;

/**
 * 保存值及其时间信息。
 *
 * @param <T> 值类型
 * @param value 要保存的项
 * @param time 时间值
 * @param unit 时间单位
 * @since 4.0.0
 */
public record Timed<T>(T value, long time, TimeUnit unit) {
    /**
     * 以给定值与时间信息构造 {@code Timed}。
     *
     * @param value 要保存的值
     * @param time  要保存的时间
     * @param unit  时间单位，不可为 null
     * @throws NullPointerException 若 {@code value} 或 {@code unit} 为 {@code null}
     */
    public Timed {
        Objects.requireNonNull(value, "value is null");
        Objects.requireNonNull(unit, "unit is null");
    }

    /**
     * 以指定时间单位返回所含时间值。
     *
     * @param unit 时间单位
     * @return 转换后的时间
     */
    public long time(@NonNull TimeUnit unit) {
        return unit.convert(time, this.unit);
    }

    @Override
    public String toString() {
        return "Timed[time=" + time + ", unit=" + unit + ", value=" + value + "]";
    }
}
