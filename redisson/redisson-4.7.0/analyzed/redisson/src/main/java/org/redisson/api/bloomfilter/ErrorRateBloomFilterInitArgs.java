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
 * {@code BF.RESERVE} 构建链中的误判率配置阶段；设置期望假阳性概率。
 *
 * @author Su Ko
 */
public interface ErrorRateBloomFilterInitArgs {

    /**
     * 设置期望误判率（假阳性概率，须大于 0 且小于 1）。
     *
     * @param errorRate 期望误判率
     * @return 容量配置阶段
     */
    CapacityBloomFilterInitArgs errorRate(double errorRate);
}