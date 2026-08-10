/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.console.handler.impl.remote;

import com.alibaba.nacos.api.common.NodeState;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.runtime.NacosRuntimeException;
import com.alibaba.nacos.api.model.response.NacosMember;
import com.alibaba.nacos.auth.config.NacosAuthConfig;
import com.alibaba.nacos.auth.config.NacosAuthConfigHolder;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.console.config.NacosConsoleAuthConfig;
import com.alibaba.nacos.console.handler.core.ClusterHandler;
import com.alibaba.nacos.core.cluster.Member;
import com.alibaba.nacos.core.cluster.NacosMemberManager;
import com.alibaba.nacos.sys.env.EnvUtil;
import org.apache.hc.core5.http.HttpRequest;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Console 远程 HTTP 转发公共连接器：提供健康节点选择、认证头注入与 context-path 解析。
 * Common connector for remote server operations in console deployment mode.
 *
 * <p>Provides shared functionality for remote HTTP forwarding services, including
 * healthy member selection, authentication identity injection, and server context path resolution.</p>
 *
 * @author nacos
 */
@Component
@EnabledRemoteHandler
public class RemoteServerConnector {
    
    /** 本地集群成员管理器 */
    private final NacosMemberManager memberManager;
    
    /** 远程集群 Handler，用于查询节点 UP/DOWN 状态 */
    private final ClusterHandler remoteClusterHandler;
    
    /** 注入成员管理器与集群 Handler */
    public RemoteServerConnector(NacosMemberManager memberManager,
        ClusterHandler remoteClusterHandler) {
        this.memberManager = memberManager;
        this.remoteClusterHandler = remoteClusterHandler;
    }
    
    /**
     * 向 HTTP 请求注入服务端身份认证头。
     *
     * @param request the HTTP request to add auth headers to
     */
    public void addAuthIdentity(HttpRequest request) {
        NacosAuthConfig authConfig = NacosAuthConfigHolder.getInstance()
            .getNacosAuthConfigByScope(NacosConsoleAuthConfig.NACOS_CONSOLE_AUTH_SCOPE);
        if (StringUtils.isNotBlank(authConfig.getServerIdentityKey())) {
            request.setHeader(authConfig.getServerIdentityKey(),
                authConfig.getServerIdentityValue());
        }
    }
    
    /**
     * 获取远程 Nacos 服务的 context-path，默认 {@code /nacos}。
     *
     * @return server context path, defaults to "/nacos"
     */
    public String getServerContextPath() {
        return EnvUtil.getProperty("nacos.console.remote.server.context-path", "/nacos");
    }
    
    /**
     * 从集群中随机选取一个 UP 状态的健康节点。
     *
     * @return a healthy cluster member
     * @throws NacosException if no healthy server node is found
     */
    public Member randomOneHealthyMember() throws NacosException {
        Collection<Member> allMembers = memberManager.allMembers();
        Collection<? extends NacosMember> membersWithState = remoteClusterHandler.getNodeList("");
        Map<String, NodeState> nodeStateMap = membersWithState.stream()
            .collect(Collectors.toMap(NacosMember::getAddress, NacosMember::getState));
        allMembers.removeIf(node -> !NodeState.UP.equals(nodeStateMap.get(node.getAddress())));
        if (CollectionUtils.isEmpty(allMembers)) {
            throw new NacosRuntimeException(NacosException.SERVER_ERROR,
                "No healthy server node found.");
        }
        return allMembers.parallelStream().findAny().orElseThrow();
    }
}
