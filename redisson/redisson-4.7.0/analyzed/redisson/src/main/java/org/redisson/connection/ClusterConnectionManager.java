/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.connection;

import io.netty.buffer.ByteBuf;
import io.netty.util.Timeout;
import org.redisson.api.NodeType;
import org.redisson.api.RFuture;
import org.redisson.client.*;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.client.protocol.RedisStrictCommand;
import org.redisson.client.protocol.decoder.ClusterNodesDecoder;
import org.redisson.client.protocol.decoder.ObjectDecoder;
import org.redisson.cluster.ClusterNodeInfo;
import org.redisson.cluster.ClusterNodeInfo.Flag;
import org.redisson.cluster.ClusterPartition;
import org.redisson.cluster.ClusterPartition.Type;
import org.redisson.config.*;
import org.redisson.misc.RedisURI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Redis 集群模式连接管理器，继承 {@link MasterSlaveConnectionManager}。
 * <p>
 * 职责包括：
 * <ul>
 *   <li>启动时通过 CLUSTER NODES 解析拓扑并建立 master/slave 连接池</li>
 *   <li>周期性扫描集群状态，处理 master 故障转移、slave 增减与槽位迁移</li>
 *   <li>维护 slot → {@link MasterSlaveEntry} 映射及 CRC16 槽位计算</li>
 * </ul>
 *
 * @author Nikita Koksharov
 *
 */
public class ClusterConnectionManager extends MasterSlaveConnectionManager {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /** 槽号 → 当前集群分区快照（引用计数管理生命周期）。 */
    private final Map<Integer, ClusterPartition> lastPartitions = new ConcurrentHashMap<>();
    /** master URI → 分区对象，用于拓扑变更比对。 */
    private final Map<RedisURI, ClusterPartition> lastUri2Partition = new ConcurrentHashMap<>();

    /** 集群拓扑扫描定时任务。 */
    private volatile Timeout monitorFuture;

    /** 最近一次成功获取 CLUSTER NODES 的节点 URI。 */
    private volatile RedisURI lastClusterNode;

    /** CLUSTER NODES 命令封装（含解码器）。 */
    private RedisStrictCommand<List<ClusterNodeInfo>> clusterNodesCommand;

    /** 配置端点（如 ElastiCache Configuration Endpoint）主机名。 */
    private String configEndpointHostName;
    /** 配置端点认证用户名。 */
    private String configEndpointUsername;
    /** 配置端点认证密码。 */
    private String configEndpointPassword;

    /** 16384 个槽位各自对应的主从条目（原子数组）。 */
    private final AtomicReferenceArray<MasterSlaveEntry> slot2entry = new AtomicReferenceArray<>(MAX_SLOT);

    /** RedisClient → 主从条目，加速按客户端查找。 */
    private final Map<RedisClient, MasterSlaveEntry> client2entry = new ConcurrentHashMap<>();

    /** 集群模式专用配置。 */
    private ClusterServersConfig cfg;

    /** 构造集群连接管理器并设置 NAT 映射器。 */
    ClusterConnectionManager(ClusterServersConfig cfg, Config configCopy) {
        super(cfg, configCopy);
        this.serviceManager.setNatMapper(cfg.getNatMapper());
    }

    /** 从集群配置创建主从配置，传递 database 编号。 */
    @Override
    protected MasterSlaveServersConfig create(BaseMasterSlaveServersConfig<?> cfg) {
        this.cfg = (ClusterServersConfig) cfg;
        MasterSlaveServersConfig res = super.create(cfg);
        res.setDatabase(((ClusterServersConfig) cfg).getDatabase());
        return res;
    }

