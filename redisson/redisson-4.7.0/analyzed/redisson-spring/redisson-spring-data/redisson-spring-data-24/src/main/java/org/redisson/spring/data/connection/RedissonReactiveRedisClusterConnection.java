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

import org.redisson.client.codec.ByteArrayCodec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.client.protocol.RedisStrictCommand;
import org.redisson.client.protocol.decoder.ObjectDecoder;
import org.redisson.client.protocol.decoder.ObjectListReplayDecoder;
import org.redisson.reactive.CommandReactiveExecutor;
import org.springframework.data.redis.connection.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Spring Data Redis 集群模式响应式连接门面。
 * <p>继承 {@link RedissonReactiveRedisConnection} 并实现 {@link ReactiveRedisClusterConnection}；
各 {@code *Commands()} 返回集群专用命令适配器，并封装 CLUSTER 拓扑/槽位管理命令。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonReactiveRedisClusterConnection extends RedissonReactiveRedisConnection implements ReactiveRedisClusterConnection {

    /** 注入响应式命令执行器。 */
    public RedissonReactiveRedisClusterConnection(CommandReactiveExecutor executorService) {
        super(executorService);
    }

    /** 返回集群 Key 命令适配器。 */
    @Override
    public ReactiveClusterKeyCommands keyCommands() {
        return new RedissonReactiveClusterKeyCommands(executorService);
    }

    /** 返回集群 String 命令适配器。 */
    @Override
    public ReactiveClusterStringCommands stringCommands() {
        return new RedissonReactiveClusterStringCommands(executorService);
    }

    /** 返回集群数值命令适配器。 */
    @Override
    public ReactiveClusterNumberCommands numberCommands() {
        return new RedissonReactiveClusterNumberCommands(executorService);
    }

    /** 返回集群 List 命令适配器。 */
    @Override
    public ReactiveClusterListCommands listCommands() {
        return new RedissonReactiveClusterListCommands(executorService);
    }

    /** 返回集群 Set 命令适配器。 */
    @Override
    public ReactiveClusterSetCommands setCommands() {
        return new RedissonReactiveClusterSetCommands(executorService);
    }

    /** 返回集群 ZSet 命令适配器。 */
    @Override
    public ReactiveClusterZSetCommands zSetCommands() {
        return new RedissonReactiveClusterZSetCommands(executorService);
    }

    /** 返回集群 Hash 命令适配器。 */
    @Override
    public ReactiveClusterHashCommands hashCommands() {
        return new RedissonReactiveClusterHashCommands(executorService);
    }

    /** 返回集群 Geo 命令适配器。 */
    @Override
    public ReactiveClusterGeoCommands geoCommands() {
        return new RedissonReactiveClusterGeoCommands(executorService);
    }

    /** 返回集群 HyperLogLog 命令适配器。 */
    @Override
    public ReactiveClusterHyperLogLogCommands hyperLogLogCommands() {
        return new RedissonReactiveClusterHyperLogLogCommands(executorService);
    }

    /** 返回集群 Server 命令适配器。 */
    @Override
    public ReactiveClusterServerCommands serverCommands() {
        return new RedissonReactiveClusterServerCommands(executorService);
    }

    /** 返回集群 Stream 命令适配器。 */
    @Override
    public ReactiveClusterStreamCommands streamCommands() {
        return new RedissonReactiveClusterStreamCommands(executorService);
    }

    /** 对指定集群节点执行 PING。 */
    @Override
    public Mono<String> ping(RedisClusterNode node) {
        return execute(node, RedisCommands.PING);
    }

    /** CLUSTER NODES：获取集群全部节点拓扑。 */
    @Override
    public Flux<RedisClusterNode> clusterGetNodes() {
        RedisStrictCommand<List<RedisClusterNode>> cluster
                = new RedisStrictCommand<List<RedisClusterNode>>("CLUSTER", "NODES",
                new ObjectDecoder(new RedisClusterNodeDecoder(executorService.getServiceManager())));

        Mono<List<RedisClusterNode>> result = read(null, StringCodec.INSTANCE, cluster);
        return result.flatMapMany(e -> Flux.fromIterable(e));
    }

    /** 按主节点 host/port 查找其从节点列表。 */
    @Override
    public Flux<RedisClusterNode> clusterGetSlaves(RedisClusterNode redisClusterNode) {
        Flux<RedisClusterNode> nodes = clusterGetNodes();
        Flux<RedisClusterNode> master = nodes.filter(e -> e.getHost().equals(redisClusterNode.getHost()) && e.getPort().equals(redisClusterNode.getPort()));
        return master.flatMap(node -> clusterGetNodes().filter(e -> Objects.equals(e.getMasterId(), node.getMasterId())));
    }

    /** 构建主节点到从节点集合的映射。 */
    @Override
    public Mono<Map<RedisClusterNode, Collection<RedisClusterNode>>> clusterGetMasterSlaveMap() {
        Flux<RedisClusterNode> nodes = clusterGetNodes();
        Flux<RedisClusterNode> masters = nodes.filter(e -> e.isMaster());
        return masters.flatMap(master -> Mono.just(master).zipWith(clusterGetNodes()
                                        .filter(e -> Objects.equals(e.getMasterId(), master.getMasterId()))
                                        .collect(Collectors.toSet())))
                      .collect(Collectors.toMap(Tuple2::getT1, Tuple2::getT2));
    }

    /** KEYSLOT：计算 key 对应的哈希槽编号。 */
    @Override
    public Mono<Integer> clusterGetSlotForKey(ByteBuffer byteBuffer) {
        return read(null, StringCodec.INSTANCE, RedisCommands.KEYSLOT, toByteArray(byteBuffer));
    }

    /** 查找负责指定槽的主节点。 */
    @Override
    public Mono<RedisClusterNode> clusterGetNodeForSlot(int slot) {
        return clusterGetNodes().filter(n -> n.isMaster() && n.getSlotRange().contains(slot)).next();
    }

    /** 按 key 计算槽位并返回负责该槽的主节点。 */
    @Override
    public Mono<RedisClusterNode> clusterGetNodeForKey(ByteBuffer byteBuffer) {
        int slot = executorService.getConnectionManager().calcSlot(toByteArray(byteBuffer));
        return clusterGetNodeForSlot(slot);
    }

    /** CLUSTER INFO：获取集群状态信息。 */
    @Override
    public Mono<ClusterInfo> clusterGetClusterInfo() {
        Mono<Map<String, String>> mono = read(null, StringCodec.INSTANCE, RedisCommands.CLUSTER_INFO);
        return mono.map(e -> {
            Properties props = new Properties();
            for (Map.Entry<String, String> entry : e.entrySet()) {
                props.setProperty(entry.getKey(), entry.getValue());
            }
            return new ClusterInfo(props);
        });
    }

    /** CLUSTER ADDSLOTS：向节点分配一个或多个槽。 */
    @Override
    public Mono<Void> clusterAddSlots(RedisClusterNode redisClusterNode, int... ints) {
        List<Integer> params = convert(ints);
        return execute(redisClusterNode, RedisCommands.CLUSTER_ADDSLOTS, params.toArray());
    }

    /** 将槽位数组转为 Redis 命令参数列表。 */
    private List<Integer> convert(int... slots) {
        List<Integer> params = new ArrayList<Integer>();
        for (int slot : slots) {
            params.add(slot);
        }
        return params;
    }

    /** CLUSTER ADDSLOTS：按 {@link SlotRange} 批量分配槽。 */
    @Override
    public Mono<Void> clusterAddSlots(RedisClusterNode redisClusterNode, RedisClusterNode.SlotRange slotRange) {
        return clusterAddSlots(redisClusterNode, slotRange.getSlotsArray());
    }

    /** CLUSTER COUNTKEYSINSLOT：统计槽内 key 数量。 */
    @Override
    public Mono<Long> clusterCountKeysInSlot(int slot) {
        Mono<RedisClusterNode> node = clusterGetNodeForSlot(slot);
        return node.flatMap(e -> {
            return execute(e, RedisCommands.CLUSTER_COUNTKEYSINSLOT, slot);
        });
    }

    /** CLUSTER DELSLOTS：从节点移除一个或多个槽。 */
    @Override
    public Mono<Void> clusterDeleteSlots(RedisClusterNode redisClusterNode, int... ints) {
        List<Integer> params = convert(ints);
        return execute(redisClusterNode, RedisCommands.CLUSTER_DELSLOTS, params.toArray());
    }

    /** CLUSTER DELSLOTS：按 {@link SlotRange} 批量移除槽。 */
    @Override
    public Mono<Void> clusterDeleteSlotsInRange(RedisClusterNode redisClusterNode, RedisClusterNode.SlotRange slotRange) {
        return clusterDeleteSlots(redisClusterNode, slotRange.getSlotsArray());
    }

    /** CLUSTER FORGET：从集群视图中移除指定节点。 */
    @Override
    public Mono<Void> clusterForget(RedisClusterNode redisClusterNode) {
        return execute(redisClusterNode, RedisCommands.CLUSTER_FORGET, redisClusterNode.getId());
    }

    /** CLUSTER MEET：将新节点加入集群。 */
    @Override
    public Mono<Void> clusterMeet(RedisClusterNode redisClusterNode) {
        return execute(redisClusterNode, RedisCommands.CLUSTER_MEET, redisClusterNode.getHost(), redisClusterNode.getPort());
    }

    /** CLUSTER SETSLOT：设置槽的导入/迁移/稳定状态。 */
    @Override
    public Mono<Void> clusterSetSlot(RedisClusterNode redisClusterNode, int slot, AddSlots addSlots) {
        return execute(redisClusterNode, RedisCommands.CLUSTER_SETSLOT, slot, addSlots);
    }

    /** CLUSTER GETKEYSINSLOT 命令定义。 */
    private static final RedisStrictCommand<List<String>> CLUSTER_GETKEYSINSLOT = new RedisStrictCommand<List<String>>("CLUSTER", "GETKEYSINSLOT", new ObjectListReplayDecoder<String>());

    /** CLUSTER GETKEYSINSLOT：返回槽内最多 count 个 key。 */
    @Override
    public Flux<ByteBuffer> clusterGetKeysInSlot(int slot, int count) {
        Mono<List<byte[]>> f = executorService.reactive(() -> {
            return executorService.readAsync((String) null, ByteArrayCodec.INSTANCE, CLUSTER_GETKEYSINSLOT, slot, count);
        });
        return f.flatMapMany(e -> Flux.fromIterable(e)).map(e -> ByteBuffer.wrap(e));
    }

    /** CLUSTER REPLICATE：将节点配置为指定主节点的从节点。 */
    @Override
    public Mono<Void> clusterReplicate(RedisClusterNode redisClusterNode, RedisClusterNode slave) {
        return execute(redisClusterNode, RedisCommands.CLUSTER_REPLICATE, slave.getId());
    }
}
