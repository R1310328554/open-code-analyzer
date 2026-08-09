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
package org.redisson.redisnode;

import org.redisson.api.RFuture;
import org.redisson.api.redisnode.RedisSentinel;
import org.redisson.api.redisnode.RedisSentinelAsync;
import org.redisson.client.RedisClient;
import org.redisson.client.RedisConnection;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.RedisCommand;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.client.protocol.RedisStrictCommand;
import org.redisson.client.protocol.Time;
import org.redisson.client.protocol.decoder.RedisURIDecoder;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.misc.CompletableFutureWrapper;
import org.redisson.misc.RedisURI;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

/**
 * 单个 Redis Sentinel 节点的同步/异步运维 API 实现。
 * <p>
 * 实现 {@link RedisSentinel} 与 {@link RedisSentinelAsync}：
 * PING、INFO、CONFIG、持久化，以及 SENTINEL 子命令
 *（masters、slaves、failover 等）。
 * <p>
 * 每次命令通过 {@link #executeAsync} 建立临时连接并在完成后关闭。
 *
 * @author Nikita Koksharov
 *
 */
public class SentinelRedisNode implements RedisSentinel, RedisSentinelAsync {

    /** Sentinel 实例的 Redis 客户端。 */
    private final RedisClient client;
    /** 异步命令服务（阻塞 get 与 eval 用）。 */
    private final CommandAsyncExecutor commandAsyncService;

    /** @param client Sentinel 连接 @param commandAsyncService 命令执行器 */
    public SentinelRedisNode(RedisClient client, CommandAsyncExecutor commandAsyncService) {
        super();
        this.client = client;
        this.commandAsyncService = commandAsyncService;
    }

    /** @return 底层 RedisClient */
    public RedisClient getClient() {
        return client;
    }

    /** @return Sentinel 网络地址 */
    @Override
    public InetSocketAddress getAddr() {
        return client.getAddr();
    }

    /** 同步 MEMORY STATS。 */
    @Override
    public Map<String, String> getMemoryStatistics() {
        return getMemoryStatisticsAsync().toCompletableFuture().join();
    }

    /** 异步 MEMORY STATS。 */
    @Override
    public RFuture<Map<String, String>> getMemoryStatisticsAsync() {
        return executeAsync(null, StringCodec.INSTANCE, -1, RedisCommands.MEMORY_STATS);
    }

    @Override
    public RFuture<Boolean> pingAsync() {
        return pingAsync(1, TimeUnit.SECONDS);
    }

    @Override
    public RFuture<Boolean> pingAsync(long timeout, TimeUnit timeUnit) {
        return executeAsync(false, null, timeUnit.toMillis(timeout), RedisCommands.PING_BOOL);
    }

    @Override
    public boolean ping() {
        return pingAsync().toCompletableFuture().join();
    }

    @Override
    public boolean ping(long timeout, TimeUnit timeUnit) {
        return pingAsync(timeout, timeUnit).toCompletableFuture().join();
    }

    @Override
    @SuppressWarnings("AvoidInlineConditionals")
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((client == null) ? 0 : client.getAddr().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SentinelRedisNode other = (SentinelRedisNode) obj;
        if (client == null) {
            if (other.client != null)
                return false;
        } else if (!client.getAddr().equals(other.client.getAddr()))
            return false;
        return true;
    }

    /** 建立临时连接执行单条命令，{@code defaultValue} 非 null 时异常返回默认值。 */
    private <T> RFuture<T> executeAsync(T defaultValue, Codec codec, long timeout, RedisCommand<T> command, Object... params) {
        CompletableFuture<RedisConnection> connectionFuture = client.connectAsync().toCompletableFuture();
        CompletableFuture<Object> f = connectionFuture.thenCompose(connection -> {
            return connection.async(timeout, codec, command, params);
        }).handle((r, e) -> {
            // 无论成功失败，完成后关闭临时连接
            if (connectionFuture.isDone() && !connectionFuture.isCompletedExceptionally()) {
                connectionFuture.getNow(null).closeAsync();
            }

            if (e != null) {
                if (defaultValue != null) {
                    return defaultValue;
                }
                throw new CompletionException(e);
            }

            return r;
        });
        return new CompletableFutureWrapper<T>((CompletionStage<T>) f);
    }

