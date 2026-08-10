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

package com.alibaba.nacos.naming.consistency.ephemeral.distro.v2;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.remote.RequestCallBack;
import com.alibaba.nacos.api.remote.response.Response;
import com.alibaba.nacos.api.remote.response.ResponseCode;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.consistency.DataOperation;
import com.alibaba.nacos.core.cluster.Member;
import com.alibaba.nacos.api.common.NodeState;
import com.alibaba.nacos.core.cluster.ServerMemberManager;
import com.alibaba.nacos.core.cluster.remote.ClusterRpcClientProxy;
import com.alibaba.nacos.core.distributed.distro.DistroConfig;
import com.alibaba.nacos.core.distributed.distro.component.DistroCallback;
import com.alibaba.nacos.core.distributed.distro.component.DistroTransportAgent;
import com.alibaba.nacos.core.distributed.distro.entity.DistroData;
import com.alibaba.nacos.core.distributed.distro.entity.DistroKey;
import com.alibaba.nacos.core.distributed.distro.exception.DistroException;
import com.alibaba.nacos.naming.cluster.remote.request.DistroDataRequest;
import com.alibaba.nacos.naming.cluster.remote.response.DistroDataResponse;
import com.alibaba.nacos.naming.core.v2.event.client.ClientEvent;
import com.alibaba.nacos.naming.misc.GlobalExecutor;
import com.alibaba.nacos.naming.misc.Loggers;
import com.alibaba.nacos.naming.monitor.NamingTpsMonitor;

import java.util.concurrent.Executor;

/**
 * v2 客户端 Distro 集群传输代理。
 *
 * <p>通过 {@link ClusterRpcClientProxy} 发送 {@link DistroDataRequest}，完成节点间同步、校验、查询与快照拉取。</p>
 *
 * @author xiweng.yy
 */
public class DistroClientTransportAgent implements DistroTransportAgent {
    
    /** 集群 RPC 客户端。 */
    private final ClusterRpcClientProxy clusterRpcClientProxy;
    
    /** 集群成员与节点状态管理。 */
    private final ServerMemberManager memberManager;
    
    public DistroClientTransportAgent(ClusterRpcClientProxy clusterRpcClientProxy,
        ServerMemberManager serverMemberManager) {
        this.clusterRpcClientProxy = clusterRpcClientProxy;
        this.memberManager = serverMemberManager;
    }
    
    @Override
    public boolean supportCallbackTransport() {
        return true;
    }
    
    /** 同步 Distro 数据到目标节点（同步 RPC）。 */
    @Override
    public boolean syncData(DistroData data, String targetServer) {
        if (isNoExistTarget(targetServer)) {
            return true;
        }
        DistroDataRequest request = new DistroDataRequest(data, data.getType());
        Member member = memberManager.find(targetServer);
        if (checkTargetServerStatusUnhealthy(member)) {
            Loggers.DISTRO
                .warn("[DISTRO] Cancel distro sync caused by target server {} unhealthy, key: {}",
                    targetServer,
                    data.getDistroKey());
            return false;
        }
        try {
            Response response = clusterRpcClientProxy.sendRequest(member, request);
            return checkResponse(response);
        } catch (NacosException e) {
            Loggers.DISTRO.error("[DISTRO-FAILED] Sync distro data failed! key: {}",
                data.getDistroKey(), e);
        }
        return false;
    }
    
    /** 异步同步 Distro 数据并在回调中通知结果。 */
    @Override
    public void syncData(DistroData data, String targetServer, DistroCallback callback) {
        if (isNoExistTarget(targetServer)) {
            callback.onSuccess();
            return;
        }
        DistroDataRequest request = new DistroDataRequest(data, data.getType());
        Member member = memberManager.find(targetServer);
        if (checkTargetServerStatusUnhealthy(member)) {
            Loggers.DISTRO
                .warn("[DISTRO] Cancel distro sync caused by target server {} unhealthy, key: {}",
                    targetServer,
                    data.getDistroKey());
            callback.onFailed(null);
            return;
        }
        try {
            clusterRpcClientProxy.asyncRequest(member, request,
                new DistroRpcCallbackWrapper(callback, member));
        } catch (NacosException nacosException) {
            callback.onFailed(nacosException);
        }
    }
    