    /** 连接集群：遍历节点地址，执行 CLUSTER NODES 并初始化 master 条目。 */
    @Override
    public void doConnect(Function<RedisURI, String> hostnameMapper) {
        if (cfg.getScanInterval() <= 0) {
            throw new IllegalArgumentException("scanInterval setting can't be 0 or less");
        }

        if (cfg.getNodeAddresses().isEmpty()) {
            throw new IllegalArgumentException("At least one cluster node should be defined!");
        }

        Throwable lastException = null;
        List<String> failedMasters = new ArrayList<>();
        boolean skipCommandsDetection = false;
        for (String address : cfg.getNodeAddresses()) {
            RedisURI addr = new RedisURI(address);
            CompletionStage<RedisConnection> connectionFuture = connectToNode(cfg, addr, addr.getHost());
            try {
                RedisConnection connection = connectionFuture.toCompletableFuture()
                        .get(config.getConnectTimeout(), TimeUnit.MILLISECONDS);

                if (cfg.getNodeAddresses().size() == 1 && !addr.isIP()) {
                    configEndpointHostName = addr.getHost();
                    configEndpointUsername = addr.getUsername();
                    configEndpointPassword = addr.getPassword();
                }

                clusterNodesCommand = new RedisStrictCommand<List<ClusterNodeInfo>>("CLUSTER", "NODES",
                        new ObjectDecoder(new ClusterNodesDecoder(addr.getScheme())));

                if (!skipCommandsDetection) {
                    subscribeService.checkShardingSupport(cfg.getShardedSubscriptionMode(), connection);
                    subscribeService.checkPatternSupport(connection);
                    skipCommandsDetection = true;
                }

                List<ClusterNodeInfo> nodes = connection.sync(clusterNodesCommand);

                StringBuilder nodesValue = new StringBuilder();
                for (ClusterNodeInfo clusterNodeInfo : nodes) {
                    nodesValue.append(clusterNodeInfo.getNodeInfo()).append("\n");
                }
                log.info("Redis cluster nodes configuration got from {}:\n{}", connection.getRedisClient().getAddr(), nodesValue);

                lastClusterNode = addr;

                CompletableFuture<Collection<ClusterPartition>> partitionsFuture = parsePartitions(nodes);
                Collection<ClusterPartition> partitions;
                try {
                    partitions = partitionsFuture.join();
                } catch (CompletionException e) {
                    lastException = e.getCause();
                    break;
                }

                // 先并行构建条目，再在有限等待内注册已完成初始化的槽位。
                // 慢条目不会阻塞整体 init；单个 master/slave 失败不再阻止健康 master 注册
                Map<ClusterPartition, CompletableFuture<MasterSlaveEntry>> buildFutures = new LinkedHashMap<>();
                for (ClusterPartition partition : partitions) {
                    if (partition.isMasterFail()) {
                        failedMasters.add(partition.getMasterAddress().toURIString());
                        continue;
                    }
                    if (partition.getMasterAddress() == null) {
                        throw new IllegalStateException("Master node: " + partition.getNodeId() + " doesn't have an address.");
                    }

                    buildFutures.put(partition, buildMasterEntry(partition, cfg));
                }

                List<CompletableFuture<Void>> completedFutures = new ArrayList<>(buildFutures.size());
                for (CompletableFuture<MasterSlaveEntry> bf : buildFutures.values()) {
                    completedFutures.add(bf.handle((entry, ex) -> (Void) null));
                }
                CompletableFuture<Void> allSettled = CompletableFuture.allOf(completedFutures.toArray(new CompletableFuture[0]));
                long timeoutMillis = (long) config.getConnectTimeout()
                        * (Math.max(1, config.getMasterConnectionMinimumIdleSize())
                         + Math.max(1, config.getSlaveConnectionMinimumIdleSize())
                         + Math.max(1, config.getSubscriptionConnectionMinimumIdleSize()));
                try {
                    allSettled.get(timeoutMillis, TimeUnit.MILLISECONDS);
                } catch (TimeoutException e) {
                    lastException = new RedisConnectionException(
                            "Timed out after " + timeoutMillis
                                    + "ms waiting for cluster master entries to initialize", e);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    lastException = new RedisConnectionException(e);
                }

                for (Map.Entry<ClusterPartition, CompletableFuture<MasterSlaveEntry>> built : buildFutures.entrySet()) {
                    CompletableFuture<MasterSlaveEntry> bf = built.getValue();
                    if (bf.isDone() && !bf.isCompletedExceptionally()) {
                        registerMasterEntry(bf.join(), built.getKey());
                    } else {
                        if (bf.isCompletedExceptionally()) {
                            try {
                                bf.join();
                            } catch (CompletionException ce) {
                                lastException = ce.getCause();
                            }
                        }
                        // 构建失败（buildMasterEntry 已清理）或超时后才完成的条目：
                        // 关闭其连接池，防止泄漏或延迟注册
                        bf.whenComplete((entry, ex) -> {
                            if (entry != null) {
                                entry.shutdownAsync();
                            }
                        });
                    }
                }
                break;
            } catch (Exception e) {
                if (e instanceof CompletionException) {
                    e = (Exception) e.getCause();
                }
                lastException = e;
                if (e instanceof TimeoutException) {
                    log.warn("Connection timeout to {}", address);
                }
                if (e.getMessage() != null) {
                    log.warn(e.getMessage());
                }
            }
        }

        if (lastPartitions.isEmpty()) {
            internalShutdown();
            if (failedMasters.isEmpty()) {
                throw new RedisConnectionException("Can't connect to servers!", lastException);
            } else {
                throw new RedisConnectionException("Can't connect to servers! Failed masters according to cluster status: " + failedMasters, lastException);
            }
        }

        if (cfg.isCheckSlotsCoverage() && lastPartitions.size() != MAX_SLOT) {
            internalShutdown();
            if (failedMasters.isEmpty()) {
                throw new RedisConnectionException("Not all slots covered! Only " + lastPartitions.size() + " slots are available. Set checkSlotsCoverage = false to avoid this check.", lastException);
            } else {
                throw new RedisConnectionException("Not all slots covered! Only " + lastPartitions.size() + " slots are available. Set checkSlotsCoverage = false to avoid this check. Failed masters according to cluster status: " + failedMasters, lastException);
            }
        }

        scheduleClusterChangeCheck(cfg);
    }

    /** 返回所有已注册的主从条目（懒连接模式下先触发 connect）。 */
    @Override
    public Collection<MasterSlaveEntry> getEntrySet() {
        lazyConnect();

        return client2entry.values();
    }

    /** 按 RedisURI 查找主或从条目。 */
    @Override
    public MasterSlaveEntry getEntry(RedisURI addr) {
        lazyConnect();

        for (MasterSlaveEntry entry : client2entry.values()) {
            if (addr.equals(entry.getClient().getAddr())) {
                return entry;
            }
            if (entry.hasSlave(addr)) {
                return entry;
            }
        }
        return null;
    }

    /** 按 RedisClient 查找条目（含从节点）。 */
    @Override
    public MasterSlaveEntry getEntry(RedisClient redisClient) {
        lazyConnect();

        MasterSlaveEntry entry = client2entry.get(redisClient);
        if (entry != null) {
            return entry;
        }

        for (MasterSlaveEntry mentry : client2entry.values()) {
            if (mentry.hasSlave(redisClient)) {
                return mentry;
            }
        }
        return null;
    }

    /** 按 InetSocketAddress 查找条目。 */
    @Override
    public MasterSlaveEntry getEntry(InetSocketAddress address) {
        lazyConnect();

        for (MasterSlaveEntry entry : client2entry.values()) {
            InetSocketAddress addr = entry.getClient().getAddr();
            if (addr.getAddress().equals(address.getAddress()) && addr.getPort() == address.getPort()) {
                return entry;
            }
            if (entry.hasSlave(address)) {
                return entry;
            }
        }
        return null;
    }

    /** 集群 master 故障转移：更新 slot 映射与 client2entry。 */
    @Override
    protected CompletableFuture<RedisClient> changeMaster(int slot, RedisURI address) {
        MasterSlaveEntry entry = getEntry(slot);
        RedisClient oldClient = entry.getClient();
        CompletableFuture<RedisClient> future = super.changeMaster(slot, address);
        return future.thenApply(res -> {
            client2entry.remove(oldClient);
            client2entry.put(entry.getClient(), entry);
            return res;
        });
    }

    /** 按槽号 O(1) 查找主从条目。 */
    @Override
    public MasterSlaveEntry getEntry(int slot) {
        lazyConnect();

        return slot2entry.get(slot);
    }

