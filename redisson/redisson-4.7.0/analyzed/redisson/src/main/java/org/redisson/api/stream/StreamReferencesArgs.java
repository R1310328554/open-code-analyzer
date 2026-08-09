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
package org.redisson.api.stream;

/**
 * 流裁剪时消费者组引用策略的参数接口。
 *
 * @author seakider
 *
 */
public interface StreamReferencesArgs<T> {
    /**
     * 设置 DELREF 引用策略：裁剪时移除消费者组 PEL 中的所有引用。
     * <p>
     * 需要 <b>Redis 8.2.0 及以上版本。</b>
     *
     * @return 参数对象
     */
    T removeReferences();

    /**
     * 设置 KEEPREF 引用策略：裁剪时保留消费者组 PEL 中的引用。
     * <p>
     * 需要 <b>Redis 8.2.0 及以上版本。</b>
     *
     * @return 参数对象
     */
    T keepReferences();

    /**
     * 设置 ACKED 引用策略：裁剪时仅移除已被所有消费者组确认的消息。
     * <p>
     * 需要 <b>Redis 8.2.0 及以上版本。</b>
     *
     * @return 参数对象
     */
    T removeAcknowledgedOnly();
}
