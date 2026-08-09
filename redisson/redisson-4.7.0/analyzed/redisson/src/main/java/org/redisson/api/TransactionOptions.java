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

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 事务配置。
 * 
 * @author Nikita Koksharov
 *
 */
public final class TransactionOptions {
    
    private long responseTimeout = 3000;
    private int retryAttempts = 3;
    private long retryInterval = 1500;

    private int syncSlaves = 0;
    private long syncTimeout = 5000;
    
    private long timeout = 5000;

    private TransactionOptions() {
    }
    
    public static TransactionOptions defaults() {
        return new TransactionOptions();
    }
    
    public long getResponseTimeout() {
        return responseTimeout;
    }

    /**
     * 定义 Redis 响应超时；
     * 从事务成功发送后开始计时。
     * <p>
     * Default is <code>3000 milliseconds</code>
     * 
     * @param timeout 超时值
     * @param unit 时间单位
     * @return 当前实例
     */
    public TransactionOptions responseTimeout(long timeout, TimeUnit unit) {
        this.responseTimeout = unit.toMillis(timeout);
        return this;
    }

    public int getRetryAttempts() {
        return retryAttempts;
    }

    /**
     * 定义事务尚未成功发送时的重试次数。
     * <p>
     * Default is <code>3 attempts</code>
     * 
     * @param retryAttempts 重试次数
     * @return 当前实例
     */
    public TransactionOptions retryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
        return this;
    }

    public long getRetryInterval() {
        return retryInterval;
    }
    
    /**
     * 定义事务尚未成功发送时每次重试的间隔时间。
     * <p>
     * Default is <code>1500 milliseconds</code>
     * 
     * @param retryInterval 重试间隔
     * @param retryIntervalUnit 重试间隔单位
     * @return 当前实例
     */
    public TransactionOptions retryInterval(long retryInterval, TimeUnit retryIntervalUnit) {
        this.retryInterval = retryIntervalUnit.toMillis(retryInterval);
        return this;
    }

    /**
     * 请改用 {@link #syncSlaves} 方法。
     *
     * @param syncTimeout 同步超时
     * @param syncUnit 同步超时时间单位
     * @return 当前实例
     */
    @Deprecated
    public TransactionOptions syncSlavesTimeout(long syncTimeout, TimeUnit syncUnit) {
        this.syncTimeout = syncUnit.toMillis(syncTimeout);
        return this;
    }
    public long getSyncTimeout() {
        return syncTimeout;
    }

    /**
     * 在指定超时内，将写操作同步到指定数量的 Redis 从节点。
     * <p>
     * Default slaves value is <code>0</code> which means available slaves
     * at the moment of execution and <code>-1</code> means no sync at all.
     * <p>
     * Default timeout value is <code>5000 milliseconds</code>
     * NOTE: Redis 3.0+ required
     *
     * @param slaves 参与同步的从节点数量
     *                 Default value is <code>0</code> which means available slaves
     *                 at the moment of execution and <code>-1</code> means no sync at all.
     * @param timeout 同步超时
     * @param unit 同步超时时间单位
     * @return 当前实例
     */
    public TransactionOptions syncSlaves(int slaves, long timeout, TimeUnit unit) {
        this.syncSlaves = slaves;
        this.syncTimeout = unit.toMillis(timeout);
        return this;
    }

    public int getSyncSlaves() {
        return syncSlaves;
    }

    public long getTimeout() {
        return timeout;
    }
    /**
     * 若事务在 <code>timeout</code> 内未提交则自动回滚；
     * 设为 <code>-1</code> 可禁用。
     * <p>
     * Default is <code>5000 milliseconds</code>
     * 
     * @param timeout 超时时间
     * @param timeoutUnit 超时时间单位
     * @return 当前实例
     */
    public TransactionOptions timeout(long timeout, TimeUnit timeoutUnit) {
        if (timeout == -1) {
            this.timeout = timeout;
            return this;
        }
        this.timeout = timeoutUnit.toMillis(timeout);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TransactionOptions that = (TransactionOptions) o;
        return responseTimeout == that.responseTimeout
                    && retryAttempts == that.retryAttempts
                        && retryInterval == that.retryInterval
                            && syncSlaves == that.syncSlaves
                                && syncTimeout == that.syncTimeout
                                    && timeout == that.timeout;
    }

    @Override
    public int hashCode() {
        return Objects.hash(responseTimeout, retryAttempts, retryInterval, syncSlaves, syncTimeout, timeout);
    }
}
