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

import io.netty.buffer.ByteBuf;
import org.redisson.SlotCallback;
import org.redisson.api.BatchOptions;
import org.redisson.api.RFuture;
import org.redisson.api.options.ObjectParams;
import org.redisson.client.RedisClient;
import org.redisson.client.RedisException;
import org.redisson.client.codec.Codec;
import org.redisson.client.protocol.RedisCommand;
import org.redisson.config.ReadMode;
import org.redisson.connection.ConnectionManager;
import org.redisson.connection.MasterSlaveEntry;
import org.redisson.connection.ServiceManager;
import org.redisson.liveobject.core.RedissonObjectBuilder;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

/**
 * 异步 Redis 命令执行器接口，Redisson 所有读写/eval/批量操作的底层入口。
 * <p>实现类 {@link CommandAsyncService} 负责路由到正确节点、创建 {@link RedisExecutor}
 * 并封装 {@link RFuture}。同步 API 通过 {@link #get} 阻塞等待 Future 完成。
 *
 * @author Nikita Koksharov
 *
 */
public interface CommandAsyncExecutor {

    /** Lua 脚本执行后的副本同步模式。 */
    enum SyncMode {AUTO, WAIT, WAIT_AOF}

    /** 按对象级参数（超时、重试、读模式）复制执行器。 */
    CommandAsyncExecutor copy(ObjectParams objectParams);

    /** 复制执行器并设置是否跟踪 Live Object 变更。 */
    CommandAsyncExecutor copy(boolean trackChanges);

    /** 当前读模式（MASTER/SLAVE 等）。 */
    ReadMode getReadMode();

    RedissonObjectBuilder getObjectBuilder();

    ConnectionManager getConnectionManager();

    ServiceManager getServiceManager();

    RedisException convertException(ExecutionException e);

    <V> void transfer(CompletionStage<V> future1, CompletableFuture<V> future2);

    <V> V getNow(CompletableFuture<V> future);

    /** 阻塞等待 {@link RFuture}，Netty 线程禁止调用。 */
    <V> V get(RFuture<V> future);

    /** 阻塞等待 {@link CompletableFuture}。 */
    <V> V get(CompletableFuture<V> future);

    <V> V getInterrupted(RFuture<V> future) throws InterruptedException;

    <V> V getInterrupted(CompletableFuture<V> future) throws InterruptedException;

    <T, R> RFuture<R> writeAsync(RedisClient client, Codec codec, RedisCommand<T> command, Object... params);

    <T, R> RFuture<R> writeAsync(MasterSlaveEntry entry, Codec codec, RedisCommand<T> command, Object... params);

    <T, R> RFuture<R> writeAsync(byte[] key, Codec codec, RedisCommand<T> command, Object... params);

    <T, R> RFuture<R> writeAsync(ByteBuf key, Codec codec, RedisCommand<T> command, Object... params);

    <T, R> RFuture<R> readAsync(RedisClient client, MasterSlaveEntry entry, Codec codec, RedisCommand<T> command, Object... params);

    <T, R> RFuture<R> readAsync(RedisClient client, String name, Codec codec, RedisCommand<T> command, Object... params);

    <T, R> RFuture<R> readAsync(RedisClient client, byte[] key, Codec codec, RedisCommand<T> command, Object... params);

    <T, R> RFuture<R> readAsync(RedisClient client, Codec codec, RedisCommand<T> command, Object... params);

    <R> List<CompletableFuture<R>> executeAllAsync(MasterSlaveEntry entry, RedisCommand<?> command, Object... params);

    <R> List<CompletableFuture<R>> executeAllAsync(RedisCommand<?> command, Object... params);

    <R> List<CompletableFuture<R>> writeAllAsync(RedisCommand<?> command, Object... params);

    <R> List<CompletableFuture<R>> writeAllAsync(Codec codec, RedisCommand<?> command, Object... params);

    <R> List<CompletableFuture<R>> readAllAsync(Codec codec, RedisCommand<?> command, Object... params);

    <R> List<CompletableFuture<R>> readAllAsync(RedisCommand<?> command, Object... params);

    <T, R> RFuture<R> evalReadAsync(RedisClient client, String name, Codec codec, RedisCommand<T> evalCommandType, String script, List<Object> keys, Object... params);

    /** 在从节点（或只读副本）执行 Lua 读脚本。 */
    <T, R> RFuture<R> evalReadAsync(String key, Codec codec, RedisCommand<T> evalCommandType, String script, List<Object> keys, Object... params);

    <T, R> RFuture<R> evalReadAsync(MasterSlaveEntry entry, Codec codec, RedisCommand<T> evalCommandType, String script, List<Object> keys, Object... params);

    <T, R> RFuture<R> evalReadAsync(RedisClient client, MasterSlaveEntry entry, Codec codec, RedisCommand<T> evalCommandType, String script, List<Object> keys, Object... params);

    <T, R> RFuture<R> evalReadAsync(ByteBuf key, Codec codec, RedisCommand<T> evalCommandType, String script, List<Object> keys, Object... params);