    /** 注册槽位条目，引用计数递增并更新 client2entry。 */
    private void addEntry(Integer slot, MasterSlaveEntry entry) {
        MasterSlaveEntry oldEntry = slot2entry.getAndSet(slot, entry);
        if (oldEntry != entry) {
            entry.incReference();
            shutdownEntry(oldEntry, entry);
        }
        client2entry.put(entry.getClient(), entry);
    }

    /** 移除槽位条目并关闭无引用的旧条目。 */
    private void removeEntry(Integer slot) {
        MasterSlaveEntry entry = slot2entry.getAndSet(slot, null);
        shutdownEntry(entry, null);
    }

    private void removeEntry(Integer slot, MasterSlaveEntry entry) {
        if (slot2entry.compareAndSet(slot, entry, null)) {
            shutdownEntry(entry, null);
        }
    }

    /** 引用计数归零时关闭条目：断开节点、nodeDown、取消订阅。 */
    private void shutdownEntry(MasterSlaveEntry entry, MasterSlaveEntry newEntry) {
        if (entry != null && entry.decReference() == 0) {
            entry.getAllEntries().forEach(e -> {
                RedisURI uri = new RedisURI(e.getClient().getConfig().getAddress().getScheme(),
                        e.getClient().getAddr().getAddress().getHostAddress(),
                        e.getClient().getAddr().getPort());
                disconnectNode(uri);
                e.nodeDown();
            });
            entry.masterDown();
            entry.shutdownAsync();
            entry.setReplacedBy(newEntry);
            subscribeService.remove(entry);
            RedisURI uri = new RedisURI(entry.getClient().getConfig().getAddress().getScheme(),
                                        entry.getClient().getAddr().getAddress().getHostAddress(),
                                        entry.getClient().getAddr().getPort());
            disconnectNode(uri);

            client2entry.remove(entry.getClient());

            String slaves = entry.getAllEntries().stream()
                    .filter(e -> !e.getClient().getAddr().equals(entry.getClient().getAddr()))
                    .map(e -> e.getClient().toString())
                    .collect(Collectors.joining(","));
            log.info("{} master and related slaves: {} removed", entry.getClient().getAddr(), slaves);
        }
    }

    /** 集群模式下从节点连接标记 readOnly（受 ReadMode 影响）。 */
    @Override
    protected RedisClientConfig createRedisConfig(NodeType type, RedisURI address, int timeout, int commandTimeout, String sslHostname) {
        RedisClientConfig result = super.createRedisConfig(type, address, timeout, commandTimeout, sslHostname);
        result.setReadOnly(type == NodeType.SLAVE && config.getReadMode() != ReadMode.MASTER);
        return result;
    }
    
    /** 异步添加新 master 分区并注册槽位。 */
    private CompletionStage<Void> addMasterEntry(ClusterPartition partition, ClusterServersConfig cfg) {
        return buildMasterEntry(partition, cfg).thenAccept(entry -> registerMasterEntry(entry, partition));
    }

    /** 构建单个 master 的主从条目（含 slave 初始化）。 */
    private CompletableFuture<MasterSlaveEntry> buildMasterEntry(ClusterPartition partition, ClusterServersConfig cfg) {
        if (partition.isMasterFail()) {
            RedisException e = new RedisException("Failed to add master: " +
                    partition.getMasterAddress() + " for slot ranges: " +
                    partition.getSlotRanges() + ". Reason - server has FAIL flag");

            if (partition.getSlotsAmount() == 0) {
                e = new RedisException("Failed to add master: " +
                        partition.getMasterAddress() + ". Reason - server has FAIL flag");
            }
            CompletableFuture<MasterSlaveEntry> result = new CompletableFuture<>();
            result.completeExceptionally(e);
            return result;
        }

        CompletionStage<RedisConnection> connectionFuture = connectToNode(cfg, partition.getMasterAddress(), configEndpointHostName);
        return connectionFuture.thenCompose(connection -> {
            MasterSlaveServersConfig config = create(cfg);
            config.setMasterAddress(partition.getMasterAddress().toURIString());

            MasterSlaveEntry entry;
            if (config.isSlaveNotUsed()) {
                entry = new SingleEntry(this, config);
            } else {
                Set<String> slaveAddresses = partition.getSlaveAddresses().stream()
                                                                            .filter(r -> !partition.getFailedSlaveAddresses().contains(r))
                                                                            .map(r -> r.toURIString())
                                                                            .collect(Collectors.toSet());
                config.setSlaveAddresses(slaveAddresses);

                entry = new MasterSlaveEntry(ClusterConnectionManager.this, config);
            }

            CompletableFuture<RedisClient> f = entry.setupMasterEntry(new RedisURI(config.getMasterAddress()), configEndpointHostName);
            CompletableFuture<MasterSlaveEntry> entryFuture = f.thenCompose(masterClient -> {
                if (!config.isSlaveNotUsed()) {
                    return entry.initSlaveBalancer(r -> configEndpointHostName).handle((r, ex) -> {
                        if (ex != null) {
                            log.warn("Unable to init slave balancer for master: {} slot ranges: {}. "
                                    + "Registering master-only. Slaves will be connected by the cluster monitor.",
                                    partition.getMasterAddress(), partition.getSlotRanges(), ex);
                        } else if (!partition.getSlaveAddresses().isEmpty()) {
                            log.info("slaves: {} added for master: {} slot ranges: {}",
                                    partition.getSlaveAddresses(), partition.getMasterAddress(), partition.getSlotRanges());
                            if (!partition.getFailedSlaveAddresses().isEmpty()) {
                                log.warn("slaves: {} down for master: {} slot ranges: {}",
                                        partition.getFailedSlaveAddresses(), partition.getMasterAddress(), partition.getSlotRanges());
                            }
                        }

                        log.info("master: {} added for slot ranges: {}", partition.getMasterAddress(), partition.getSlotRanges());
                        return entry;
                    });
                }

                log.info("master: {} added for slot ranges: {}", partition.getMasterAddress(), partition.getSlotRanges());
                return CompletableFuture.completedFuture(entry);
            });
            entryFuture.whenComplete((e, ex) -> {
                if (ex != null) {
                    entry.shutdownAsync();
                }
            });
            return entryFuture;
        }).toCompletableFuture();
    }

