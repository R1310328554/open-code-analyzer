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
package org.redisson.api.bitset;

/**
 * BITFIELD 位偏移包装类，支持按位或按索引（{@code #} 前缀）偏移。
 *
 * @author Su Ko
 */
public final class BitOffset {

    private final long value;
    private final boolean indexed;

    private BitOffset(long value, boolean indexed) {
        this.value = value;
        this.indexed = indexed;
    }

    /**
     * 创建a bit offset.。
     *
     * @param offset 从 0 开始的位偏移
     * @return 偏移包装对象
     */
    public static BitOffset bit(long offset) {
        return new BitOffset(offset, false);
    }

    /**
     * 创建an index-based offset (prefixed with '#').。
     *
     * @param index 整型元素索引
     * @return 偏移包装对象
     */
    public static BitOffset index(long index) {
        return new BitOffset(index, true);
    }

    /** 返回原始 long 偏移值。 */
    public long getLongValue() {
        return value;
    }

    /** 返回 Redis 命令使用的偏移字符串（索引型带 {@code #} 前缀）。 */
    public String getValue() {
        if (indexed) {
            return "#" + value;
        }

        return Long.toString(value);
    }

    /** 是否为索引型偏移（{@code #} 前缀）。 */
    public boolean isIndexed() {
        return indexed;
    }
}
