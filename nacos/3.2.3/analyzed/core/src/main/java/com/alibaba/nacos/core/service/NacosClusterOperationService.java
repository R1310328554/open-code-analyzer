/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.core.service;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.utils.LoggerUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.core.cluster.Member;
import com.alibaba.nacos.api.common.NodeState;
import com.alibaba.nacos.core.cluster.ServerMemberManager;
import com.alibaba.nacos.core.model.request.LookupUpdateRequest;
import com.alibaba.nacos.core.utils.Loggers;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Nacos 集群运维服务，提供控制台所需的成员列表、状态更新与寻址模式切换。
 *
 * <p>封装 {@link ServerMemberManager}，对外暴露集群节点查询与批量更新能力。</p>
 *
 * @author dongyafei
 * @date 2022/8/15
 */
@Service
public class NacosClusterOperationService {
    
    /** 集群成员管理器。 */
    private final ServerMemberManager memberManager;
    
    /** 注入集群成员管理器。 */
    public NacosClusterOperationService(ServerMemberManager memberManager) {
        this.memberManager = memberManager;
    }
    
    /** 返回当前节点在集群中的 {@link Member} 信息。 */
    public Member self() {
        return memberManager.getSelf();
    }
    
    /**
     * 控制台展示集群成员列表，可按地址前缀与节点状态过滤。
     *
     * @param address   地址前缀过滤，空则不过滤
     * @param nodeState 节点状态过滤，{@code null} 则不过滤
     * @return 符合条件的成员集合
     * @throws NacosException 查询失败时抛出
     */
    public Collection<Member> listNodes(String address, NodeState nodeState) throws NacosException {
        
        Collection<Member> members = memberManager.allMembers();
        Collection<Member> result = new ArrayList<>();
        
        for (Member member : members) {
            if (StringUtils.isNoneBlank(address)
                && !StringUtils.startsWith(member.getAddress(), address)) {
                continue;
            }
            if (nodeState != null && member.getState() != nodeState) {
                continue;
            }
            result.add(member);
        }
        return result;
    }
    
    /**
     * 批量更新集群成员信息，非法节点会被跳过。
     *
     * @param nodes 待更新的成员列表
     * @return 固定返回 {@code true}
     */
    public Boolean updateNodes(List<Member> nodes) {
        for (Member node : nodes) {
            if (!node.check()) {
                LoggerUtils.printIfWarnEnabled(Loggers.CLUSTER,
                    "node information is illegal, ignore node: {}", node);
                continue;
            }
            
            LoggerUtils.printIfDebugEnabled(Loggers.CLUSTER, "node state updating, node: {}", node);
            node.setState(NodeState.UP);
            node.setFailAccessCnt(0);
            
            boolean update = memberManager.update(node);
            if (!update) {
                LoggerUtils.printIfErrorEnabled(Loggers.CLUSTER,
                    "node state update failed, node: {}", node);
            }
        }
        return true;
    }
    
    /**
     * 切换集群寻址模式（如 file / address-server）。
     *
     * @param request 含目标寻址类型的请求
     * @return 固定返回 {@code true}
     * @throws NacosException 切换失败时抛出
     */
    public Boolean updateLookup(LookupUpdateRequest request) throws NacosException {
        memberManager.switchLookup(request.getType());
        return true;
    }
    
    /** 查询当前节点健康状态（返回 {@link NodeState} 名称字符串）。 */
    public String selfHealth() {
        return memberManager.getSelf().getState().name();
    }
}
