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
package org.redisson.api.atomic;

import java.time.Duration;
import java.time.Instant;

/**
 * 扩展原子递增操作的基础参数接口。
 *
 * @author lamnt2008
 *
 * @param <T> 参数对象类型
 */
public interface BaseIncrementArgs<T> {

    /**
     * 将递增结果限制在下界或上界（未显式指定时使用类型极限）内，
     * 而非拒绝本次操作。
     * <p>
     * 未启用此选项时，越界结果会保持当前值与过期时间不变，并返回当前值。
     *
     * @return 参数对象
     */
    T saturate();

    /**
     * 设置指定的过期时间（TTL）。
     *
     * @param ttl 存活时长
     * @return 参数对象
     */
    T timeToLive(Duration ttl);

    /**
     * 设置键将在指定 Unix 时间点过期。
     *
     * @param time 过期时间
     * @return 参数对象
     */
    T expireAt(Instant time);

    /**
     * 移除现有过期时间，使键持久化。
     *
     * @return 参数对象
     */
    T persist();

    /**
     * 仅在键尚未设置过期时间时才应用过期配置。
     *
     * @return 参数对象
     */
    T expireIfNotSet();

}