    /** 向目标节点发送校验数据（同步 RPC）。 */
    @Override
    public boolean syncVerifyData(DistroData verifyData, String targetServer) {
        if (isNoExistTarget(targetServer)) {
            return true;
        }
        // 将目标地址设为本机，以便对端校验失败时能回调本节点。
        verifyData.getDistroKey().setTargetServer(memberManager.getSelf().getAddress());
        DistroDataRequest request = new DistroDataRequest(verifyData, DataOperation.VERIFY);
        Member member = memberManager.find(targetServer);
        if (checkTargetServerStatusUnhealthy(member)) {
            Loggers.DISTRO
                .warn("[DISTRO] Cancel distro verify caused by target server {} unhealthy, key: {}",
                    targetServer,
                    verifyData.getDistroKey());
            return false;
        }
        try {
            Response response = clusterRpcClientProxy.sendRequest(member, request);
            return checkResponse(response);
        } catch (NacosException e) {
            Loggers.DISTRO.error("[DISTRO-FAILED] Verify distro data failed! key: {} ",
                verifyData.getDistroKey(), e);
        }
        return false;
    }
    
    @Override
    public void syncVerifyData(DistroData verifyData, String targetServer,
        DistroCallback callback) {
        if (isNoExistTarget(targetServer)) {
            callback.onSuccess();
            return;
        }
        DistroDataRequest request = new DistroDataRequest(verifyData, DataOperation.VERIFY);
        Member member = memberManager.find(targetServer);
        if (checkTargetServerStatusUnhealthy(member)) {
            Loggers.DISTRO
                .warn("[DISTRO] Cancel distro verify caused by target server {} unhealthy, key: {}",
                    targetServer,
                    verifyData.getDistroKey());
            callback.onFailed(null);
            return;
        }
        try {
            DistroVerifyCallbackWrapper wrapper = new DistroVerifyCallbackWrapper(targetServer,
                verifyData.getDistroKey().getResourceKey(), callback, member);
            clusterRpcClientProxy.asyncRequest(member, request, wrapper);
        } catch (NacosException nacosException) {
            callback.onFailed(nacosException);
        }
    }
    
    /** 从目标节点查询指定 Distro 键的数据。 */
    @Override
    public DistroData getData(DistroKey key, String targetServer) {
        Member member = memberManager.find(targetServer);
        if (checkTargetServerStatusUnhealthy(member)) {
            throw new DistroException(
                String.format("[DISTRO] Cancel get snapshot caused by target server %s unhealthy",
                    targetServer));
        }
        DistroDataRequest request = new DistroDataRequest();
        DistroData distroData = new DistroData();
        distroData.setDistroKey(key);
        distroData.setType(DataOperation.QUERY);
        request.setDistroData(distroData);
        request.setDataOperation(DataOperation.QUERY);
        try {
            Response response = clusterRpcClientProxy.sendRequest(member, request);
            if (checkResponse(response)) {
                return ((DistroDataResponse) response).getDistroData();
            } else {
                throw new DistroException(
                    String.format(
                        "[DISTRO-FAILED] Get data request to %s failed, code: %d, message: %s",
                        targetServer, response.getErrorCode(), response.getMessage()));
            }
        } catch (NacosException e) {
            throw new DistroException("[DISTRO-FAILED] Get distro data failed! ", e);
        }
    }
    
