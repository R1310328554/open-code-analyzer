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
package org.redisson.api;

import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的 {@link java.util.concurrent.Semaphore} Reactor 响应式 API。
 * <p>
 * 非公平模式，获取顺序不可预测。
 *
 * @author Nikita Koksharov
 *
 */
public interface RSemaphoreReactive extends RExpirableReactive {

    /**
     * 获取一个许可；若无可用许可则阻塞等待。
     *
     * @return 获取成功则为 true，否则 false
     */
    Mono<Boolean> tryAcquire();
    
    /**
     * 尝试立即获取指定数量的当前可用许可。
     *
     * @param permits 待获取许可数量
     * @return 获取成功则为 true，否则 false
     */
    Mono<Boolean> tryAcquire(int permits);

    /**
     * 获取一个许可；若无可用许可则阻塞等待。
     * 
     * @return 无返回值
     *
     */
    Mono<Void> acquire();

    /**
     * 获取指定数量的许可；若不足则阻塞等待直至全部可用。
     *
     * @param permits 待获取许可数量
     * @throws IllegalArgumentException 当 {@code permits} 为负数时抛出
     * @return 无返回值
     */
    Mono<Void> acquire(int permits);

    /**
     * 释放一个许可。
     *
     * @return 无返回值
     */
    Mono<Void> release();

    /**
     * 释放指定数量的许可。
     *
     * @param permits 许可数量
     * @return 无返回值
     */
    Mono<Void> release(int permits);

    /**
     * 仅当信号量存在时释放指定数量许可，可用许可数相应增加。
     *
     * @param permits 许可数量
     */
    Mono<Boolean> releaseIfExists(int permits);

    /**
     * 尝试设置许可总数。
     *
     * @param permits 许可总数
     * @return 设置成功则为 true，否则 false  
     */
    Mono<Boolean> trySetPermits(int permits);

    /**
     * 尝试设置许可总数并指定存活时间（TTL）。
     *
     * @param timeToLive 存活时间
     * @param permits 许可总数
     * @return 设置成功则为 true，否则 false
     */
    Mono<Boolean> trySetPermits(int permits, Duration timeToLive);

    /**
     * 请改用 {@link #tryAcquire(Duration)}。
     *
     * @param waitTime 最长等待时间
     * @param unit 时间单位
     * @return 获取成功则为 true，否则 false
     */
    @Deprecated
    Mono<Boolean> tryAcquire(long waitTime, TimeUnit unit);
    
    /**
     * 尝试立即获取当前可用许可。
     * Waits up to defined <code>waitTime</code> if necessary until a permit became available.
     *
     * @param waitTime 最长等待时间
     * @return 获取成功则为 true，否则 false
     */
    Mono<Boolean> tryAcquire(Duration waitTime);

    /**
     * 请改用 {@link #tryAcquire(int, Duration)}。
     *
     * @param permits 许可数量
     * @param waitTime 最长等待时间
     * @param unit 时间单位
     * @return 获取成功则为 true，否则 false
     */
    @Deprecated
    Mono<Boolean> tryAcquire(int permits, long waitTime, TimeUnit unit);

    /**
     * 尝试立即获取指定数量的当前可用许可。
     * Waits up to defined <code>waitTime</code> if necessary until all permits became available.
     *
     * @param permits 许可数量
     * @param waitTime 最长等待时间
     * @param unit 时间单位
     * @return 获取成功则为 true，否则 false
     */
    Mono<Boolean> tryAcquire(int permits, Duration waitTime);

    /**
     * 按指定值增加或减少可用许可数量。
     *
     * @param permits 许可数量 to add/remove
     */
    Mono<Void> addPermits(int permits);

    /**
     * 返回当前可用许可数量。
     *
     * @return 许可数量
     */
    Mono<Integer> availablePermits();

    /**
     * 获取并返回当前全部立即可用的许可数量。
     *
     * @return 许可数量
     */
    Mono<Integer> drainPermits();

}