    /** 在主节点执行 Lua 写脚本（EVAL/EVALSHA）。 */
    <T, R> RFuture<R> evalWriteAsync(String key, Codec codec, RedisCommand<T> evalCommandType, String script, List<Object> keys, Object... params);

    <T, R> RFuture<R> evalWriteAsync(ByteBuf key, Codec codec, RedisCommand<T> evalCommandType, String script, List<Object> keys, Object... params);

    <T, R> RFuture<R> evalWriteNoRetryAsync(String key, Codec codec, RedisCommand<T> evalCommandType, String script, List<Object> keys, Object... params);

    <T, R> RFuture<R> evalWriteAsync(MasterSlaveEntry entry, Codec codec, RedisCommand<T> evalCommandType, String script, List<Object> keys, Object... params);

    <T, R> RFuture<R> readAsync(byte[] key, Codec codec, RedisCommand<T> command, Object... params);

    <T, R> RFuture<R> readAsync(ByteBuf key, Codec codec, RedisCommand<T> command, Object... params);

    /** 从 key 对应 slot 按读模式异步读取。 */
    <T, R> RFuture<R> readAsync(String key, Codec codec, RedisCommand<T> command, Object... params);

    /** 向 key 所在 slot 的主节点异步写入命令。 */
    <T, R> RFuture<R> writeAsync(String key, Codec codec, RedisCommand<T> command, Object... params);

    <T> RFuture<Void> writeAllVoidAsync(RedisCommand<T> command, Object... params);

    <T, R> RFuture<R> writeAsync(String key, RedisCommand<T> command, Object... params);

    <T, R> RFuture<R> readAsync(String key, RedisCommand<T> command, Object... params);

    <T, R> RFuture<R> readAsync(MasterSlaveEntry entry, Codec codec, RedisCommand<T> command, Object... params);

    <T, R> RFuture<R> readRandomAsync(Codec codec, RedisCommand<T> command, Object... params);

    <T, R> RFuture<R> readRandomAsync(RedisClient client, Codec codec, RedisCommand<T> command, Object... params);

    <T, R> RFuture<R> readRoundRobinAsync(Codec codec, RedisCommand<T> command, Object... params);

    <T, R> RFuture<R> writeRoundRobinAsync(Codec codec, RedisCommand<T> command, Object... params);

    <V> RFuture<V> pollFromAnyAsync(String name, Codec codec, RedisCommand<?> command, long secondsTimeout, String... queueNames);

    ByteBuf encode(Codec codec, Object value);

    ByteBuf encodeMapKey(Codec codec, Object value);

    ByteBuf encodeMapValue(Codec codec, Object value);

    /** 按 slot 分片批量读，避免 CROSSSLOT 错误。 */
    <T, R> RFuture<R> readBatchedAsync(Codec codec, RedisCommand<T> command, SlotCallback<T, R> callback, Object... keys);

    /** 按 slot 分片批量写。 */
    <T, R> RFuture<R> writeBatchedAsync(Codec codec, RedisCommand<T> command, SlotCallback<T, R> callback, Object... keys);

    <T, R> RFuture<R> evalWriteBatchedAsync(Codec codec, RedisCommand<T> command, String script, List<Object> keys, SlotCallback<T, R> callback);

    <T, R> RFuture<R> evalReadBatchedAsync(Codec codec, RedisCommand<T> command, String script, List<Object> keys, SlotCallback<T, R> callback);

    boolean isEvalShaROSupported();

    void setEvalShaROSupported(boolean value);

    <T> RFuture<T> syncedEvalWithRetry(String key, Codec codec, RedisCommand<T> evalCommandType, String script, List<Object> keys, Object... params);

    <T> RFuture<T> syncedEvalNoRetry(String key, Codec codec, RedisCommand<T> evalCommandType, String script, List<Object> keys, Object... params);

    <T> RFuture<T> syncedEvalNoRetry(long timeout, SyncMode syncMode, String key, Codec codec, RedisCommand<T> evalCommandType, String script, List<Object> keys, Object... params);

    <T> RFuture<T> syncedEvalWithRetry(long timeout, SyncMode syncMode, String key, Codec codec, RedisCommand<T> evalCommandType, String script, List<Object> keys, Object... params);

    <T> RFuture<T> syncedEval(String key, Codec codec, RedisCommand<T> evalCommandType, String script, List<Object> keys, Object... params);

    <T> CompletionStage<T> handleNoSync(CompletionStage<T> stage, Function<Throwable, CompletionStage<?>> supplier);

    boolean isTrackChanges();

    /** 创建批量命令服务，聚合多条命令后一次性提交。 */
    CommandBatchService createCommandBatchService(BatchOptions options);

    /** 工厂方法：构造默认 {@link CommandAsyncService}。 */
    static CommandAsyncExecutor create(ConnectionManager connectionManager, RedissonObjectBuilder objectBuilder,
                                       RedissonObjectBuilder.ReferenceType referenceType) {
        return new CommandAsyncService(connectionManager, objectBuilder, referenceType);
    }

}