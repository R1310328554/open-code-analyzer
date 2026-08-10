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

import com.alibaba.nacos.common.JustForTest;
import com.alibaba.nacos.common.model.RestResult;
import com.alibaba.nacos.common.utils.ConvertUtils;
import com.alibaba.nacos.common.utils.InternetAddressUtil;
import com.alibaba.nacos.common.utils.LoggerUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.common.utils.ThreadUtils;
import com.alibaba.nacos.consistency.ProtoMessageUtil;
import com.alibaba.nacos.consistency.RequestProcessor;
import com.alibaba.nacos.consistency.SerializeFactory;
import com.alibaba.nacos.consistency.Serializer;
import com.alibaba.nacos.consistency.cp.RequestProcessor4CP;
import com.alibaba.nacos.consistency.entity.ReadRequest;
import com.alibaba.nacos.consistency.entity.Response;
import com.alibaba.nacos.consistency.exception.ConsistencyException;
import com.alibaba.nacos.core.distributed.raft.exception.DuplicateRaftGroupException;
import com.alibaba.nacos.core.distributed.raft.exception.JRaftException;
import com.alibaba.nacos.core.distributed.raft.exception.NoLeaderException;
import com.alibaba.nacos.core.distributed.raft.exception.NoSuchRaftGroupException;
import com.alibaba.nacos.core.distributed.raft.utils.FailoverClosure;
import com.alibaba.nacos.core.distributed.raft.utils.FailoverClosureImpl;
import com.alibaba.nacos.core.distributed.raft.utils.JRaftConstants;
import com.alibaba.nacos.core.distributed.raft.utils.JRaftUtils;
import com.alibaba.nacos.core.distributed.raft.utils.RaftExecutor;
import com.alibaba.nacos.core.distributed.raft.utils.RaftOptionsBuilder;
import com.alibaba.nacos.core.monitor.MetricsMonitor;
import com.alibaba.nacos.core.utils.Loggers;
import com.alibaba.nacos.sys.env.EnvUtil;
import com.alipay.sofa.jraft.CliService;
import com.alipay.sofa.jraft.Node;
import com.alipay.sofa.jraft.RaftGroupService;
import com.alipay.sofa.jraft.RaftServiceFactory;
import com.alipay.sofa.jraft.RouteTable;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.closure.ReadIndexClosure;
import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.core.CliServiceImpl;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.entity.Task;
import com.alipay.sofa.jraft.error.RaftError;
import com.alipay.sofa.jraft.option.CliOptions;
import com.alipay.sofa.jraft.option.NodeOptions;
import com.alipay.sofa.jraft.option.RaftOptions;
import com.alipay.sofa.jraft.rpc.InvokeCallback;
import com.alipay.sofa.jraft.rpc.RpcProcessor;
import com.alipay.sofa.jraft.rpc.RpcServer;
import com.alipay.sofa.jraft.rpc.impl.cli.CliClientServiceImpl;
import com.alipay.sofa.jraft.util.BytesUtil;
import com.alipay.sofa.jraft.util.Endpoint;
import com.google.protobuf.Message;
import org.springframework.util.CollectionUtils;

import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

