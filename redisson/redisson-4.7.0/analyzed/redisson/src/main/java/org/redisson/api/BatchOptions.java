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

import org.redisson.config.*;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * {@link RBatch} 批量操作的配置项。
 * <p>控制执行模式、响应超时、重试策略及主从/AOF 同步等选项。
 *
 * @author Nikita Koksharov
 */
public final class BatchOptions {
    
    public enum ExecutionMode {

        /**
         * 将批量调用存入 Redis 并以单条命令原子执行（读路径）。
         * <p>集群模式下所有键须位于同一 slot。
         * https://github.com/antirez/redis/issues/3682
         */
        REDIS_READ_ATOMIC,

        /**
         * 将批量调用存入 Redis 并以单条命令原子执行（写路径）。
         * <p>集群模式下所有键须位于同一 slot。
         * https://github.com/antirez/redis/issues/3682
         */
        REDIS_WRITE_ATOMIC,

        /**
         * 在 Redisson 客户端内存中缓存批量调用，再逐条发往 Redis。
         * <p>默认模式。
         */
        IN_MEMORY,
        
        /**
         * 在 Redisson 端缓存批量调用，再以单条 Redis 命令原子执行。
         * <p>集群模式下所有键须位于同一 slot。
         * https://github.com/antirez/redis/issues/3682
         */
        IN_MEMORY_ATOMIC,
        
    }
    
    private ExecutionMode executionMode = ExecutionMode.IN_MEMORY;
    
    private long responseTimeout;
    private int retryAttempts = -1;
    private DelayStrategy retryDelay;

    private long syncTimeout;
    private int syncSlaves;
    private int syncLocals;
    private boolean syncAOF;
    private boolean skipResult;

    private BatchOptions() {
    }
    
    public static BatchOptions defaults() {
        return new BatchOptions();
    }
    
    public long getResponseTimeout() {
        return responseTimeout;
    }

    /**
     * 设置 Redis 响应超时。
     * <p>自命令成功发送后开始计时；默认取 {@link BaseConfig#getTimeout()}。
     *
     * @param timeout 超时数值
     * @param unit 时间单位
     * @return 当前实例
     */
    public BatchOptions responseTimeout(long timeout, TimeUnit unit) {
        this.responseTimeout = unit.toMillis(timeout);
        return this;
    }

    public int getRetryAttempts() {
        return retryAttempts;
    }

    /**
     * 设置批量命令尚未成功发送时的重试次数。
     * <p>默认取 {@link BaseConfig#getRetryAttempts()}。
     *
     * @param retryAttempts 重试次数
     * @return 当前实例
     */
    public BatchOptions retryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
        return this;
    }

    /**
     * 请改用 {@link #retryDelay(DelayStrategy)}。
     *
     * @param retryInterval 重试间隔
     * @param retryIntervalUnit 间隔时间单位
     * @return 当前实例
     */
    @Deprecated
    public BatchOptions retryInterval(long retryInterval, TimeUnit retryIntervalUnit) {
        this.retryDelay = new ConstantDelay(Duration.ofMillis(retryIntervalUnit.toMillis(retryInterval)));
        return this;
    }

    
    /**
     * 请改用 {@link #sync(int, Duration)}。
     */
    @Deprecated
    public BatchOptions syncSlaves(int slaves, long timeout, TimeUnit unit) {
        this.syncSlaves = slaves;
        this.syncTimeout = unit.toMillis(timeout);
        return this;
    }

    /**
     * 在指定超时内，将写操作同步到给定数量的 Redis 从节点。
     * <p>需要 Redis 3.0+。
     *
     * @param slaves 参与同步的从节点数量
     * @param timeout 同步超时
     * @return 当前实例
     */
    public BatchOptions sync(int slaves, Duration timeout) {
        this.syncSlaves = slaves;
        this.syncTimeout = timeout.toMillis();
        return this;
    }

    public long getSyncTimeout() {
        return syncTimeout;
    }
    public int getSyncSlaves() {
        return syncSlaves;
    }

    /**
     * 告知 Redis 不返回应答，可节省大批量响应的网络流量。
     * <p>需要 Redis 3.2+。
     *
     * @return 当前实例
     */
    public BatchOptions skipResult() {
        skipResult = true;
        return this;
    }

    /**
     * 在指定超时内，将写操作同步到 AOF 及给定数量的从节点与本地 Redis。
     * <p>需要 Redis 7.2+。
     *
     * @param localNum 参与同步的本地 Redis 数量
     * @param slaves 参与同步的从节点数量
     * @param timeout 同步超时
     * @return 当前实例
     */
    public BatchOptions syncAOF(int localNum, int slaves, Duration timeout) {
        this.syncSlaves = slaves;
        this.syncAOF = true;
        this.syncLocals = localNum;
        this.syncTimeout = timeout.toMillis();
        return this;
    }
    public boolean isSkipResult() {
        return skipResult;
    }

    public int getSyncLocals() {
        return syncLocals;
    }

    public boolean isSyncAOF() {
        return syncAOF;
    }

    /**
     * 设置批量执行模式。
     *
     * @see ExecutionMode
     * @param executionMode 批量执行模式
     * @return 当前实例
     */
    public BatchOptions executionMode(ExecutionMode executionMode) {
        this.executionMode = executionMode;
        return this;
    }
    public ExecutionMode getExecutionMode() {
        return executionMode;
    }

    @Override
    public String toString() {
        return "BatchOptions [queueStore=" + executionMode + "]";
    }

    public DelayStrategy getRetryDelay() {
        return retryDelay;
    }

    /**
     * 设置批量发送失败后的重试延迟策略。
     * <p>默认取 {@link BaseConfig#getRetryDelay()}。
     *
     * @see DecorrelatedJitterDelay
     * @see EqualJitterDelay
     * @see FullJitterDelay
     * @see ConstantDelay
     *
     * @param retryDelay 延迟策略实现
     * @return 当前实例
     */
    public BatchOptions retryDelay(DelayStrategy retryDelay) {
        this.retryDelay = retryDelay;
        return this;
    }
}
