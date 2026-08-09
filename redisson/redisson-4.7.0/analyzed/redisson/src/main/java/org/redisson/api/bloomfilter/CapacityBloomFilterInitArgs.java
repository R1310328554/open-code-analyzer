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
 * {@code BF.RESERVE} 构建链中的容量配置阶段；设置预期插入元素数。
 *
 * @author Su Ko
 */
public interface CapacityBloomFilterInitArgs {

    /**
     * 设置过滤器设计容量（预期插入元素数，须大于 0）。
     * 达到容量后将创建新的子过滤器（除非启用 nonScaling）。
     *
     * @param capacity 预期插入元素数
     * @return 可选扩展参数配置阶段
     */
    OptionalBloomFilterInitArgs capacity(long capacity);
}
