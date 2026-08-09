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
package org.redisson.command;

import org.redisson.api.BatchOptions;
import org.redisson.client.codec.Codec;
import org.redisson.client.protocol.BatchCommandData;
import org.redisson.client.protocol.RedisCommand;
import org.redisson.command.CommandBatchService.Entry;
import org.redisson.config.DelayStrategy;
import org.redisson.connection.ConnectionManager;
import org.redisson.connection.NodeSource;
import org.redisson.liveobject.core.RedissonObjectBuilder;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 批量命令执行器基类，将单条命令登记到 {@link CommandBatchService} 的命令表，
 * 而非立即发送到 Redis。
 * <p>子类 {@link RedisBatchExecutor}（内存聚合）与 {@link RedisQueuedBatchExecutor}
 * （Redis MULTI/EXEC 队列）决定具体入队与发送策略。
 *
 * @author Nikita Koksharov
 *
 * @param <V> Redis 回复值类型
 * @param <R> 业务层返回类型
 */
public class BaseRedisBatchExecutor<V, R> extends RedisExecutor<V, R> {

    /** 按节点源分组的待执行批量命令表。 */
    final ConcurrentMap<NodeSource, Entry> commands;
    /** 批量执行选项（模式、超时、同步从库等）。 */
    final BatchOptions options;
    /** 全局命令序号，保证批量结果按添加顺序排列。 */
    final AtomicInteger index;
    
    /** 批量是否已 execute/discard，防止重复提交。 */
    final AtomicBoolean executed;
    
    @SuppressWarnings("ParameterNumber")
    public BaseRedisBatchExecutor(boolean readOnlyMode, NodeSource source, Codec codec, RedisCommand<V> command,
                                  Object[] params, CompletableFuture<R> mainPromise, boolean ignoreRedirect,
                                  ConnectionManager connectionManager, RedissonObjectBuilder objectBuilder,
                                  ConcurrentMap<NodeSource, Entry> commands,
                                  BatchOptions options, AtomicInteger index, AtomicBoolean executed, RedissonObjectBuilder.ReferenceType referenceType,
                                  boolean noRetry) {
        
        super(readOnlyMode, source, codec, command, params, mainPromise, ignoreRedirect, connectionManager,
                objectBuilder, referenceType, noRetry,
                retryAttempts(connectionManager, options),
                retryInterval(connectionManager, options),
                timeout(connectionManager, options),
                false, null);
        this.commands = commands;
        this.options = options;
        this.index = index;
        this.executed = executed;
    }

    /** 计算批量响应超时：基础 timeout + 可选 sync 等待时间。 */
    private static int timeout(ConnectionManager connectionManager, BatchOptions options) {
        int result = connectionManager.getServiceManager().getConfig().getTimeout();
        if (options.getResponseTimeout() > 0) {
            result = (int) options.getResponseTimeout();
        }
        if (options.getSyncSlaves() > 0) {
            result += (int) options.getSyncTimeout();
        }
        return result;
    }

    /** 解析批量重试间隔策略。 */
    private static DelayStrategy retryInterval(ConnectionManager connectionManager, BatchOptions options) {
        if (options.getRetryDelay() != null) {
            return options.getRetryDelay();
        }
        return connectionManager.getServiceManager().getConfig().getRetryDelay();
    }

    /** 解析批量最大重试次数。 */
    private static int retryAttempts(ConnectionManager connectionManager, BatchOptions options) {
        if (options.getRetryAttempts() >= 0) {
            return options.getRetryAttempts();
        }
        return connectionManager.getServiceManager().getConfig().getRetryAttempts();
    }

    /** 将当前命令封装为 {@link BatchCommandData} 并加入对应节点 Entry。 */
    protected final void addBatchCommandData(Object[] batchParams) {
        Entry entry = commands.computeIfAbsent(source, k -> new Entry());

        if (!readOnlyMode) {
            entry.setReadOnlyMode(false);
        }

        Codec codecToUse = getCodec(codec);
        BatchCommandData<V, R> commandData = new BatchCommandData<>(mainPromise, codecToUse, command, batchParams, index.incrementAndGet());
        entry.addCommand(commandData);
    }
        
}
