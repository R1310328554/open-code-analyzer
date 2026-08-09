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

import java.util.Collection;

/**
 * {@code BF.INSERT} 命令参数的默认实现，封装待插入元素及可选容量、误判率、扩展等配置。
 *
 * @author Su Ko
 * @param <V> 元素类型
 */
public class BloomFilterInsertParams<V> implements BloomFilterInsertArgs<V>, OptionalBloomFilterInsertArgs<V> {
    /** 待插入的元素集合。 */
    private final Collection<V> elements;

    /** 期望误判率，可选。 */
    private Double errorRate;
    /** 设计容量，可选。 */
    private Long capacity;
    /** 扩展倍率，与 nonScaling 互斥。 */
    private Long expansionRate;
    /** 是否禁止创建新子过滤器。 */
    private Boolean nonScaling;
    /** 过滤器不存在时是否跳过创建。 */
    private Boolean noCreate;

    /** 以给定元素集合构造插入参数。 */
    public BloomFilterInsertParams(Collection<V> elements) {
        this.elements = elements;
    }

    /** 返回期望误判率。 */
    public Double getErrorRate() {
        return errorRate;
    }

    /** 返回设计容量。 */
    public Long getCapacity() {
        return capacity;
    }

    /** 返回扩展倍率。 */
    public Long getExpansionRate() {
        return expansionRate;
    }

    /** 返回 noCreate 选项。 */
    public Boolean isNoCreate() {
        return noCreate;
    }

    /** 返回 nonScaling 选项。 */
    public Boolean isNonScaling() {
        return nonScaling;
    }

    /** 返回待插入元素集合。 */
    public Collection<V> getElements() {
        return elements;
    }

    @Override
    public OptionalBloomFilterInsertArgs<V> capacity(long capacity) {
        this.capacity = capacity;
        return this;
    }

    @Override
    public OptionalBloomFilterInsertArgs<V> errorRate(double errorRate) {
        this.errorRate = errorRate;
        return this;
    }

    @Override
    public OptionalBloomFilterInsertArgs<V> expansionRate(long expansionRate) {
        this.expansionRate = expansionRate;
        return this;
    }

    @Override
    public OptionalBloomFilterInsertArgs<V> nonScaling(boolean nonScaling) {
        this.nonScaling = nonScaling;
        return this;
    }

    @Override
    public OptionalBloomFilterInsertArgs<V> noCreate(boolean noCreate) {
        this.noCreate = noCreate;
        return this;
    }
}
