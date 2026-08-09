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

import org.redisson.api.TDigestInfo;
import org.redisson.client.handler.State;

import java.util.List;

/**
 * Redis TDigest {@code TDIGEST.INFO} 响应解码器。
 * <p>
 * 从键值交替的 {@link List} 中提取压缩率、容量、节点数、
 * 权重与观测次数等字段，构造 {@link TDigestInfo}。
 *
 * @author Nikita Koksharov
 *
 */
public class TDigestInfoDecoder implements MultiDecoder<TDigestInfo> {

    /** 按预定义字段名从 parts 中取值并填充 {@link TDigestInfo}。 */
    @Override
    public TDigestInfo decode(List<Object> parts, State state) {
        return new TDigestInfo(
                getLong(parts, "Compression"),
                getLong(parts, "Capacity"),
                getLong(parts, "Merged nodes"),
                getLong(parts, "Unmerged nodes"),
                getDouble(parts, "Merged weight"),
                getDouble(parts, "Unmerged weight"),
                getLong(parts, "Observations"),
                getLong(parts, "Total compressions"),
                getLong(parts, "Memory usage"));
    }

    /** 在键值对序列中线性查找指定键对应的值。 */
    private static Object getValue(List<Object> list, String key) {
        for (int i = 0; i < list.size() - 1; i += 2) {
            if (key.equals(String.valueOf(list.get(i)))) {
                return list.get(i + 1);
            }
        }
        return null;
    }

    /** 将字段值安全转为 {@code long}，缺失或非法时返回 0。 */
    private static long getLong(List<Object> list, String key) {
        Object value = getValue(list, key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value != null) {
            try {
                return (long) Double.parseDouble(value.toString());
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }
        return 0;
    }

    /** 将字段值安全转为 {@code double}，缺失或非法时返回 0。 */
    private static double getDouble(List<Object> list, String key) {
        Object value = getValue(list, key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value != null) {
            try {
                return Double.parseDouble(value.toString());
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }
        return 0;
    }

}
