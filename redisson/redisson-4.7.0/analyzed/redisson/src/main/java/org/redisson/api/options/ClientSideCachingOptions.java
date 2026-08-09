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
package org.redisson.api.options;

import java.time.Duration;

/**
 * 客户端侧缓存（Client Side Caching）配置选项。
 *
 * @author Nikita Koksharov
 *
 */
public interface ClientSideCachingOptions {

    enum EvictionPolicy {

        /**
         * 不启用驱逐策略的客户端缓存。
         */
        NONE,

        /**
         * 最近最少使用（LRU）驱逐策略。
         */
        LRU,

        /**
         * 最不经常使用（LFU）驱逐策略。
         */
        LFU,

        /**
         * 对值使用软引用（Soft Reference）的驱逐策略，
         * 当 JVM 内存不足时由 GC 回收缓存条目。
         */
        SOFT,

        /**
         * 对值使用弱引用（Weak Reference）的驱逐策略，
         * 当引用变为弱可达时由 GC 回收缓存条目。
         */
        WEAK
    };

    /**
     * 创建默认客户端侧缓存选项。
     *
     * @return 选项实例
     */
    static ClientSideCachingOptions defaults() {
        return new ClientSideCachingParams();
    }

    /**
     * 设置客户端缓存驱逐策略。
     *
     * @param evictionPolicy
     *         <p><code>LRU</code> — 最近最少使用驱逐。
     *         <p><code>LFU</code> — 最不经常使用驱逐。
     *         <p><code>SOFT</code> — 软引用；JVM 内存不足时 GC 驱逐条目。
     *         <p><code>WEAK</code> — 弱引用；引用弱可达时 GC 驱逐条目。
     *         <p><code>NONE</code> — 不启用驱逐，但 timeToLive 与 maxIdle 仍生效。
     * @return 选项实例
     */
    ClientSideCachingOptions evictionPolicy(EvictionPolicy evictionPolicy);

    /**
     * 设置客户端缓存容量。
     * <p>
     * 若 size 为 <code>0</code>，则缓存无上限。
     * <p>
     * 若 size 为 <code>-1</code>，则缓存始终为空、不存储数据。
     *
     * @param size 客户端缓存大小
     * @return 选项实例
     */
    ClientSideCachingOptions size(int size);

    /**
     * 设置客户端缓存中每条目的存活时间（TTL）。
     * 若值为 <code>0</code>，则不应用超时。
     *
     * @param ttl 存活时间
     * @return LocalCachedMapOptions 实例
     */
    ClientSideCachingOptions timeToLive(Duration ttl);

    /**
     * 设置客户端缓存中每条目的最大空闲时间。
     * 若值为 <code>0</code>，则不应用超时。
     *
     * @param idleTime 最大空闲时间
     * @return LocalCachedMapOptions 实例
     */
    ClientSideCachingOptions maxIdle(Duration idleTime);

}
