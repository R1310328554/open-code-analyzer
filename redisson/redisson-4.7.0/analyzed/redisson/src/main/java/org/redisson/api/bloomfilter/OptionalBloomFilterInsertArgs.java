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
 * {@code BF.INSERT} 构建链中的可选参数阶段；可配置容量、误判率、扩展及 noCreate 等选项。
 *
 * @author Su Ko
 * @param <V> 元素类型
 */
public interface OptionalBloomFilterInsertArgs<V> extends BloomFilterInsertArgs<V> {

    /**
     * 设置设计容量（预期插入元素数，须大于 0）；达到容量时创建新子过滤器。
     *
     * @param capacity 预期插入元素数
     * @return 当前构建器
     */
    OptionalBloomFilterInsertArgs<V> capacity(long capacity);

    /**
     * 设置期望误判率（须大于 0 且小于 1）。
     *
     * @param errorRate 期望误判率
     * @return 当前构建器
     */
    OptionalBloomFilterInsertArgs<V> errorRate(double errorRate);

    /**
     * 设置扩展倍率；与 {@link #nonScaling(boolean)} 互斥。
     *
     * @param expansionRate 扩展倍率
     * @return 当前构建器
     */
    OptionalBloomFilterInsertArgs<V> expansionRate(long expansionRate);

    /**
     * 启用非缩放模式；与 {@link #expansionRate(long)} 互斥。
     *
     * @param nonScaling 是否禁止扩展
     * @return 当前构建器
     */
    OptionalBloomFilterInsertArgs<V> nonScaling(boolean nonScaling);

    /**
     * 设置 noCreate：过滤器不存在时不自动创建。
     *
     * @param noCreate 是否跳过自动创建
     * @return 当前构建器
     */
    OptionalBloomFilterInsertArgs<V> noCreate(boolean noCreate);
}