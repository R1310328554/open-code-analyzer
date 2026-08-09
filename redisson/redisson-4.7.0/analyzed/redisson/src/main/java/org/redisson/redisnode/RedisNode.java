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

import org.redisson.api.NodeType;
import org.redisson.api.RFuture;
import org.redisson.api.redisnode.*;
import org.redisson.client.RedisClient;
import org.redisson.client.RedisTimeoutException;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.client.protocol.Time;
import org.redisson.cluster.ClusterSlotRange;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.misc.CompletableFutureWrapper;
import org.redisson.misc.RedisURI;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * 单个 Redis/Valkey 节点的管理 API 实现：
 * 同时实现 Master/Slave 与 Cluster 角色的同步与异步接口。
 * <p>
 * 封装 PING、INFO、CONFIG、持久化、集群槽位等运维命令，
 * 直接对 {@link RedisClient} 读写，不经过键路由层。
 *
 * @author Nikita Koksharov
 *
 */
public class RedisNode implements RedisClusterMaster, RedisClusterSlave, RedisMaster, RedisSlave,
                                        RedisClusterMasterAsync, RedisClusterSlaveAsync,
                                        RedisMasterAsync, RedisSlaveAsync {

    /** 目标节点的 Redis 连接客户端。 */
    final RedisClient client;
    /** 异步命令执行器。 */
    final CommandAsyncExecutor commandExecutor;
    /** 节点角色类型（Master/Slave 等）。 */
    private final NodeType type;

    /** @param client 节点连接 @param commandExecutor 命令执行器 @param type 节点类型 */
    public RedisNode(RedisClient client, CommandAsyncExecutor commandExecutor, NodeType type) {
        super();
        this.client = client;
        this.commandExecutor = commandExecutor;
        this.type = type;
    }

    /** @return 底层 RedisClient */
    public RedisClient getClient() {
        return client;
    }

    /** @return 节点网络地址 */
    @Override
    public InetSocketAddress getAddr() {
        return client.getAddr();
    }

    /** 默认 1 秒超时的异步 PING。 */
    @Override
    public RFuture<Boolean> pingAsync() {
        return pingAsync(1, TimeUnit.SECONDS);
    }
    
    /** 带自定义超时的 PING；超时或异常均视为失败。 */
    @Override
    public RFuture<Boolean> pingAsync(long timeout, TimeUnit timeUnit) {
        // 读 PING；任意异常映射为 false
        RFuture<Boolean> f = commandExecutor.readAsync(client, null, RedisCommands.PING_BOOL);
        CompletionStage<Boolean> s = f.exceptionally(e -> false);
        // 超时定时器：未完成则以 RedisTimeoutException 结束
        commandExecutor.getServiceManager().newTimeout(t -> {
            RedisTimeoutException ex = new RedisTimeoutException("Command execution timeout (" + timeUnit.toMillis(timeout) + "ms) for command: PING, Redis client: " + client);
            s.toCompletableFuture().completeExceptionally(ex);
        }, timeout, timeUnit);
        return new CompletableFutureWrapper<>(s);
    }
    
    /** 同步 PING，阻塞至默认超时。 */
    @Override
    public boolean ping() {
        return commandExecutor.get(pingAsync());
    }
    
    /** 同步 PING，指定超时。 */
    @Override
    public boolean ping(long timeout, TimeUnit timeUnit) {
        return commandExecutor.get(pingAsync(timeout, timeUnit));
    }

    /** 按客户端地址哈希。 */
    @Override
    @SuppressWarnings("AvoidInlineConditionals")
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((client == null) ? 0 : client.getAddr().hashCode());
        return result;
    }

    /** 同类型且 client 地址相等则视为同一节点。 */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RedisNode other = (RedisNode) obj;
        if (client == null) {
            if (other.client != null)
                return false;
        } else if (!client.getAddr().equals(other.client.getAddr()))
            return false;
        return true;
    }

    /** 异步获取 Redis 服务器时间（TIME 命令）。 */
    @Override
    public RFuture<Time> timeAsync() {
        return commandExecutor.readAsync(client, LongCodec.INSTANCE, RedisCommands.TIME);
    }
    
    /** 同步获取服务器时间。 */
    @Override
    public Time time() {
        return commandExecutor.get(timeAsync());
    }

    /** 调试字符串：客户端与节点类型。 */
    @Override
    public String toString() {
        return "RedisClientEntry [client=" + client + ", type=" + type + "]";
    }

    /** 异步 CLUSTER INFO。 */
    @Override
    public RFuture<Map<String, String>> clusterInfoAsync() {
        return commandExecutor.readAsync(client, StringCodec.INSTANCE, RedisCommands.CLUSTER_INFO);
    }

    /** 同步 CLUSTER INFO。 */
    @Override
    public Map<String, String> clusterInfo() {
        return commandExecutor.get(clusterInfoAsync());
    }

    /** 同步获取本节点 cluster ID（CLUSTER MYID）。 */
    @Override
    public String clusterId() {
        return commandExecutor.get(clusterIdAsync());
    }

    /** 同步 CLUSTER ADDSLOTS。 */
    @Override
    public void clusterAddSlots(int... slots) {
        commandExecutor.get(clusterAddSlotsAsync(slots));
    }

    /** 同步 CLUSTER REPLICATE：配置本节点复制指定 master。 */
    @Override
    public void clusterReplicate(String nodeId) {
        commandExecutor.get(clusterReplicateAsync(nodeId));
    }

    /** 同步 CLUSTER FORGET：从集群视图移除节点。 */
    @Override
    public void clusterForget(String nodeId) {
        commandExecutor.get(clusterForgetAsync(nodeId));
    }

    /** 同步 CLUSTER DELSLOTS。 */
    @Override
    public void clusterDeleteSlots(int... slots) {
        commandExecutor.get(clusterDeleteSlotsAsync(slots));
    }

    /** 同步统计槽内 key 数量。 */
    @Override
    public long clusterCountKeysInSlot(int slot) {
        return commandExecutor.get(clusterCountKeysInSlotAsync(slot));
    }

    /** 同步获取槽内最多 count 个 key 名。 */
    @Override
    public List<String> clusterGetKeysInSlot(int slot, int count) {
        return commandExecutor.get(clusterGetKeysInSlotAsync(slot, count));
    }

    /** 同步 CLUSTER SETSLOT（IMPORT/MIGRATING/STABLE 等）。 */
    @Override
    public void clusterSetSlot(int slot, SetSlotCommand command) {
        commandExecutor.get(clusterSetSlotAsync(slot, command));
    }

    /** 带目标节点 ID 的 CLUSTER SETSLOT。 */
    @Override
    public void clusterSetSlot(int slot, SetSlotCommand command, String nodeId) {
        commandExecutor.get(clusterSetSlotAsync(slot, command, nodeId));
    }

    /** 同步 CLUSTER MEET：将 address 节点加入集群。 */
    @Override
    public void clusterMeet(String address) {
        commandExecutor.get(clusterMeetAsync(address));
    }

    /** 同步统计节点失效报告次数。 */
    @Override
    public long clusterCountFailureReports(String nodeId) {
        return commandExecutor.get(clusterCountFailureReportsAsync(nodeId));
    }

    /** 同步 CLUSTER FLUSHSLOTS：清空本节点槽分配。 */
    @Override
    public void clusterFlushSlots() {
        commandExecutor.get(clusterFlushSlotsAsync());
    }

    /** 同步 CLUSTER SLOTS：槽范围到节点地址映射。 */
    @Override
    public Map<ClusterSlotRange, Set<String>> clusterSlots() {
        return commandExecutor.get(clusterSlotsAsync());
    }

    /** 同步 INFO，按 {@link InfoSection} 选择段落。 */
    @Override
    public Map<String, String> info(org.redisson.api.redisnode.RedisNode.InfoSection section) {
        return commandExecutor.get(infoAsync(section));
    }

    /** 同步 MEMORY STATS。 */
    @Override
    public Map<String, String> getMemoryStatistics() {
        return commandExecutor.get(getMemoryStatisticsAsync());
    }

    /** 异步 MEMORY STATS。 */
    @Override
    public RFuture<Map<String, String>> getMemoryStatisticsAsync() {
        return commandExecutor.readAsync(client, StringCodec.INSTANCE, RedisCommands.MEMORY_STATS);
    }

    /** 异步 CLUSTER MYID。 */
    @Override
    public RFuture<String> clusterIdAsync() {
        return commandExecutor.readAsync(client, StringCodec.INSTANCE, RedisCommands.CLUSTER_MYID);
    }

    /** 异步 CLUSTER ADDSLOTS。 */
    @Override
    public RFuture<Void> clusterAddSlotsAsync(int... slots) {
        return commandExecutor.writeAsync(client, StringCodec.INSTANCE, RedisCommands.CLUSTER_ADDSLOTS, IntStream.of(slots).boxed().toArray());
    }

    /** 异步 CLUSTER REPLICATE。 */
    @Override
    public RFuture<Void> clusterReplicateAsync(String nodeId) {
        return commandExecutor.writeAsync(client, StringCodec.INSTANCE, RedisCommands.CLUSTER_REPLICATE, nodeId);
    }

    /** 异步 CLUSTER FORGET。 */
    @Override
    public RFuture<Void> clusterForgetAsync(String nodeId) {
        return commandExecutor.writeAsync(client, StringCodec.INSTANCE, RedisCommands.CLUSTER_FORGET, nodeId);
    }

    /** 异步 CLUSTER DELSLOTS。 */
    @Override
    public RFuture<Void> clusterDeleteSlotsAsync(int... slots) {
        return commandExecutor.writeAsync(client, StringCodec.INSTANCE, RedisCommands.CLUSTER_DELSLOTS, IntStream.of(slots).boxed().toArray());
    }

    /** 异步统计槽内 key 数。 */
    @Override
    public RFuture<Long> clusterCountKeysInSlotAsync(int slot) {
        return commandExecutor.readAsync(client, StringCodec.INSTANCE, RedisCommands.CLUSTER_COUNTKEYSINSLOT, slot);
    }

    /** 异步获取槽内 key 样本。 */
    @Override
    public RFuture<List<String>> clusterGetKeysInSlotAsync(int slot, int count) {
        return commandExecutor.readAsync(client, StringCodec.INSTANCE, RedisCommands.CLUSTER_GETKEYSINSLOT, slot, count);
    }

    /** 异步 CLUSTER SETSLOT。 */
    @Override
    public RFuture<Void> clusterSetSlotAsync(int slot, SetSlotCommand command) {
        return commandExecutor.writeAsync(client, StringCodec.INSTANCE, RedisCommands.CLUSTER_SETSLOT, slot, command);
    }

    /** 异步 CLUSTER SETSLOT（含 nodeId）。 */
    @Override
    public RFuture<Void> clusterSetSlotAsync(int slot, SetSlotCommand command, String nodeId) {
        return commandExecutor.writeAsync(client, StringCodec.INSTANCE, RedisCommands.CLUSTER_SETSLOT, slot, command, nodeId);
    }

    /** 异步 CLUSTER MEET。 */
    @Override
    public RFuture<Void> clusterMeetAsync(String address) {
        RedisURI uri = new RedisURI(address);
        return commandExecutor.writeAsync(client, StringCodec.INSTANCE, RedisCommands.CLUSTER_MEET, uri.getHost(), uri.getPort());
    }

    /** 异步 CLUSTER COUNT-FAILURE-REPORTS。 */
    @Override
    public RFuture<Long> clusterCountFailureReportsAsync(String nodeId) {
        return commandExecutor.readAsync(client, StringCodec.INSTANCE, RedisCommands.CLUSTER_COUNTFAILUREREPORTS, nodeId);
    }

    /** 异步 CLUSTER FLUSHSLOTS。 */
    @Override
    public RFuture<Void> clusterFlushSlotsAsync() {
        return commandExecutor.readAsync(client, StringCodec.INSTANCE, RedisCommands.CLUSTER_FLUSHSLOTS);
    }

    /** 异步 CLUSTER SLOTS。 */
    @Override
    public RFuture<Map<ClusterSlotRange, Set<String>>> clusterSlotsAsync() {
        return commandExecutor.readAsync(client, StringCodec.INSTANCE, RedisCommands.CLUSTER_SLOTS);
    }

    /** 异步 INFO，按 section 选择 RedisCommands.INFO_*。 */
    @Override
    public RFuture<Map<String, String>> infoAsync(org.redisson.api.redisnode.RedisNode.InfoSection section) {
        // 按 InfoSection 分发到对应 INFO 子命令
        if (section == org.redisson.api.redisnode.RedisNode.InfoSection.ALL) {
            return commandExecutor.readAsync(client, StringCodec.INSTANCE, RedisCommands.INFO_ALL);
        } else if (section == org.redisson.api.redisnode.RedisNode.InfoSection.DEFAULT) {
                return commandExecutor.readAsync(client, StringCodec.INSTANCE, RedisCommands.INFO_DEFAULT);
        } else if (section == org.redisson.api.redisnode.RedisNode.InfoSection.SERVER) {
            return commandExecutor.readAsync(client, StringCodec.INSTANCE, RedisCommands.INFO_SERVER);
        } else if (section == org.redisson.api.redisnode.RedisNode.InfoSection.CLIENTS) {
            return commandExecutor.readAsync(client, StringCodec.INSTANCE, RedisCommands.INFO_CLIENTS);
        } else if (section == org.redisson.api.redisnode.RedisNode.InfoSection.MEMORY) {
            return commandExecutor.readAsync(client, StringCodec.INSTANCE, RedisCommands.INFO_MEMORY);
        } else if (section == org.redisson.api.redisnode.RedisNode.InfoSection.PERSISTENCE) {
            return commandExecutor.readAsync(client, StringCodec.INSTANCE, RedisCommands.INFO_PERSISTENCE);
        } else if (section == org.redisson.api.redisnode.RedisNode.InfoSection.STATS) {
            return commandExecutor.readAsync(client, StringCodec.INSTANCE, RedisCommands.INFO_STATS);
        } else if (section == org.redisson.api.redisnode.RedisNode.InfoSection.REPLICATION) {
            return commandExecutor.readAsync(client, StringCodec.INSTANCE, RedisCommands.INFO_REPLICATION);
        } else if (section == org.redisson.api.redisnode.RedisNode.InfoSection.CPU) {
            return commandExecutor.readAsync(client, StringCodec.INSTANCE, RedisCommands.INFO_CPU);
        } else if (section == org.redisson.api.redisnode.RedisNode.InfoSection.COMMANDSTATS) {
            return commandExecutor.readAsync(client, StringCodec.INSTANCE, RedisCommands.INFO_COMMANDSTATS);
        } else if (section == org.redisson.api.redisnode.RedisNode.InfoSection.CLUSTER) {
            return commandExecutor.readAsync(client, StringCodec.INSTANCE, RedisCommands.INFO_CLUSTER);
        } else if (section == org.redisson.api.redisnode.RedisNode.InfoSection.KEYSPACE) {
            return commandExecutor.readAsync(client, StringCodec.INSTANCE, RedisCommands.INFO_KEYSPACE);
        }
        // 未知 section
        throw new IllegalStateException();
    }

    /** 同步 CONFIG GET。 */
    @Override
    public Map<String, String> getConfig(String parameter) {
        return commandExecutor.get(getConfigAsync(parameter));
    }

    /** 同步 CONFIG SET。 */
    @Override
    public void setConfig(String parameter, String value) {
        commandExecutor.get(setConfigAsync(parameter, value));
    }

    /** 异步 CONFIG GET。 */
    @Override
    public RFuture<Map<String, String>> getConfigAsync(String parameter) {
        return commandExecutor.readAsync(client, StringCodec.INSTANCE, RedisCommands.CONFIG_GET_MAP, parameter);
    }

    /** 异步 CONFIG SET。 */
    @Override
    public RFuture<Void> setConfigAsync(String parameter, String value) {
        return commandExecutor.writeAsync(client, StringCodec.INSTANCE, RedisCommands.CONFIG_SET, parameter, value);
    }

    /** 同步 BGSAVE。 */
    @Override
    public void bgSave() {
        commandExecutor.get(bgSaveAsync());
    }

    /** 同步 BGSAVE SCHEDULE（延迟后台保存）。 */
    @Override
    public void scheduleBgSave() {
        commandExecutor.get(scheduleBgSaveAsync());
    }

    /** 同步 SAVE（阻塞式 RDB 快照）。 */
    @Override
    public void save() {
        commandExecutor.get(saveAsync());
    }

    /** 同步 LASTSAVE，返回上次成功保存时间。 */
    @Override
    public Instant getLastSaveTime() {
        return commandExecutor.get(getLastSaveTimeAsync());
    }

    /** 异步 BGSAVE。 */
    @Override
    public RFuture<Void> bgSaveAsync() {
        return commandExecutor.writeAsync(client, StringCodec.INSTANCE, RedisCommands.BGSAVE);
    }

    /** 异步 BGSAVE SCHEDULE。 */
    @Override
    public RFuture<Void> scheduleBgSaveAsync() {
        return commandExecutor.writeAsync(client, StringCodec.INSTANCE, RedisCommands.BGSAVE, "SCHEDULE");
    }

    /** 异步 SAVE。 */
    @Override
    public RFuture<Void> saveAsync() {
        return commandExecutor.writeAsync(client, StringCodec.INSTANCE, RedisCommands.SAVE);
    }

    /** 异步 LASTSAVE。 */
    @Override
    public RFuture<Instant> getLastSaveTimeAsync() {
        return commandExecutor.writeAsync(client, StringCodec.INSTANCE, RedisCommands.LASTSAVE_INSTANT);
    }

    /** 同步 BGREWRITEAOF。 */
    @Override
    public void bgRewriteAOF() {
        commandExecutor.get(bgRewriteAOFAsync());
    }

    /** 异步 BGREWRITEAOF。 */
    @Override
    public RFuture<Void> bgRewriteAOFAsync() {
        return commandExecutor.writeAsync(client, StringCodec.INSTANCE, RedisCommands.BGREWRITEAOF);
    }

    /** 同步 DBSIZE：当前库 key 总数。 */
    @Override
    public long size() {
        return commandExecutor.get(sizeAsync());
    }

    /** 异步 DBSIZE。 */
    @Override
    public RFuture<Long> sizeAsync() {
        return commandExecutor.writeAsync(client, StringCodec.INSTANCE, RedisCommands.DBSIZE);
    }

}
