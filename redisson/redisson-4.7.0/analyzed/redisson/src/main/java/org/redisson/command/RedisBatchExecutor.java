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
import org.redisson.client.protocol.RedisCommand;
import org.redisson.command.CommandBatchService.Entry;
import org.redisson.connection.ConnectionManager;
import org.redisson.connection.NodeSource;
import org.redisson.liveobject.core.RedissonObjectBuilder;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 内存模式批量执行器：仅将命令登记到 {@link CommandBatchService}，
 * 在 {@code executeAsync} 时统一打包发送（可配合 MULTI/EXEC 或 Pipeline）。
 *
 * @author Nikita Koksharov
 *
 * @param <V> Redis 回复值类型
 * @param <R> 业务层返回类型
 */
public class RedisBatchExecutor<V, R> extends BaseRedisBatchExecutor<V, R> {

    @SuppressWarnings("ParameterNumber")
    public RedisBatchExecutor(boolean readOnlyMode, NodeSource source, Codec codec, RedisCommand<V> command,
                              Object[] params, CompletableFuture<R> mainPromise, boolean ignoreRedirect, ConnectionManager connectionManager,
                              RedissonObjectBuilder objectBuilder, ConcurrentMap<NodeSource, Entry> commands,
                              BatchOptions options, AtomicInteger index,
                              AtomicBoolean executed, RedissonObjectBuilder.ReferenceType referenceType, boolean noRetry) {
        super(readOnlyMode, source, codec, command, params, mainPromise, ignoreRedirect, connectionManager, objectBuilder,
                commands, options, index, executed, referenceType, noRetry);
    }
    
    /** 将命令参数登记到批量队列，不立即建立连接。 */
    @Override
    public void execute() {
        try {
            addBatchCommandData(params);
        } catch (Exception e) {
            free();
            handleError(connectionFuture, e);
            throw e;
        }
    }
    
}
