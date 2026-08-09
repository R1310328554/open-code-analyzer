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
package org.redisson.spring.data.connection;

import org.redisson.api.BatchResult;
import org.redisson.api.RFuture;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.redisson.client.codec.ByteArrayCodec;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.RedisCommand;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.client.protocol.RedisStrictCommand;
import org.redisson.client.protocol.decoder.ObjectDecoder;
import org.redisson.client.protocol.decoder.ObjectListReplayDecoder;
import org.redisson.client.protocol.decoder.StringMapDataDecoder;
import org.redisson.command.CommandBatchService;
import org.redisson.connection.ClientConnectionsEntry;
import org.redisson.connection.MasterSlaveEntry;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.data.redis.connection.ClusterInfo;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisClusterNode;
import org.springframework.data.redis.connection.RedisClusterNode.SlotRange;
import org.springframework.data.redis.connection.convert.Converters;
import org.springframework.data.redis.connection.convert.StringToRedisClientInfoConverter;
import org.springframework.data.redis.core.types.RedisClientInfo;
import org.springframework.util.Assert;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Redisson 集群模式 {@link RedisClusterConnection} 实现。
 * <p>扩展 {@link RedissonConnection}，提供 CLUSTER 管理命令、
 * 槽位/key 路由及按节点执行服务端命令的能力。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonClusterConnection extends RedissonConnection implements RedisClusterConnection {

    /** 绑定 Redisson 客户端并初始化集群连接。 */
    public RedissonClusterConnection(RedissonClient redisson) {
        super(redisson);
    }

    /** 返回 CLUSTER NODES 解析结果。 */
    @Override
    public Iterable<RedisClusterNode> clusterGetNodes() {
        RedisStrictCommand<List<RedisClusterNode>> cluster
                = new RedisStrictCommand<List<RedisClusterNode>>("CLUSTER", "NODES",
                new ObjectDecoder(new RedisClusterNodeDecoder(executorService.getServiceManager())));
        return read(null, StringCodec.INSTANCE, cluster);
    }

    /** 返回指定 master 的 slave 节点。 */
    @Override
    public Collection<RedisClusterNode> clusterGetSlaves(RedisClusterNode master) {
        Iterable<RedisClusterNode> res = clusterGetNodes();
        RedisClusterNode masterNode = null;
        for (Iterator<RedisClusterNode> iterator = res.iterator(); iterator.hasNext();) {
            RedisClusterNode redisClusterNode = iterator.next();
            if (master.getHost().equals(redisClusterNode.getHost()) 
                    && master.getPort().equals(redisClusterNode.getPort())) {
                masterNode = redisClusterNode;
                break;
            }
        }
        
        // 未在 CLUSTER NODES 结果中找到匹配的 master 节点。
        if (masterNode == null) {
            throw new IllegalStateException("Unable to find master node: " + master);
        }
        
        for (Iterator<RedisClusterNode> iterator = res.iterator(); iterator.hasNext();) {
            RedisClusterNode redisClusterNode = iterator.next();
            if (redisClusterNode.getMasterId() == null 
                    || !redisClusterNode.getMasterId().equals(masterNode.getId())) {
                iterator.remove();
            }
        }
        return (Collection<RedisClusterNode>) res;
    }

    /** 返回 master 到 slaves 的映射。 */
    @Override
    public Map<RedisClusterNode, Collection<RedisClusterNode>> clusterGetMasterSlaveMap() {
        Iterable<RedisClusterNode> res = clusterGetNodes();
        
        Set<RedisClusterNode> masters = new HashSet<RedisClusterNode>();
        for (Iterator<RedisClusterNode> iterator = res.iterator(); iterator.hasNext();) {
            RedisClusterNode redisClusterNode = iterator.next();
            if (redisClusterNode.isMaster()) {
                masters.add(redisClusterNode);
            }
        }
        
        Map<RedisClusterNode, Collection<RedisClusterNode>> result = new HashMap<RedisClusterNode, Collection<RedisClusterNode>>();
        for (Iterator<RedisClusterNode> iterator = res.iterator(); iterator.hasNext();) {
            RedisClusterNode redisClusterNode = iterator.next();
            
            for (RedisClusterNode masterNode : masters) {
                if (redisClusterNode.getMasterId() != null 
                        && redisClusterNode.getMasterId().equals(masterNode.getId())) {
                    Collection<RedisClusterNode> list = result.get(masterNode);
                    if (list == null) {
                        list = new ArrayList<RedisClusterNode>();
                        result.put(masterNode, list);
                    }
                    list.add(redisClusterNode);
                }
            }
        }
        return result;
    }

    /** 返回 key 的 CRC16 槽位。 */
    @Override
    public Integer clusterGetSlotForKey(byte[] key) {
        RFuture<Integer> f = executorService.readAsync((String)null, StringCodec.INSTANCE, RedisCommands.KEYSLOT, key);
        return syncFuture(f);
    }

    /** 返回槽位对应节点。 */
    @Override
    public RedisClusterNode clusterGetNodeForSlot(int slot) {
        Iterable<RedisClusterNode> res = clusterGetNodes();
        for (RedisClusterNode redisClusterNode : res) {
            if (redisClusterNode.isMaster() && redisClusterNode.getSlotRange().contains(slot)) {
                return redisClusterNode;
            }
        }
        return null;
    }

    /** 返回 key 所在槽位对应节点。 */
    @Override
    public RedisClusterNode clusterGetNodeForKey(byte[] key) {
        int slot = executorService.getConnectionManager().calcSlot(key);
        return clusterGetNodeForSlot(slot);
    }

    /** 返回 CLUSTER INFO 解析结果。 */
    @Override
    public ClusterInfo clusterGetClusterInfo() {
        RFuture<Map<String, String>> f = executorService.readAsync((String)null, StringCodec.INSTANCE, RedisCommands.CLUSTER_INFO);
        Map<String, String> entries = syncFuture(f);

        Properties props = new Properties();
        for (Entry<String, String> entry : entries.entrySet()) {
            props.setProperty(entry.getKey(), entry.getValue());
        }
        return new ClusterInfo(props);
    }

    /** 为当前节点分配槽位。 */
    @Override
    public void clusterAddSlots(RedisClusterNode node, int... slots) {
        RedisClient entry = getEntry(node);
        List<Integer> params = convert(slots);
        RFuture<Map<String, String>> f = executorService.writeAsync(entry, StringCodec.INSTANCE, RedisCommands.CLUSTER_ADDSLOTS, params.toArray());
        syncFuture(f);
    }

    protected List<Integer> convert(int... slots) {
        List<Integer> params = new ArrayList<Integer>();
        for (int slot : slots) {
            params.add(slot);
        }
        return params;
    }

    /** 为当前节点分配槽位。 */
    @Override
    public void clusterAddSlots(RedisClusterNode node, SlotRange range) {
        clusterAddSlots(node, range.getSlotsArray());
    }

    /** 返回槽位内 key 数量。 */
    @Override
    public Long clusterCountKeysInSlot(int slot) {
        RedisClusterNode node = clusterGetNodeForSlot(slot);
        MasterSlaveEntry entry = executorService.getConnectionManager().getEntry(new InetSocketAddress(node.getHost(), node.getPort()));
        RFuture<Long> f = executorService.readAsync(entry, StringCodec.INSTANCE, RedisCommands.CLUSTER_COUNTKEYSINSLOT, slot);
        return syncFuture(f);
    }

    /** 删除当前节点槽位。 */
    @Override
    public void clusterDeleteSlots(RedisClusterNode node, int... slots) {
        RedisClient entry = getEntry(node);
        List<Integer> params = convert(slots);
        RFuture<Long> f = executorService.writeAsync(entry, StringCodec.INSTANCE, RedisCommands.CLUSTER_DELSLOTS, params.toArray());
        syncFuture(f);
    }

    /** 删除槽位区间。 */
    @Override
    public void clusterDeleteSlotsInRange(RedisClusterNode node, SlotRange range) {
        clusterDeleteSlots(node, range.getSlotsArray());
    }

    /** CLUSTER FORGET 移除节点。 */
    @Override
    public void clusterForget(RedisClusterNode node) {
        RFuture<Void> f = executorService.writeAsync((String)null, StringCodec.INSTANCE, RedisCommands.CLUSTER_FORGET, node.getId());
        syncFuture(f);
    }

    /** CLUSTER MEET 加入节点。 */
    @Override
    public void clusterMeet(RedisClusterNode node) {
        Assert.notNull(node, "Cluster node must not be null for CLUSTER MEET command!");
        Assert.hasText(node.getHost(), "Node to meet cluster must have a host!");
        Assert.isTrue(node.getPort() > 0, "Node to meet cluster must have a port greater 0!");
        
        RFuture<Void> f = executorService.writeAsync((String)null, StringCodec.INSTANCE, RedisCommands.CLUSTER_MEET, node.getHost(), node.getPort());
        syncFuture(f);
    }

    /** 设置槽位 IMPORTING/MIGRATING/STABLE/... */
    @Override
    public void clusterSetSlot(RedisClusterNode node, int slot, AddSlots mode) {
        RedisClient entry = getEntry(node);
        RFuture<Map<String, String>> f = executorService.writeAsync(entry, StringCodec.INSTANCE, RedisCommands.CLUSTER_SETSLOT, slot, mode);
        syncFuture(f);
    }
    
    private static final RedisStrictCommand<List<String>> CLUSTER_GETKEYSINSLOT = new RedisStrictCommand<List<String>>("CLUSTER", "GETKEYSINSLOT", new ObjectListReplayDecoder<String>());

    /** 返回槽位内 key 列表。 */
    @Override
    public List<byte[]> clusterGetKeysInSlot(int slot, Integer count) {
        RFuture<List<byte[]>> f = executorService.readAsync((String)null, ByteArrayCodec.INSTANCE, CLUSTER_GETKEYSINSLOT, slot, count);
        return syncFuture(f);
    }

    /** 配置节点复制指定 master。 */
    @Override
    public void clusterReplicate(RedisClusterNode master, RedisClusterNode slave) {
        RedisClient entry = getEntry(master);
        RFuture<Long> f = executorService.writeAsync(entry, StringCodec.INSTANCE, RedisCommands.CLUSTER_REPLICATE, slave.getId());
        syncFuture(f);
    }

//    集群节点 PING 暂未启用（历史注释保留）。
//    @Override
//    public String ping(RedisClusterNode node) {
//        return execute(node, RedisCommands.PING);
//    }

    /** 后台 AOF 重写。 */
    @Override
    public void bgReWriteAof(RedisClusterNode node) {
        execute(node, RedisCommands.BGREWRITEAOF);
    }

    /** 后台 BGSAVE。 */
    @Override
    public void bgSave(RedisClusterNode node) {
        execute(node, RedisCommands.BGSAVE);
    }

    /** 返回上次 RDB 持久化时间。 */
    @Override
    public Long lastSave(RedisClusterNode node) {
        return execute(node, RedisCommands.LASTSAVE);
    }

    /** 同步 SAVE。 */
    @Override
    public void save(RedisClusterNode node) {
        execute(node, RedisCommands.SAVE);
    }

    /** 返回当前库 key 数量。 */
    @Override
    public Long dbSize(RedisClusterNode node) {
        return execute(node, RedisCommands.DBSIZE);
    }

    private <T> T execute(RedisClusterNode node, RedisCommand<T> command) {
        RedisClient entry = getEntry(node);
        RFuture<T> f = executorService.writeAsync(entry, StringCodec.INSTANCE, command);
        return syncFuture(f);
    }

    protected RedisClient getEntry(RedisClusterNode node) {
        InetSocketAddress addr = new InetSocketAddress(node.getHost(), node.getPort());
        MasterSlaveEntry entry = executorService.getConnectionManager().getEntry(addr);
        ClientConnectionsEntry e = entry.getEntry(addr);
        return e.getClient();
    }

    /** 清空当前数据库。 */
    @Override
    public void flushDb(RedisClusterNode node) {
        execute(node, RedisCommands.FLUSHDB);
    }

    /** 清空全部数据库。 */
    @Override
    public void flushAll(RedisClusterNode node) {
        execute(node, RedisCommands.FLUSHALL);
    }

    /** 返回 INFO 信息。 */
    @Override
    public Properties info(RedisClusterNode node) {
        Map<String, String> info = execute(node, RedisCommands.INFO_ALL);
        Properties result = new Properties();
        for (Entry<String, String> entry : info.entrySet()) {
            result.setProperty(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /** 返回 INFO 信息。 */
    @Override
    public Properties info(RedisClusterNode node, String section) {
        RedisStrictCommand<Map<String, String>> command = new RedisStrictCommand<Map<String, String>>("INFO", section, new StringMapDataDecoder());

        Map<String, String> info = execute(node, command);
        Properties result = new Properties();
        for (Entry<String, String> entry : info.entrySet()) {
            result.setProperty(entry.getKey(), entry.getValue());
        }
        return result;
    }

    private final RedisStrictCommand<List<byte[]>> KEYS = new RedisStrictCommand<>("KEYS");

    /** 按模式匹配返回 key 集合（慎用）。 */
    @Override
    public Set<byte[]> keys(RedisClusterNode node, byte[] pattern) {
        RedisClient entry = getEntry(node);
        RFuture<Collection<byte[]>> f = executorService.readAsync(entry, ByteArrayCodec.INSTANCE, KEYS, pattern);
        Collection<byte[]> keys = syncFuture(f);
        return new HashSet<>(keys);
    }

    /** 随机返回一个 key。 */
    @Override
    public byte[] randomKey(RedisClusterNode node) {
        RedisClient entry = getEntry(node);
        RFuture<byte[]> f = executorService.readRandomAsync(entry, ByteArrayCodec.INSTANCE, RedisCommands.RANDOM_KEY);
        return syncFuture(f);
    }

    /** 关闭 Redis 服务。 */
    @Override
    public void shutdown(RedisClusterNode node) {
        RedisClient entry = getEntry(node);
        RFuture<Void> f = executorService.readAsync(entry, ByteArrayCodec.INSTANCE, RedisCommands.SHUTDOWN);
        syncFuture(f);
    }

    /** 读取 CONFIG GET。 */
    @Override
    public List<String> getConfig(RedisClusterNode node, String pattern) {
        RedisClient entry = getEntry(node);
        RFuture<List<String>> f = executorService.writeAsync(entry, StringCodec.INSTANCE, RedisCommands.CONFIG_GET, pattern);
        return syncFuture(f);
    }

    /** 写入 CONFIG SET。 */
    @Override
    public void setConfig(RedisClusterNode node, String param, String value) {
        RedisClient entry = getEntry(node);
        RFuture<Void> f = executorService.writeAsync(entry, StringCodec.INSTANCE, RedisCommands.CONFIG_SET, param, value);
        syncFuture(f);
    }

    /** 重置 CONFIG 统计。 */
    @Override
    public void resetConfigStats(RedisClusterNode node) {
        RedisClient entry = getEntry(node);
        RFuture<Void> f = executorService.writeAsync(entry, StringCodec.INSTANCE, RedisCommands.CONFIG_RESETSTAT);
        syncFuture(f);
    }

    /** 返回服务器时间。 */
    @Override
    public Long time(RedisClusterNode node) {
        RedisClient entry = getEntry(node);
        RFuture<Long> f = executorService.readAsync(entry, LongCodec.INSTANCE, RedisCommands.TIME_LONG);
        return syncFuture(f);
    }

    private static final StringToRedisClientInfoConverter CONVERTER = new StringToRedisClientInfoConverter();
    
    /** 返回 CLIENT LIST 信息。 */
    @Override
    public List<RedisClientInfo> getClientList(RedisClusterNode node) {
        RedisClient entry = getEntry(node);
        RFuture<List<String>> f = executorService.readAsync(entry, StringCodec.INSTANCE, RedisCommands.CLIENT_LIST);
        List<String> list = syncFuture(f);
        return CONVERTER.convert(list.toArray(new String[list.size()]));
    }

    /** 重命名 key。 */
    @Override
    public void rename(byte[] oldName, byte[] newName) {

        if (isPipelined()) {
            throw new InvalidDataAccessResourceUsageException("Clustered rename is not supported in a pipeline");
        }

        if (executorService.getConnectionManager().calcSlot(oldName) == executorService.getConnectionManager().calcSlot(newName)) {
            super.rename(oldName, newName);
            return;
        }

        final byte[] value = dump(oldName);

        if (null != value) {

            final Long sourceTtlInSeconds = ttl(oldName);

            final long ttlInMilliseconds;
            if (null != sourceTtlInSeconds && sourceTtlInSeconds > 0) {
                ttlInMilliseconds = sourceTtlInSeconds * 1000;
            } else {
                ttlInMilliseconds = 0;
            }

            restore(newName, ttlInMilliseconds, value);
            del(oldName);
        }
    }

    /** 仅当新 key 不存在时重命名。 */
    @Override
    public Boolean renameNX(byte[] oldName, byte[] newName) {
        if (isPipelined()) {
            throw new InvalidDataAccessResourceUsageException("Clustered rename is not supported in a pipeline");
        }

        if (executorService.getConnectionManager().calcSlot(oldName) == executorService.getConnectionManager().calcSlot(newName)) {
            return super.renameNX(oldName, newName);
        }

        final byte[] value = dump(oldName);

        if (null != value && !exists(newName)) {

            final Long sourceTtlInSeconds = ttl(oldName);

            final long ttlInMilliseconds;
            if (null != sourceTtlInSeconds && sourceTtlInSeconds > 0) {
                ttlInMilliseconds = sourceTtlInSeconds * 1000;
            } else {
                ttlInMilliseconds = 0;
            }

            restore(newName, ttlInMilliseconds, value);
            del(oldName);

            return true;
        }

        return false;
    }

    /** 删除一个或多个 key。 */
    @Override
    public Long del(byte[]... keys) {
        if (isQueueing() || isPipelined()) {
            for (byte[] key: keys) {
                write(key, LongCodec.INSTANCE, RedisCommands.DEL, key);
            }

            return null;
        }

        CommandBatchService es = new CommandBatchService(executorService);
        for (byte[] key: keys) {
            es.writeAsync(key, StringCodec.INSTANCE, RedisCommands.DEL, key);
        }
        BatchResult<Long> b = (BatchResult<Long>) es.execute();
        return b.getResponses().stream().collect(Collectors.summarizingLong(v -> v)).getSum();
    }

    /** 批量 GET。 */
    @Override
    public List<byte[]> mGet(byte[]... keys) {
        if (isQueueing() || isPipelined()) {
            for (byte[] key : keys) {
                read(key, ByteArrayCodec.INSTANCE, RedisCommands.GET, key);
            }
            return null;
        }

        CommandBatchService es = new CommandBatchService(executorService);
        for (byte[] key: keys) {
            es.readAsync(key, ByteArrayCodec.INSTANCE, RedisCommands.GET, key);
        }
        BatchResult<byte[]> r = (BatchResult<byte[]>) es.execute();
        return r.getResponses();
    }

    /** 批量 SET。 */
    @Override
    public void mSet(Map<byte[], byte[]> tuple) {
        if (isQueueing() || isPipelined()) {
            for (Entry<byte[], byte[]> entry: tuple.entrySet()) {
                write(entry.getKey(), StringCodec.INSTANCE, RedisCommands.SET, entry.getKey(), entry.getValue());
            }
            return;
        }

        CommandBatchService es = new CommandBatchService(executorService);
        for (Entry<byte[], byte[]> entry: tuple.entrySet()) {
            es.writeAsync(entry.getKey(), StringCodec.INSTANCE, RedisCommands.SET, entry.getKey(), entry.getValue());
        }
        es.execute();
    }

    /** PING 测试连通性。 */
    @Override
    public String ping(RedisClusterNode node) {
        RedisClient entry = getEntry(node);
        RFuture<String> f = executorService.readAsync(entry, LongCodec.INSTANCE, RedisCommands.PING);
        return syncFuture(f);
    }


}
