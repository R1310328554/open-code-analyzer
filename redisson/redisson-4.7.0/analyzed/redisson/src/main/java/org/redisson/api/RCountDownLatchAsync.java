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
 * 基于 Redis 的 {@link RCountDownLatch} 异步 API。
 * <p>各方法返回 {@link RFuture}；可通过 {@link #trySetCountAsync} 设置计数。
 *
 * @author Nikita Koksharov
 */
public interface RCountDownLatchAsync extends RObjectAsync {

    /**
     * 阻塞等待计数归零。
     *
     * @return void
     *
     */
    RFuture<Void> awaitAsync();

    /**
     * 阻塞等待计数归零，或在指定超时内返回。
     *
     * @param waitTime 最长等待时间
     * @param unit 时间单位
     * @return 见方法说明
     *         if timeout reached before the count reached zero
     */
    RFuture<Boolean> awaitAsync(long waitTime, TimeUnit unit);

    /**
     * 递减闭锁计数；归零时唤醒等待者。
     * 计数归零时唤醒所有等待线程。
     * 
     * @return void
     */
    RFuture<Void> countDownAsync();

    /**
     * 返回当前计数值。
     *
     * @return 当前计数
     */
    RFuture<Long> getCountAsync();

    /**
     * 仅当前计数已归零或未初始化时设置新计数。
     * 或尚未初始化计数。
     *
     * @param count 计数初始值
     *        before threads can pass through <code>await</code>
     * @return 见方法说明
     *         <code>false</code> if previous count has not reached zero
     */
    RFuture<Boolean> trySetCountAsync(long count);

}