    /** 从目标节点拉取完整 Distro 快照。 */
    @Override
    public DistroData getDatumSnapshot(String targetServer) {
        Member member = memberManager.find(targetServer);
        if (checkTargetServerStatusUnhealthy(member)) {
            throw new DistroException(
                String.format("[DISTRO] Cancel get snapshot caused by target server %s unhealthy",
                    targetServer));
        }
        DistroDataRequest request = new DistroDataRequest();
        request.setDataOperation(DataOperation.SNAPSHOT);
        try {
            Response response = clusterRpcClientProxy
                .sendRequest(member, request,
                    DistroConfig.getInstance().getLoadDataTimeoutMillis());
            if (checkResponse(response)) {
                return ((DistroDataResponse) response).getDistroData();
            } else {
                throw new DistroException(
                    String.format(
                        "[DISTRO-FAILED] Get snapshot request to %s failed, code: %d, message: %s",
                        targetServer, response.getErrorCode(), response.getMessage()));
            }
        } catch (NacosException e) {
            throw new DistroException("[DISTRO-FAILED] Get distro snapshot failed! ", e);
        }
    }
    
    private boolean isNoExistTarget(String target) {
        return !memberManager.hasMember(target);
    }
    
    private boolean checkTargetServerStatusUnhealthy(Member member) {
        return null == member || !NodeState.UP.equals(member.getState())
            || !clusterRpcClientProxy.isRunning(member);
    }
    
    private boolean checkResponse(Response response) {
        return ResponseCode.SUCCESS.getCode() == response.getResultCode();
    }
    
    /** 包装 Distro 同步异步 RPC 回调并上报 TPS 指标。 */
    private class DistroRpcCallbackWrapper implements RequestCallBack<Response> {
        
        private final DistroCallback distroCallback;
        
        private final Member member;
        
        public DistroRpcCallbackWrapper(DistroCallback distroCallback, Member member) {
            this.distroCallback = distroCallback;
            this.member = member;
        }
        
        @Override
        public Executor getExecutor() {
            return GlobalExecutor.getCallbackExecutor();
        }
        
        @Override
        public long getTimeout() {
            return DistroConfig.getInstance().getSyncTimeoutMillis();
        }
        
        @Override
        public void onResponse(Response response) {
            if (checkResponse(response)) {
                NamingTpsMonitor.distroSyncSuccess(member.getAddress(), member.getIp());
                distroCallback.onSuccess();
            } else {
                NamingTpsMonitor.distroSyncFail(member.getAddress(), member.getIp());
                distroCallback.onFailed(null);
            }
        }
        
        @Override
        public void onException(Throwable e) {
            distroCallback.onFailed(e);
        }
    }
    
    /** 包装 Distro 校验异步 RPC 回调，失败时发布 {@link ClientEvent.ClientVerifyFailedEvent}。 */
    private class DistroVerifyCallbackWrapper implements RequestCallBack<Response> {
        
        private final String targetServer;
        
        private final String clientId;
        
        private final DistroCallback distroCallback;
        
        private final Member member;
        
        private DistroVerifyCallbackWrapper(String targetServer, String clientId,
            DistroCallback distroCallback,
            Member member) {
            this.targetServer = targetServer;
            this.clientId = clientId;
            this.distroCallback = distroCallback;
            this.member = member;
        }
        
        @Override
        public Executor getExecutor() {
            return GlobalExecutor.getCallbackExecutor();
        }
        
        @Override
        public long getTimeout() {
            return DistroConfig.getInstance().getVerifyTimeoutMillis();
        }
        
        @Override
        public void onResponse(Response response) {
            if (checkResponse(response)) {
                NamingTpsMonitor.distroVerifySuccess(member.getAddress(), member.getIp());
                distroCallback.onSuccess();
            } else {
                Loggers.DISTRO.info("Target {} verify client {} failed, sync new client",
                    targetServer, clientId);
                NotifyCenter
                    .publishEvent(new ClientEvent.ClientVerifyFailedEvent(clientId, targetServer));
                NamingTpsMonitor.distroVerifyFail(member.getAddress(), member.getIp());
                distroCallback.onFailed(null);
            }
        }
        
        @Override
        public void onException(Throwable e) {
            distroCallback.onFailed(e);
        }
    }
}