    @Override
    public RFuture<Time> timeAsync() {
        return executeAsync(null, LongCodec.INSTANCE, -1, RedisCommands.TIME);
    }

    @Override
    public Time time() {
        return timeAsync().toCompletableFuture().join();
    }

    @Override
    public String toString() {
        return this.getClass().toString() + " [client=" + client + "]";
    }

    @Override
    public Map<String, String> info(InfoSection section) {
        return infoAsync(section).toCompletableFuture().join();
    }

    /** 按 {@link InfoSection} 分发到对应 INFO 子命令。 */
    @Override
    public RFuture<Map<String, String>> infoAsync(InfoSection section) {
        // 按 InfoSection 选择 Redis INFO 段落
        if (section == InfoSection.ALL) {
            return executeAsync(null, StringCodec.INSTANCE, -1, RedisCommands.INFO_ALL);
        } else if (section == InfoSection.DEFAULT) {
            return executeAsync(null, StringCodec.INSTANCE, -1, RedisCommands.INFO_DEFAULT);
        } else if (section == InfoSection.SERVER) {
            return executeAsync(null, StringCodec.INSTANCE, -1, RedisCommands.INFO_SERVER);
        } else if (section == InfoSection.CLIENTS) {
            return executeAsync(null, StringCodec.INSTANCE, -1, RedisCommands.INFO_CLIENTS);
        } else if (section == InfoSection.MEMORY) {
            return executeAsync(null, StringCodec.INSTANCE, -1, RedisCommands.INFO_MEMORY);
        } else if (section == InfoSection.PERSISTENCE) {
            return executeAsync(null, StringCodec.INSTANCE, -1, RedisCommands.INFO_PERSISTENCE);
        } else if (section == InfoSection.STATS) {
            return executeAsync(null, StringCodec.INSTANCE, -1, RedisCommands.INFO_STATS);
        } else if (section == InfoSection.REPLICATION) {
            return executeAsync(null, StringCodec.INSTANCE, -1, RedisCommands.INFO_REPLICATION);
        } else if (section == InfoSection.CPU) {
            return executeAsync(null, StringCodec.INSTANCE, -1, RedisCommands.INFO_CPU);
        } else if (section == InfoSection.COMMANDSTATS) {
            return executeAsync(null, StringCodec.INSTANCE, -1, RedisCommands.INFO_COMMANDSTATS);
        } else if (section == InfoSection.CLUSTER) {
            return executeAsync(null, StringCodec.INSTANCE, -1, RedisCommands.INFO_CLUSTER);
        } else if (section == InfoSection.KEYSPACE) {
            return executeAsync(null, StringCodec.INSTANCE, -1, RedisCommands.INFO_KEYSPACE);
        }
        throw new IllegalStateException();
    }

    /** SENTINEL GET-MASTER-ADDR-BY-NAME 同步版。 */
    @Override
    public RedisURI getMasterAddr(String masterName) {
        return commandAsyncService.get(getMasterAddrAsync(masterName));
    }

    @Override
    public List<Map<String, String>> getSentinels(String masterName) {
        return commandAsyncService.get(getSentinelsAsync(masterName));
    }

    @Override
    public List<Map<String, String>> getMasters() {
        return commandAsyncService.get(getMastersAsync());
    }

    @Override
    public List<Map<String, String>> getSlaves(String masterName) {
        return commandAsyncService.get(getSlavesAsync(masterName));
    }

    @Override
    public Map<String, String> getMaster(String masterName) {
        return commandAsyncService.get(getMasterAsync(masterName));
    }

    @Override
    public void failover(String masterName) {
        commandAsyncService.get(failoverAsync(masterName));
    }

    /** 异步查询指定 master 名称对应的主节点地址。 */
    @Override
    public RFuture<RedisURI> getMasterAddrAsync(String masterName) {
        RedisStrictCommand<RedisURI> masterHostCommand = new RedisStrictCommand<>("SENTINEL", "GET-MASTER-ADDR-BY-NAME",
                new RedisURIDecoder(client.getConfig().getAddress().getScheme()));

        return executeAsync(null, StringCodec.INSTANCE, -1, masterHostCommand, masterName);
    }

