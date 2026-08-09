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
package org.redisson.api.bloomfilter;

/**
 * 布隆过滤器信息值对象，对应 Redis {@code BF.INFO} 命令的返回字段。
 * 包含容量、位数组大小、子过滤器数量、已插入元素数及扩展倍率等元数据。
 *
 * @author Su Ko
 */
public class BloomFilterInfo {
    private final long capacity;
    private final long size;
    private final long subFilterCount;
    private final long itemCount;
    private final long expansionRate;

    public BloomFilterInfo(Long capacity, Long size, Long subFilterCount, Long itemCount, Long expansionRate) {
        this.capacity = capacity;
        this.size = size;
        this.subFilterCount = subFilterCount;
        this.itemCount = itemCount;
        this.expansionRate = expansionRate;
    }

    /** 返回扩展倍率（创建新子过滤器时上一子过滤器大小的乘数）。 */
    public long getExpansionRate() {
        return expansionRate;
    }

    /** 返回已插入元素数量。 */
    public long getItemCount() {
        return itemCount;
    }

    /** 返回子过滤器（sub-filter）数量。 */
    public long getSubFilterCount() {
        return subFilterCount;
    }

    /** 返回位数组总大小（比特数）。 */
    public long getSize() {
        return size;
    }

    /** 返回设计容量（预期可插入元素数）。 */
    public long getCapacity() {
        return capacity;
    }
}
