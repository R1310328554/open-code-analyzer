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
package org.redisson.api.bucket;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * 批量设置 Bucket 键值对的参数接口。
 * <p>
 * 支持指定条目映射，并可配置 TTL、保留原 TTL 或绝对过期时间。
 *
 * @author seakider
 * @author Nikita Koksharov
 *
 */
public interface SetArgs {

    /**
     * 创建包含待设置条目映射的参数对象。
     *
     * @param values 键值映射
     * @return 参数对象
     */
    static SetArgs entries(Map<String, ?> values) {
        return new SetParams(values);
    }

    /**
     * 保留各键原有的 TTL（不重置过期时间）。
     *
     * @return 参数对象
     */
    SetArgs keepTTL();

    /**
     * 为写入的键设置生存时间。
     *
     * @param ttl 生存时长
     * @return 参数对象
     */
    SetArgs timeToLive(Duration ttl);

    /**
     * 为写入的键设置绝对过期时间点（Unix 时间）。
     *
     * @param time 过期时刻
     * @return 参数对象
     */
    SetArgs expireAt(Instant time);

}
