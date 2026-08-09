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

import java.util.List;
import java.util.concurrent.TimeUnit;

import reactor.core.publisher.Mono;

/**
 * 支持租约时间的可过期信号量 Reactor API。
 * <p>每次 acquire 生成 128 位唯一 permitId，释放时必须携带对应 ID。
 * <p>非公平模式，各方法返回 {@link Mono}。
 *
 * @author Nikita Koksharov
 */
public interface RPermitExpirableSemaphoreReactive extends RExpirableReactive {
    
    /**
     * 获取一个许可并返回其 ID；必要时阻塞等待直到许可可用。
     * 
     * @return 许可 ID
     */
    Mono<String> acquire();

    /**
     * 响应式获取指定数量许可；必要时等待全部可用。
     *
     * @param permits 待获取许可数量
     * @return 许可 ID 列表
     */
    Mono<List<String>> acquire(int permits);
    
    /**
     * 获取带租约时间的许可并返回 ID；必要时阻塞等待。
     * 
     * @param leaseTime 许可租约时间
     * @param unit 时间单位
     * @return 许可 ID
     */
    Mono<String> acquire(long leaseTime, TimeUnit unit);

    /**
     * Acquires defined amount of <code>permits</code> with defined <code>leaseTime</code> and returns ids.
     * Waits if necessary until all permits became available.
     *
     * @param permits 待获取许可数量
     * @param leaseTime permits lease time
     * @param unit 时间单位
     * @return 许可 ID 列表
     */
    Mono<List<String>> acquire(int permits, long leaseTime, TimeUnit unit);
    
    /**
     * 尝试立即获取当前可用许可并返回 ID。
     *
     * @return 许可 ID if a permit was acquired and {@code null}
     *         otherwise
     */
    Mono<String> tryAcquire();

    /**
     * 尝试立即获取指定数量的当前可用许可并返回 ID 列表。
     *
     * @param permits 待获取许可数量
     * @return 许可 ID 列表 if permits were acquired and empty collection
     *         otherwise
     */
    Mono<List<String>> tryAcquire(int permits);

    /**
     * 尝试立即获取当前可用许可并返回 ID。
     * Waits up to defined <code>waitTime</code> if necessary until a permit became available.
     *
     * @param waitTime 最大等待时间
     * @param unit the time unit
     * @return 许可 ID if a permit was acquired and {@code null}
     *         if the waiting time elapsed before a permit was acquired
     */
    Mono<String> tryAcquire(long waitTime, TimeUnit unit);

    /**
     * 尝试获取带租约的许可；必要时最多等待 {@code waitTime}。
     *
     * @param waitTime 最大等待时间
     * @param leaseTime 许可租约时间, use -1 to make it permanent
     * @param unit the time unit
     * @return 许可 ID if a permit was acquired and <code>null</code>
     *         if the waiting time elapsed before a permit was acquired
     */
    Mono<String> tryAcquire(long waitTime, long leaseTime, TimeUnit unit);
    
    /**
     * 响应式尝试获取指定数量且带租约的许可；必要时最多等待 {@code waitTime}。
     * 
     * @param permits 待获取许可数量
     * @param waitTime 最大等待时间
     * @param leaseTime 许可租约时间，-1 表示永久
     * @param unit the time unit
     * @return 许可 ID 列表 if permits were acquired and empty collection
     *         if the waiting time elapsed before permits were acquired
     */
    Mono<List<String>> tryAcquire(int permits, long waitTime, long leaseTime, TimeUnit unit);

    /**
     * 尝试按 ID 释放许可。
     *
     * @param permitId 许可 ID
     * @return 释放成功则为 true，否则 false
     */
    Mono<Boolean> tryRelease(String permitId);

    /**
     * 尝试按 ID 列表批量释放许可。
     *
     * @param permitsIds 许可 ID 列表
     * @return 成功释放的许可数量
     */
    Mono<Integer> tryRelease(List<String> permitsIds);
    
    /**
     * 按 ID 释放许可并增加可用计数；ID 无效或已释放时抛出异常。
     * 
     * @param permitId 许可 ID
     * @return 无返回值
     */
    Mono<Void> release(String permitId);
    
    /**
     * 响应式按 ID 列表释放许可；无效 ID 时抛出异常。
     *
     * @param permitsIds 许可 ID 列表
     * @return 无返回值
     */
    Mono<Void> release(List<String> permitsIds);

    /**
     * 返回当前可用许可数量。
     *
     * @return 许可总数
     */
    Mono<Integer> availablePermits();

    /**
     * 返回信号量许可总数。
     *
     * @return 许可总数
     */
    Mono<Integer> getPermits();

    /**
     * 返回当前已被占用的许可数量。
     *
     * @return 已占用许可数
     */
    Mono<Integer> acquiredPermits();

    /**
     * 尝试设置许可总数（仅首次有效）。
     *
     * @param permits 许可数量
     * @return 设置成功则为 true，否则 false  
     */
    Mono<Boolean> trySetPermits(int permits);

    /**
     * 将许可总数设为给定值；按与当前值的差值调整可用许可数。
     *
     * @param permits 许可数量
     */
    Mono<Void> setPermits(int permits);

    /**
     * 按给定增量增加或减少可用许可数量。 
     *
     * @param permits 增减的许可数量
     * @return 无返回值
     */
    Mono<Void> addPermits(int permits);

    /**
     * 覆盖并更新指定 permitId 的租约时间。
     * 
     * @param permitId 许可 ID
     * @param leaseTime 许可租约时间, use -1 to make it permanent
     * @param unit the time unit
     * @return 更新成功则为 true，否则 false
     */
    Mono<Boolean> updateLeaseTime(String permitId, long leaseTime, TimeUnit unit);
    
    /**
     * 返回指定 permitId 的剩余租约时间（毫秒）
     *
     * @param permitId 许可 ID
     * @return 租约毫秒数；无租约则为 -1
     * @throws IllegalArgumentException if permit id doesn't exist or has already been released.
     */
    Mono<Long> getLeaseTime(String permitId);
    
}