    /** 将 master 条目注册到其所有槽位。 */
    private void registerMasterEntry(MasterSlaveEntry entry, ClusterPartition partition) {
        for (Integer slot : partition.getSlots()) {
            addEntry(slot, entry);
            addPartition(slot, partition);
        }
        if (partition.getSlotsAmount() > 0) {
            lastUri2Partition.put(partition.getMasterAddress(), partition);
        }
    }

    /** 更新槽位分区快照（引用计数管理）。 */
    private void addPartition(Integer slot, ClusterPartition partition) {
        partition.incReference();
        ClusterPartition prevPartiton = lastPartitions.put(slot, partition);
        if (prevPartiton != null
                && prevPartiton.decReference() == 0) {
            lastUri2Partition.remove(prevPartiton.getMasterAddress());
        }
    }

    /** 按 scanInterval 调度下一次集群拓扑扫描。 */
    private void scheduleClusterChangeCheck(ClusterServersConfig cfg) {
        monitorFuture = serviceManager.newTimeout(t -> {
            if (configEndpointHostName != null) {
                String address = cfg.getNodeAddresses().iterator().next();
                RedisURI uri = new RedisURI(address);
                CompletableFuture<List<RedisURI>> allNodes = serviceManager.resolveAll(uri);
                allNodes.whenComplete((nodes, ex) -> {
                    log.debug("{} resolved to {}", uri, nodes);

                    AtomicReference<Throwable> lastException = new AtomicReference<>(ex);
                    if (ex != null) {
                        checkClusterState(cfg, Collections.emptyIterator(), lastException, nodes);
                        return;
                    }

                    Iterator<RedisURI> nodesIterator = nodes.iterator();
                    checkClusterState(cfg, nodesIterator, lastException, nodes);
                });
            } else {
                AtomicReference<Throwable> lastException = new AtomicReference<>();
                List<RedisURI> nodes = new ArrayList<>();
                List<RedisURI> slaves = new ArrayList<>();

                for (ClusterPartition partition : getLastPartitions()) {
                    if (!partition.isMasterFail()) {
                        nodes.add(partition.getMasterAddress());
                    }

                    Set<RedisURI> partitionSlaves = new HashSet<>(partition.getSlaveAddresses());
                    partitionSlaves.removeAll(partition.getFailedSlaveAddresses());
                    slaves.addAll(partitionSlaves);
                }
                Collections.shuffle(nodes);
                Collections.shuffle(slaves);

                // 优先尝试 master 节点，再追加 slave
                nodes.addAll(slaves);

                Iterator<RedisURI> nodesIterator = nodes.iterator();

                checkClusterState(cfg, nodesIterator, lastException, nodes);
            }
        }, cfg.getScanInterval(), TimeUnit.MILLISECONDS);
    }

    /** 依次尝试节点列表获取 CLUSTER NODES，失败则切换下一节点。 */
    private void checkClusterState(ClusterServersConfig cfg, Iterator<RedisURI> iterator, AtomicReference<Throwable> lastException, List<RedisURI> allNodes) {
        if (!iterator.hasNext()) {
            if (lastException.get() != null) {
                log.error("Can't update cluster state using nodes: {}. A new attempt will be made.", allNodes, lastException.getAndSet(null));
            }
            scheduleClusterChangeCheck(cfg);
            return;
        }
        if (serviceManager.isShuttingDown()) {
            return;
        }
        RedisURI uri = iterator.next();
        CompletionStage<RedisConnection> connectionFuture = connectToNode(cfg, uri, configEndpointHostName);
        connectionFuture.whenComplete((connection, e) -> {
            if (e != null) {
                if (!lastException.compareAndSet(null, e)) {
                    lastException.get().addSuppressed(e);
                }
                checkClusterState(cfg, iterator, lastException, allNodes);
                return;
            }

            updateClusterState(cfg, connection, iterator, uri, lastException, allNodes);
        });
    }

    /** 解析 CLUSTER NODES 响应并链式检查 master/slave/槽位变更。 */
    private void updateClusterState(ClusterServersConfig cfg, RedisConnection connection,
            Iterator<RedisURI> iterator, RedisURI uri, AtomicReference<Throwable> lastException, List<RedisURI> allNodes) {
        RFuture<List<ClusterNodeInfo>> future = connection.async(StringCodec.INSTANCE, clusterNodesCommand);
        future.handle((nodes, e) -> {
                if (e != null) {
                    if (!lastException.compareAndSet(null, e)) {
                        lastException.get().addSuppressed(e);
                    }
                    checkClusterState(cfg, iterator, lastException, allNodes);
                    return null;
                }

                if (nodes.isEmpty()) {
                    log.debug("cluster nodes state got from {}: doesn't contain any nodes", connection.getRedisClient().getAddr());
                    checkClusterState(cfg, iterator, lastException, allNodes);
                    return null;
                }

                lastClusterNode = uri;

                if (log.isDebugEnabled()) {
                    StringBuilder nodesValue = new StringBuilder();
                    for (ClusterNodeInfo clusterNodeInfo : nodes) {
                        nodesValue.append(clusterNodeInfo.getNodeInfo()).append("\n");
                    }

                    log.debug("Cluster nodes state got from {}:\n{}", connection.getRedisClient().getAddr(), nodesValue);
                    serviceManager.setLastClusterNodes(nodesValue.toString());
                }

                CompletableFuture<Collection<ClusterPartition>> newPartitionsFuture = parsePartitions(nodes);
                newPartitionsFuture
                        .whenComplete((r, ex) -> {
                            if (ex != null) {
                                StringBuilder nodesValue = new StringBuilder();
                                for (ClusterNodeInfo clusterNodeInfo : nodes) {
                                    nodesValue.append(clusterNodeInfo.getNodeInfo()).append("\n");
                                }
                                log.error("Unable to parse cluster nodes state got from: {}:\n{}", connection.getRedisClient().getAddr(), nodesValue, ex);

                                if (!lastException.compareAndSet(null, ex)) {
                                    lastException.get().addSuppressed(ex);
                                }
                                checkClusterState(cfg, iterator, lastException, allNodes);
                            }
                        })
                        .thenCompose(newPartitions -> checkMasterNodesChange(cfg, newPartitions))
                        .thenCompose(r -> newPartitionsFuture)
                        .thenCompose(newPartitions -> checkSlaveNodesChange(newPartitions))
                        .thenCompose(r -> newPartitionsFuture)
                        .whenComplete((newPartitions, ex) -> {
                            if (newPartitions != null
                                    && !newPartitions.isEmpty()) {
                                try {
                                    checkSlotsMigration(newPartitions);
                                    checkSlotsChange(newPartitions);
                                } catch (Exception exc) {
                                    log.error(exc.getMessage(), exc);
                                }
                            }
                            if (ex != null) {
                                log.error(ex.getMessage(), ex);
                            }

                            scheduleClusterChangeCheck(cfg);
                        });
                return null;
        }).exceptionally(ex -> {
                log.error("Unable to update cluster state", ex);
                scheduleClusterChangeCheck(cfg);
                return null;
        });
    }

