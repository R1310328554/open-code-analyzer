/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.api;

import java.util.Objects;

/**
 * 时间序列集合条目。
 *
 * @author Nikita Koksharov
 *
 * @param <V> 值类型
 * @param <L> 标签类型
 */
public class TimeSeriesEntry<V, L> {

    private long timestamp;
    private V value;

    private L label;

    public TimeSeriesEntry(long timestamp, V value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    public TimeSeriesEntry(long timestamp, V value, L label) {
        this.timestamp = timestamp;
        this.value = value;
        this.label = label;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public V getValue() {
        return value;
    }

    public L getLabel() {
        return label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeSeriesEntry<?, ?> that = (TimeSeriesEntry<?, ?>) o;
        return timestamp == that.timestamp && value.equals(that.value) && Objects.equals(label, that.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, value, label);
    }

    @Override
    public String toString() {
        return "Entry{" +
                "timestamp=" + timestamp +
                ", value=" + value +
                '}';
    }

}
