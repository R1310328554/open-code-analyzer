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

import com.alibaba.nacos.common.model.RestResult;
import com.alibaba.nacos.common.model.RestResultUtils;
import com.alipay.sofa.jraft.CliService;
import com.alipay.sofa.jraft.JRaftUtils;
import com.alipay.sofa.jraft.Node;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.entity.PeerId;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * JRaft 运维命令枚举：将 HTTP/CLI 命令名映射为 {@link com.alipay.sofa.jraft.CliService} 调用，返回统一 {@link com.alibaba.nacos.common.model.RestResult}。
 * jraft maintain service.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
@SuppressWarnings("all")
public enum JRaftOps {
    
    /** 将指定 Peer 设为 Leader。 */
    TRANSFER_LEADER(JRaftConstants.TRANSFER_LEADER) {
        
        @Override
        public RestResult<String> execute(CliService cliService, String groupId, Node node,
            Map<String, String> args) {
            final Configuration conf = node.getOptions().getInitialConf();
            final PeerId leader = PeerId.parsePeer(args.get(JRaftConstants.COMMAND_VALUE));
            Status status = cliService.transferLeader(groupId, conf, leader);
            if (status.isOk()) {
                return RestResultUtils.success();
            }
            return RestResultUtils.failed(status.getErrorMsg());
        }
    },
    
    /** 通过 changePeers 重置整个 Raft 集群配置。 */
    RESET_RAFT_CLUSTER(JRaftConstants.RESET_RAFT_CLUSTER) {
        
        @Override
        public RestResult<String> execute(CliService cliService, String groupId, Node node,
            Map<String, String> args) {
            final Configuration conf = node.getOptions().getInitialConf();
            final String peerIds = args.get(JRaftConstants.COMMAND_VALUE);
            Configuration newConf = JRaftUtils.getConfiguration(peerIds);
            Status status = cliService.changePeers(groupId, conf, newConf);
            if (status.isOk()) {
                return RestResultUtils.success();
            }
            return RestResultUtils.failed(status.getErrorMsg());
        }
    },
    
    /** 对指定 Peer 触发快照。 */
    DO_SNAPSHOT(JRaftConstants.DO_SNAPSHOT) {
        
        @Override
        public RestResult<String> execute(CliService cliService, String groupId, Node node,
            Map<String, String> args) {
            final Configuration conf = node.getOptions().getInitialConf();
            final PeerId peerId = PeerId.parsePeer(args.get(JRaftConstants.COMMAND_VALUE));
            Status status = cliService.snapshot(groupId, peerId);
            if (status.isOk()) {
                return RestResultUtils.success();
            }
            return RestResultUtils.failed(status.getErrorMsg());
        }
    },
    
    /** 从集群移除单个 Peer（已不存在则视为成功）。 */
    REMOVE_PEER(JRaftConstants.REMOVE_PEER) {
        
        @Override
        public RestResult<String> execute(CliService cliService, String groupId, Node node,
            Map<String, String> args) {
            final Configuration conf = node.getOptions().getInitialConf();
            
            List<PeerId> peerIds = cliService.getPeers(groupId, conf);
            
            final PeerId waitRemove = PeerId.parsePeer(args.get(JRaftConstants.COMMAND_VALUE));
            
            if (!peerIds.contains(waitRemove)) {
                return RestResultUtils.success();
            }
            
            Status status = cliService.removePeer(groupId, conf, waitRemove);
            if (status.isOk()) {
                return RestResultUtils.success();
            }
            return RestResultUtils.failed(status.getErrorMsg());
        }
    },
    
    /** 按逗号分隔批量移除 Peer。 */
    REMOVE_PEERS(JRaftConstants.REMOVE_PEERS) {
        
        @Override
        public RestResult<String> execute(CliService cliService, String groupId, Node node,
            Map<String, String> args) {
            final Configuration conf = node.getOptions().getInitialConf();
            final String peers = args.get(JRaftConstants.COMMAND_VALUE);
            for (String s : peers.split(",")) {
                
                List<PeerId> peerIds = cliService.getPeers(groupId, conf);
                final PeerId waitRemove = PeerId.parsePeer(s);
                
                if (!peerIds.contains(waitRemove)) {
                    continue;
                }
                
                Status status = cliService.removePeer(groupId, conf, waitRemove);
                if (!status.isOk()) {
                    return RestResultUtils.failed(status.getErrorMsg());
                }
            }
            return RestResultUtils.success();
        }
    },
    
    /** 将集群成员替换为新 Peer 列表（配置未变则直接成功）。 */
    CHANGE_PEERS(JRaftConstants.CHANGE_PEERS) {
        
        @Override
        public RestResult<String> execute(CliService cliService, String groupId, Node node,
            Map<String, String> args) {
            final Configuration conf = node.getOptions().getInitialConf();
            final Configuration newConf = new Configuration();
            String peers = args.get(JRaftConstants.COMMAND_VALUE);
            for (String peer : peers.split(",")) {
                newConf.addPeer(PeerId.parsePeer(peer.trim()));
            }
            
            if (Objects.equals(conf, newConf)) {
                return RestResultUtils.success();
            }
            
            Status status = cliService.changePeers(groupId, conf, newConf);
            if (status.isOk()) {
                return RestResultUtils.success();
            }
            return RestResultUtils.failed(status.getErrorMsg());
        }
    },
    
    /**
     * 紧急 resetPeers：在本地节点上强制重置成员列表。
     * <p>
     * 仅在可用性优先于一致性的极端场景使用！
     * https://www.sofastack.tech/projects/sofa-jraft/jraft-user-guide/#7.3
     * </p>
     */
    RESET_PEERS(JRaftConstants.RESET_PEERS) {
        
        @Override
        public RestResult<String> execute(CliService cliService, String groupId, Node node,
            Map<String, String> args) {
            final Configuration newConf = new Configuration();
            String peers = args.get(JRaftConstants.COMMAND_VALUE);
            for (String peer : peers.split(",")) {
                newConf.addPeer(PeerId.parsePeer(peer.trim()));
            }
            
            final PeerId nodePeerId = node.getNodeId().getPeerId();
            Status status = cliService.resetPeer(groupId, nodePeerId, newConf);
            if (status.isOk()) {
                return RestResultUtils.success();
            }
            return RestResultUtils.failed(status.getErrorMsg());
        }
    };
    
    /** 命令字符串，与 {@link JRaftConstants} 中常量对应。 */
    private String name;
    
    /** 绑定命令名。 */
    JRaftOps(String name) {
        this.name = name;
    }
    
    /**
     * 按命令字符串查找枚举，未匹配返回 null。
     *
     * @param command 命令名
     * @return 对应 {@link JRaftOps} 或 null
     */
    public static JRaftOps sourceOf(String command) {
        for (JRaftOps enums : JRaftOps.values()) {
            if (Objects.equals(command, enums.name)) {
                return enums;
            }
        }
        return null;
    }
    
    /**
     * 默认空实现（仅基类占位）；各枚举常量覆盖此方法执行具体 CLI 操作。
     *
     * @param cliService JRaft CLI 服务
     * @param groupId Raft 组 ID
     * @param node 本地 Raft 节点
     * @param args 命令参数
     * @return 运维结果
     */
    public RestResult<String> execute(CliService cliService, String groupId, Node node,
        Map<String, String> args) {
        return RestResultUtils.success();
    }
}
