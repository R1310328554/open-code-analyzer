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
package org.redisson.api.cuckoofilter;

/**
 * {@link CuckooFilterInitArgs} 的默认实现。
 * <p>
 * 保存容量、桶大小、最大迭代次数及扩容倍率等初始化参数。
 *
 * @author Nikita Koksharov
 *
 */
public final class CuckooFilterInitArgsImpl implements CuckooFilterInitArgs {

    final long capacity;
    Long bucketSize;
    Long maxIterations;
    Long expansion;

    CuckooFilterInitArgsImpl(long capacity) {
        this.capacity = capacity;
    }

    /** 设置每桶元素数。 */
    @Override
    public CuckooFilterInitArgs bucketSize(long bucketSize) {
        this.bucketSize = bucketSize;
        return this;
    }

    /** 设置最大交换迭代次数。 */
    @Override
    public CuckooFilterInitArgs maxIterations(long maxIterations) {
        this.maxIterations = maxIterations;
        return this;
    }

    /** 设置扩容倍率。 */
    @Override
    public CuckooFilterInitArgs expansion(long expansion) {
        this.expansion = expansion;
        return this;
    }

    /** 返回过滤器容量。 */
    public long getCapacity() {
        return capacity;
    }

    public Long getBucketSize() {
        return bucketSize;
    }

    public Long getMaxIterations() {
        return maxIterations;
    }

    public Long getExpansion() {
        return expansion;
    }
}
