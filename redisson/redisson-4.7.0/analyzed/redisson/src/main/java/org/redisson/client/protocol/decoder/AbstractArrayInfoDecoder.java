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
package org.redisson.client.protocol.decoder;

import org.redisson.api.array.ArrayInfo;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * {@link ArrayInfoDecoder} 与 {@link ArrayFullInfoDecoder} 共享的数组元信息解码逻辑。
 * <p>
 * 将 Redis 返回的键值对列表转为 {@link Map}，并提供基础字段填充与类型转换辅助方法。
 *
 * @author Nikita Koksharov
 *
 */
abstract class AbstractArrayInfoDecoder {

    /** 将偶数索引为键、奇数索引为值的列表转为 {@link Map}，跳过值为 {@code null} 的项。 */
    protected Map<String, Object> toMap(List<Object> parts) {
        return IntStream.range(0, parts.size())
                .filter(i -> i % 2 == 0)
                .filter(i -> i + 1 < parts.size())
                .mapToObj(i -> parts.subList(i, i + 2))
                .filter(p -> p.get(1) != null)
                .collect(Collectors.toMap(e -> (String) e.get(0), e -> e.get(1)));
    }

    /** 从 {@code map} 填充 {@link ArrayInfo} 的通用字段（count、len、slices 等）。 */
    protected void populateBase(Map<String, Object> map, ArrayInfo info) {
        setLong(map, "count", info::setCount);
        setLong(map, "len", info::setLength);
        setLong(map, "next-insert-index", info::setNextInsertIndex);
        setLong(map, "slices", info::setSlices);
        setLong(map, "directory-size", info::setDirectorySize);
        setLong(map, "super-dir-entries", info::setSuperDirectoryEntries);
        setLong(map, "slice-size", info::setSliceSize);
    }

    /** 若 {@code map} 中存在 {@code key}，将其转为 long 后调用 {@code setter}。 */
    protected void setLong(Map<String, Object> map, String key, Consumer<Long> setter) {
        Object value = map.get(key);
        if (value != null) {
            setter.accept(toLong(value));
        }
    }

    /** 若 {@code map} 中存在 {@code key}，将其转为 double 后调用 {@code setter}。 */
    protected void setDouble(Map<String, Object> map, String key, Consumer<Double> setter) {
        Object value = map.get(key);
        if (value != null) {
            setter.accept(toDouble(value));
        }
    }

    /** 将 {@link Number} 或字符串转为 {@code long}。 */
    private long toLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(value.toString());
    }

    /** 将 {@link Number} 或字符串转为 {@code double}。 */
    private double toDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return Double.parseDouble(value.toString());
    }

}
