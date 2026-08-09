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

import org.redisson.api.atomic.CompareAndDeleteArgs;
import org.redisson.api.atomic.DoubleIncrementArgs;

/**
 * {@link RAtomicDouble} 的异步 API 接口。
 * <p>各方法返回 {@link RFuture}，基于 Redis {@code INCRBYFLOAT} 等命令实现分布式浮点原子操作。
 *
 * @author Nikita Koksharov
 */
public interface RAtomicDoubleAsync extends RExpirableAsync {

    /**
     * 若当前值满足 {@link CompareAndDeleteArgs} 定义的条件，则原子删除。
     *
     * @param args 比较并删除参数
     * @return 删除成功为 {@code true}，否则 {@code false}
     */
    RFuture<Boolean> compareAndDeleteAsync(CompareAndDeleteArgs args);

    /**
     * 仅当当前值等于 {@code expect} 时，原子设置为 {@code update}（CAS）。
     *
     * @param expect 期望值
     * @param update 新值
     * @return 成功为 {@code true}；当前值与期望不符则为 {@code false}
     */
    RFuture<Boolean> compareAndSetAsync(double expect, double update);

    /**
     * 原子地将 {@code delta} 加到当前值上。
     *
     * @param delta 增量
     * @return 更新后的值
     */
    RFuture<Double> addAndGetAsync(double delta);

    /**
     * 原子地将当前值减 1。
     *
     * @return 更新后的值
     */
    RFuture<Double> decrementAndGetAsync();

    /**
     * 返回当前值。
     *
     * @return 当前值
     */
    RFuture<Double> getAsync();

    /**
     * 读取当前值并删除该 Redis 键。
     *
     * @return 删除前的值
     */
    RFuture<Double> getAndDeleteAsync();
    
    /**
     * 原子地将 {@code delta} 加到当前值上并返回更新后的值。
     *
     * @param delta 增量
     * @return 更新后的值
     */
    RFuture<Double> getAndAddAsync(double delta);

    /**
     * 原子地设置为 {@code newValue} 并返回旧值。
     *
     * @param newValue 新值
     * @return 设置前的旧值
     */
    RFuture<Double> getAndSetAsync(double newValue);

    /**
     * 原子地将当前值加 1。
     *
     * @return 更新后的值
     */
    RFuture<Double> incrementAndGetAsync();

    /**
     * 按 {@code args} 指定的步长与上界原子递增当前值。
     *
     * @param args 递增参数（步长、上界等）
     * @return 更新后的值
     */
    RFuture<Double> incrementAndGetAsync(DoubleIncrementArgs args);

    /**
     * 原子地将当前值加 1 并返回旧值。
     *
     * @return 加 1 前的旧值
     */
    RFuture<Double> getAndIncrementAsync();

    /**
     * 原子地将当前值减 1 并返回旧值。
     *
     * @return 减 1 前的旧值
     */
    RFuture<Double> getAndDecrementAsync();

    /**
     * 原子地设置为 {@code newValue}。
     *
     * @param newValue 新值
     */
    RFuture<Void> setAsync(double newValue);
    
    /**
     * 仅当当前值小于 {@code less} 时，原子设置为 {@code value}。
     *
     * @param less 比较阈值
     * @param value 新值
     * @return 更新成功为 {@code true}
     */
    RFuture<Boolean> setIfLessAsync(double less, double value);
    
    /**
     * 仅当当前值大于 {@code greater} 时，原子设置为 {@code value}。
     *
     * @param greater 比较阈值
     * @param value 新值
     * @return 更新成功为 {@code true}
     */
    RFuture<Boolean> setIfGreaterAsync(double greater, double value);

    /**
     * 注册对象事件监听器。
     *
     * @see org.redisson.api.listener.IncrByListener
     * @see org.redisson.api.ExpiredObjectListener
     * @see org.redisson.api.DeletedObjectListener
     *
     * @param listener 事件监听器
     * @return 监听器 ID
     */
    RFuture<Integer> addListenerAsync(ObjectListener listener);

}
