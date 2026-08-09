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
import org.redisson.config.DefaultNameMapper;
import org.redisson.api.NodeType;
import org.redisson.client.*;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.cluster.ClusterSlotRange;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.config.*;
import org.redisson.liveobject.core.RedissonObjectBuilder;
import org.redisson.misc.RedisURI;
import org.redisson.pubsub.PublishSubscribeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 主从/单节点 Redis 连接管理器基类，实现 {@link ConnectionManager}。
 * <p>
 * 职责包括：延迟连接（lazyConnect）、主从条目管理、RedisClient 创建、
 * DNS 监控、集群模式探测及优雅关闭。子类包括 {@link ClusterConnectionManager}、
 * {@link SentinelConnectionManager}、{@link ReplicatedConnectionManager} 等。
 *
 * @author Nikita Koksharov
 *
 */
public class MasterSlaveConnectionManager implements ConnectionManager {

    /** Redis 集群最大槽数。 */
    public static final int MAX_SLOT = 16384;

    /** 默认失败节点检测器。 */
    private static final FailedNodeDetector DEFAULT_FAILED_NODE_DETECTOR = new FailedConnectionDetector();

    /** 非集群模式下的全槽范围（0..16383）。 */
    protected final ClusterSlotRange singleSlotRange = new ClusterSlotRange(0, MAX_SLOT-1);

    /** 日志记录器。 */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /** DNS 变更监视器（主机名解析 IP 变化时触发重连）。 */
    protected DNSMonitor dnsMonitor;

    /** 主从服务器配置。 */
    protected MasterSlaveServersConfig config;

    /** 当前主从条目（单条目模式）。 */
    private MasterSlaveEntry masterSlaveEntry;

    /** Pub/Sub 订阅服务。 */
    protected final PublishSubscribeService subscribeService;

    /** 底层 Netty/线程池/DNS 等服务管理器。 */
    protected final ServiceManager serviceManager;

    /** 临时节点直连缓存（拓扑探测等场景）。 */
    private final Map<RedisURI, RedisConnection> nodeConnections = new ConcurrentHashMap<>();

    /** 延迟连接同步门闩，保证 connect 只执行一次。 */
    protected final AtomicReference<CompletableFuture<Void>> lazyConnectLatch = new AtomicReference<>();

    // 持有 lazyConnectLatch 的线程：doConnect 中同步拆条目会重入 lazyConnect，
    // 不可 join() 自己持有的门闩，否则会自死锁。
    /** 当前持有 lazyConnectLatch 的连接线程。 */
    private volatile Thread connectingThread;

    /** 是否为 connect 重试的最后一次尝试。 */
    private boolean lastAttempt;

    /** 轮询计数器，用于 getNextEntry 均衡。 */
    protected final AtomicInteger rrCounter = new AtomicInteger(0);

    /** 由主从配置构造，初始化 ServiceManager 与 Pub/Sub 服务。 */
    MasterSlaveConnectionManager(BaseMasterSlaveServersConfig<?> cfg, Config configCopy) {
        if (cfg instanceof MasterSlaveServersConfig) {
            this.config = (MasterSlaveServersConfig) cfg;
            if (this.config.getSlaveAddresses().isEmpty()
                    && (this.config.getReadMode() == ReadMode.SLAVE || this.config.getReadMode() == ReadMode.MASTER_SLAVE)) {
                throw new IllegalArgumentException("Slaves aren't defined. readMode can't be SLAVE or MASTER_SLAVE");
            }
        } else {
            this.config = create(cfg);
        }

        serviceManager = new ServiceManager(this.config, configCopy);
        subscribeService = new PublishSubscribeService(this);
    }

    @Override
    public ServiceManager getServiceManager() {
        return serviceManager;
    }

    /** 同步关闭所有临时节点连接。 */
    protected void closeNodeConnections() {
        nodeConnections.values().stream()
                .map(c -> c.getRedisClient().shutdownAsync())
                .forEach(f -> f.toCompletableFuture().join());
    }