    /** 比对新旧分区，增删 slave 并更新 fail/up 状态。 */
    private CompletableFuture<Void> checkSlaveNodesChange(Collection<ClusterPartition> newPartitions) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (ClusterPartition newPart : newPartitions) {
            ClusterPartition currentPart = lastUri2Partition.get(newPart.getMasterAddress());
            if (currentPart == null) {
                continue;
            }

            MasterSlaveEntry entry = getEntry(currentPart.getSlotRanges().iterator().next().getStartSlot());
            // 必须先调用以清除过期的 failedSlaveAddresses
            CompletableFuture<Set<RedisURI>> addedSlavesFuture = addRemoveSlaves(entry, currentPart, newPart);
            CompletableFuture<Void> f = addedSlavesFuture.thenCompose(addedSlaves -> {
                // 是否有 slave 从 FAIL 恢复为可用？
                return upDownSlaves(entry, currentPart, newPart, addedSlaves);
            });
            futures.add(f);
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                                    .exceptionally(e -> {
                                        if (e != null) {
                                            log.error("Unable to add/remove slave nodes", e);
                                        }
                                        return null;
                                    });
    }

    /** 处理 slave 上线（slaveUp）与下线（slaveDown）状态同步。 */
    private CompletableFuture<Void> upDownSlaves(MasterSlaveEntry entry, ClusterPartition currentPart, ClusterPartition newPart, Set<RedisURI> addedSlaves) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        List<RedisURI> nonFailedSlaves = currentPart.getFailedSlaveAddresses().stream()
                .filter(uri -> !addedSlaves.contains(uri) && !newPart.getFailedSlaveAddresses().contains(uri))
                .collect(Collectors.toList());
        nonFailedSlaves.forEach(uri -> {
            if (entry.hasSlave(uri)) {
                CompletableFuture<Boolean> f = entry.slaveUpNoMasterExclusionAsync(uri);
                f = f.thenApply(v -> {
                    if (v) {
                        log.info("slave: {} is up for slot ranges: {}", uri, currentPart.getSlotRanges());
                        currentPart.removeFailedSlaveAddress(uri);
                        entry.excludeMasterFromSlaves(uri);
                    }
                    return v;
                });
                futures.add(f);
            }
        });

        newPart.getFailedSlaveAddresses().stream()
                .filter(uri -> !currentPart.getFailedSlaveAddresses().contains(uri))
                .forEach(uri -> {
                    currentPart.addFailedSlaveAddress(uri);
                    boolean slaveDown = entry.slaveDown(uri);
                    if (config.isSlaveNotUsed() || slaveDown) {
                        disconnectNode(uri);
                        log.warn("slave: {} has down for slot ranges: {}", uri, currentPart.getSlotRanges());
                    }
                });
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    /** 检测并执行 slave 增删，返回新增加的 slave 集合。 */
    private CompletableFuture<Set<RedisURI>> addRemoveSlaves(MasterSlaveEntry entry, ClusterPartition currentPart, ClusterPartition newPart) {
        Set<RedisURI> removedSlaves = new HashSet<>(currentPart.getSlaveAddresses());
        removedSlaves.removeAll(newPart.getSlaveAddresses());

        if (!removedSlaves.isEmpty()) {
            log.info("removed slaves detected for master {}. current slaves {} last slaves {}",
                    currentPart.getMasterAddress(), currentPart.getSlaveAddresses(), newPart.getSlaveAddresses());
        }

        for (RedisURI uri : removedSlaves) {
            currentPart.removeSlaveAddress(uri);

            boolean slaveDown = entry.slaveDown(uri);
            if (config.isSlaveNotUsed() || slaveDown) {
                disconnectNode(uri);
                log.info("slave {} removed for master {} and slot ranges: {}",
                        currentPart.getMasterAddress(), uri, currentPart.getSlotRanges());
            }
        }

        Set<RedisURI> addedSlaves = newPart.getSlaveAddresses().stream()
                                                                .filter(uri -> (!currentPart.getSlaveAddresses().contains(uri)
                                                                                            && !newPart.getFailedSlaveAddresses().contains(uri))
                                                                                    || (currentPart.getSlaveAddresses().contains(uri)
                                                                                            && currentPart.getFailedSlaveAddresses().contains(uri)
                                                                                            && !newPart.getFailedSlaveAddresses().contains(uri)
                                                                                            && !entry.hasSlave(uri))
                                                                )
                                                                .collect(Collectors.toSet());

        if (!addedSlaves.isEmpty()) {
            log.info("added slaves detected for master {}. current slaves {} last slaves {} last failed slaves {}",
                    currentPart.getMasterAddress(), currentPart.getSlaveAddresses(),
                    newPart.getSlaveAddresses(), newPart.getFailedSlaveAddresses());
        }

        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (RedisURI uri : addedSlaves) {
            ClientConnectionsEntry slaveEntry = entry.getEntry(uri);
            if (slaveEntry != null) {
                CompletableFuture<Boolean> slaveUpFuture = entry.slaveUpNoMasterExclusionAsync(uri);
                slaveUpFuture = slaveUpFuture.thenApply(v -> {
                    if (v) {
                        currentPart.addSlaveAddress(uri);
                        currentPart.removeFailedSlaveAddress(uri);
                        log.info("slave: {} unfreezed for master {} and slot ranges: {}",
                                currentPart.getMasterAddress(), uri, currentPart.getSlotRanges());
                        entry.excludeMasterFromSlaves(uri);
                    }
                    return v;
                });
                futures.add(slaveUpFuture);
                continue;
            }
            
            if (config.isSlaveNotUsed()) {
                // 此模式下不连接 slave，但仍须更新分区状态，
                // 避免每次拓扑扫描重复检测为新增 slave
                currentPart.addSlaveAddress(uri);
                currentPart.removeFailedSlaveAddress(uri);
                continue;
            }

            CompletableFuture<Void> slaveUpFuture = entry.addSlave(uri, configEndpointHostName);
            CompletableFuture<Void> f = slaveUpFuture.thenAccept(res -> {
                currentPart.addSlaveAddress(uri);
                currentPart.removeFailedSlaveAddress(uri);
                log.info("slave: {} added for master {} and slot ranges: {}",
                        currentPart.getMasterAddress(), uri, currentPart.getSlotRanges());
                entry.excludeMasterFromSlaves(uri);
            });
            futures.add(f);
        }

        CompletableFuture<Void> f = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        return f.thenApply(r -> addedSlaves);
    }

