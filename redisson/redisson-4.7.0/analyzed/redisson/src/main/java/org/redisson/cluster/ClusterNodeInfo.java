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

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.redisson.misc.RedisURI;

/**
 * {@code CLUSTER NODES} 回复中单个节点的解析结果。
 * <p>
 * 由 {@link org.redisson.client.protocol.decoder.ClusterNodesDecoder} 填充，
 * 包含节点 ID、地址、角色标志、主从关系及负责的槽位区间。
 *
 * @author Nikita Koksharov
 *
 */
public class ClusterNodeInfo {

    /** Redis 集群节点在 {@code CLUSTER NODES} 输出中的标志位。 */
    public enum Flag {
        NOFLAGS("noflags"), SLAVE("slave"), MASTER("master"), MYSELF("myself"),
        FAIL("fail"), EVENTUAL_FAIL("fail?"), HANDSHAKE("handshake"), NOADDR("noaddr");

        /** 在 CLUSTER NODES 文本中出现的标志字符串。 */
        private final String value;

        Flag(String value) {
            this.value = value;
        }

        /** 返回标志的原始文本值。 */
        public String getValue() {
            return value;
        }
    };

    /** 原始节点信息行，便于调试。 */
    private final String nodeInfo;
    
    /** 40 字符节点 ID。 */
    private String nodeId;
    /** 客户端连接地址（含 scheme）。 */
    private RedisURI address;
    /** 节点当前角色与状态标志集合。 */
    private final Set<Flag> flags = EnumSet.noneOf(Flag.class);
    /** 若为从节点，则为主节点 ID；主节点时为 {@code null}。 */
    private String slaveOf;
    /** 可选的主机名（CLUSTER NODES 第二列逗号后缀）。 */
    private String hostName;

    /** 该节点负责或迁移中的槽位区间。 */
    private final Set<ClusterSlotRange> slotRanges = new HashSet<>();

    /** @param nodeInfo 原始 CLUSTER NODES 行文本 */
    public ClusterNodeInfo(String nodeInfo) {
        this.nodeInfo = nodeInfo;
    }
    
    public String getNodeId() {
        return nodeId;
    }
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public RedisURI getAddress() {
        return address;
    }
    /** 将地址字符串包装为 {@link RedisURI}。 */
    public void setAddress(String address) {
        this.address = new RedisURI(address);
    }

    /** 追加该节点负责的一个槽位区间。 */
    public void addSlotRange(ClusterSlotRange range) {
        slotRanges.add(range);
    }
    /** 返回不可变的槽位区间集合。 */
    public Set<ClusterSlotRange> getSlotRanges() {
        return Collections.unmodifiableSet(slotRanges);
    }

    /** 检查是否包含指定标志。 */
    public boolean containsFlag(Flag flag) {
        return flags.contains(flag);
    }
    /** 添加节点标志。 */
    public void addFlag(Flag flag) {
        this.flags.add(flag);
    }

    public String getSlaveOf() {
        return slaveOf;
    }
    public void setSlaveOf(String slaveOf) {
        this.slaveOf = slaveOf;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /** 返回原始 CLUSTER NODES 行。 */
    public String getNodeInfo() {
        return nodeInfo;
    }
    
    @Override
    public String toString() {
        return "ClusterNodeInfo [nodeId=" + nodeId + ", address=" + address + ", flags=" + flags
                + ", slaveOf=" + slaveOf + ", hostName=" + hostName + ", slotRanges=" + slotRanges + "]";
    }

}
