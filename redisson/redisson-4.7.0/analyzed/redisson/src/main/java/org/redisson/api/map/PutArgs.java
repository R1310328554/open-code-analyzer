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
package org.redisson.api.map;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * Map 批量写入操作的参数接口。
 * <p>
 * 支持指定待写入条目，并可配置 TTL、保留原 TTL 或绝对过期时间。
 *
 * @author Nikita Koksharov
 *
 */
public interface PutArgs<K, V> {

    /**
     * 创建包含待写入条目映射的参数对象。
     *
     * @param values 待写入的键值映射
     * @return 参数对象
     */
    static <K, V> PutArgs<K, V> entries(Map<K, V> values) {
        return new PutParams<>(values);
    }

    /**
     * 保留各键原有的 TTL（不重置过期时间）。
     *
     * @return 参数对象
     */
    PutArgs<K, V> keepTTL();

    /**
     * 为写入的键设置生存时间。
     *
     * @param ttl 生存时长
     * @return 参数对象
     */
    PutArgs<K, V> timeToLive(Duration ttl);

    /**
     * 为写入的键设置绝对过期时间点（Unix 时间）。
     *
     * @param time 过期时刻
     * @return 参数对象
     */
    PutArgs<K, V> expireAt(Instant time);

}
