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
package com.alibaba.csp.sentinel.command.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.csp.sentinel.command.CommandHandler;
import com.alibaba.csp.sentinel.command.CommandRequest;
import com.alibaba.csp.sentinel.command.CommandResponse;
import com.alibaba.csp.sentinel.command.annotation.CommandMapping;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.command.vo.NodeVo;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import com.alibaba.fastjson.JSONArray;

/**
 * 返回全部 ClusterNode 的 {@link NodeVo} 列表（JSON）。
 * 参数 {@code type=notZero} 时可过滤 totalRequest &lt;= 0 的节点。
 *
 * @author jialiang.linjl
 */
@CommandMapping(name = "clusterNode", desc = "获取全部 ClusterNode VO，type=notZero 可忽略无流量节点")
public class FetchSimpleClusterNodeCommandHandler implements CommandHandler<String> {

    @Override
    public CommandResponse<String> handle(CommandRequest request) {
        /* type=notZero 时忽略 totalRequest <= 0 的节点 */
        String type = request.getParam("type");
        List<NodeVo> list = new ArrayList<NodeVo>();
        Map<ResourceWrapper, ClusterNode> map = ClusterBuilderSlot.getClusterNodeMap();
        if (map == null) {
            return CommandResponse.ofSuccess(JSONArray.toJSONString(list));
        }
        for (Map.Entry<ResourceWrapper, ClusterNode> entry : map.entrySet()) {
            if ("notZero".equalsIgnoreCase(type)) {
                if (entry.getValue().totalRequest() > 0) {
                    list.add(NodeVo.fromClusterNode(entry.getKey(), entry.getValue()));
                }
            } else {
                list.add(NodeVo.fromClusterNode(entry.getKey(), entry.getValue()));
            }
        }
        return CommandResponse.ofSuccess(JSONArray.toJSONString(list));
    }

}