    /** 在分区集合中查找包含指定槽的分区。 */
    private ClusterPartition find(Collection<ClusterPartition> partitions, Integer slot) {
        return partitions.stream().filter(p -> p.hasSlot(slot)).findFirst().orElseThrow(() -> {
            return new IllegalStateException("Unable to find partition with slot " + slot);
        });
    }

    /** 检测 master 故障转移与新 master 加入。 */
    private CompletableFuture<Void> checkMasterNodesChange(ClusterServersConfig cfg, Collection<ClusterPartition> newPartitions) {
        Map<RedisURI, ClusterPartition> addedPartitions = new HashMap<>();
        Set<RedisURI> mastersElected = new HashSet<>();
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (ClusterPartition newPart : newPartitions) {
            if (newPart.getSlotsAmount() == 0) {
                continue;
            }

            ClusterPartition currentPart = lastUri2Partition.get(newPart.getMasterAddress());
            boolean masterFound = currentPart != null;
            if (masterFound && newPart.isMasterFail()) {
                for (Integer slot : currentPart.getSlots()) {
                    ClusterPartition newMasterPart = find(newPartitions, slot);
                    // 该槽位是否已选举新 master？
                    if (!Objects.equals(newMasterPart.getMasterAddress(), currentPart.getMasterAddress())) {
                        RedisURI newUri = newMasterPart.getMasterAddress();
                        RedisURI oldUri = currentPart.getMasterAddress();

                        mastersElected.add(newUri);

                        CompletableFuture<RedisClient> future = changeMaster(slot, newUri);
                        currentPart.setMasterAddress(newUri);
                        CompletableFuture<RedisClient> f = future.whenComplete((res, e) -> {
                            if (e != null) {
                                currentPart.setMasterAddress(oldUri);
                            } else {
                                disconnectNode(oldUri);
                            }
                        });
                        futures.add(f);
                    }
                }
            }

            if (!masterFound && !newPart.isMasterFail()) {
                addedPartitions.put(newPart.getMasterAddress(), newPart);
            }
        }

        addedPartitions.keySet().removeAll(mastersElected);
        for (ClusterPartition newPart : addedPartitions.values()) {
            CompletionStage<Void> future = addMasterEntry(newPart, cfg);
            futures.add(future.toCompletableFuture());
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                                    .exceptionally(e -> {
                                        if (e != null) {
                                            log.error("Unable to add/change master node", e);
                                        }
                                        return null;
                                    });
    }

    /** 检测槽位增删（集群扩缩容场景）。 */
    private void checkSlotsChange(Collection<ClusterPartition> newPartitions) {
        int newSlotsAmount = newPartitions.stream()
                                .mapToInt(ClusterPartition::getSlotsAmount)
                                .sum();
        if (newSlotsAmount == lastPartitions.size() && lastPartitions.size() == MAX_SLOT) {
            return;
        }

        Set<Integer> removedSlots = lastPartitions.keySet().stream()
                .filter(s -> newPartitions.stream().noneMatch(p -> p.hasSlot(s)))
                .collect(Collectors.toSet());

        for (Integer slot : removedSlots) {
            ClusterPartition p = lastPartitions.remove(slot);
            if (p != null
                    && p.decReference() == 0
                        && lastUri2Partition.size() > 1) {
                lastUri2Partition.remove(p.getMasterAddress());
            }
            removeEntry(slot);
        }
        if (!removedSlots.isEmpty()) {
            log.info("{} slots removed", removedSlots.size());
        }

        int addedSlots = 0;
        for (ClusterPartition clusterPartition : newPartitions) {
            MasterSlaveEntry entry = getEntry(clusterPartition.getMasterAddress());
            for (Integer slot : clusterPartition.getSlots()) {
                if (lastPartitions.containsKey(slot)) {
                    continue;
                }

                if (entry != null) {
                    addEntry(slot, entry);
                    addPartition(slot, clusterPartition);
                    lastUri2Partition.put(clusterPartition.getMasterAddress(), clusterPartition);
                    addedSlots++;
                }
            }
        }
        if (addedSlots > 0) {
            log.info("{} slots added", addedSlots);
        }
    }

    /** 检测同一节点内的槽位迁移并更新映射，触发 Pub/Sub 重挂接。 */
    private void checkSlotsMigration(Collection<ClusterPartition> newPartitions) {
        Collection<ClusterPartition> clusterLastPartitions = getLastPartitions();

        // https://github.com/redisson/redisson/issues/3635 — 按 nodeId 索引条目
        Map<String, MasterSlaveEntry> nodeEntries = clusterLastPartitions.stream()
                                                                          .collect(Collectors.toMap(p -> p.getNodeId(),
                                                                                    p -> getEntry(p.getSlotRanges().iterator().next().getStartSlot())));

        Set<Integer> changedSlots = new HashSet<>();
        for (ClusterPartition currentPartition : clusterLastPartitions) {
            String nodeId = currentPartition.getNodeId();

            for (ClusterPartition newPartition : newPartitions) {
                if (!Objects.equals(nodeId, newPartition.getNodeId())
                        || newPartition.getSlotRanges().equals(currentPartition.getSlotRanges())) {
                    continue;
                }

                MasterSlaveEntry entry = nodeEntries.get(nodeId);
                BitSet addedSlots = newPartition.copySlots();
                addedSlots.andNot(currentPartition.slots());

                addedSlots.stream().forEach(slot -> {
                    addEntry(slot, entry);
                    addPartition(slot, currentPartition);
                    changedSlots.add(slot);
                });
                if (!addedSlots.isEmpty()) {
                    lastUri2Partition.put(currentPartition.getMasterAddress(), currentPartition);
                    log.info("{} slots added to {}", addedSlots.cardinality(), currentPartition.getMasterAddress());
                }

                BitSet removedSlots = currentPartition.copySlots();
                removedSlots.andNot(newPartition.slots());

                removedSlots.stream().forEach(slot -> {
                    if (lastPartitions.remove(slot, currentPartition)) {
                        if (currentPartition.decReference() == 0
                                && lastUri2Partition.size() > 1) {
                            lastUri2Partition.remove(currentPartition.getMasterAddress());
                        }
                        removeEntry(slot, entry);
                        changedSlots.add(slot);
                    }
                });
                if (!removedSlots.isEmpty()) {
                    log.info("{} slots removed from {}", removedSlots.cardinality(), currentPartition.getMasterAddress());
                }

                if (!addedSlots.isEmpty() || !removedSlots.isEmpty()) {
                    // https://github.com/redisson/redisson/issues/3695 — 槽位变更时同步更新 slotRanges
                    currentPartition.updateSlotRanges(newPartition.getSlotRanges(), newPartition.slots());
                }
                break;
            }
        }

        // 槽位变更后重挂接受影响频道的 Pub/Sub
        changedSlots.forEach(subscribeService::reattachPubSub);
    }
    
    /** 在字节数组中查找指定元素下标。 */
    private int indexOf(byte[] array, byte element) {
        for (int i = 0; i < array.length; ++i) {
            if (array[i] == element) {
                return i;
            }
        }
        return -1;
    }  
    
    /** 计算 key 槽号，支持 hash tag（{...} 内子串）。 */
    @Override
    public int calcSlot(byte[] key) {
        if (key == null) {
            return 0;
        }

        int start = indexOf(key, (byte) '{');
        if (start != -1) {
            int end = indexOf(key, (byte) '}');
            if (end != -1 && start + 1 < end) {
                key = Arrays.copyOfRange(key, start + 1, end);
            }
        }
        
        int result = CRC16.crc16(key) % MAX_SLOT;
        return result;
    }

    /** 对 ByteBuf key 计算槽号（支持 hash tag）。 */
    @Override
    public int calcSlot(ByteBuf key) {
        if (key == null) {
            return 0;
        }

        int start = key.indexOf(key.readerIndex(), key.readerIndex() + key.readableBytes(), (byte) '{');
        if (start != -1) {
            int end = key.indexOf(start + 1, key.readerIndex() + key.readableBytes(), (byte) '}');
            if (end != -1 && start + 1 < end) {
                key = key.slice(start + 1, end-start - 1);
            }
        }

        int result = CRC16.crc16(key) % MAX_SLOT;
        log.debug("slot {} for {}", result, key);
        return result;
    }

    /** 对字符串 key 计算槽号（支持 hash tag）。 */
    @Override
    public int calcSlot(String key) {
        if (key == null) {
            return 0;
        }

        int start = key.indexOf('{');
        if (start != -1) {
            int end = key.indexOf('}');
            if (end != -1 && start + 1 < end) {
                key = key.substring(start + 1, end);
            }
        }

        int result = CRC16.crc16(key.getBytes()) % MAX_SLOT;
        log.debug("slot {} for {}", result, key);
        return result;
    }

    /** 解析 CLUSTER NODES 输出为 ClusterPartition 集合（含 DNS 解析与 master-link 检查）。 */
    private CompletableFuture<Collection<ClusterPartition>> parsePartitions(List<ClusterNodeInfo> nodes) {
        Map<String, ClusterPartition> partitions = new ConcurrentHashMap<>();
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (ClusterNodeInfo clusterNodeInfo : nodes) {
            if (clusterNodeInfo.containsFlag(Flag.NOADDR)
                    || clusterNodeInfo.containsFlag(Flag.HANDSHAKE)
                        || clusterNodeInfo.getAddress() == null
                            || (clusterNodeInfo.getSlotRanges().isEmpty() && clusterNodeInfo.containsFlag(Flag.MASTER))) {
                // 跳过无地址、握手或无效节点
                continue;
            }

            String masterId;
            if (clusterNodeInfo.containsFlag(Flag.SLAVE)) {
                masterId = clusterNodeInfo.getSlaveOf();
            } else {
                masterId = clusterNodeInfo.getNodeId();
            }

            if (masterId == null) {
                // 无 master 关联则跳过
                continue;
            }

            RedisURI uri;
            if (clusterNodeInfo.getHostName() != null) {
                uri = new RedisURI(clusterNodeInfo.getAddress().getScheme() + "://" + clusterNodeInfo.getHostName() +
                        ":" + clusterNodeInfo.getAddress().getPort());
            } else {
                uri = clusterNodeInfo.getAddress();
            }

            CompletableFuture<List<RedisURI>> ipsFuture = serviceManager.resolveAll(uri);
            CompletableFuture<Void> f = ipsFuture.handle((r, ex) -> {
                        if (ex != null) {
                            RedisURI mappedUri = serviceManager.toURI(clusterNodeInfo.getAddress().getScheme(), clusterNodeInfo.getAddress().getHost(), "" + clusterNodeInfo.getAddress().getPort());
                            return Collections.singletonList(mappedUri);
                        }
                        return r;
                    })
                    .thenCompose(addresses -> {
                int index = 0;
                if (addresses.size() > 1) {
                    addresses.sort(Comparator.comparing(RedisURI::getHost));
                }

                RedisURI address = addresses.get(index);

                if (configEndpointPassword != null) {
                    address = new RedisURI(configEndpointUsername, configEndpointPassword, address);
                }

                if (addresses.size() > 1) {
                    for (RedisURI addr : addresses) {
                        for (ClusterPartition value : lastUri2Partition.values()) {
                            if (value.getNodeId().equals(clusterNodeInfo.getNodeId())
                                    && value.getMasterAddress().equals(addr)) {
                                address = addr;
                                break;
                            }
                        }
                    }
                }

                if (addresses.size() == 1) {
                    if (!uri.equals(address)) {
                        log.debug("{} resolved to {}", uri, address);
                    }
                } else {
                    log.debug("{} resolved to {} and {} selected", uri, addresses, address);
                }

                if (clusterNodeInfo.containsFlag(Flag.SLAVE)) {
                    ClusterPartition masterPartition = partitions.computeIfAbsent(masterId, k -> new ClusterPartition(masterId));
                    ClusterPartition slavePartition = partitions.computeIfAbsent(clusterNodeInfo.getNodeId(),
                            k -> new ClusterPartition(clusterNodeInfo.getNodeId()));
                    slavePartition.setType(Type.SLAVE);
                    slavePartition.setParent(masterPartition);

                    masterPartition.addSlaveAddress(address);
                    if (clusterNodeInfo.containsFlag(Flag.FAIL)) {
                        masterPartition.addFailedSlaveAddress(address);
                    }

                    if (cfg.isCheckMasterLinkStatus()) {
                        CompletionStage<RedisConnection> connectionFuture = connectToNode(cfg, address, configEndpointHostName);
                        RedisURI finalAddress = address;
                        return connectionFuture.thenCompose(con -> {
                            RFuture<Map<String, String>> future = con.async(StringCodec.INSTANCE, RedisCommands.INFO_REPLICATION);
                            return future.handle((info, ex) -> {
                                if (ex != null) {
                                    if (ex instanceof RedisTimeoutException) {
                                        return null;
                                    }

                                    throw new CompletionException(ex);
                                }

                                String masterLinkStatus = info.getOrDefault("master_link_status", "");
                                if ("down".equals(masterLinkStatus)) {
                                    masterPartition.addFailedSlaveAddress(finalAddress);
                                }
                                return null;
                            });
                        });
                    }
                    return CompletableFuture.<Void>completedFuture(null);

                } else if (clusterNodeInfo.containsFlag(Flag.MASTER)) {
                    ClusterPartition masterPartition = partitions.computeIfAbsent(masterId, k -> new ClusterPartition(masterId));
                    masterPartition.setSlotRanges(clusterNodeInfo.getSlotRanges());
                    masterPartition.setMasterAddress(address);
                    masterPartition.setType(Type.MASTER);
                    if (clusterNodeInfo.containsFlag(Flag.FAIL)) {
                        masterPartition.setMasterFail(true);
                    }
                }

                return CompletableFuture.<Void>completedFuture(null);


            }).exceptionally(ex -> {
                if (clusterNodeInfo.containsFlag(Flag.FAIL)
                        || clusterNodeInfo.containsFlag(Flag.EVENTUAL_FAIL)) {
                    return null;
                }

                log.error(ex.getMessage(), ex);
                return null;
            });
            futures.add(f);
        }

        CompletableFuture<Void> future = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        return future.thenApply(r -> {
            addCascadeSlaves(partitions.values());

            List<ClusterPartition> ps = partitions.values()
                    .stream()
                    .filter(cp -> cp.getType() == Type.MASTER
                                    && cp.getMasterAddress() != null
                                        && ((!cp.slots().isEmpty() && partitions.size() == 1) || partitions.size() > 1))
                    .collect(Collectors.toList());
            return ps;
        });
    }

    /** 将级联 slave 的地址合并到父 master 分区并移除 slave 分区对象。 */
    private void addCascadeSlaves(Collection<ClusterPartition> partitions) {
        Iterator<ClusterPartition> iter = partitions.iterator();
        while (iter.hasNext()) {
            ClusterPartition cp = iter.next();
            if (cp.getType() != Type.SLAVE) {
                continue;
            }
            
            if (cp.getParent() != null && cp.getParent().getType() == Type.MASTER) {
                ClusterPartition parent = cp.getParent();
                for (RedisURI addr : cp.getSlaveAddresses()) {
                    parent.addSlaveAddress(addr);
                }
                for (RedisURI addr : cp.getFailedSlaveAddresses()) {
                    parent.addFailedSlaveAddress(addr);
                }
            }
            iter.remove();
        }
    }

    /** 取消拓扑扫描定时任务并关闭所有连接。 */
    @Override
    public void shutdown(long quietPeriod, long timeout, TimeUnit unit) {
        if (monitorFuture != null) {
            monitorFuture.cancel();
        }
        
        closeNodeConnections();
        super.shutdown(quietPeriod, timeout, unit);
    }

    /** 异步关闭：先取消 monitor 再关闭连接。 */
    @Override
    public CompletionStage<Void> shutdownAsync(long quietPeriod, long timeout, TimeUnit unit) {
        if (monitorFuture != null) {
            monitorFuture.cancel();
        }
        return closeNodeConnectionsAsync().thenCompose(v -> super.shutdownAsync(quietPeriod, timeout, unit));
    }

    /** 返回去重后的最新分区快照（按 nodeId 取最新时间戳）。 */
    private Collection<ClusterPartition> getLastPartitions() {
        return lastUri2Partition.values().stream().collect(Collectors.toMap(e -> e.getNodeId(), Function.identity(),
                                                                BinaryOperator.maxBy(Comparator.comparing(e -> e.getTime())))).values();
    }

    /** 查找条目对应的任意槽号（用于日志/诊断）。 */
    public int getSlot(MasterSlaveEntry entry) {
        return lastPartitions.entrySet().stream()
                .filter(e -> e.getValue().getMasterAddress().equals(entry.getClient().getConfig().getAddress()))
                .findAny()
                .map(m -> m.getKey())
                .orElse(-1);
    }

    /** 返回最近一次成功通信的集群节点 URI。 */
    @Override
    public RedisURI getLastClusterNode() {
        return lastClusterNode;
    }
    
}

