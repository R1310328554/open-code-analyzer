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
package org.redisson.cluster;

import org.redisson.misc.RedisURI;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.redisson.connection.MasterSlaveConnectionManager.MAX_SLOT;

/**
 * 集群拓扑中单个分片（主节点及其从节点）的运行时视图。
 * <p>
 * 维护槽位分配、主从地址、故障标记及引用计数，
 * 供 {@link ClusterConnectionManager} 路由与故障转移决策使用。
 *
 * @author Nikita Koksharov
 *
 */
public class ClusterPartition {

    /** 分片内节点角色：主或从。 */
    public enum Type {MASTER, SLAVE}
    
    /** 当前分片视角下的节点类型。 */
    private Type type = Type.MASTER;
    
    /** 分片标识，通常为主节点 ID。 */
    private final String nodeId;
    /** 主节点是否处于 FAIL 状态。 */
    private boolean masterFail;
    /** 主节点连接地址。 */
    private RedisURI masterAddress;
    /** 已知从节点地址集合。 */
    private final Set<RedisURI> slaveAddresses = Collections.newSetFromMap(new ConcurrentHashMap<>());
    /** 已标记为故障的从节点地址。 */
    private final Set<RedisURI> failedSlaves = Collections.newSetFromMap(new ConcurrentHashMap<>());

    /** 槽位位图，快速判断 slot 归属。 */
    private BitSet slots;
    /** 槽位区间集合，与 CLUSTER NODES 解析结果对应。 */
    private Set<ClusterSlotRange> slotRanges = Collections.emptySet();

    /** 从节点分片指向其主分片。 */
    private ClusterPartition parent;

    /** 活跃引用计数，用于延迟回收拓扑对象。 */
    private int references;

    /** 拓扑信息最后更新时间戳。 */
    private long time = System.currentTimeMillis();
    
    /** @param nodeId 分片/节点 ID */
    public ClusterPartition(String nodeId) {
        super();
        this.nodeId = nodeId;
    }
    
    public ClusterPartition getParent() {
        return parent;
    }

    public void setParent(ClusterPartition parent) {
        this.parent = parent;
    }

    public void setType(Type type) {
        this.type = type;
    }
    
    public Type getType() {
        return type;
    }
    
    public String getNodeId() {
        return nodeId;
    }

    public void setMasterFail(boolean masterFail) {
        this.masterFail = masterFail;
    }
    public boolean isMasterFail() {
        return masterFail;
    }

    /** 同时更新槽位区间与预计算的位图。 */
    public void updateSlotRanges(Set<ClusterSlotRange> ranges, BitSet slots) {
        this.slotRanges = ranges;
        this.slots = slots;
    }

    /** 根据区间集合重建槽位位图。 */
    public void setSlotRanges(Set<ClusterSlotRange> ranges) {
        slots = new BitSet(MAX_SLOT);
        for (ClusterSlotRange clusterSlotRange : ranges) {
            slots.set(clusterSlotRange.getStartSlot(), clusterSlotRange.getEndSlot() + 1);
        }
        slotRanges = ranges;
    }

    /** 返回不可变的槽位区间集合。 */
    public Set<ClusterSlotRange> getSlotRanges() {
        return Collections.unmodifiableSet(slotRanges);
    }

    /** 迭代本分片负责的所有槽位编号。 */
    public Iterable<Integer> getSlots() {
        return slots.stream()::iterator;
    }
    
    /** 返回槽位位图（内部可变，调用方勿修改）。 */
    public BitSet slots() {
        return slots;
    }
    
    /** 返回槽位位图的副本。 */
    public BitSet copySlots() {
        return (BitSet) slots.clone();
    }
    
    /** 判断指定槽位是否由本分片负责。 */
    public boolean hasSlot(int slot) {
        return slots.get(slot);
    }
    
    /** 返回本分片负责的槽位总数。 */
    public int getSlotsAmount() {
        return slots.cardinality();
    }

    public RedisURI getMasterAddress() {
        return masterAddress;
    }
    public void setMasterAddress(RedisURI masterAddress) {
        this.masterAddress = masterAddress;
    }

    /** 标记从节点地址为故障。 */
    public void addFailedSlaveAddress(RedisURI address) {
        failedSlaves.add(address);
    }
    public Set<RedisURI> getFailedSlaveAddresses() {
        return Collections.unmodifiableSet(failedSlaves);
    }
    public void removeFailedSlaveAddress(RedisURI uri) {
        failedSlaves.remove(uri);
    }

    public void addSlaveAddress(RedisURI address) {
        slaveAddresses.add(address);
    }
    public Set<RedisURI> getSlaveAddresses() {
        return Collections.unmodifiableSet(slaveAddresses);
    }
    /** 移除从节点地址，同时清除其故障标记。 */
    public void removeSlaveAddress(RedisURI uri) {
        slaveAddresses.remove(uri);
        failedSlaves.remove(uri);
    }

    /** 增加引用计数。 */
    public void incReference() {
        references++;
    }
    /** 减少引用计数并返回新值。 */
    public int decReference() {
        return --references;
    }

    /** 返回拓扑快照时间戳。 */
    public long getTime() {
        return time;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClusterPartition that = (ClusterPartition) o;
        return Objects.equals(nodeId, that.nodeId);
    }

    @Override
    public String toString() {
        return "ClusterPartition [nodeId=" + nodeId + ", masterFail=" + masterFail + ", masterAddress=" + masterAddress
                + ", slaveAddresses=" + slaveAddresses + ", failedSlaves=" + failedSlaves + ", slotRanges=" + slotRanges
                + "]";
    }
    
}
