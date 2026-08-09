/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.rocketmq.controller.helper.BrokerLifecycleListener;
import org.apache.rocketmq.remoting.RemotingServer;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;
import org.apache.rocketmq.remoting.protocol.body.SyncStateSet;
import org.apache.rocketmq.remoting.protocol.header.controller.AlterSyncStateSetRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.controller.admin.CleanControllerBrokerDataRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.controller.ElectMasterRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.controller.GetReplicaInfoRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.controller.register.ApplyBrokerIdRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.controller.register.GetNextBrokerIdRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.controller.register.RegisterBrokerToControllerRequestHeader;

/**
 * Controller 核心 API：主从选举、副本集变更、Broker 注册及元数据查询。
 * Leader 节点负责调度副本状态同步相关事件。
 */
public interface Controller {

    /** 启动 Controller 进程与共识组件。 */
    void startup();

    /** 关闭 Controller 并释放共识与网络资源。 */
    void shutdown();

    /** 成为 Leader 后启动副本状态调度任务。 */
    void startScheduling();

    /** 失去 Leader 身份后停止调度任务。 */
    void stopScheduling();

    /** 当前节点是否为 Controller Leader。 */
    boolean isLeaderState();

    /**
     * 修改 Broker 副本集的 SyncStateSet（同步副本集合）。
     *
     * @param request AlterSyncStateSetRequestHeader
     * @return RemotingCommand(AlterSyncStateSetResponseHeader)
     */
    CompletableFuture<RemotingCommand> alterSyncStateSet(
        final AlterSyncStateSetRequestHeader request, final SyncStateSet syncStateSet);

    /**
     * 为指定 Broker 组选举新 Master。
     *
     * @param request ElectMasterRequest
     * @return RemotingCommand(ElectMasterResponseHeader)
     */
    CompletableFuture<RemotingCommand> electMaster(final ElectMasterRequestHeader request);

    /** 申请 Broker 组内下一个可用 brokerId。 */
    CompletableFuture<RemotingCommand> getNextBrokerId(final GetNextBrokerIdRequestHeader request);

    /** 向 Controller 申请并预留指定 brokerId。 */
    CompletableFuture<RemotingCommand> applyBrokerId(final ApplyBrokerIdRequestHeader request);

    /**
     * 注册 Broker 及其唯一 brokerId 与当前地址。
     *
     * @param request RegisterBrokerToControllerRequest
     * @return RemotingCommand(RegisterBrokerToControllerResponseHeader)
     */
    CompletableFuture<RemotingCommand> registerBroker(final RegisterBrokerToControllerRequestHeader request);

    /**
     * 查询目标 Broker 组的副本与 Master 信息。
     *
     * @param request GetRouteInfoRequest
     * @return RemotingCommand(GetReplicaInfoResponseHeader)
     */
    CompletableFuture<RemotingCommand> getReplicaInfo(final GetReplicaInfoRequestHeader request);

    /** 返回 Controller 集群元数据（成员、Leader 等）。 */
    RemotingCommand getControllerMetadata();

    /** 批量查询 Broker 同步状态，供管理工具使用。 */
    CompletableFuture<RemotingCommand> getSyncStateData(final List<String> brokerNames);

    /**
     * 注册 Broker 生命周期监听器。
     * @param listener 监听器实例
     */
    void registerBrokerLifecycleListener(final BrokerLifecycleListener listener);

    /** 返回 Controller 使用的 RemotingServer，供上层复用注册处理器。 */
    RemotingServer getRemotingServer();

    /** 清理 Controller 中指定 Broker 的持久化元数据。 */
    CompletableFuture<RemotingCommand> cleanBrokerData(final CleanControllerBrokerDataRequestHeader requestHeader);
}
