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
package org.redisson.executor;

import org.redisson.api.RScheduledFuture;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * 调度任务的 Future，实现 {@link RScheduledFuture} 与 {@link Delayed}。
 * <p>
 * 按计划执行时间与当前时间的差值计算 {@link #getDelay}，
 * 用于延迟队列排序。
 *
 * @author Nikita Koksharov
 *
 * @param <V> value type
 */
public class RedissonScheduledFuture<V> extends RedissonExecutorFuture<V> implements RScheduledFuture<V> {

    /** 计划执行时间戳（毫秒）。 */
    private final long scheduledExecutionTime;
    /** 内部 RemotePromise，供调度器访问。 */
    private final RemotePromise<V> promise;

    /** 以 promise 与计划执行时间构造。 */
    public RedissonScheduledFuture(RemotePromise<V> promise, long scheduledExecutionTime) {
        super(promise);
        this.scheduledExecutionTime = scheduledExecutionTime;
        this.promise = promise;
    }

    /** 返回内部 RemotePromise。 */
    public RemotePromise<V> getInnerPromise() {
        return promise;
    }
    
    /** 按剩余延迟比较，用于 DelayQueue 排序。 */
    @Override
    public int compareTo(Delayed other) {
        if (this == other) {
            return 0;
        }
        
        long diff = getDelay(TimeUnit.MILLISECONDS) - other.getDelay(TimeUnit.MILLISECONDS);

        if (diff == 0) {
            return 0;
        }
        if (diff < 0) {
            return -1;
        }
        return 1;
    }
    
    /** 返回距离计划执行时间的剩余延迟。 */
    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(scheduledExecutionTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }
    
}
