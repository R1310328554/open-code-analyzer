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

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelPromise;
import io.netty.util.Timeout;
import org.redisson.api.BatchOptions;
import org.redisson.api.BatchOptions.ExecutionMode;
import org.redisson.client.RedisConnection;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.CommandData;
import org.redisson.client.protocol.CommandsData;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.command.CommandBatchService.Entry;
import org.redisson.config.DelayStrategy;
import org.redisson.connection.ConnectionManager;
import org.redisson.connection.NodeSource;
import org.redisson.connection.NodeSource.Redirect;
import org.redisson.liveobject.core.RedissonObjectBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 批量命令的实际发送执行器：从 {@link Entry} 取出已聚合的命令列表，
 * 经单连接一次性写入 Redis（Pipeline 或 MULTI/EXEC）。
 * <p>所有分片槽位各有一个实例并行执行，{@link #slots} 计数归零后完成整批。
 *
 * @author Nikita Koksharov
 *
 */
public class RedisCommonBatchExecutor extends RedisExecutor<Object, Void> {

    /** 日志记录器。 */
    static final Logger log = LoggerFactory.getLogger(RedisCommonBatchExecutor.class);

    /** 本节点待发送的命令集合。 */
    private final Entry entry;
    /** 剩余待完成的分片/节点数，归零时 complete 主 Promise。 */
    private final AtomicInteger slots;
    /** 批量选项，决定原子性、跳过回复、WAIT 等行为。 */
    private final BatchOptions options;
    
    public RedisCommonBatchExecutor(NodeSource source, CompletableFuture<Void> mainPromise,
                                    ConnectionManager connectionManager, BatchOptions options, Entry entry,
                                    AtomicInteger slots, RedissonObjectBuilder.ReferenceType referenceType, boolean noRetry) {
        super(entry.isReadOnlyMode(), source, null, null, null,
                mainPromise, false, connectionManager, null, referenceType, noRetry,
                retryAttempts(connectionManager, options),
                retryInterval(connectionManager, options),
                timeout(connectionManager, options),
                false, null);
        this.options = options;
        this.entry = entry;
        this.slots = slots;
    }

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

    private static DelayStrategy retryInterval(ConnectionManager connectionManager, BatchOptions options) {
        if (options.getRetryDelay() != null) {
            return options.getRetryDelay();
        }
        return connectionManager.getServiceManager().getConfig().getRetryDelay();
    }

    private static int retryAttempts(ConnectionManager connectionManager, BatchOptions options) {
        if (options.getRetryAttempts() >= 0) {
            return options.getRetryAttempts();
        }
        return connectionManager.getServiceManager().getConfig().getRetryAttempts();
    }

    /** 异常时清除 Entry 中各命令的错误状态以便重试。 */
    @Override
    protected void onException() {
        entry.clearErrors();
    }
    
    /** 释放 Entry 内所有命令参数的引用计数。 */
    @Override
    protected void free() {
        free(entry);
    }
    
    private void free(Entry entry) {
        for (CommandData<?, ?> command : entry.getCommands()) {
            free(command.getParams());
        }
    }

    @Override
    protected CompletableFuture<RedisConnection> getConnection(CompletableFuture<Void> attemptPromise) {
        CompletableFuture<RedisConnection> f = super.getConnection(attemptPromise);
        f.whenComplete((r, e) -> {
            if (e != null) {
                if (source.getEntry().getReplacedBy() != null) {
                    source = new NodeSource(source.getEntry().getReplacedBy());
                }
            }
        });
        return f;
    }

    /** 组装 ASKING、命令列表并委托 {@link CommandsData} 批量发送。 */
    @Override
    protected void sendCommand(CompletableFuture<Void> attemptPromise, RedisConnection connection) {
        boolean isAtomic = options.getExecutionMode() != ExecutionMode.IN_MEMORY;
        boolean isQueued = options.getExecutionMode() == ExecutionMode.REDIS_READ_ATOMIC 
                                || options.getExecutionMode() == ExecutionMode.REDIS_WRITE_ATOMIC;

        List<CommandData<?, ?>> list = new ArrayList<>(entry.getCommands().size());
        if (source.getRedirect() == Redirect.ASK) {
            CompletableFuture<Void> promise = new CompletableFuture<Void>();
            list.add(new CommandData<>(promise, StringCodec.INSTANCE, RedisCommands.ASKING, new Object[] {}));
        } 
        for (CommandData<?, ?> c : entry.getCommands()) {
            if ((c.getPromise().isCancelled() || (c.getPromise().isDone() && !c.getPromise().isCompletedExceptionally()))
                    && !isWaitCommand(c) 
                        && !isAtomic) {
                // 已成功或已取消的非 WAIT 命令在内存模式下可跳过
                continue;
            }
            list.add(c);
        }
        
        if (list.isEmpty()) {
            writeFuture = connection.getChannel().newPromise();
            attemptPromise.complete(null);
            timeout.ifPresent(Timeout::cancel);
            return;
        }

        sendCommand(connection, attemptPromise, list);
    }

    private void sendCommand(RedisConnection connection, CompletableFuture<Void> attemptPromise, List<CommandData<?, ?>> list) {
        boolean isAtomic = options.getExecutionMode() != ExecutionMode.IN_MEMORY;
        boolean isQueued = options.getExecutionMode() == ExecutionMode.REDIS_READ_ATOMIC
                || options.getExecutionMode() == ExecutionMode.REDIS_WRITE_ATOMIC;

        CommandData<?, ?> lastCommand = connection.getLastCommand();
        if (lastCommand != null && options.isSkipResult()) {
            writeFuture = connection.getChannel().newPromise();
            lastCommand.getPromise().whenComplete((r, e) -> {
                CommandData<?, ?> currentLastCommand = connection.getLastCommand();
                if (lastCommand != currentLastCommand && currentLastCommand != null) {
                    sendCommand(connection, attemptPromise, list);
                    return;
                }

                ChannelFuture wf = connection.send(new CommandsData(attemptPromise, list, options.isSkipResult(), isAtomic, isQueued, options.getSyncSlaves() > 0));
                wf.addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        ((ChannelPromise) writeFuture).trySuccess(future.getNow());
                    } else {
                        ((ChannelPromise) writeFuture).tryFailure(future.cause());
                    }
                });
            });
            return;
        }

        writeFuture = connection.send(new CommandsData(attemptPromise, list, options.isSkipResult(), isAtomic, isQueued, options.getSyncSlaves() > 0));
    }

    /** 判断是否为 WAIT / WAITAOF 同步从库命令。 */
    protected boolean isWaitCommand(CommandData<?, ?> c) {
        return c.getCommand().getName().equals(RedisCommands.WAIT.getName())
                || c.getCommand().getName().equals(RedisCommands.WAITAOF.getName());
    }

    /** 本分片发送成功后递减 slots，全部完成时通知主 Promise。 */
    @Override
    protected void handleResult(CompletableFuture<Void> attemptPromise, CompletableFuture<RedisConnection> connectionFuture) throws ReflectiveOperationException {
        if (attemptPromise.isDone() && !attemptPromise.isCompletedExceptionally()) {
            if (slots.decrementAndGet() == 0) {
                handleSuccess(mainPromise, connectionFuture, null);
            }
        } else {
            handleError(connectionFuture, cause(attemptPromise));
        }
    }
    
}
