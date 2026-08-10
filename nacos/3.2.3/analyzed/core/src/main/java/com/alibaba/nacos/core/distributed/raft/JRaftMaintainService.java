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

package com.alibaba.nacos.core.distributed.raft;

import com.alibaba.nacos.common.model.RestResult;
import com.alibaba.nacos.common.model.RestResultUtils;
import com.alibaba.nacos.core.distributed.raft.utils.JRaftConstants;
import com.alibaba.nacos.core.distributed.raft.utils.JRaftOps;
import com.alipay.sofa.jraft.CliService;
import com.alipay.sofa.jraft.Node;

import java.util.Map;
import java.util.Objects;

/**
 * JRaft 运维命令服务：解析 {@link com.alibaba.nacos.core.distributed.raft.utils.JRaftConstants} 参数，对指定或全部 Raft 组执行 {@link com.alibaba.nacos.core.distributed.raft.utils.JRaftOps} 定义的 CLI 操作。
 * JRaft operations interface.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class JRaftMaintainService {
    
    /** 底层 JRaft 服务端，提供 CliService 与 Node 访问。 */
    private final JRaftServer raftServer;
    
    /**
     * 注入 JRaft 服务端。
     *
     * @param raftServer Raft 服务实例
     */
    public JRaftMaintainService(JRaftServer raftServer) {
        this.raftServer = raftServer;
    }
    
    /** 字符串数组参数入口（当前未实现）。 */
    public RestResult<String> execute(String[] args) {
        return RestResultUtils.failed("not support yet");
    }
    
    /**
     * 执行 JRaft 运维命令：若指定 groupId 则单组执行，否则遍历所有 Raft 组。
     *
     * @param args {@link Map}
     * @return {@link RestResult}
     */
    public RestResult<String> execute(Map<String, String> args) {
        final CliService cliService = raftServer.getCliService();
        if (args.containsKey(JRaftConstants.GROUP_ID)) {
            final String groupId = args.get(JRaftConstants.GROUP_ID);
            final Node node = raftServer.findNodeByGroup(groupId);
            return single(cliService, groupId, node, args);
        }
        Map<String, JRaftServer.RaftGroupTuple> tupleMap = raftServer.getMultiRaftGroup();
        for (Map.Entry<String, JRaftServer.RaftGroupTuple> entry : tupleMap.entrySet()) {
            final String group = entry.getKey();
            final Node node = entry.getValue().getNode();
            RestResult<String> result = single(cliService, group, node, args);
            if (!result.ok()) {
                return result;
            }
        }
        return RestResultUtils.success();
    }
    
    /**
     * 对单个 Raft 组解析 command 并委托 {@link com.alibaba.nacos.core.distributed.raft.utils.JRaftOps} 执行。
     *
     * @param cliService JRaft CLI 服务
     * @param groupId Raft 组 ID
     * @param node 组内 Node
     * @param args 命令参数映射
     * @return 执行结果
     */
    private RestResult<String> single(CliService cliService, String groupId, Node node,
        Map<String, String> args) {
        try {
            if (node == null) {
                return RestResultUtils.failed("not this raft group : " + groupId);
            }
            final String command = args.get(JRaftConstants.COMMAND_NAME);
            JRaftOps ops = JRaftOps.sourceOf(command);
            if (Objects.isNull(ops)) {
                return RestResultUtils.failed("Not support command : " + command);
            }
            return ops.execute(cliService, groupId, node, args);
        } catch (Throwable ex) {
            return RestResultUtils.failed(ex.getMessage());
        }
    }
    
}