    /** 异步关闭所有临时节点连接。 */
    protected CompletableFuture<Void> closeNodeConnectionsAsync() {
        List<CompletableFuture<Void>> futures = nodeConnections.values().stream()
                .map(c -> c.getRedisClient().shutdownAsync().toCompletableFuture())
                .collect(Collectors.toList());
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    /** 关闭并移除指定临时节点连接。 */
    protected void closeNodeConnection(RedisConnection conn) {
        if (nodeConnections.values().removeAll(Arrays.asList(conn))) {
            conn.closeAsync();
        }
    }

    /** 断开并移除指定 URI 的临时节点连接。 */
    protected final void disconnectNode(RedisURI addr) {
        RedisConnection conn = nodeConnections.remove(addr);
        if (conn != null) {
            nodeConnections.values().removeAll(Arrays.asList(conn));
            conn.closeAsync();
        }
    }

    /** 以 MASTER 类型连接指定节点（拓扑探测）。 */
    protected final CompletionStage<RedisConnection> connectToNode(BaseConfig<?> cfg, RedisURI addr, String sslHostname) {
        return connectToNode(NodeType.MASTER, cfg, addr, sslHostname);
    }

    /** 连接指定类型节点，结果缓存于 nodeConnections。 */
    protected final CompletionStage<RedisConnection> connectToNode(NodeType type, BaseConfig<?> cfg, RedisURI addr, String sslHostname) {
        RedisConnection conn = nodeConnections.get(addr);
        if (conn != null) {
            if (!conn.isActive()) {
                closeNodeConnection(conn);
            } else {
                return CompletableFuture.completedFuture(conn);
            }
        }

        RedisClient client = createClient(type, addr, cfg.getConnectTimeout(), cfg.getTimeout(), sslHostname);
        CompletionStage<RedisConnection> future = client.connectAsync();
        return future.thenCompose(connection -> {
            if (connection.isActive()) {
                if (!addr.isIP()) {
                    RedisURI address = new RedisURI(addr.getScheme()
                                 + "://" + connection.getRedisClient().getAddr().getAddress().getHostAddress()
                                 + ":" + connection.getRedisClient().getAddr().getPort());
                    nodeConnections.put(address, connection);
                }
                nodeConnections.put(addr, connection);
                return CompletableFuture.completedFuture(connection);
            } else {
                connection.closeAsync();
                CompletableFuture<RedisConnection> f = new CompletableFuture<>();
                f.completeExceptionally(new RedisException("Connection to " + connection.getRedisClient().getAddr() + " is not active!"));
                return f;
            }
        });
    }

    @Override
    /** 返回主从条目集合（延迟连接后）。 */
    public Collection<MasterSlaveEntry> getEntrySet() {
        lazyConnect();

        if (masterSlaveEntry != null) {
            return Collections.singletonList(masterSlaveEntry);
        }
        return Collections.emptyList();
    }

    /**
     * 轮询选取下一个 master 条目。
     * 单节点模式返回唯一主节点；集群模式在所有 master 间均衡分配。
     */
    /** 轮询返回下一个主从条目。 */
    public MasterSlaveEntry getNextEntry() {
        lazyConnect();

        if (masterSlaveEntry != null) {
            return masterSlaveEntry;
        }

        Collection<MasterSlaveEntry> entries = getEntrySet();
        if (entries.isEmpty()) {
            return null;
        }

        List<MasterSlaveEntry> list = new ArrayList<>(entries);
        int index = Math.floorMod(rrCounter.getAndIncrement(), list.size());
        return list.get(index);
    }

    /** 延迟连接入口：首次调用触发 connect()，后续 join 等待完成。 */
    protected final void lazyConnect() {
        if (isInitialized()) {
            return;
        }

        // 连接线程重入：直接返回，避免 join 自己持有的门闩导致自死锁
        if (Thread.currentThread() == connectingThread) {
            return;
        }

        CompletableFuture<Void> newFuture = new CompletableFuture<>();
        if (!lazyConnectLatch.compareAndSet(null, newFuture)) {
            CompletableFuture<Void> currentFuture = lazyConnectLatch.get();
            if (currentFuture.isCompletedExceptionally()) {
                if (!lazyConnectLatch.compareAndSet(currentFuture, newFuture)) {
                    lazyConnectLatch.get().join();
                    return;
                }
            } else {
                lazyConnectLatch.get().join();
                return;
            }
        }

        // 标记当前线程持有门闩，重入 lazyConnect 时不 join
        connectingThread = Thread.currentThread();
        try {
            connect();
            newFuture.complete(null);
        } catch (Exception e) {
            newFuture.completeExceptionally(e);
            throw e;
        } finally {
            connectingThread = null;
        }
    }

    @Override
    /** 建立连接，按 retryAttempts 重试。 */
    public final void connect() {
        int attempt = config.getRetryAttempts() + 1;
        for (int i = 0; i < attempt; i++) {
            try {
                if (i == attempt - 1) {
                    lastAttempt = true;
                }
                doConnect(u -> null);

                detectCluster();
                return;
            } catch (IllegalArgumentException e) {
                shutdown();
                throw e;
            } catch (Exception e) {
                if (i == attempt - 1) {
                    lastAttempt = false;
                    throw e;
                }
                Throwable cause = e.getCause();
                if (cause instanceof InterruptedException) {
                    throw e;
                }
                try {
                    Duration timeout = config.getRetryDelay().calcDelay(attempt);
                    Thread.sleep(timeout.toMillis());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new RedisConnectionException(ex);
                }
            }
        }
    }

    /** 通过 CROSSSLOT 探测是否为集群模式。 */
    private void detectCluster() {
        if (masterSlaveEntry == null) {
            return;
        }

        CompletableFuture<RedisConnection> c = masterSlaveEntry.connectionReadOp(RedisCommands.EVAL_VOID, false);
        RedisConnection cc = c.join();
        try {
            String script = "redis.call('get', KEYS[1]) " +
                            "redis.call('get', KEYS[2])";

            cc.sync(RedisCommands.EVAL_VOID, script, 2, "test1", "test2");
        } catch (Exception e) {
            if (e.getMessage().startsWith("CROSSSLOT")) {
                log.info("Cluster setup detected");
                serviceManager.setClusterDetected(true);
            }
        } finally {
            masterSlaveEntry.releaseRead(cc);
        }
    }

    /** 核心连接逻辑：创建 MasterSlaveEntry/SingleEntry、建立主从连接池、启动 DNS 监控。 */
    protected void doConnect(Function<RedisURI, String> hostnameMapper) {
        try {
            if (config.isSlaveNotUsed()) {
                masterSlaveEntry = new SingleEntry(this, config);
            } else {
                masterSlaveEntry = new MasterSlaveEntry(this, config);
            }

            RedisURI uri = new RedisURI(config.getMasterAddress());
            String hostname = hostnameMapper.apply(uri);
            CompletableFuture<RedisClient> masterFuture = masterSlaveEntry.setupMasterEntry(uri, hostname);
            try {
                // 即使 minimumIdleSize==0 也限制等待时间，避免主节点初始化卡住导致 lazyConnect 永不完成
                masterFuture.get((long) config.getConnectTimeout() * Math.max(1, config.getMasterConnectionMinimumIdleSize()), TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RedisConnectionException(e);
            } catch (ExecutionException | TimeoutException e) {
                throw new RedisConnectionException(e);
            }

            if (!config.isSlaveNotUsed()) {
                CompletableFuture<Void> fs = masterSlaveEntry.initSlaveBalancer(hostnameMapper);
                try {
                    // 限制从节点均衡器等待时间，避免 lazyConnect 永久阻塞
                    fs.get((long) config.getConnectTimeout() * Math.max(1, config.getSlaveConnectionMinimumIdleSize()), TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RedisConnectionException(e);
                } catch (ExecutionException | TimeoutException e) {
                    throw new RedisConnectionException(e);
                }
            }

            startDNSMonitoring(masterFuture.getNow(null));
        } catch (Exception e) {
            internalShutdown();
            if (e instanceof CompletionException) {
                if (e.getCause() instanceof RuntimeException) {
                    throw (RuntimeException) e.getCause();
                }
                throw new RedisConnectionException(e.getCause());
            }
            throw e;
        }
    }

    /** 非 IP 地址且启用 DNS 监控时启动 {@link DNSMonitor}。 */
    protected void startDNSMonitoring(RedisClient masterHost) {
        if (masterHost.getConfig().getAddress().isIP()) {
            return;
        }

        if (config.getDnsMonitoringInterval() != -1) {
            Set<RedisClient> slaveAddresses = masterSlaveEntry.getAllEntries().stream().filter(e -> e.getNodeType().equals(NodeType.SLAVE)).map(ClientConnectionsEntry::getClient).collect(Collectors.toSet());
            dnsMonitor = new DNSMonitor(this, masterHost,
                    slaveAddresses, config.getDnsMonitoringInterval(), config.getDnsMonitoringTimes());
            dnsMonitor.start();
        }
    }

    /** 从基类配置复制字段到 MasterSlaveServersConfig。 */
    protected MasterSlaveServersConfig create(BaseMasterSlaveServersConfig<?> cfg) {
        MasterSlaveServersConfig c = new MasterSlaveServersConfig();
        
        if (cfg.getUsername() != null) {
            c.setUsername(cfg.getUsername());
        }
        if (cfg.getPassword() != null) {
            c.setPassword(cfg.getPassword());
        }
        if (!(cfg.getNameMapper() instanceof DefaultNameMapper)) {
            c.setNameMapper(cfg.getNameMapper());
        }
        if (!(cfg.getCommandMapper() instanceof DefaultCommandMapper)) {
            c.setCommandMapper(cfg.getCommandMapper());
        }
        if (!(cfg.getCredentialsResolver() instanceof DefaultCredentialsResolver)) {
            c.setCredentialsResolver(cfg.getCredentialsResolver());
        }
        
        if (cfg.getSslVerificationMode() != SslVerificationMode.STRICT) {
            c.setSslVerificationMode(cfg.getSslVerificationMode());
        }
        if (cfg.getSslKeystoreType() != null) {
            c.setSslKeystoreType(cfg.getSslKeystoreType());
        }
        if (cfg.getSslProvider() != SslProvider.JDK) {
            c.setSslProvider(cfg.getSslProvider());
        }
        if (cfg.getSslTruststore() != null) {
            c.setSslTruststore(cfg.getSslTruststore());
        }
        if (cfg.getSslTruststorePassword() != null) {
            c.setSslTruststorePassword(cfg.getSslTruststorePassword());
        }
        if (cfg.getSslKeystore() != null) {
            c.setSslKeystore(cfg.getSslKeystore());
        }
        if (cfg.getSslKeystorePassword() != null) {
            c.setSslKeystorePassword(cfg.getSslKeystorePassword());
        }
        if (cfg.getSslProtocols() != null) {
            c.setSslProtocols(cfg.getSslProtocols());
        }
        if (cfg.getSslCiphers() != null) {
            c.setSslCiphers(cfg.getSslCiphers());
        }
        if (cfg.getSslKeyManagerFactory() != null) {
            c.setSslKeyManagerFactory(cfg.getSslKeyManagerFactory());
        }
        if (cfg.getSslTrustManagerFactory() != null) {
            c.setSslTrustManagerFactory(cfg.getSslTrustManagerFactory());
        }
        
        if (cfg.isKeepAlive()) {
            c.setKeepAlive(cfg.isKeepAlive());
        }
        if (cfg.getTcpKeepAliveCount() != 0) {
            c.setTcpKeepAliveCount(cfg.getTcpKeepAliveCount());
        }
        if (cfg.getTcpKeepAliveIdle() != 0) {
            c.setTcpKeepAliveIdle(cfg.getTcpKeepAliveIdle());
        }
        if (cfg.getTcpKeepAliveInterval() != 0) {
            c.setTcpKeepAliveInterval(cfg.getTcpKeepAliveInterval());
        }
        if (cfg.getTcpUserTimeout() != 0) {
            c.setTcpUserTimeout(cfg.getTcpUserTimeout());
        }
        if (!cfg.isTcpNoDelay()) {
            c.setTcpNoDelay(cfg.isTcpNoDelay());
        }
        
        c.setPingConnectionInterval(cfg.getPingConnectionInterval());
        c.setRetryDelay(cfg.getRetryDelay());
        c.setReconnectionDelay(cfg.getReconnectionDelay());
        c.setRetryAttempts(cfg.getRetryAttempts());
        c.setTimeout(cfg.getTimeout());
        c.setLoadBalancer(cfg.getLoadBalancer());
        c.setClientName(cfg.getClientName());
        c.setMasterConnectionPoolSize(cfg.getMasterConnectionPoolSize());
        c.setSlaveConnectionPoolSize(cfg.getSlaveConnectionPoolSize());
        c.setSubscriptionConnectionPoolSize(cfg.getSubscriptionConnectionPoolSize());
        c.setSubscriptionsPerConnection(cfg.getSubscriptionsPerConnection());
        c.setConnectTimeout(cfg.getConnectTimeout());
        c.setIdleConnectionTimeout(cfg.getIdleConnectionTimeout());
        
        c.setFailedSlaveReconnectionInterval(cfg.getFailedSlaveReconnectionInterval());
        c.setFailedSlaveNodeDetector(cfg.getFailedSlaveNodeDetector());
        c.setMasterConnectionMinimumIdleSize(cfg.getMasterConnectionMinimumIdleSize());
        c.setSlaveConnectionMinimumIdleSize(cfg.getSlaveConnectionMinimumIdleSize());
        c.setSubscriptionConnectionMinimumIdleSize(cfg.getSubscriptionConnectionMinimumIdleSize());
        c.setReadMode(cfg.getReadMode());
        c.setSubscriptionMode(cfg.getSubscriptionMode());
        c.setDnsMonitoringInterval(cfg.getDnsMonitoringInterval());
        c.setDnsMonitoringTimes(cfg.getDnsMonitoringTimes());
        c.setSubscriptionTimeout(cfg.getSubscriptionTimeout());
        
        return c;
    }

    @Override
    /** 创建 RedisClient（URI 地址）。 */
    public RedisClient createClient(NodeType type, RedisURI address, String sslHostname) {
        RedisClient client = createClient(type, address, config.getConnectTimeout(), config.getTimeout(), sslHostname);
        return client;
    }

    @Override
    public RedisClient createClient(NodeType type, InetSocketAddress address, RedisURI uri, String sslHostname) {
        RedisClient client = createClient(type, address, uri, config.getConnectTimeout(), config.getTimeout(), sslHostname);
        return client;
    }

    protected RedisClient createClient(NodeType type, RedisURI address, int timeout, int commandTimeout, String sslHostname) {
        RedisClientConfig redisConfig = createRedisConfig(type, address, timeout, commandTimeout, sslHostname);
        return RedisClient.create(redisConfig);
    }

    private RedisClient createClient(NodeType type, InetSocketAddress address, RedisURI uri, int timeout, int commandTimeout, String sslHostname) {
        RedisClientConfig redisConfig = createRedisConfig(type, null, timeout, commandTimeout, sslHostname);
        redisConfig.setAddress(address, uri);
        return RedisClient.create(redisConfig);
    }

    /** 组装 RedisClientConfig，合并全局 Config 与 MasterSlaveServersConfig。 */
    protected RedisClientConfig createRedisConfig(NodeType type, RedisURI address, int timeout, int commandTimeout, String sslHostname) {
        Config serviceCfg = serviceManager.getCfg();
        RedisClientConfig redisConfig = new RedisClientConfig();
        FailedNodeDetector failedNodeDetector = DEFAULT_FAILED_NODE_DETECTOR;
        if (type == NodeType.SLAVE) {
            failedNodeDetector = config.getFailedSlaveNodeDetector();
        }
        redisConfig.setAddress(address)
                .setTimer(serviceManager.getTimer())
                .setExecutor(serviceManager.getExecutor())
                .setResolverGroup(serviceManager.getResolverGroup())
                .setGroup(serviceManager.getGroup())
                .setSocketChannelClass(serviceManager.getSocketChannelClass())
                .setConnectTimeout(timeout)
                .setCommandTimeout(commandTimeout)
                .setSslHostname(sslHostname)
                .setSslVerificationMode(serviceCfg.getSslVerificationMode())
                .setSslProvider(serviceCfg.getSslProvider())
                .setSslKeystoreType(Objects.toString(serviceCfg.getSslKeystoreType(), config.getSslKeystoreType()))
                .setSslTruststore(Optional.ofNullable(serviceCfg.getSslTruststore()).orElse(config.getSslTruststore()))
                .setSslTruststorePassword(Objects.toString(serviceCfg.getSslTruststorePassword(), config.getSslTruststorePassword()))
                .setSslKeystore(Optional.ofNullable(serviceCfg.getSslKeystore()).orElse(config.getSslKeystore()))
                .setSslKeystorePassword(Objects.toString(serviceCfg.getSslKeystorePassword(), config.getSslKeystorePassword()))
                .setSslProtocols(Optional.ofNullable(serviceCfg.getSslProtocols()).orElse(config.getSslProtocols()))
                .setSslCiphers(Optional.ofNullable(serviceCfg.getSslCiphers()).orElse(config.getSslCiphers()))
                .setSslKeyManagerFactory(Optional.ofNullable(serviceCfg.getSslKeyManagerFactory()).orElse(config.getSslKeyManagerFactory()))
                .setSslTrustManagerFactory(Optional.ofNullable(serviceCfg.getSslTrustManagerFactory()).orElse(config.getSslTrustManagerFactory()))
                .setClientName(config.getClientName())
                .setKeepPubSubOrder(serviceCfg.isKeepPubSubOrder())
                .setPingConnectionInterval(config.getPingConnectionInterval())
                .setUsername(Objects.toString(serviceCfg.getUsername(), config.getUsername()))
                .setPassword(Objects.toString(serviceCfg.getPassword(), config.getPassword()))
                .setNettyHook(serviceCfg.getNettyHook())
                .setFailedNodeDetector(failedNodeDetector)
                .setProtocol(serviceCfg.getProtocol())
                .setCapabilities(serviceCfg.getValkeyCapabilities())
                .setReconnectionDelay(config.getReconnectionDelay())
                .setCommandMapper(serviceManager.getCommandMapper())
                .setCredentialsResolver(serviceCfg.getCredentialsResolver())
                .setConnectedListener(addr -> {
                    if (!serviceManager.isShuttingDown()) {
                        NodeType nt = getNodeType(type, addr);
                        serviceManager.getConnectionEventsHub().fireConnect(addr, nt);
                    }
                })
                .setDisconnectedListener(addr -> {
                    if (!serviceManager.isShuttingDown()) {
                        NodeType nt = getNodeType(type, addr);
                        serviceManager.getConnectionEventsHub().fireDisconnect(addr, nt);
                    }
                });
        
        if (redisConfig.getCredentialsResolver() instanceof DefaultCredentialsResolver) {
            redisConfig.setCredentialsResolver(config.getCredentialsResolver());
        }
        if (redisConfig.getSslVerificationMode() == SslVerificationMode.STRICT && config.getSslVerificationMode() != SslVerificationMode.STRICT) {
            redisConfig.setSslVerificationMode(config.getSslVerificationMode());
        }
        if (redisConfig.getSslProvider() == SslProvider.JDK && config.getSslProvider() != SslProvider.JDK) {
            redisConfig.setSslProvider(config.getSslProvider());
        }
        
        if (config.isKeepAlive()) {
            redisConfig.setKeepAlive(config.isKeepAlive());
        } else {
            redisConfig.setKeepAlive(serviceCfg.isTcpKeepAlive());
        }
        if (config.getTcpKeepAliveCount() != 0) {
            redisConfig.setTcpKeepAliveCount(config.getTcpKeepAliveCount());
        } else {
            redisConfig.setTcpKeepAliveCount(serviceCfg.getTcpKeepAliveCount());
        }
        if (config.getTcpKeepAliveIdle() != 0) {
            redisConfig.setTcpKeepAliveIdle(config.getTcpKeepAliveIdle());
        } else {
            redisConfig.setTcpKeepAliveIdle(serviceCfg.getTcpKeepAliveIdle());
        }
        if (config.getTcpKeepAliveInterval() != 0) {
            redisConfig.setTcpKeepAliveInterval(config.getTcpKeepAliveInterval());
        } else {
            redisConfig.setTcpKeepAliveInterval(serviceCfg.getTcpKeepAliveInterval());
        }
        if (config.getTcpUserTimeout() != 0) {
            redisConfig.setTcpUserTimeout(config.getTcpUserTimeout());
        } else {
            redisConfig.setTcpUserTimeout(serviceCfg.getTcpUserTimeout());
        }
        if (!config.isTcpNoDelay()) {
            redisConfig.setTcpNoDelay(config.isTcpNoDelay());
        } else {
            redisConfig.setTcpNoDelay(serviceCfg.isTcpNoDelay());
        }
        
        if (type != NodeType.SENTINEL) {
            redisConfig.setDatabase(config.getDatabase());
        }
        
        return redisConfig;
    }

    private NodeType getNodeType(NodeType type, InetSocketAddress address) {
        if (!isInitialized()) {
            // 初始化前 getEntry() 可能触发 lazyConnect 并在连接线程持有的门闩上阻塞
            return type;
        }
        if (getServiceManager().getCfg().isSingleConfig()) {
            return NodeType.MASTER;
        }

        if (type != NodeType.SENTINEL) {
            MasterSlaveEntry entry = getEntry(address);
            if (entry != null) {
                if (!entry.isInit()) {
                    return type;
                }
                InetSocketAddress addr = entry.getClient().getAddr();
                if (addr.getAddress().equals(address.getAddress())
                        && addr.getPort() == address.getPort()) {
                    return NodeType.MASTER;
                }
            }
            return NodeType.SLAVE;
        }
        return type;
    }

    @Override
    /** 非集群模式固定返回全槽起始槽号。 */
    public int calcSlot(String key) {
        return singleSlotRange.getStartSlot();
    }

    @Override
    public int calcSlot(byte[] key) {
        return singleSlotRange.getStartSlot();
    }

    @Override
    public int calcSlot(ByteBuf key) {
        return singleSlotRange.getStartSlot();
    }

    @Override
    public MasterSlaveEntry getEntry(InetSocketAddress address) {
        lazyConnect();

        return masterSlaveEntry;
    }

    @Override
    public MasterSlaveEntry getEntry(RedisURI addr) {
        lazyConnect();

        return masterSlaveEntry;
    }

    @Override
    public MasterSlaveEntry getEntry(RedisClient redisClient) {
        lazyConnect();

        return masterSlaveEntry;
    }

    @Override
    public MasterSlaveEntry getEntry(String name) {
        int slot = calcSlot(name);
        return getEntry(slot);
    }

    /** 按槽号查找条目（非集群返回唯一条目）。 */
    public MasterSlaveEntry getEntry(int slot) {
        lazyConnect();

        return masterSlaveEntry;
    }

    @Override
    public MasterSlaveEntry getWriteEntry(int slot) {
        return getEntry(slot);
    }

    @Override
    public MasterSlaveEntry getReadEntry(int slot) {
        return getEntry(slot);
    }

    /** 切换指定槽的主节点。 */
    protected CompletableFuture<RedisClient> changeMaster(int slot, RedisURI address) {
        MasterSlaveEntry entry = getEntry(slot);
        return entry.changeMaster(address);
    }

    /** 连接失败时的内部清理。 */
    protected void internalShutdown() {
        if (lazyConnectLatch.get() == null && lastAttempt) {
            shutdown();
        }
    }

    @Override
    /** 使用默认 Netty 参数关闭。 */
    public void shutdown() {
        shutdown(0, 10, TimeUnit.SECONDS); // Netty 默认优雅关闭参数
    }

    @Override
    /** 异步优雅关闭：DNS 监控、连接池、线程池、EventLoop。 */
    public CompletionStage<Void> shutdownAsync(long quietPeriod, long timeout, TimeUnit unit) {
        if (dnsMonitor != null) {
            dnsMonitor.stop();
        }
        long timeoutInNanos = unit.toNanos(timeout);

        serviceManager.close();
        serviceManager.getConnectionWatcher().stop();
        serviceManager.getResolverGroup().close();

        return serviceManager.shutdownFuturesAsync(timeout, unit)
                .thenCompose(v -> {
                    if (!isInitialized()) {
                        return CompletableFuture.completedFuture(null);
                    }
                    List<CompletableFuture<Void>> futures = new ArrayList<>();
                    for (MasterSlaveEntry entry : getEntrySet()) {
                        futures.add(entry.shutdownAsync());
                    }
                    CompletableFuture<Void> allEntries = CompletableFuture.allOf(
                            futures.toArray(new CompletableFuture[0]));
                    serviceManager.newTimeout(t -> allEntries.completeExceptionally(new TimeoutException()),
                            timeoutInNanos, TimeUnit.NANOSECONDS);
                    return allEntries.exceptionally(e -> null);
                })
                .thenCompose(v -> {
                    if (serviceManager.getCfg().getExecutor() == null) {
                        serviceManager.getExecutor().shutdown();
                        return CompletableFuture.runAsync(() -> {
                            try {
                                serviceManager.getExecutor().awaitTermination(timeoutInNanos, TimeUnit.NANOSECONDS);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        });
                    }
                    return CompletableFuture.completedFuture(null);
                })
                .thenCompose(v -> {
                    serviceManager.getTimer().stop();
                    if (serviceManager.getCfg().getEventLoopGroup() == null) {
                        long quietPeriodNanos = unit.toNanos(quietPeriod);
                        if (timeoutInNanos < quietPeriodNanos) {
                            quietPeriodNanos = 0;
                        }
                        io.netty.util.concurrent.Future<?> nettyFuture = serviceManager.getGroup()
                                .shutdownGracefully(quietPeriodNanos, timeoutInNanos, TimeUnit.NANOSECONDS);
                        CompletableFuture<Void> cf = new CompletableFuture<>();
                        nettyFuture.addListener(f -> {
                            if (f.isSuccess()) {
                                cf.complete(null);
                            } else {
                                cf.completeExceptionally(f.cause());
                            }
                        });
                        return cf;
                    }
                    return CompletableFuture.completedFuture(null);
                });
    }

    @Override
    /** 同步优雅关闭。 */
    public void shutdown(long quietPeriod, long timeout, TimeUnit unit) {
        if (dnsMonitor != null) {
            dnsMonitor.stop();
        }
        long timeoutInNanos = unit.toNanos(timeout);

        serviceManager.close();
        serviceManager.getConnectionWatcher().stop();
        serviceManager.getResolverGroup().close();

        long startTime = System.nanoTime();
        serviceManager.shutdownFutures(timeout, unit);
        timeoutInNanos = Math.max(0, timeoutInNanos - (System.nanoTime() - startTime));

        if (isInitialized()) {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (MasterSlaveEntry entry : getEntrySet()) {
                futures.add(entry.shutdownAsync());
            }
            CompletableFuture<Void> future = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

            try {
                startTime = System.nanoTime();
                future.get(timeoutInNanos, TimeUnit.NANOSECONDS);
                timeoutInNanos = Math.max(0, timeoutInNanos - (System.nanoTime() - startTime));
            } catch (Exception e) {
                // 关闭超时忽略
            }
        }

        if (serviceManager.getCfg().getExecutor() == null) {
            serviceManager.getExecutor().shutdown();
            try {
                startTime = System.nanoTime();
                serviceManager.getExecutor().awaitTermination(timeoutInNanos, TimeUnit.NANOSECONDS);
                timeoutInNanos = Math.max(0, timeoutInNanos - (System.nanoTime() - startTime));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        serviceManager.getTimer().stop();

        if (serviceManager.getCfg().getEventLoopGroup() == null) {
            if (timeoutInNanos < quietPeriod) {
                quietPeriod = 0;
            }
            serviceManager.getGroup()
                    .shutdownGracefully(unit.toNanos(quietPeriod), timeoutInNanos, TimeUnit.NANOSECONDS)
                    .syncUninterruptibly();
        }
    }

    /** 是否已完成延迟连接（或非 lazy 模式）。 */
    private boolean isInitialized() {
        return !serviceManager.getCfg().isLazyInitialization()
                    || (lazyConnectLatch.get() != null
                            && lazyConnectLatch.get().isDone()
                                && !lazyConnectLatch.get().isCompletedExceptionally());
    }

    @Override
    public PublishSubscribeService getSubscribeService() {
        return subscribeService;
    }

    @Override
    public RedisURI getLastClusterNode() {
        return null;
    }

    @Override
    /** 创建命令异步执行器。 */
    public CommandAsyncExecutor createCommandExecutor(RedissonObjectBuilder objectBuilder, RedissonObjectBuilder.ReferenceType referenceType) {
        return CommandAsyncExecutor.create(this, objectBuilder, referenceType);
    }
}
