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
package org.redisson.api.redisnode;

import org.redisson.api.RFuture;
import org.redisson.cluster.ClusterSlotRange;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Redis 集群节点异步基础 API。
 * <p>
 * 以 {@link RFuture} 形式暴露 {@link RedisClusterNode} 中的集群运维与查询能力。
 *
 * @author Nikita Koksharov
 *
 */
public interface RedisClusterNodeAsync extends RedisNodeAsync {

    /**
     * 异步返回该 Redis 节点上报的集群信息。
     *
     * @return 集群信息键值对
     */
    RFuture<Map<String, String>> clusterInfoAsync();

    /**
     * 异步返回该 Redis 节点的集群 ID。
     *
     * @return 节点 ID
     */
    RFuture<String> clusterIdAsync();

    /**
     * 异步向该 Redis 节点添加槽位。
     *
     * @param slots 要添加的槽位编号
     * @return void
     */
    RFuture<Void> clusterAddSlotsAsync(int... slots);

    /**
     * 异步将该 Redis 节点重新配置为指定 ID 节点的从副本。
     *
     * @param nodeId 目标主节点 ID
     * @return void
     */
    RFuture<Void> clusterReplicateAsync(String nodeId);

    /**
     * 异步从集群中移除指定 ID 的 Redis 节点。
     *
     * @param nodeId 待移除节点 ID
     * @return void
     */
    RFuture<Void> clusterForgetAsync(String nodeId);

    /**
     * 异步从该 Redis 节点移除槽位。
     *
     * @param slots 要移除的槽位编号
     * @return void
     */
    RFuture<Void> clusterDeleteSlotsAsync(int... slots);

    /**
     * 异步统计指定槽位中的键数量。
     *
     * @param slot 槽位编号
     * @return 键数量
     */
    RFuture<Long> clusterCountKeysInSlotAsync(int slot);

    /**
     * 异步返回指定槽位中的键，数量受 count 限制。
     *
     * @param slot 槽位编号
     * @param count 最多返回的键数量
     * @return 键名列表
     */
    RFuture<List<String>> clusterGetKeysInSlotAsync(int slot, int count);

    /**
     * 异步按指定命令设置槽位归属。
     *
     * @param slot 槽位编号
     * @param command 槽位操作命令
     * @return void
     */
    RFuture<Void> clusterSetSlotAsync(int slot, SetSlotCommand command);

    /**
     * 异步按指定命令设置槽位归属，并关联目标节点 ID。
     *
     * @param slot 槽位编号
     * @param command 槽位操作命令
     * @param nodeId 关联的 Redis 节点 ID
     * @return void
     */
    RFuture<Void> clusterSetSlotAsync(int slot, SetSlotCommand command, String nodeId);

    /**
     * 异步将指定地址的 Redis 节点加入集群。
     * <p>
     * 地址示例：<code>redis://127.0.0.1:9233</code>
     *
     * @param address Redis 节点地址
     * @return void
     */
    RFuture<Void> clusterMeetAsync(String address);

    /**
     * 异步返回指定节点 ID 收到的故障报告数量。
     *
     * @param nodeId Redis 节点 ID
     * @return 故障报告次数
     */
    RFuture<Long> clusterCountFailureReportsAsync(String nodeId);

    /**
     * 异步清除该 Redis 节点上的所有槽位分配。
     * @return void
     */
    RFuture<Void> clusterFlushSlotsAsync();

    /**
     * 异步返回集群槽位区间到 Redis 节点的映射关系。
     *
     * @return 槽位区间与节点 ID 集合的映射
     */
    RFuture<Map<ClusterSlotRange, Set<String>>> clusterSlotsAsync();

}
