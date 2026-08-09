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

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.command.CommandHandler;
import com.alibaba.csp.sentinel.command.CommandRequest;
import com.alibaba.csp.sentinel.command.CommandResponse;
import com.alibaba.csp.sentinel.command.annotation.CommandMapping;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.command.vo.NodeVo;

import com.alibaba.fastjson.JSON;

/**
 * 以 JSON 数组返回从根节点开始的调用树 {@link NodeVo} 列表（前序遍历）。
 * 用于 Dashboard 或客户端可视化资源层级与实时指标。
 *
 * @author leyou
 */
@CommandMapping(name = "jsonTree", desc = "从根节点获取调用树 NodeVo JSON")
public class FetchJsonTreeCommandHandler implements CommandHandler<String> {

    @Override
    public CommandResponse<String> handle(CommandRequest request) {
        List<NodeVo> results = new ArrayList<NodeVo>();
        visit(Constants.ROOT, results, null);
        return CommandResponse.ofSuccess(JSON.toJSONString(results));
    }

    /**
     * 前序遍历调用树，将每个 {@link DefaultNode} 转为 {@link NodeVo} 并递归子节点。
     */
    private void visit(DefaultNode node, List<NodeVo> results, String parentId) {
        NodeVo vo = NodeVo.fromDefaultNode(node, parentId);
        results.add(vo);
        String id = vo.getId();
        for (Node n : node.getChildList()) {
            visit((DefaultNode)n, results, id);
        }
    }
}
