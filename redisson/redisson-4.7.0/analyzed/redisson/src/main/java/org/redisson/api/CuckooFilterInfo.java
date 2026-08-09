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

import java.util.List;

/**
 * {@code CF.INFO} 命令返回的布谷鸟过滤器统计信息。
 *
 * @author Nikita Koksharov
 */
public class CuckooFilterInfo {

    private final long size;
    private final long numberOfBuckets;
    private final long numberOfFilters;
    private final long numberOfInsertedItems;
    private final long numberOfDeletedItems;
    private final long bucketSize;
    private final long expansionRate;
    private final long maxIterations;

    /**
     * 从 {@code CF.INFO} 返回的原始列表构造实例。
     * <p>响应为字段名与值交替的扁平列表：
     * {@code [field1, value1, field2, value2, ...]}。
     *
     * @param info 原始响应列表
     */
    public CuckooFilterInfo(List<Object> info) {
        this.size = getLong(info, "Size");
        this.numberOfBuckets = getLong(info, "Number of buckets");
        this.numberOfFilters = getLong(info, "Number of filters");
        this.numberOfInsertedItems = getLong(info, "Number of items inserted");
        this.numberOfDeletedItems = getLong(info, "Number of items deleted");
        this.bucketSize = getLong(info, "Bucket size");
        this.expansionRate = getLong(info, "Expansion rate");
        this.maxIterations = getLong(info, "Max iterations");
    }

    private static long getLong(List<Object> list, String key) {
        for (int i = 0; i < list.size() - 1; i += 2) {
            if (key.equals(String.valueOf(list.get(i)))) {
                Object value = list.get(i + 1);
                if (value instanceof Number) {
                    return ((Number) value).longValue();
                }
            }
        }
        return 0;
    }

    /**
     * 返回过滤器占用的内存字节数。
     *
     * @return 字节数
     */
    public long getSize() {
        return size;
    }

    /**
     * 返回过滤器中的桶数量。
     *
     * @return 桶数量
     */
    public long getNumberOfBuckets() {
        return numberOfBuckets;
    }

    /**
     * 返回子过滤器数量。
     *
     * @return 子过滤器数量
     */
    public long getNumberOfFilters() {
        return numberOfFilters;
    }

    /**
     * 返回已插入的元素数量。
     *
     * @return 已插入元素数
     */
    public long getNumberOfInsertedItems() {
        return numberOfInsertedItems;
    }

    /**
     * 返回已删除的元素数量。
     *
     * @return 已删除元素数
     */
    public long getNumberOfDeletedItems() {
        return numberOfDeletedItems;
    }

    /**
     * 返回每个桶可容纳的元素数量。
     *
     * @return 桶容量
     */
    public long getBucketSize() {
        return bucketSize;
    }

    /**
     * 返回扩容倍率。
     *
     * @return 扩容倍率
     */
    public long getExpansionRate() {
        return expansionRate;
    }

    /**
     * 返回判定过滤器已满前的最大交换尝试次数。
     *
     * @return 最大迭代次数
     */
    public long getMaxIterations() {
        return maxIterations;
    }
}
