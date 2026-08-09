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
 * 分布式双精度原子变量 {@link java.util.concurrent.atomic.AtomicDouble} 的 Redis 实现。
 * <p>基于 {@code INCRBYFLOAT} 等命令提供原子读写与 CAS 操作。
 *
 * @author Nikita Koksharov
 */
public interface RAtomicDouble extends RExpirable, RAtomicDoubleAsync {

    /**
     * 若当前值满足 {@link CompareAndDeleteArgs} 定义的条件，则原子删除。
     *
     * @param args 比较并删除参数
     * @return 删除成功返回 {@code true}，否则 {@code false}
     */
    boolean compareAndDelete(CompareAndDeleteArgs args);

    /**
     * 将当前值原子减一并返回旧值。
     *
     * @return 减一前的值
     */
    double getAndDecrement();

    /**
     * 将给定增量原子加到当前值上。
     *
     * @param delta 要增加的增量
     * @return 更新后的值
     */
    double addAndGet(double delta);

    /**
     * 仅当当前值等于期望值时，原子设置为新值。
     *
     * @param expect 期望值
     * @param update 新值
     * @return 成功返回 {@code true}；实际值与期望值不等时返回 {@code false}
     */
    boolean compareAndSet(double expect, double update);

    /**
     * 将当前值原子减一并返回新值。
     *
     * @return 减一后的值
     */
    double decrementAndGet();

    /**
     * 返回当前值。
     *
     * @return 当前值
     */
    double get();
    
    /**
     * 读取当前值并删除该对象。
     *
     * @return 删除前的当前值
     */
    double getAndDelete();

    /**
     * 将给定增量原子加到当前值上并返回旧值。
     *
     * @param delta 要增加的增量
     * @return 加算前的旧值
     */
    double getAndAdd(double delta);

    /**
     * 原子设置为新值并返回旧值。
     *
     * @param newValue 新值
     * @return 旧值
     */
    double getAndSet(double newValue);

    /**
     * 将当前值原子加一。
     *
     * @return 加一后的值
     */
    double incrementAndGet();

    /**
     * 按 {@link DoubleIncrementArgs} 指定规则原子递增当前值。
     *
     * @param args 递增参数
     * @return 递增后的值
     */
    double incrementAndGet(DoubleIncrementArgs args);

    /**
     * 将当前值原子加一并返回旧值。
     *
     * @return 加一前的旧值
     */
    double getAndIncrement();

    /**
     * 原子设置给定值。
     *
     * @param newValue 新值
     */
    void set(double newValue);
    
    /**
     * 仅当当前值小于给定阈值时，原子设置为新值。
     *
     * @param less  比较阈值
     * @param value 新值
     * @return 更新成功返回 {@code true}
     */
    boolean setIfLess(double less, double value);
    
    /**
     * 仅当当前值大于给定阈值时，原子设置为新值。
     *
     * @param greater  比较阈值
     * @param value 新值
     * @return 更新成功返回 {@code true}
     */
    boolean setIfGreater(double greater, double value);

    /**
     * 注册对象事件监听器。
     *
     * @see org.redisson.api.listener.IncrByListener
     * @see org.redisson.api.ExpiredObjectListener
     * @see org.redisson.api.DeletedObjectListener
     *
     * @param listener 对象事件监听器
     * @return 监听器 ID
     */
    int addListener(ObjectListener listener);

}
