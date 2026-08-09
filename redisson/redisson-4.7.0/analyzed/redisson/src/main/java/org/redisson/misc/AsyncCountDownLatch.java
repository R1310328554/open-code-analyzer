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
package org.redisson.misc;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 异步命令计数闩锁：跟踪已发送与待完成的异步 Redis 命令数量。
 * <p>
 * {@link #latch} 注册回调与初始计数；每完成一条命令调用 {@link #countDown}，
 * 计数归零时触发回调。用于批量异步操作全部完成后再执行后续逻辑。
 *
 * @author Nikita Koksharov
 *
 */
public final class AsyncCountDownLatch {

    /** 命令计数器：latch 时减去初始 count，countDown 时递增，归零触发回调。 */
    private final AtomicInteger commandsSent = new AtomicInteger();
    /** 全部命令完成时执行的一次性回调。 */
    private volatile Runnable callback;

    /** 标记一条异步命令已完成；计数归零时运行 callback。 */
    public void countDown() {
        if (commandsSent.incrementAndGet() == 0) {
            callback.run();
        }
    }

    /**
     * 初始化闩锁：设置回调并将计数减去 count（待完成的命令数）。
     * 若初始即为 0 则立即执行 callback；不可重复调用。
     */
    public void latch(Runnable callback, int count) {
        if (this.callback != null) {
            throw new IllegalStateException("Latch can't be called twice");
        }
        this.callback = callback;
        if (commandsSent.addAndGet(-count) == 0) {
            callback.run();
        }
    }
}
