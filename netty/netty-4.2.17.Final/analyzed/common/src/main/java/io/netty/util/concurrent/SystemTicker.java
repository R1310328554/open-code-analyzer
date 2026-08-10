/*
 * Copyright 2025 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.util.concurrent;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/** 基于 {@link System#nanoTime()} 的单例 {@link Ticker}，减去固定起点以统一相对时间。 */
final class SystemTicker implements Ticker {
    /** 全局单例。 */
    static final SystemTicker INSTANCE = new SystemTicker();
    /** JVM 启动时记录的 nanoTime 基准，使 {@link #nanoTime()} 从 0 附近开始。 */
    private static final long START_TIME = System.nanoTime();

    /** 返回初始化时的 nanoTime 基准值。 */
    @Override
    public long initialNanoTime() {
        return START_TIME;
    }

    /** 相对起点的单调纳秒时间（可为负，与 System.nanoTime 语义一致）。 */
    @Override
    public long nanoTime() {
        return System.nanoTime() - START_TIME;
    }

    /** 委托 {@link TimeUnit#sleep(long)} 阻塞指定时长。 */
    @Override
    public void sleep(long delay, TimeUnit unit) throws InterruptedException {
        Objects.requireNonNull(unit, "unit");
        unit.sleep(delay);
    }
}
