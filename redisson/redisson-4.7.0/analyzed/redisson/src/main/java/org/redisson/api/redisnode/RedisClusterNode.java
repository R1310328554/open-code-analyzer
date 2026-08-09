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

import org.redisson.cluster.ClusterSlotRange;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Redis 集群节点基础 API。
 * <p>
 * 在 {@link RedisNode} 通用节点能力之上，提供槽位分配、节点加入/移除、
 * 故障报告及集群拓扑查询等集群运维操作。
 *
 * @author Nikita Koksharov
 *
 */
public interface RedisClusterNode extends RedisNode, RedisClusterNodeAsync {

    /**
     * 返回该 Redis 节点上报的集群信息。
     *
     * @return 集群信息键值对
     */
    Map<String, String> clusterInfo();

    /**
     * 返回该 Redis 节点的集群 ID。
     *
     * @return 节点 ID
     */
    String clusterId();

    /**
     * 向该 Redis 节点添加槽位（slot）。
     *
     * @param slots 要添加的槽位编号
     */
    void clusterAddSlots(int... slots);

    /**
     * 将该 Redis 节点重新配置为指定 ID 节点的从副本。
     *
     * @param nodeId 目标主节点 ID
     */
    void clusterReplicate(String nodeId);

    /**
     * 从集群中移除指定 ID 的 Redis 节点。
     *
     * @param nodeId 待移除节点 ID
     */
    void clusterForget(String nodeId);

    /**
     * 从该 Redis 节点移除槽位。
     *
     * @param slots 要移除的槽位编号
     */
    void clusterDeleteSlots(int... slots);

    /**
     * 统计指定槽位中的键数量。
     *
     * @param slot 槽位编号
     * @return 键数量
     */
    long clusterCountKeysInSlot(int slot);

    /**
     * 返回指定槽位中的键，数量受 count 限制。
     *
     * @param slot 槽位编号
     * @param count 最多返回的键数量
     * @return 键名列表
     */
    List<String> clusterGetKeysInSlot(int slot, int count);

    /**
     * 按指定命令设置槽位归属（如 IMPORTING、MIGRATING、STABLE 等）。
     *
     * @param slot 槽位编号
     * @param command 槽位操作命令
     */
    void clusterSetSlot(int slot, SetSlotCommand command);

    /**
     * 按指定命令设置槽位归属，并关联目标节点 ID。
     *
     * @param slot 槽位编号
     * @param command 槽位操作命令
     * @param nodeId 关联的 Redis 节点 ID
     */
    void clusterSetSlot(int slot, SetSlotCommand command, String nodeId);

    /**
     * 将指定地址的 Redis 节点加入集群。
     * <p>
     * 地址示例：<code>redis://127.0.0.1:9233</code>
     *
     * @param address Redis 节点地址
     */
    void clusterMeet(String address);

    /**
     * 返回指定节点 ID 收到的故障报告数量。
     *
     * @param nodeId Redis 节点 ID
     * @return 故障报告次数
     */
    long clusterCountFailureReports(String nodeId);

    /**
     * 清除该 Redis 节点上的所有槽位分配。
     */
    void clusterFlushSlots();

    /**
     * 返回集群槽位区间到 Redis 节点的映射关系。
     *
     * @return 槽位区间与节点 ID 集合的映射
     */
    Map<ClusterSlotRange, Set<String>> clusterSlots();

}
