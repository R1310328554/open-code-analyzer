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
 * {@code BF.RESERVE} 构建链中的可选参数阶段；可配置扩展倍率或非缩放模式。
 *
 * @author Su Ko
 */
public interface OptionalBloomFilterInitArgs extends BloomFilterInitArgs{

    /**
     * 设置扩展倍率：达到容量创建新子过滤器时，新子过滤器大小为上一子过滤器大小乘以该值。
     * 与 {@link #nonScaling(boolean)} 互斥。
     *
     * @param expansionRate 扩展倍率
     * @return 当前构建器
     */
    OptionalBloomFilterInitArgs expansionRate(long expansionRate);

    /**
     * 启用非缩放模式：达到容量时不创建新子过滤器。
     * 与 {@link #expansionRate(long)} 互斥。
     *
     * @param nonScaling 是否禁止扩展
     * @return 当前构建器
     */
    OptionalBloomFilterInitArgs nonScaling(boolean nonScaling);
}