/**
 * JRaft 服务端实例，脱离 Spring IOC 容器独立管理生命周期。
 * <p>
 * 为何按 {@code LogProcessor.group()} 创建多个 Raft Group：每个功能模块（如 naming、config）拥有独立状态机，日志处理互不阻塞。若共用一个状态机，任一模块日志处理异常或长时间阻塞都会影响其他模块。
 * </p>
 * JRaft server instance, away from Spring IOC management.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
@SuppressWarnings("all")
public class JRaftServer {
    
    // 生命周期核心组件
    
    /** JRaft 节点间通信 RPC 服务端。 */
    private RpcServer rpcServer;
    
    /** CLI 客户端服务，用于向 Leader 转发读写请求。 */
    private CliClientServiceImpl cliClientService;
    
    /** JRaft 集群管理 CLI 服务（增删节点、查询 peers 等）。 */
    private CliService cliService;
    
    // 运行时状态
    
    /** 多 Raft Group 注册表：groupName → 节点元组。 */
    private Map<String, RaftGroupTuple> multiRaftGroup = new ConcurrentHashMap<>();
    
    /** 服务端是否已完成启动。 */
    private volatile boolean isStarted = false;
    
    /** 是否已进入关闭流程。 */
    private volatile boolean isShutdown = false;
    
    /** 集群初始成员配置（Peer 列表）。 */
    private Configuration conf;
    
    /** 用户自定义 RPC 处理器（预留扩展点）。 */
    private RpcProcessor userProcessor;
    
    /** 创建各 Raft Group 时复用的节点选项模板。 */
    private NodeOptions nodeOptions;
    
    /** 请求/响应序列化器。 */
    private Serializer serializer;
    
    /** 已注册的 CP 请求处理器集合（线程安全）。 */
    private Collection<RequestProcessor4CP> processors =
        Collections.synchronizedSet(new HashSet<>());
    
    /** 本机 IP（从 selfMember 解析）。 */
    private String selfIp;
    
    /** 本机 Raft 端口。 */
    private int selfPort;
    
    /** Raft 协议运行时配置。 */
    private RaftConfig raftConfig;
    
    /** 本节点在集群中的 PeerId。 */
    private PeerId localPeerId;
    
    /** 请求转发失败时的重试次数上限。 */
    private int failoverRetries;
    
    /** 向 Leader 发起 RPC 的超时时间（毫秒）。 */
    private int rpcRequestTimeoutMs;
    
    /** 构造空配置的服务端实例。 */
    public JRaftServer() {
        this.conf = new Configuration();
    }
    
    /** 设置请求转发失败重试次数。 */
    public void setFailoverRetries(int failoverRetries) {
        this.failoverRetries = failoverRetries;
    }
    
    /** 根据配置初始化选举超时、RPC 超时、CLI 服务等核心组件。 */
    void init(RaftConfig config) {
        this.raftConfig = config;
        this.serializer = SerializeFactory.getDefault();
        Loggers.RAFT.info("Initializes the Raft protocol, raft-config info : {}", config);
        RaftExecutor.init(config);
        
        final String self = config.getSelfMember();
        String[] info = InternetAddressUtil.splitIpPortStr(self);
        selfIp = info[0];
        selfPort = Integer.parseInt(info[1]);
        localPeerId = PeerId.parsePeer(self);
        nodeOptions = new NodeOptions();
        
        // 设置选举超时，默认 5 秒。
        int electionTimeout = Math.max(
            ConvertUtils.toInt(config.getVal(RaftSysConstants.RAFT_ELECTION_TIMEOUT_MS),
                RaftSysConstants.DEFAULT_ELECTION_TIMEOUT),
            RaftSysConstants.DEFAULT_ELECTION_TIMEOUT);
        
        rpcRequestTimeoutMs =
            ConvertUtils.toInt(raftConfig.getVal(RaftSysConstants.RAFT_RPC_REQUEST_TIMEOUT_MS),
                RaftSysConstants.DEFAULT_RAFT_RPC_REQUEST_TIMEOUT_MS);
        
        nodeOptions.setSharedElectionTimer(true);
        nodeOptions.setSharedVoteTimer(true);
        nodeOptions.setSharedStepDownTimer(true);
        nodeOptions.setSharedSnapshotTimer(true);
        
        nodeOptions.setElectionTimeoutMs(electionTimeout);
        RaftOptions raftOptions = RaftOptionsBuilder.initRaftOptions(raftConfig);
        nodeOptions.setRaftOptions(raftOptions);
        // 开启 JRaft 节点指标采集
        nodeOptions.setEnableMetrics(true);
        
        CliOptions cliOptions = new CliOptions();
        
        this.cliService = RaftServiceFactory.createAndInitCliService(cliOptions);
        this.cliClientService =
            (CliClientServiceImpl) ((CliServiceImpl) this.cliService).getCliClientService();
    }
    
    /** 启动 RPC 服务并创建所有已注册 Processor 对应的 Raft Group。 */
    synchronized void start() {
        if (!isStarted) {
            Loggers.RAFT.info("========= The raft protocol is starting... =========");
            try {
                // 初始化集群成员并注册地址
                com.alipay.sofa.jraft.NodeManager raftNodeManager =
                    com.alipay.sofa.jraft.NodeManager.getInstance();
                for (String address : raftConfig.getMembers()) {
                    PeerId peerId = PeerId.parsePeer(address);
                    conf.addPeer(peerId);
                    raftNodeManager.addAddress(peerId.getEndpoint());
                }
                nodeOptions.setInitialConf(conf);
                
                rpcServer = JRaftUtils.initRpcServer(this, localPeerId);
                
                if (!this.rpcServer.init(null)) {
                    Loggers.RAFT.error("Fail to init [BaseRpcServer].");
                    throw new RuntimeException("Fail to init [BaseRpcServer].");
                }
                
                // 按 Processor 批量创建多 Raft Group
                isStarted = true;
                createMultiRaftGroup(processors);
                Loggers.RAFT.info("========= The raft protocol start finished... =========");
            } catch (Exception e) {
                Loggers.RAFT.error("raft protocol start failure, cause: ", e);
                throw new JRaftException(e);
            }
        }
    }
    
    /** 为每个 CP Processor 创建独立 Raft Group 与状态机；未启动时仅缓存 Processor。 */
    synchronized void createMultiRaftGroup(Collection<RequestProcessor4CP> processors) {
        // 未启动时先缓存 Processor，避免同步阻塞
        if (!this.isStarted) {
            this.processors.addAll(processors);
            return;
        }
        
        final String parentPath =
            Paths.get(EnvUtil.getNacosHome(), "data/protocol/raft").toString();
        
        for (RequestProcessor4CP processor : processors) {
            final String groupName = processor.group();
            if (multiRaftGroup.containsKey(groupName)) {
                throw new DuplicateRaftGroupException(groupName);
            }
            
            // 每个 Group 使用独立 Configuration 与 NodeOptions 副本
            Configuration configuration = conf.copy();
            NodeOptions copy = nodeOptions.copy();
            JRaftUtils.initDirectory(parentPath, groupName, copy);
            
            // LogProcessor 注入状态机，onApply 时回调 Processor.onApply
            NacosStateMachine machine = new NacosStateMachine(this, processor);
            
            copy.setFsm(machine);
            copy.setInitialConf(configuration);
            
            // 快照间隔默认 1800 秒
            int doSnapshotInterval =
                ConvertUtils.toInt(raftConfig.getVal(RaftSysConstants.RAFT_SNAPSHOT_INTERVAL_SECS),
                    RaftSysConstants.DEFAULT_RAFT_SNAPSHOT_INTERVAL_SECS);
            
            // 业务未实现快照处理器则禁用定时快照
            doSnapshotInterval =
                CollectionUtils.isEmpty(processor.loadSnapshotOperate()) ? 0 : doSnapshotInterval;
            
            copy.setSnapshotIntervalSecs(doSnapshotInterval);
            Loggers.RAFT.info("create raft group : {}", groupName);
            RaftGroupService raftGroupService =
                new RaftGroupService(groupName, localPeerId, copy, rpcServer, true);
            
            // RPC 服务已启动，此处不再重复 start RPC
            Node node = raftGroupService.start(false);
            machine.setNode(node);
            RouteTable.getInstance().updateConfiguration(groupName, configuration);
            
            RaftExecutor.executeByCommon(
                () -> registerSelfToCluster(groupName, localPeerId, configuration));
            
            // 为该 Group 开启 Leader/路由表定时刷新
            long period =
                nodeOptions.getElectionTimeoutMs() + ThreadLocalRandom.current().nextInt(5 * 1000);
            RaftExecutor.scheduleRaftMemberRefreshJob(() -> refreshRouteTable(groupName),
                nodeOptions.getElectionTimeoutMs(), period, TimeUnit.MILLISECONDS);
            multiRaftGroup.put(groupName,
                new RaftGroupTuple(node, processor, raftGroupService, machine));
        }
    }
    
    /** 线性一致读：优先 ReadIndex，失败则降级为 Leader 读。 */
    CompletableFuture<Response> get(final ReadRequest request) {
        final String group = request.getGroup();
        CompletableFuture<Response> future = new CompletableFuture<>();
        final RaftGroupTuple tuple = findTupleByGroup(group);
        if (Objects.isNull(tuple)) {
            future.completeExceptionally(new NoSuchRaftGroupException(group));
            return future;
        }
        final Node node = tuple.node;
        final RequestProcessor processor = tuple.processor;
        try {
            node.readIndex(BytesUtil.EMPTY_BYTES, new ReadIndexClosure() {
                
                @Override
                public void run(Status status, long index, byte[] reqCtx) {
                    if (status.isOk()) {
                        try {
                            Response response = processor.onRequest(request);
                            future.complete(response);
                        } catch (Throwable t) {
                            MetricsMonitor.raftReadIndexFailed();
                            future.completeExceptionally(new ConsistencyException(
                                "The conformance protocol is temporarily unavailable for reading",
                                t));
                        }
                        return;
                    }
                    MetricsMonitor.raftReadIndexFailed();
                    Loggers.RAFT.error("ReadIndex has error : {}, go to Leader read.",
                        status.getErrorMsg());
                    MetricsMonitor.raftReadFromLeader();
                    readFromLeader(request, future);
                }
            });
            return future;
        } catch (Throwable e) {
            MetricsMonitor.raftReadFromLeader();
            Loggers.RAFT.warn("Raft linear read failed, go to Leader read logic : {}",
                e.toString());
            // run raft read
            readFromLeader(request, future);
            return future;
        }
    }
    
    /** ReadIndex 失败时的 Leader 读降级入口。 */
    public void readFromLeader(final ReadRequest request,
        final CompletableFuture<Response> future) {
        commit(request.getGroup(), request, future);
    }
    
    /** 提交读写请求：本机为 Leader 则直接 apply，否则转发 Leader。 */
    public CompletableFuture<Response> commit(final String group, final Message data,
        final CompletableFuture<Response> future) {
        LoggerUtils.printIfDebugEnabled(Loggers.RAFT, "data requested this time : {}", data);
        final RaftGroupTuple tuple = findTupleByGroup(group);
        if (tuple == null) {
            future.completeExceptionally(
                new IllegalArgumentException("No corresponding Raft Group found : " + group));
            return future;
        }
        
        FailoverClosureImpl closure = new FailoverClosureImpl(future);
        
        final Node node = tuple.node;
        if (node.isLeader()) {
            // Leader 本地直接 apply
            applyOperation(node, data, closure);
        } else {
            // Follower 转发至 Leader 处理
            invokeToLeader(group, data, rpcRequestTimeoutMs, closure);
        }
        return future;
    }
    
    /**
     * 将本节点加入指定 Raft Group 集群（启动后异步执行，失败则每秒重试）。
     *
     * @param groupId raft group
     * @param selfIp  local raft node address
     * @param conf    {@link Configuration} without self info
     * @return join success
     */
    void registerSelfToCluster(String groupId, PeerId selfIp, Configuration conf) {
        while (!isShutdown) {
            try {
                List<PeerId> peerIds = cliService.getPeers(groupId, conf);
                if (peerIds.contains(selfIp)) {
                    return;
                }
                Status status = cliService.addPeer(groupId, conf, selfIp);
                if (status.isOk()) {
                    return;
                }
                Loggers.RAFT.warn("Failed to join the cluster, retry...");
            } catch (Exception e) {
                Loggers.RAFT.error("Failed to join the cluster, retry...", e);
            }
            ThreadUtils.sleep(1_000L);
        }
    }
    
    /** 从路由表查询指定 Group 的当前 Leader。 */
    protected PeerId getLeader(final String raftGroupId) {
        return RouteTable.getInstance().selectLeader(raftGroupId);
    }
    
    /** 关闭所有 Raft Group、CLI 服务与 RPC 客户端。 */
    synchronized void shutdown() {
        if (isShutdown) {
            return;
        }
        isShutdown = true;
        try {
            Loggers.RAFT.info("========= The raft protocol is starting to close =========");
            
            for (Map.Entry<String, RaftGroupTuple> entry : multiRaftGroup.entrySet()) {
                final RaftGroupTuple tuple = entry.getValue();
                final Node node = tuple.getNode();
                tuple.node.shutdown();
                tuple.raftGroupService.shutdown();
            }
            
            cliService.shutdown();
            cliClientService.shutdown();
            
            Loggers.RAFT.info("========= The raft protocol has been closed =========");
        } catch (Throwable t) {
            Loggers.RAFT.error("There was an error in the raft protocol shutdown, cause: ", t);
        }
    }
    
    /** 构造带读写类型标记的 Task 并提交到 Raft 节点 apply。 */
    public void applyOperation(Node node, Message data, FailoverClosure closure) {
        final Task task = new Task();
        task.setDone(new NacosClosure(data, status -> {
            NacosClosure.NacosStatus nacosStatus = (NacosClosure.NacosStatus) status;
            closure.setThrowable(nacosStatus.getThrowable());
            closure.setResponse(nacosStatus.getResponse());
            closure.run(nacosStatus);
        }));
        
        // 在 Task 数据头部附加请求类型字段（读/写）。
        byte[] requestTypeFieldBytes = new byte[2];
        requestTypeFieldBytes[0] = ProtoMessageUtil.REQUEST_TYPE_FIELD_TAG;
        if (data instanceof ReadRequest) {
            requestTypeFieldBytes[1] = ProtoMessageUtil.REQUEST_TYPE_READ;
        } else {
            requestTypeFieldBytes[1] = ProtoMessageUtil.REQUEST_TYPE_WRITE;
        }
        
        byte[] dataBytes = data.toByteArray();
        task.setData(
            (ByteBuffer) ByteBuffer.allocate(requestTypeFieldBytes.length + dataBytes.length)
                .put(requestTypeFieldBytes).put(dataBytes).position(0));
        node.apply(task);
    }
    
    /** 异步 RPC 调用 Leader 处理请求，结果通过 FailoverClosure 回传。 */
    private void invokeToLeader(final String group, final Message request, final int timeoutMillis,
        FailoverClosure closure) {
        try {
            final Endpoint leaderIp = Optional.ofNullable(getLeader(group))
                .orElseThrow(() -> new NoLeaderException(group)).getEndpoint();
            cliClientService.getRpcClient().invokeAsync(leaderIp, request, new InvokeCallback() {
                
                @Override
                public void complete(Object o, Throwable ex) {
                    if (Objects.nonNull(ex)) {
                        closure.setThrowable(ex);
                        closure.run(new Status(RaftError.UNKNOWN, ex.getMessage()));
                        return;
                    }
                    if (!((Response) o).getSuccess()) {
                        closure.setThrowable(new IllegalStateException(((Response) o).getErrMsg()));
                        closure.run(new Status(RaftError.UNKNOWN, ((Response) o).getErrMsg()));
                        return;
                    }
                    closure.setResponse((Response) o);
                    closure.run(Status.OK());
                }
                
                @Override
                public Executor executor() {
                    return RaftExecutor.getRaftCliServiceExecutor();
                }
            }, timeoutMillis);
        } catch (Exception e) {
            closure.setThrowable(e);
            closure.run(new Status(RaftError.UNKNOWN, e.toString()));
        }
    }
    
    /** 处理集群成员变更：更新配置并对各 Group 执行 remove peers 维护命令。 */
    boolean peerChange(JRaftMaintainService maintainService, Set<String> newPeers) {
        // 仅处理节点删除；新节点启动时会自行 join 集群
        Set<String> oldPeers = new HashSet<>(this.raftConfig.getMembers());
        this.raftConfig.setMembers(localPeerId.toString(), newPeers);
        oldPeers.removeAll(newPeers);
        if (oldPeers.isEmpty()) {
            return true;
        }
        
        Set<String> waitRemove = oldPeers;
        AtomicInteger successCnt = new AtomicInteger(0);
        multiRaftGroup.forEach(new BiConsumer<String, RaftGroupTuple>() {
            
            @Override
            public void accept(String group, RaftGroupTuple tuple) {
                Map<String, String> params = new HashMap<>();
                params.put(JRaftConstants.GROUP_ID, group);
                params.put(JRaftConstants.COMMAND_NAME, JRaftConstants.REMOVE_PEERS);
                params.put(JRaftConstants.COMMAND_VALUE,
                    StringUtils.join(waitRemove, StringUtils.COMMA));
                RestResult<String> result = maintainService.execute(params);
                if (result.ok()) {
                    successCnt.incrementAndGet();
                } else {
                    Loggers.RAFT.error("Node removal failed : {}", result);
                }
            }
        });
        return successCnt.get() == multiRaftGroup.size();
    }
    
    /** 定时刷新指定 Group 的 Leader 与成员配置到 RouteTable。 */
    void refreshRouteTable(String group) {
        if (isShutdown) {
            return;
        }
        
        final String groupName = group;
        Status status = null;
        try {
            RouteTable instance = RouteTable.getInstance();
            Configuration oldConf = instance.getConfiguration(groupName);
            String oldLeader =
                Optional.ofNullable(instance.selectLeader(groupName)).orElse(PeerId.emptyPeer())
                    .getEndpoint().toString();
            // 修复 #3661：Leader 与配置需定期刷新
            status = instance.refreshLeader(this.cliClientService, groupName, rpcRequestTimeoutMs);
            if (!status.isOk()) {
                Loggers.RAFT.error("Fail to refresh leader for group : {}, status is : {}",
                    groupName, status);
            }
            status = instance.refreshConfiguration(this.cliClientService, groupName,
                rpcRequestTimeoutMs);
            if (!status.isOk()) {
                Loggers.RAFT
                    .error("Fail to refresh route configuration for group : {}, status is : {}",
                        groupName, status);
            }
        } catch (Exception e) {
            Loggers.RAFT.error("Fail to refresh raft metadata info for group : {}, error is : {}",
                groupName, e);
        }
    }
    
    /** 按 group 名查找 RaftGroupTuple。 */
    public RaftGroupTuple findTupleByGroup(final String group) {
        RaftGroupTuple tuple = multiRaftGroup.get(group);
        return tuple;
    }
    
    /** 按 group 名查找 JRaft Node，不存在返回 null。 */
    public Node findNodeByGroup(final String group) {
        final RaftGroupTuple tuple = multiRaftGroup.get(group);
        if (Objects.nonNull(tuple)) {
            return tuple.node;
        }
        return null;
    }
    
    /** 严格模式下要求各 Group 均已选出 Leader；否则仅检查是否已启动。 */
    public boolean isReady() {
        if (raftConfig.isStrictMode()) {
            for (RequestProcessor4CP each : processors) {
                if (null == getLeader(each.group())) {
                    return false;
                }
            }
        }
        return isStarted;
    }
    
    /** 返回多 Group 注册表（包内可见）。 */
    Map<String, RaftGroupTuple> getMultiRaftGroup() {
        return multiRaftGroup;
    }
    
    @JustForTest
    /** 测试用：注入 mock 的多 Group 映射。 */
    void mockMultiRaftGroup(Map<String, RaftGroupTuple> map) {
        this.multiRaftGroup = map;
    }
    
    /** 返回 JRaft CLI 服务实例。 */
    CliService getCliService() {
        return cliService;
    }
    
    /** 单个 Raft Group 的运行时元组：节点、Processor、服务与状态机。 */
    public static class RaftGroupTuple {
        
        /** 该 Group 绑定的 CP 请求处理器。 */
        private RequestProcessor processor;
        
        /** JRaft Raft 节点实例。 */
        private Node node;
        
        /** 封装 Node 生命周期的 Group 服务。 */
        private RaftGroupService raftGroupService;
        
        /** 该 Group 对应的 Nacos 状态机。 */
        private NacosStateMachine machine;
        
        @JustForTest
        /** 测试用无参构造。 */
        public RaftGroupTuple() {
        }
        
        /** 组装 Group 元组四元组。 */
        public RaftGroupTuple(Node node, RequestProcessor processor,
            RaftGroupService raftGroupService,
            NacosStateMachine machine) {
            this.node = node;
            this.processor = processor;
            this.raftGroupService = raftGroupService;
            this.machine = machine;
        }
        
        /** 返回 JRaft Node。 */
        public Node getNode() {
            return node;
        }
        
        /** 返回绑定的 RequestProcessor。 */
        public RequestProcessor getProcessor() {
            return processor;
        }
        
        /** 返回 RaftGroupService。 */
        public RaftGroupService getRaftGroupService() {
            return raftGroupService;
        }
    }
    
}
