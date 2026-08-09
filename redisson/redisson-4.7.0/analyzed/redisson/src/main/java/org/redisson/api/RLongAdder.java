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

import java.util.concurrent.TimeUnit;

/**
 * {@link java.util.concurrent.atomic.LongAdder} 的分布式实现。
 * <p>客户端维护内部状态，{@link #sum()} 聚合所有实例的计数。
 *
 * @author Nikita Koksharov
 */
public interface RLongAdder extends RExpirable, RDestroyable {

    /**
     * 累加指定值。
     * 
     * @param x 累加值
     */
    void add(long x);
    
    /**
     * 将计数加 1。
     */
    void increment();

    /**
     * 将计数减 1。
     */
    void decrement();
    
    /**
     * 聚合所有 {@link RLongAdder} 实例的计数总和。
     * 
     * @return 聚合后的总和
     */
    long sum();
    
    /**
     * 重置所有 {@link RLongAdder} 实例的计数。
     */
    void reset();
    
    /**
     * Accumulates sum across all RLongAdder instances
     * 
     * @return accumulated sum
     */
    RFuture<Long> sumAsync();

    /**
     * 聚合所有 {@link RLongAdder} 实例的计数总和。
     * 在指定 {@code timeout} 内完成。
     * 
     * @param timeout 操作超时
     * @param timeUnit 时间单位
     * 
     * @return 聚合后的总和
     */
    RFuture<Long> sumAsync(long timeout, TimeUnit timeUnit);
    
    /**
     * 重置所有 {@link RLongAdder} 实例的计数。
     * 
     * @return 无返回值
     */
    RFuture<Void> resetAsync();
    
    /**
     * 重置所有 {@link RLongAdder} 实例的计数。
     * 在指定 {@code timeout} 内完成。
     * 
     * @param timeout 操作超时
     * @param timeUnit 时间单位
     * 
     * @return 无返回值
     */
    RFuture<Void> resetAsync(long timeout, TimeUnit timeUnit);

}
