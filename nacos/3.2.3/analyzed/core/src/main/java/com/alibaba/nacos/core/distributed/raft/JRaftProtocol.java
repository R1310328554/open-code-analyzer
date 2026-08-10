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
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.notify.listener.Subscriber;
import com.alibaba.nacos.common.notify.Event;
import com.alibaba.nacos.common.utils.MapUtil;
import com.alibaba.nacos.common.utils.ThreadUtils;
import com.alibaba.nacos.consistency.ProtocolMetaData;
import com.alibaba.nacos.consistency.SerializeFactory;
import com.alibaba.nacos.consistency.Serializer;
import com.alibaba.nacos.consistency.cp.CPProtocol;
import com.alibaba.nacos.consistency.cp.RequestProcessor4CP;
import com.alibaba.nacos.consistency.cp.MetadataKey;
import com.alibaba.nacos.consistency.entity.ReadRequest;
import com.alibaba.nacos.consistency.entity.Response;
import com.alibaba.nacos.consistency.entity.WriteRequest;
import com.alibaba.nacos.core.cluster.Member;
import com.alibaba.nacos.core.cluster.ServerMemberManager;
import com.alibaba.nacos.core.distributed.AbstractConsistencyProtocol;
import com.alibaba.nacos.core.distributed.raft.exception.NoSuchRaftGroupException;
import com.alibaba.nacos.core.monitor.MetricsMonitor;
import com.alibaba.nacos.core.utils.Loggers;
import com.alipay.sofa.jraft.Node;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * CP 一致性协议 JRaft 实现：管理 {@link JRaftServer} 生命周期、注册 {@link RequestProcessor4CP} 创建 Raft 组、处理读写请求与集群成员变更，并通过 {@link RaftEvent} 更新协议元数据。
 * A concrete implementation of CP protocol: JRaft.
 *
 * <pre>
 *                                           ┌──────────────────────┐
 *            ┌──────────────────────┐       │                      ▼
 *            │   ProtocolManager    │       │        ┌───────────────────────────┐
 *            └──────────────────────┘       │        │for p in [LogProcessor4CP] │
 *                        │                  │        └───────────────────────────┘
 *                        ▼                  │                      │
 *      ┌──────────────────────────────────┐ │                      ▼
 *      │    discovery LogProcessor4CP     │ │             ┌─────────────────┐
 *      └──────────────────────────────────┘ │             │  get p.group()  │
 *                        │                  │             └─────────────────┘
 *                        ▼                  │                      │
 *                 ┌─────────────┐           │                      │
 *                 │ RaftConfig  │           │                      ▼
 *                 └─────────────┘           │      ┌──────────────────────────────┐
 *                        │                  │      │  create raft group service   │
 *                        ▼                  │      └──────────────────────────────┘
 *              ┌──────────────────┐         │
 *              │  JRaftProtocol   │         │
 *              └──────────────────┘         │
 *                        │                  │
 *                     init()                │
 *                        │                  │
 *                        ▼                  │
 *               ┌─────────────────┐         │
 *               │   JRaftServer   │         │
 *               └─────────────────┘         │
 *                        │                  │
 *                        │                  │
 *                        ▼                  │
 *             ┌────────────────────┐        │
 *             │JRaftServer.start() │        │
 *             └────────────────────┘        │
 *                        │                  │
 *                        └──────────────────┘
 * </pre>
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
@SuppressWarnings("all")
public class JRaftProtocol extends AbstractConsistencyProtocol<RaftConfig, RequestProcessor4CP>
    implements CPProtocol<RaftConfig, RequestProcessor4CP> {
    
    /** 是否已完成 init，保证单次初始化。 */
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    
    /** 是否已 shutdown，避免重复关闭。 */
    private final AtomicBoolean shutdowned = new AtomicBoolean(false);
    
    /** 默认序列化器（预留扩展）。 */
    private final Serializer serializer = SerializeFactory.getDefault();
    
    /** Raft 运行时配置。 */
    private RaftConfig raftConfig;
    
    /** JRaft 服务端实例。 */
    private JRaftServer raftServer;
    
    /** JRaft 运维命令门面。 */
    private JRaftMaintainService jRaftMaintainService;
    
    /** 集群成员管理器，用于注入协议元数据。 */
    private ServerMemberManager memberManager;
    
    /**
     * 构造 JRaft 协议并创建服务端与运维服务。
     *
     * @param memberManager 集群成员管理器
     * @throws Exception 创建 JRaftServer 失败时抛出
     */
    public JRaftProtocol(ServerMemberManager memberManager) throws Exception {
        this.memberManager = memberManager;
        this.raftServer = new JRaftServer();
        this.jRaftMaintainService = new JRaftMaintainService(raftServer);
    }
    
    /**
     * 初始化 Raft：加载配置、启动 {@link JRaftServer}、订阅 {@link RaftEvent} 更新 Leader/Term 等元数据。
     *
     * @param config Raft 配置
     */
    @Override
    public void init(RaftConfig config) {
        if (initialized.compareAndSet(false, true)) {
            this.raftConfig = config;
            NotifyCenter.registerToSharePublisher(RaftEvent.class);
            this.raftServer.init(this.raftConfig);
            this.raftServer.start();
            
            // 单消费者保证 Raft 事件顺序处理，避免并发竞争
            NotifyCenter.registerSubscriber(new Subscriber<RaftEvent>() {
                
                @Override
                public void onEvent(RaftEvent event) {
                    Loggers.RAFT.info("This Raft event changes : {}", event);
                    final String groupId = event.getGroupId();
                    Map<String, Map<String, Object>> value = new HashMap<>();
                    Map<String, Object> properties = new HashMap<>();
                    final String leader = event.getLeader();
                    final Long term = event.getTerm();
                    final List<String> raftClusterInfo = event.getRaftClusterInfo();
                    final String errMsg = event.getErrMsg();
                    
                    // Leader 等信息仅在有效时写入协议元数据
                    MapUtil.putIfValNoEmpty(properties, MetadataKey.LEADER_META_DATA, leader);
                    MapUtil.putIfValNoNull(properties, MetadataKey.TERM_META_DATA, term);
                    MapUtil.putIfValNoEmpty(properties, MetadataKey.RAFT_GROUP_MEMBER,
                        raftClusterInfo);
                    MapUtil.putIfValNoEmpty(properties, MetadataKey.ERR_MSG, errMsg);
                    MetricsMonitor.refreshRaftGroupMetrics(groupId, leader, term,
                        raftConfig.getSelfMember());
                    
                    value.put(groupId, properties);
                    metaData.load(value);
                    
                    // 将协议元数据注入当前集群成员扩展字段
                    injectProtocolMetaData(metaData);
                }
                
                @Override
                public Class<? extends Event> subscribeType() {
                    return RaftEvent.class;
                }
                
            });
        }
    }
    
    /** {@inheritDoc} 为各 {@link RequestProcessor4CP} 创建对应 Raft 组。 */
    @Override
    public void addRequestProcessors(Collection<RequestProcessor4CP> processors) {
        raftServer.createMultiRaftGroup(processors);
    }
    
    /** {@inheritDoc} 同步读，最多等待 5 秒。 */
    @Override
    public Response getData(ReadRequest request) throws Exception {
        CompletableFuture<Response> future = aGetData(request);
        return future.get(5_000L, TimeUnit.MILLISECONDS);
    }
    
    /** {@inheritDoc} 异步读请求。 */
    @Override
    public CompletableFuture<Response> aGetData(ReadRequest request) {
        return raftServer.get(request);
    }
    
    /** {@inheritDoc} 同步写，最多等待 10 秒。 */
    @Override
    public Response write(WriteRequest request) throws Exception {
        CompletableFuture<Response> future = writeAsync(request);
        // 同步写最多等待 10 秒以尽量完成提交
        return future.get(10_000L, TimeUnit.MILLISECONDS);
    }
    
    /** {@inheritDoc} 异步提交写请求到指定 Raft 组。 */
    @Override
    public CompletableFuture<Response> writeAsync(WriteRequest request) {
        return raftServer.commit(request.getGroup(), request, new CompletableFuture<>());
    }
    
    /**
     * {@inheritDoc} 变更 Raft 集群成员，最多重试 5 次。
     *
     * @param addresses 新成员地址集合
     */
    @Override
    public void memberChange(Set<String> addresses) {
        for (int i = 0; i < 5; i++) {
            if (this.raftServer.peerChange(jRaftMaintainService, addresses)) {
                return;
            }
            ThreadUtils.sleep(100L);
        }
        Loggers.RAFT.warn("peer removal failed");
    }
    
    /** {@inheritDoc} 关闭 JRaft 服务端。 */
    @Override
    public void shutdown() {
        if (initialized.get() && shutdowned.compareAndSet(false, true)) {
            Loggers.RAFT.info("shutdown jraft server");
            raftServer.shutdown();
        }
    }
    
    /** {@inheritDoc} 委托 {@link JRaftMaintainService} 执行运维命令。 */
    @Override
    public RestResult<String> execute(Map<String, String> args) {
        return jRaftMaintainService.execute(args);
    }
    
    /** 将 Raft 协议元数据写入本节点 Member 扩展信息并持久化。 */
    private void injectProtocolMetaData(ProtocolMetaData metaData) {
        Member member = memberManager.getSelf();
        member.setExtendVal("raftMetaData", metaData);
        memberManager.update(member);
    }
    
    /**
     * {@inheritDoc} 判断本节点是否为指定 Raft 组的 Leader。
     *
     * @param group Raft 组 ID
     * @return 是否为 Leader
     * @throws com.alibaba.nacos.core.distributed.raft.exception.NoSuchRaftGroupException 组不存在时
     */
    @Override
    public boolean isLeader(String group) {
        Node node = raftServer.findNodeByGroup(group);
        if (node == null) {
            throw new NoSuchRaftGroupException(group);
        }
        return node.isLeader();
    }
    
    /** {@inheritDoc} 判断 JRaft 服务端是否就绪。 */
    @Override
    public boolean isReady() {
        return raftServer.isReady();
    }
}
