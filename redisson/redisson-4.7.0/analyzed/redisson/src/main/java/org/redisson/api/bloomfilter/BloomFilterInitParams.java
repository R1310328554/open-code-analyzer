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
 * {@code BF.RESERVE} 命令参数的默认实现，实现误判率、容量及可选扩展/非缩放配置的链式构建。
 *
 * @author Su Ko
 */
public class BloomFilterInitParams implements BloomFilterInitArgs, ErrorRateBloomFilterInitArgs, CapacityBloomFilterInitArgs, OptionalBloomFilterInitArgs {

    /** 期望误判率（0 到 1 之间）。 */
    private double errorRate;
    /** 设计容量（预期插入元素数）。 */
    private long capacity;
    /** 扩展倍率，与 nonScaling 互斥。 */
    private Long expansionRate;
    /** 是否禁止在达到容量时创建新子过滤器。 */
    private Boolean nonScaling;

    @Override
    public CapacityBloomFilterInitArgs errorRate(double errorRate) {
        this.errorRate = errorRate;
        return this;
    }

    @Override
    public OptionalBloomFilterInitArgs capacity(long capacity) {
        this.capacity = capacity;
        return this;
    }

    @Override
    public OptionalBloomFilterInitArgs expansionRate(long expansionRate) {
        this.expansionRate = expansionRate;
        return this;
    }

    @Override
    public OptionalBloomFilterInitArgs nonScaling(boolean nonScaling) {
        this.nonScaling = nonScaling;
        return this;
    }

    /** 返回期望误判率。 */
    public double getErrorRate() {
        return errorRate;
    }

    /** 返回设计容量。 */
    public long getCapacity() {
        return capacity;
    }

    /** 返回扩展倍率，未设置时为 null。 */
    public Long getExpansionRate() {
        return expansionRate;
    }

    /** 返回是否启用非缩放模式。 */
    public Boolean isNonScaling() {
        return nonScaling;
    }
}
