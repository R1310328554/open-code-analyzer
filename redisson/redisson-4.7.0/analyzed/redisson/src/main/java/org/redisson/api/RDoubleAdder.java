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
 * {@link java.util.concurrent.atomic.DoubleAdder} 的分布式实现。
 * <p>局部增量在客户端维护，{@link #sum()} 汇总所有实例。
 *
 * @author Nikita Koksharov
 */
public interface RDoubleAdder extends RExpirable, RDestroyable {

    /**
     * 累加指定浮点增量。
     * 
     * @param x 增量值
     */
    void add(double x);
    
    /**
     * 将计数加 1。
     */
    void increment();

    /**
     * 将计数减 1。
     */
    void decrement();
    
    /**
     * 汇总所有 RDoubleAdder 实例的局部增量。
     * 
     * @return 汇总后的总和
     */
    double sum();
    
    /**
     * 将所有 RDoubleAdder 实例的计数重置为零。
     */
    void reset();
    
    /**
     * Accumulates sum across all RDoubleAdder instances
     * 
     * @return accumulated sum
     */
    RFuture<Double> sumAsync();

    
    /**
     * 汇总所有 RDoubleAdder 实例的局部增量。
     * 在指定 {@code timeout} 内完成。
     * 
     * @param timeout 最长等待时间
     * @param timeUnit 延迟时间单位
     * 
     * @return 汇总后的总和
     */
    RFuture<Double> sumAsync(long timeout, TimeUnit timeUnit);

    /**
     * 将所有 RDoubleAdder 实例的计数重置为零。
     * 
     * @return void
     */
    RFuture<Void> resetAsync();
    
    /**
     * 将所有 RDoubleAdder 实例的计数重置为零。
     * 在指定 {@code timeout} 内完成。
     * 
     * @param timeout 最长等待时间
     * @param timeUnit 延迟时间单位
     * 
     * @return void
     */
    RFuture<Void> resetAsync(long timeout, TimeUnit timeUnit);

}