    /** SENTINEL SENTINELS：监控同一 master 的其他 Sentinel。 */
    @Override
    public RFuture<List<Map<String, String>>> getSentinelsAsync(String masterName) {
        return executeAsync(null, StringCodec.INSTANCE, -1, RedisCommands.SENTINEL_SENTINELS, masterName);
    }

    /** SENTINEL MASTERS：全部被监控的 master 列表。 */
    @Override
    public RFuture<List<Map<String, String>>> getMastersAsync() {
        return executeAsync(null, StringCodec.INSTANCE, -1, RedisCommands.SENTINEL_MASTERS);
    }

    /** SENTINEL SLAVES：指定 master 的从节点列表。 */
    @Override
    public RFuture<List<Map<String, String>>> getSlavesAsync(String masterName) {
        return executeAsync(null, StringCodec.INSTANCE, -1, RedisCommands.SENTINEL_SLAVES, masterName);
    }

    /** SENTINEL MASTER：单个 master 的监控状态。 */
    @Override
    public RFuture<Map<String, String>> getMasterAsync(String masterName) {
        return executeAsync(null, StringCodec.INSTANCE, -1, RedisCommands.SENTINEL_MASTER, masterName);
    }

    /** SENTINEL FAILOVER：触发手动故障转移。 */
    @Override
    public RFuture<Void> failoverAsync(String masterName) {
        return executeAsync(null, StringCodec.INSTANCE, -1, RedisCommands.SENTINEL_FAILOVER, masterName);
    }

    @Override
    public Map<String, String> getConfig(String parameter) {
        return getConfigAsync(parameter).toCompletableFuture().join();
    }

    @Override
    public void setConfig(String parameter, String value) {
        setConfigAsync(parameter, value).toCompletableFuture().join();
    }

    /** CONFIG GET 异步版。 */
    @Override
    public RFuture<Map<String, String>> getConfigAsync(String parameter) {
        return executeAsync(null, StringCodec.INSTANCE, -1, RedisCommands.CONFIG_GET_MAP, parameter);
    }

    /** CONFIG SET 异步版。 */
    @Override
    public RFuture<Void> setConfigAsync(String parameter, String value) {
        return executeAsync(null, StringCodec.INSTANCE, -1, RedisCommands.CONFIG_SET, parameter, value);
    }

    @Override
    public void bgSave() {
        commandAsyncService.get(bgSaveAsync());
    }

    @Override
    public void scheduleBgSave() {
        commandAsyncService.get(scheduleBgSaveAsync());
    }

    @Override
    public void save() {
        commandAsyncService.get(saveAsync());
    }

    @Override
    public Instant getLastSaveTime() {
        return commandAsyncService.get(getLastSaveTimeAsync());
    }

    @Override
    public RFuture<Void> bgSaveAsync() {
        return executeAsync(null, StringCodec.INSTANCE, -1, RedisCommands.BGSAVE);
    }

    @Override
    public RFuture<Void> scheduleBgSaveAsync() {
        return executeAsync(null, StringCodec.INSTANCE, -1, RedisCommands.BGSAVE, "SCHEDULE");
    }

    @Override
    public RFuture<Void> saveAsync() {
        return executeAsync(null, StringCodec.INSTANCE, -1, RedisCommands.SAVE);
    }

    @Override
    public RFuture<Instant> getLastSaveTimeAsync() {
        return executeAsync(null, StringCodec.INSTANCE, -1, RedisCommands.LASTSAVE_INSTANT);
    }

    @Override
    public void bgRewriteAOF() {
        commandAsyncService.get(bgRewriteAOFAsync());
    }

    @Override
    public RFuture<Void> bgRewriteAOFAsync() {
        return executeAsync(null, StringCodec.INSTANCE, -1, RedisCommands.BGREWRITEAOF);
    }

    @Override
    public long size() {
        return commandAsyncService.get(sizeAsync());
    }

    /** DBSIZE 异步版（Sentinel 通常返回 0）。 */
    @Override
    public RFuture<Long> sizeAsync() {
        return executeAsync(null, StringCodec.INSTANCE, -1, RedisCommands.DBSIZE);
    }

}
