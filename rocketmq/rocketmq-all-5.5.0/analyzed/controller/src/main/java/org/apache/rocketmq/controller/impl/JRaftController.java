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
package org.apache.rocketmq.controller.impl;

import com.alipay.sofa.jraft.Node;
import com.alipay.sofa.jraft.RaftGroupService;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.entity.NodeId;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.entity.Task;
import com.alipay.sofa.jraft.option.NodeOptions;
import org.apache.commons.io.FileUtils;
import org.apache.rocketmq.common.ControllerConfig;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.controller.Controller;
import org.apache.rocketmq.controller.helper.BrokerLifecycleListener;
import org.apache.rocketmq.controller.impl.closure.ControllerClosure;
import org.apache.rocketmq.controller.impl.task.BrokerCloseChannelRequest;
import org.apache.rocketmq.controller.impl.task.CheckNotActiveBrokerRequest;
import org.apache.rocketmq.controller.impl.task.GetBrokerLiveInfoRequest;
import org.apache.rocketmq.controller.impl.task.GetSyncStateDataRequest;
import org.apache.rocketmq.controller.impl.task.RaftBrokerHeartBeatEventRequest;
import org.apache.rocketmq.remoting.ChannelEventListener;
import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.apache.rocketmq.remoting.RemotingServer;
import org.apache.rocketmq.remoting.netty.NettyRemotingServer;
import org.apache.rocketmq.remoting.netty.NettyServerConfig;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;
import org.apache.rocketmq.remoting.protocol.RequestCode;
import org.apache.rocketmq.remoting.protocol.ResponseCode;
import org.apache.rocketmq.remoting.protocol.body.SyncStateSet;
import org.apache.rocketmq.remoting.protocol.header.controller.AlterSyncStateSetRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.controller.ElectMasterRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.controller.GetMetaDataResponseHeader;
import org.apache.rocketmq.remoting.protocol.header.controller.GetReplicaInfoRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.controller.admin.CleanControllerBrokerDataRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.controller.register.ApplyBrokerIdRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.controller.register.GetNextBrokerIdRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.controller.register.RegisterBrokerToControllerRequestHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 基于 SOFA JRaft 的 RocketMQ 控制器：通过 Raft 共识管理 Broker 元数据与主从选举。
 */
