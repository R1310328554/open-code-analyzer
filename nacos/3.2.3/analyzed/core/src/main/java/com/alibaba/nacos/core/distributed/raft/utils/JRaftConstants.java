/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.core.distributed.raft.utils;

/**
 * JRaft 运维命令与日志扩展字段常量：供 {@link JRaftOps}、CLI 请求参数及日志附加信息使用。
 * constant.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class JRaftConstants {
    
    /** 日志扩展信息键，取 {@link JRaftLogOperation} 全限定类名。 */
    public static final String JRAFT_EXTEND_INFO_KEY = JRaftLogOperation.class.getCanonicalName();
    
    /** Raft 组 ID 参数名。 */
    public static final String GROUP_ID = "groupId";
    
    /** 运维命令名称参数键。 */
    public static final String COMMAND_NAME = "command";
    
    /** 运维命令参数值键。 */
    public static final String COMMAND_VALUE = "value";
    
    /** 转移 Leader 命令标识。 */
    public static final String TRANSFER_LEADER = "transferLeader";
    
    /** 重置 Raft 集群成员（changePeers）命令标识。 */
    public static final String RESET_RAFT_CLUSTER = "restRaftCluster";
    
    /** 触发快照命令标识。 */
    public static final String DO_SNAPSHOT = "doSnapshot";
    
    /** 移除单个 Peer 命令标识。 */
    public static final String REMOVE_PEER = "removePeer";
    
    /** 批量移除 Peer 命令标识。 */
    public static final String REMOVE_PEERS = "removePeers";
    
    /** 变更集群成员列表命令标识。 */
    public static final String CHANGE_PEERS = "changePeers";
    
    /**
     * 紧急 resetPeers 命令标识（仅在可用性优先于一致性时使用）。
     */
    public static final String RESET_PEERS = "resetPeers";
    
}
