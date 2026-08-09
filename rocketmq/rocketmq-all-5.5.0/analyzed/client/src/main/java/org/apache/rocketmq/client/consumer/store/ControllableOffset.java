/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.client.consumer.store;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 线程安全的可控消费偏移量：支持原子更新与冻结（freeze），冻结后不可再更新。
 * <p>
 * 并发场景说明：
 * 若在任意 {@code update} 之前调用 {@code updateAndFreeze}，会将 {@code allowToUpdate} 置为 false
 * 并将偏移量设为目标值，此后 {@code update} 不再生效。
 * <p>
 * 若 {@code update} 与 {@code updateAndFreeze} 并发执行，最终结果取决于操作顺序：
 * 1. 若 {@code update} 的原子更新先完成，{@code updateAndFreeze} 会覆盖偏移量并禁止后续更新；
 * 2. 若 {@code updateAndFreeze} 先执行，进行中的 {@code update} 不会生效。
 * {@link AtomicLong#getAndUpdate} 保证原子性并尊重 {@code updateAndFreeze} 的最终状态。
 * <p>
 * 一旦执行 {@code updateAndFreeze}，由于 {@code allowToUpdate} 的 volatile 可见性，
 * 后续 {@code update} 调用均无法改变偏移量。
 */
public class ControllableOffset {
    // 原子方式保存当前偏移量
    private final AtomicLong value;
    // 控制是否允许更新偏移量
    private volatile boolean allowToUpdate;

    public ControllableOffset(long value) {
        this.value = new AtomicLong(value);
        this.allowToUpdate = true;
    }

    /**
     * 尝试将偏移量更新为目标值。{@code increaseOnly} 为 true 时仅允许增大。
     * 操作原子且线程安全；若偏移量已被 {@link #updateAndFreeze(long)} 冻结则不再更新。
     *
     * @param target 目标偏移量
     * @param increaseOnly 为 true 时仅当目标值大于当前值才更新
     */
    public void update(long target, boolean increaseOnly) {
        if (allowToUpdate) {
            value.getAndUpdate(val -> {
                if (allowToUpdate) {
                    if (increaseOnly) {
                        return Math.max(target, val);
                    } else {
                        return target;
                    }
                } else {
                    return val;
                }
            });
        }
    }

    /**
     * 无条件更新偏移量。
     *
     * @param target 目标偏移量
     */
    public void update(long target) {
        update(target, false);
    }

    /**
     * 将偏移量冻结为目标值；冻结后 {@link #update(long, boolean)} 无法再修改。
     * 先将 allowToUpdate 置为 false 再更新偏移量，确保为最终状态。
     *
     * @param target 冻结时的目标偏移量
     */
    public void updateAndFreeze(long target) {
        value.getAndUpdate(val -> {
            allowToUpdate = false;
            return target;
        });
    }

    /** 获取当前偏移量。 */
    public long getOffset() {
        return value.get();
    }
}