public class JRaftController implements Controller {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.CONTROLLER_LOGGER_NAME);
    /** JRaft Raft 组服务，负责节点启动与关闭。 */
    private final RaftGroupService raftGroupService;
    /** 当前 JRaft 节点实例。 */
    private Node node;
    /** 控制器状态机，处理已提交的 Remoting 请求。 */
    private final JRaftControllerStateMachine stateMachine;
    /** 控制器运行时配置（选举超时、存储路径等）。 */
    private final ControllerConfig controllerConfig;
    /** Broker 生命周期监听器列表。 */
    private final List<BrokerLifecycleListener> brokerLifecycleListeners;
    private final Map<PeerId/* jRaft peerId */, String/* Controller RPC Server Addr */> peerIdToAddr;
    /** 对外提供 Controller RPC 的 Netty 服务端。 */
    private final NettyRemotingServer remotingServer;

    /** 初始化 JRaft 节点、状态机与 Remoting 服务。 */
    public JRaftController(ControllerConfig controllerConfig,
        final ChannelEventListener channelEventListener) throws IOException {
        this.controllerConfig = controllerConfig;
        this.brokerLifecycleListeners = new ArrayList<>();

        final NodeOptions nodeOptions = new NodeOptions();
        nodeOptions.setElectionTimeoutMs(controllerConfig.getJraftConfig().getjRaftElectionTimeoutMs());
        nodeOptions.setSnapshotIntervalSecs(controllerConfig.getJraftConfig().getjRaftSnapshotIntervalSecs());
        final PeerId serverId = new PeerId();
        if (!serverId.parse(controllerConfig.getJraftConfig().getjRaftServerId())) {
            throw new IllegalArgumentException("Fail to parse serverId:" + controllerConfig.getJraftConfig().getjRaftServerId());
        }
        final Configuration initConf = new Configuration();
        if (!initConf.parse(controllerConfig.getJraftConfig().getjRaftInitConf())) {
            throw new IllegalArgumentException("Fail to parse initConf:" + controllerConfig.getJraftConfig().getjRaftInitConf());
        }
        nodeOptions.setInitialConf(initConf);

        FileUtils.forceMkdir(new File(controllerConfig.getControllerStorePath()));
        nodeOptions.setLogUri(controllerConfig.getControllerStorePath() + File.separator + "log");
        nodeOptions.setRaftMetaUri(controllerConfig.getControllerStorePath() + File.separator + "raft_meta");
        nodeOptions.setSnapshotUri(controllerConfig.getControllerStorePath() + File.separator + "snapshot");

        this.stateMachine = new JRaftControllerStateMachine(controllerConfig, new NodeId(controllerConfig.getJraftConfig().getjRaftGroupId(), serverId));
        this.stateMachine.registerOnLeaderStart(this::onLeaderStart);
        this.stateMachine.registerOnLeaderStop(this::onLeaderStop);
        nodeOptions.setFsm(this.stateMachine);

        this.raftGroupService = new RaftGroupService(controllerConfig.getJraftConfig().getjRaftGroupId(), serverId, nodeOptions);

        this.peerIdToAddr = new HashMap<>();
        initPeerIdMap();

        NettyServerConfig nettyServerConfig = new NettyServerConfig();
        nettyServerConfig.setListenPort(Integer.parseInt(this.peerIdToAddr.get(serverId).split(":")[1]));
        remotingServer = new NettyRemotingServer(nettyServerConfig, channelEventListener);
    }

    /** 解析初始集群配置，建立 PeerId 到 RPC 地址的映射。 */
    private void initPeerIdMap() {
        String[] peers = this.controllerConfig.getJraftConfig().getjRaftInitConf().split(",");
        String[] rpcAddrs = this.controllerConfig.getJraftConfig().getjRaftControllerRPCAddr().split(",");
        for (int i = 0; i < peers.length; i++) {
            PeerId peerId = new PeerId();
            if (!peerId.parse(peers[i])) {
                throw new IllegalArgumentException("Fail to parse peerId:" + peers[i]);
            }
            this.peerIdToAddr.put(peerId, rpcAddrs[i]);
        }
    }

    @Override
    /** 启动 Remoting 服务与 JRaft 节点。 */
    public void startup() {
        this.remotingServer.start();
        this.node = this.raftGroupService.start();
        log.info("Controller {} started.", node.getNodeId());
    }

    @Override
    /** 停止调度、关闭 Raft 组与 Remoting 服务。 */
    public void shutdown() {
        this.stopScheduling();
        this.raftGroupService.shutdown();
        this.remotingServer.shutdown();
        log.info("Controller {} stopped.", node.getNodeId());
    }

    @Override
    public void startScheduling() {
    }

    @Override
    public void stopScheduling() {
    }

    @Override
    /** 判断当前节点是否为 Raft Leader。 */
    public boolean isLeaderState() {
        return node.isLeader();
    }

    /** 将 Remoting 请求封装为 JRaft Task 并提交到 Leader 节点。 */
    private <T extends CommandCustomHeader> CompletableFuture<RemotingCommand> applyToJRaft(RemotingCommand request) {
        if (!isLeaderState()) {
            final RemotingCommand command = RemotingCommand.createResponseCommand(ResponseCode.CONTROLLER_NOT_LEADER, "The controller is not in leader state");
            final CompletableFuture<RemotingCommand> future = new CompletableFuture<>();
            future.complete(command);
            log.warn("Apply to none leader controller, controller state is {}", node.getNodeState());
            return future;
        }
        ControllerClosure closure = new ControllerClosure(request);
        Task task = closure.taskWithThisClosure();
        if (task != null) {
            node.apply(task);
            return closure.getFuture();
        } else {
            log.error("Apply task failed, task is null.");
            return CompletableFuture.completedFuture(RemotingCommand.createResponseCommand(ResponseCode.CONTROLLER_JRAFT_INTERNAL_ERROR, "Apply task failed, Please see the server log."));
        }
    }

    @Override
    public CompletableFuture<RemotingCommand> alterSyncStateSet(AlterSyncStateSetRequestHeader request,
        SyncStateSet syncStateSet) {
        final RemotingCommand requestCommand = RemotingCommand.createRequestCommand(RequestCode.CONTROLLER_ALTER_SYNC_STATE_SET, request);
        requestCommand.setBody(syncStateSet.encode());
        return applyToJRaft(requestCommand);
    }

    @Override
    public CompletableFuture<RemotingCommand> electMaster(ElectMasterRequestHeader request) {
        final RemotingCommand requestCommand = RemotingCommand.createRequestCommand(RequestCode.CONTROLLER_ELECT_MASTER, request);
        return applyToJRaft(requestCommand);
    }

    @Override
    public CompletableFuture<RemotingCommand> getNextBrokerId(GetNextBrokerIdRequestHeader request) {
        final RemotingCommand requestCommand = RemotingCommand.createRequestCommand(RequestCode.CONTROLLER_GET_NEXT_BROKER_ID, request);
        return applyToJRaft(requestCommand);
    }

    @Override
    public CompletableFuture<RemotingCommand> applyBrokerId(ApplyBrokerIdRequestHeader request) {
        final RemotingCommand requestCommand = RemotingCommand.createRequestCommand(RequestCode.CONTROLLER_APPLY_BROKER_ID, request);
        return applyToJRaft(requestCommand);
    }

    @Override
    public CompletableFuture<RemotingCommand> registerBroker(RegisterBrokerToControllerRequestHeader request) {
        final RemotingCommand requestCommand = RemotingCommand.createRequestCommand(RequestCode.CONTROLLER_REGISTER_BROKER, request);
        return applyToJRaft(requestCommand);
    }

    @Override
    public CompletableFuture<RemotingCommand> getReplicaInfo(GetReplicaInfoRequestHeader request) {
        final RemotingCommand requestCommand = RemotingCommand.createRequestCommand(RequestCode.CONTROLLER_GET_REPLICA_INFO, request);
        return applyToJRaft(requestCommand);
    }

    @Override
    public CompletableFuture<RemotingCommand> getSyncStateData(List<String> brokerNames) {
        final RemotingCommand requestCommand = RemotingCommand.createRequestCommand(RequestCode.CONTROLLER_GET_SYNC_STATE_DATA, new GetSyncStateDataRequest());
        requestCommand.setBody(RemotingSerializable.encode(brokerNames));
        return applyToJRaft(requestCommand);
    }

    @Override
    public CompletableFuture<RemotingCommand> cleanBrokerData(CleanControllerBrokerDataRequestHeader requestHeader) {
        final RemotingCommand requestCommand = RemotingCommand.createRequestCommand(RequestCode.CLEAN_BROKER_DATA, requestHeader);
        return applyToJRaft(requestCommand);
    }

    @Override
    public void registerBrokerLifecycleListener(BrokerLifecycleListener listener) {
        this.brokerLifecycleListeners.add(listener);
    }

    @Override
    public RemotingCommand getControllerMetadata() {
        List<PeerId> peers = node.getOptions().getInitialConf().getPeers();
        final StringBuilder sb = new StringBuilder();
        for (PeerId peer : peers) {
            sb.append(peerIdToAddr.get(peer)).append(";");
        }
        return RemotingCommand.createResponseCommandWithHeader(ResponseCode.SUCCESS, new GetMetaDataResponseHeader(
            node.getGroupId(),
            node.getLeaderId() == null ? "" : node.getLeaderId().toString(),
            this.peerIdToAddr.get(node.getLeaderId()),
            node.isLeader(),
            sb.toString()
        ));
    }

    @Override
    public RemotingServer getRemotingServer() {
        return remotingServer;
    }

    /** Leader 任期开始时的回调。 */
    public void onLeaderStart(long term) {
        log.info("Controller start leadership, term: {}.", term);
    }

    /** Leader 任期结束时的回调，停止调度任务。 */
    public void onLeaderStop(Status status) {
        log.info("Controller {} stop leadership, status: {}.", node.getNodeId(), status);
        this.stopScheduling();
    }

    public CompletableFuture<RemotingCommand> getBrokerLiveInfo(GetBrokerLiveInfoRequest requestHeader) {
        final RemotingCommand requestCommand = RemotingCommand.createRequestCommand(RequestCode.GET_BROKER_LIVE_INFO_REQUEST, requestHeader);
        return applyToJRaft(requestCommand);
    }

    public CompletableFuture<RemotingCommand> onBrokerHeartBeat(RaftBrokerHeartBeatEventRequest requestHeader) {
        final RemotingCommand requestCommand = RemotingCommand.createRequestCommand(RequestCode.RAFT_BROKER_HEART_BEAT_EVENT_REQUEST, requestHeader);
        return applyToJRaft(requestCommand);
    }

    public CompletableFuture<RemotingCommand> onBrokerCloseChannel(BrokerCloseChannelRequest requestHeader) {
        final RemotingCommand requestCommand = RemotingCommand.createRequestCommand(RequestCode.BROKER_CLOSE_CHANNEL_REQUEST, requestHeader);
        return applyToJRaft(requestCommand);
    }

    public CompletableFuture<RemotingCommand> checkNotActiveBroker(CheckNotActiveBrokerRequest requestHeader) {
        final RemotingCommand requestCommand = RemotingCommand.createRequestCommand(RequestCode.CHECK_NOT_ACTIVE_BROKER_REQUEST, requestHeader);
        return applyToJRaft(requestCommand);
    }
}
