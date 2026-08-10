/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.core.cluster.remote;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.remote.RemoteConstants;
import com.alibaba.nacos.api.remote.RequestCallBack;
import com.alibaba.nacos.api.remote.request.Request;
import com.alibaba.nacos.api.remote.response.Response;
import com.alibaba.nacos.auth.config.NacosAuthConfigHolder;
import com.alibaba.nacos.auth.util.AuthHeaderUtil;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.remote.ConnectionType;
import com.alibaba.nacos.common.remote.client.RpcClient;
import com.alibaba.nacos.common.remote.client.RpcClientFactory;
import com.alibaba.nacos.common.remote.client.ServerListFactory;
import com.alibaba.nacos.common.remote.client.grpc.DefaultGrpcClientConfig;
import com.alibaba.nacos.common.remote.client.grpc.GrpcClientConfig;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.core.auth.NacosServerAuthConfig;
import com.alibaba.nacos.core.cluster.Member;
import com.alibaba.nacos.core.cluster.MemberChangeListener;
import com.alibaba.nacos.core.cluster.MembersChangeEvent;
import com.alibaba.nacos.core.cluster.ServerMemberManager;
import com.alibaba.nacos.core.utils.Loggers;
import com.alibaba.nacos.sys.env.EnvUtil;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static com.alibaba.nacos.api.exception.NacosException.CLIENT_INVALID_PARAM;

/**
 * 集群 RPC 客户端代理：为每个远程成员维护 gRPC 连接，并在 {@link MembersChangeEvent} 时刷新客户端池。
 * cluster rpc client proxy.
 *
 * @author liuzunfei
 * @version $Id: ClusterRpcClientProxy.java, v 0.1 2020年08月11日 2:11 PM liuzunfei Exp $
 */
@Service
public class ClusterRpcClientProxy extends MemberChangeListener {
    
    private static final long DEFAULT_REQUEST_TIME_OUT = 3000L;
    
    /** 集群成员管理器，提供成员列表与变更事件源。 */
    final ServerMemberManager serverMemberManager;
    
    public ClusterRpcClientProxy(ServerMemberManager serverMemberManager) {
        this.serverMemberManager = serverMemberManager;
    }
    
    /**
     * 构造完成后订阅成员变更并初始化 RPC 客户端。
     */
    @PostConstruct
    public void init() {
        try {
            NotifyCenter.registerSubscriber(this);
            List<Member> members = serverMemberManager.allMembersWithoutSelf();
            refresh(members);
            Loggers.CLUSTER.info(
                "[ClusterRpcClientProxy] success to refresh cluster rpc client on start up,members ={} ",
                members);
        } catch (NacosException e) {
            Loggers.CLUSTER.warn("[ClusterRpcClientProxy] fail to refresh cluster rpc client,{} ",
                e.getMessage());
        }
        
    }
    
    /**
     * 为给定成员创建或复用 RPC 客户端，并清理已离开节点的客户端。
     *
     * @param members 当前集群成员（不含本机）
     */
    private void refresh(List<Member> members) throws NacosException {
        
        // 确保为新成员创建客户端
        for (Member member : members) {
            createRpcClientAndStart(member, ConnectionType.GRPC);
        }
        
        // 关闭并移除已离开成员的客户端
        Set<Map.Entry<String, RpcClient>> allClientEntrys = RpcClientFactory.getAllClientEntries();
        Iterator<Map.Entry<String, RpcClient>> iterator = allClientEntrys.iterator();
        List<String> newMemberKeys =
            members.stream().map(this::memberClientKey).collect(Collectors.toList());
        while (iterator.hasNext()) {
            Map.Entry<String, RpcClient> next1 = iterator.next();
            if (next1.getKey().startsWith("Cluster-") && !newMemberKeys.contains(next1.getKey())) {
                Loggers.CLUSTER.info("member leave,destroy client of member - > : {}",
                    next1.getKey());
                RpcClient client = RpcClientFactory.getClient(next1.getKey());
                if (client != null) {
                    RpcClientFactory.getClient(next1.getKey()).shutdown();
                }
                iterator.remove();
            }
        }
        
    }
    
    private String memberClientKey(Member member) {
        return "Cluster-" + member.getAddress();
    }
    
