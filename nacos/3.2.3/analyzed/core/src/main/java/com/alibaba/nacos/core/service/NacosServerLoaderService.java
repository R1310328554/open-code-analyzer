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

package com.alibaba.nacos.core.service;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.response.ServerLoaderMetric;
import com.alibaba.nacos.api.model.response.ServerLoaderMetrics;
import com.alibaba.nacos.api.remote.RequestCallBack;
import com.alibaba.nacos.api.remote.request.RequestMeta;
import com.alibaba.nacos.api.remote.request.ServerLoaderInfoRequest;
import com.alibaba.nacos.api.remote.request.ServerReloadRequest;
import com.alibaba.nacos.api.remote.response.Response;
import com.alibaba.nacos.api.remote.response.ServerLoaderInfoResponse;
import com.alibaba.nacos.core.cluster.Member;
import com.alibaba.nacos.core.cluster.ServerMemberManager;
import com.alibaba.nacos.core.cluster.remote.ClusterRpcClientProxy;
import com.alibaba.nacos.core.remote.Connection;
import com.alibaba.nacos.core.remote.ConnectionManager;
import com.alibaba.nacos.core.remote.core.ServerLoaderInfoRequestHandler;
import com.alibaba.nacos.core.remote.core.ServerReloaderRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Nacos 服务端连接负载管理服务，支持客户端连接迁移与集群级智能均衡。
 *
 * <p>通过 {@link ConnectionManager} 执行单连接/批量重载，并聚合各节点 SDK 连接指标实现 {@link #smartReload(float)}。</p>
 *
 * @author xiweng.yy
 */
@Service
public class NacosServerLoaderService {
    
    /** 本类日志记录器。 */
    private static final Logger LOGGER = LoggerFactory.getLogger(NacosServerLoaderService.class);
    
    /** 长连接管理器，执行连接重载。 */
    private final ConnectionManager connectionManager;
    
    /** 集群成员管理器。 */
    private final ServerMemberManager serverMemberManager;
    
    /** 集群 RPC 客户端代理，向其他节点发起异步请求。 */
    private final ClusterRpcClientProxy clusterRpcClientProxy;
    
    /** 本节点连接重载请求处理器。 */
    private final ServerReloaderRequestHandler serverReloaderRequestHandler;
    
    /** 本节点负载指标查询处理器。 */
    private final ServerLoaderInfoRequestHandler serverLoaderInfoRequestHandler;
    
    /** 构造注入连接管理、集群成员与 RPC 相关依赖。 */
    public NacosServerLoaderService(ConnectionManager connectionManager,
        ServerMemberManager serverMemberManager,
        ClusterRpcClientProxy clusterRpcClientProxy,
        ServerReloaderRequestHandler serverReloaderRequestHandler,
        ServerLoaderInfoRequestHandler serverLoaderInfoRequestHandler) {
        this.connectionManager = connectionManager;
        this.serverMemberManager = serverMemberManager;
        this.clusterRpcClientProxy = clusterRpcClientProxy;
        this.serverReloaderRequestHandler = serverReloaderRequestHandler;
        this.serverLoaderInfoRequestHandler = serverLoaderInfoRequestHandler;
    }
    
    /**
     * 获取当前所有 gRPC 长连接客户端（2.0+ 客户端）。
     *
     * @return 连接 ID 到 {@link Connection} 的映射
     */
    public Map<String, Connection> getAllClients() {
        return connectionManager.currentClients();
    }
    
    /**
     * 将指定客户端连接重载到其他服务端节点。
     *
     * @param connectionId    待迁移的连接 ID
     * @param redirectAddress 目标节点地址，为空则随机选择其他节点
     */
    public void reloadClient(String connectionId, String redirectAddress) {
        connectionManager.loadSingle(connectionId, redirectAddress);
    }
    
    /**
     * 按剩余连接数目标批量重载客户端到其他节点。
     *
     * @param count           期望保留的连接数，超出部分将被迁移
     * @param redirectAddress 目标节点地址，为空则随机选择
     */
    public void reloadCount(int count, String redirectAddress) {
        connectionManager.loadCount(count, redirectAddress);
    }
    
    /**
     * 根据集群各节点 SDK 连接总数，智能均衡各节点连接数。
     * <p>
     * 以全集群平均连接数为基准，按 {@code loaderFactor} 计算上下限：
     * 下限 = 平均 × (1 - loaderFactor)，上限 = 平均 × (1 + loaderFactor)。
     * 超出上限的节点向低于下限的节点迁移连接。
     * </p>
     *
     * @param loaderFactor 负载因子，默认 0.1，取值范围 [0, 1]
     * @return {@code true} 全部重载任务成功，{@code false} 存在失败
     */
    public boolean smartReload(float loaderFactor) {
        ServerLoaderMetrics serverLoadMetrics = getServerLoaderMetrics();
        List<ServerLoaderMetric> details = serverLoadMetrics.getDetail();
        int overLimitCount = (int) (serverLoadMetrics.getAvg() * (1 + loaderFactor));
        int lowLimitCount = (int) (serverLoadMetrics.getAvg() * (1 - loaderFactor));
        List<ServerLoaderMetric> overLimitServer = details.stream()
            .filter(metric -> metric.getSdkConCount() > overLimitCount)
            .collect(Collectors.toList());
        List<ServerLoaderMetric> lowLimitServer = details.stream()
            .filter(metric -> metric.getSdkConCount() < lowLimitCount).collect(Collectors.toList());
        overLimitServer
            .sort(Comparator.comparingInt(ServerLoaderMetric::getSdkConCount).reversed());
        LOGGER.info("Over load limit server list ={}", overLimitServer);
        lowLimitServer.sort(Comparator.comparingInt(ServerLoaderMetric::getSdkConCount));
        LOGGER.info("Low load limit server list ={}", lowLimitServer);
        AtomicBoolean result = new AtomicBoolean(true);
        
        for (int i = 0; i < overLimitServer.size() && i < lowLimitServer.size(); i++) {
            ServerReloadRequest serverLoaderInfoRequest = new ServerReloadRequest();
            serverLoaderInfoRequest.setReloadCount(overLimitCount);
            serverLoaderInfoRequest.setReloadServer(lowLimitServer.get(i).getAddress());
            Member member = serverMemberManager.find(overLimitServer.get(i).getAddress());
            
            LOGGER.info("Reload task submit ,fromServer ={},toServer={}, ",
                overLimitServer.get(i).getAddress(),
                lowLimitServer.get(i).getAddress());
            
            if (serverMemberManager.getSelf().equals(member)) {
                try {
                    serverReloaderRequestHandler.handle(serverLoaderInfoRequest, new RequestMeta());
                } catch (NacosException e) {
                    LOGGER.error("Fail to loader self server", e);
                    result.set(false);
                }
            } else {
                
                try {
                    clusterRpcClientProxy.asyncRequest(member, serverLoaderInfoRequest,
                        new RequestCallBack() {
                            
                            @Override
                            public Executor getExecutor() {
                                return null;
                            }
                            
                            @Override
                            public long getTimeout() {
                                return 100L;
                            }
                            
                            @Override
                            public void onResponse(Response response) {
                                if (response == null || !response.isSuccess()) {
                                    LOGGER.error("Fail to loader member={},response={}",
                                        member.getAddress(), response);
                                    result.set(false);
                                    
                                }
                            }
                            
                            @Override
                            public void onException(Throwable e) {
                                LOGGER.error("Fail to loader member={}", member.getAddress(), e);
                                result.set(false);
                            }
                        });
                } catch (NacosException e) {
                    LOGGER.error("Fail to loader member={}", member.getAddress(), e);
                    result.set(false);
                }
            }
        }
        
        return result.get();
    }
    
    /**
     * 聚合集群各节点 SDK 连接负载指标。
     *
     * @return 含明细、最大/最小/平均连接数等的 {@link ServerLoaderMetrics}
     */
    public ServerLoaderMetrics getServerLoaderMetrics() {
        List<ServerLoaderMetric> responseList = new CopyOnWriteArrayList<>();
        int memberSize = serverMemberManager.allMembersWithoutSelf().size();
        CountDownLatch countDownLatch = new CountDownLatch(memberSize);
        for (Member member : serverMemberManager.allMembersWithoutSelf()) {
            ServerLoaderInfoRequest serverLoaderInfoRequest = new ServerLoaderInfoRequest();
            ServerLoaderMetricCallBack callBack =
                new ServerLoaderMetricCallBack(member, responseList, countDownLatch);
            try {
                clusterRpcClientProxy.asyncRequest(member, serverLoaderInfoRequest, callBack);
            } catch (NacosException e) {
                LOGGER.error("Get metrics fail,member={}", member.getAddress(), e);
                countDownLatch.countDown();
            }
        }
        responseList.add(getSelfServerLoaderMetric());
        waitAsyncGetLoaderMetricFinish(countDownLatch);
        int max =
            responseList.stream().mapToInt(ServerLoaderMetric::getSdkConCount).max().orElse(0);
        int min =
            responseList.stream().mapToInt(ServerLoaderMetric::getSdkConCount).min().orElse(0);
        int total = responseList.stream().mapToInt(ServerLoaderMetric::getSdkConCount).sum();
        responseList.sort(Comparator.comparing(ServerLoaderMetric::getAddress));
        return buildMetrics(responseList, max, min, total);
    }
    
    /** 获取本节点 SDK 连接负载指标。 */
    private ServerLoaderMetric getSelfServerLoaderMetric() {
        ServerLoaderMetric.Builder builder = ServerLoaderMetric.Builder.newBuilder();
        builder.withAddress(serverMemberManager.getSelf().getAddress());
        try {
            ServerLoaderInfoResponse handle =
                serverLoaderInfoRequestHandler.handle(new ServerLoaderInfoRequest(),
                    new RequestMeta());
            builder.convertFromMap(handle.getLoaderMetrics());
        } catch (NacosException e) {
            LOGGER.error("Get self metrics fail", e);
        }
        return builder.build();
    }
    
    /** 等待异步拉取其他节点指标完成（最多 1 秒）。 */
    private void waitAsyncGetLoaderMetricFinish(CountDownLatch countDownLatch) {
        try {
            countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            LOGGER.warn("Get  metrics timeout,metrics info may not complete.");
        }
    }
    
    /** 根据各节点指标构建汇总 {@link ServerLoaderMetrics}。 */
    private ServerLoaderMetrics buildMetrics(List<ServerLoaderMetric> responseList, int max,
        int min, int total) {
        ServerLoaderMetrics serverLoaderMetrics = new ServerLoaderMetrics();
        serverLoaderMetrics.setDetail(responseList);
        serverLoaderMetrics.setMemberCount(serverMemberManager.allMembers().size());
        serverLoaderMetrics.setMetricsCount(responseList.size());
        serverLoaderMetrics
            .setCompleted(responseList.size() == serverMemberManager.allMembers().size());
        serverLoaderMetrics.setMax(max);
        serverLoaderMetrics.setMin(min);
        serverLoaderMetrics.setAvg(total / responseList.size());
        serverLoaderMetrics.setThreshold(String.valueOf(serverLoaderMetrics.getAvg() * 1.1d));
        serverLoaderMetrics.setTotal(total);
        return serverLoaderMetrics;
    }
    
    /** 异步拉取远程节点负载指标的回调实现。 */
    private static class ServerLoaderMetricCallBack implements RequestCallBack<Response> {
        
        private final Member member;
        
        private final List<ServerLoaderMetric> responseList;
        
        private final CountDownLatch countDownLatch;
        
        private ServerLoaderMetricCallBack(Member member, List<ServerLoaderMetric> responseList,
            CountDownLatch countDownLatch) {
            this.member = member;
            this.responseList = responseList;
            this.countDownLatch = countDownLatch;
        }
        
        @Override
        public Executor getExecutor() {
            return null;
        }
        
        @Override
        public long getTimeout() {
            return 200L;
        }
        
        @Override
        public void onResponse(Response response) {
            if (response instanceof ServerLoaderInfoResponse) {
                ServerLoaderMetric.Builder builder = ServerLoaderMetric.Builder.newBuilder();
                builder.withAddress(member.getAddress())
                    .convertFromMap(((ServerLoaderInfoResponse) response).getLoaderMetrics());
                responseList.add(builder.build());
            }
            countDownLatch.countDown();
        }
        
        @Override
        public void onException(Throwable e) {
            LOGGER.error("Get metrics fail,member={}", member.getAddress(), e);
            countDownLatch.countDown();
        }
    }
}