    private void createRpcClientAndStart(Member member, ConnectionType type) throws NacosException {
        Map<String, String> labels = new HashMap<>(2);
        labels.put(RemoteConstants.LABEL_SOURCE, RemoteConstants.LABEL_SOURCE_CLUSTER);
        String memberClientKey = memberClientKey(member);
        RpcClient client = buildRpcClient(type, labels, memberClientKey);
        if (!client.getConnectionType().equals(type)) {
            Loggers.CLUSTER.info("connection type changed, destroy client of member - > : {}",
                member);
            RpcClientFactory.destroyClient(memberClientKey);
            client = buildRpcClient(type, labels, memberClientKey);
        }
        
        if (client.isWaitInitiated()) {
            Loggers.CLUSTER.info("start a new rpc client to member -> : {}", member);
            
            // 单固定服务端地址的 ServerListFactory
            client.serverListFactory(new ServerListFactory() {
                
                @Override
                public String genNextServer() {
                    return member.getAddress();
                }
                
                @Override
                public String getCurrentServer() {
                    return member.getAddress();
                }
                
                @Override
                public List<String> getServerList() {
                    return CollectionUtils.list(member.getAddress());
                }
            });
            
            client.start();
        }
    }
    
    /**
     * 基于 {@link EnvUtil#getAvailableProcessors(int)} 配置集群 gRPC 客户端线程池。
     */
    private RpcClient buildRpcClient(ConnectionType type, Map<String, String> labels,
        String memberClientKey) {
        Properties properties = EnvUtil.getProperties();
        GrpcClientConfig clientConfig =
            DefaultGrpcClientConfig.newBuilder().buildClusterFromProperties(properties)
                .setLabels(labels).setName(memberClientKey)
                .setThreadPoolCoreSize(EnvUtil.getAvailableProcessors(2))
                .setThreadPoolMaxSize(EnvUtil.getAvailableProcessors(8)).build();
        return RpcClientFactory.createClusterClient(memberClientKey, type, clientConfig);
    }
    
    /**
     * 向指定成员同步发送 RPC 请求（默认超时 3s）。
     *
     * @param member  目标成员
     * @param request 请求体
     * @return 响应
     * @throws NacosException 无客户端或发送失败时抛出
     */
    public Response sendRequest(Member member, Request request) throws NacosException {
        return sendRequest(member, request, DEFAULT_REQUEST_TIME_OUT);
    }
    
    /**
     * send request to member.
     *
     * @param member  member of server.
     * @param request request.
     * @return Response response.
     * @throws NacosException exception may throws.
      * <p>集群 RPC 客户端代理；详见类级说明。</p>
     */
    public Response sendRequest(Member member, Request request, long timeoutMills)
        throws NacosException {
        RpcClient client = RpcClientFactory.getClient(memberClientKey(member));
        if (client != null) {
            injectorServerIdentity(request);
            return client.request(request, timeoutMills);
        } else {
            throw new NacosException(CLIENT_INVALID_PARAM,
                "No rpc client related to member: " + member);
        }
    }
    
    /**
     * 异步向成员发送 RPC 请求并通过回调接收结果。
     *
     * @param member   目标成员
     * @param request  请求体
     * @param callBack 异步回调
     * @throws NacosException 无客户端时抛出
     */
    public void asyncRequest(Member member, Request request, RequestCallBack callBack)
        throws NacosException {
        RpcClient client = RpcClientFactory.getClient(memberClientKey(member));
        if (client != null) {
            injectorServerIdentity(request);
            client.asyncRequest(request, callBack);
        } else {
            throw new NacosException(CLIENT_INVALID_PARAM,
                "No rpc client related to member: " + member);
        }
    }
    
    /**
     * 向除本机外的全部成员广播请求。
     *
     * @param request 请求体
     * @throws NacosException 任一发送失败时抛出
     */
    public void sendRequestToAllMembers(Request request) throws NacosException {
        List<Member> members = serverMemberManager.allMembersWithoutSelf();
        for (Member member1 : members) {
            sendRequest(member1, request);
        }
    }
    
    @Override
    public void onEvent(MembersChangeEvent event) {
        try {
            List<Member> members = serverMemberManager.allMembersWithoutSelf();
            refresh(members);
        } catch (NacosException e) {
            Loggers.CLUSTER.warn(
                "[serverlist] fail to refresh cluster rpc client, event:{}, msg: {} ", event,
                e.getMessage());
        }
    }
    
    /**
     * 检查目标成员的 RPC 客户端是否已连接就绪。
     *
     * @param member 目标成员
     * @return 已连接返回 {@code true}
     */
    public boolean isRunning(Member member) {
        RpcClient client = RpcClientFactory.getClient(memberClientKey(member));
        if (null == client) {
            return false;
        }
        return client.isRunning();
    }
    
    private void injectorServerIdentity(Request request) {
        AuthHeaderUtil.addIdentityToHeader(request, NacosAuthConfigHolder.getInstance()
            .getNacosAuthConfigByScope(NacosServerAuthConfig.NACOS_SERVER_AUTH_SCOPE));
    }
}
