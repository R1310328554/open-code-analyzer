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
package org.redisson.config;

import io.netty.channel.EventLoopGroup;
import org.redisson.client.DefaultCredentialsResolver;
import org.redisson.client.DefaultNettyHook;
import org.redisson.client.NettyHook;
import org.redisson.client.codec.Codec;
import org.redisson.codec.Kryo5Codec;
import org.redisson.connection.AddressResolverGroupFactory;
import org.redisson.connection.ConnectionListener;
import org.redisson.connection.SequentialDnsAddressResolverFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Redisson 客户端全局配置入口，聚合连接模式、线程池、编解码、SSL 等全部参数。
 * <p>
 * 支持五种部署模式（互斥）：单节点 {@link #useSingleServer()}、主从 {@link #useMasterSlaveServers()}、
 * 哨兵 {@link #useSentinelServers()}、集群 {@link #useClusterServers()}、复制 {@link #useReplicatedServers()}。
 * 可通过 YAML 静态加载（{@link #fromYAML}）或编程式链式配置。
 *
 * @author Nikita Koksharov
 *
 */
public class Config {

    /** 哨兵模式配置（与单节点/主从/集群/复制互斥）。 */
    private SentinelServersConfig sentinelServersConfig;

    /** 主从模式配置。 */
    private MasterSlaveServersConfig masterSlaveServersConfig;

    /** 单节点模式配置。 */
    private SingleServerConfig singleServerConfig;

    /** 集群模式配置。 */
    private ClusterServersConfig clusterServersConfig;

    /** 复制模式配置（Azure/AWS 托管 Redis 常用）。 */
    private ReplicatedServersConfig replicatedServersConfig;

    /** 全局 Redis 认证密码（可被各子配置覆盖）。 */
    private String password;

    /** 全局 Redis 认证用户名（Redis 6.0+）。 */
    private String username;

    /** 动态凭据解析器，支持令牌轮换等场景。 */
    private CredentialsResolver credentialsResolver = new DefaultCredentialsResolver();

    /** RTopic/RRemoteService/RExecutorService 共享的业务线程数。 */
    private int threads = 16;

    /** 所有 Redis 客户端共享的 Netty I/O 线程数。 */
    private int nettyThreads = 32;

    /** 外部 Netty 执行器（可选，调用方负责关闭）。 */
    private Executor nettyExecutor;

    /** Redis 值编解码器，默认 Kryo5Codec。 */
    private Codec codec;

    /** 外部业务线程池（可选）。 */
    private ExecutorService executor;

    /** 是否启用 Redisson Reference 特性。 */
    private boolean referenceEnabled = true;

    /** Netty 传输模式（NIO/EPOLL/KQUEUE 等）。 */
    private TransportMode transportMode = TransportMode.NIO;

    /** 共享 EventLoopGroup，多 Redisson 实例可复用。 */
    private EventLoopGroup eventLoopGroup;

    /** 分布式锁看门狗续期超时（毫秒）。 */
    private long lockWatchdogTimeout = 30 * 1000;

    /** 单次看门狗批量续期的锁数量。 */
    private int lockWatchdogBatchSize = 100;

    /** 公平锁默认最大等待时间（毫秒）。 */
    private long fairLockWaitTimeout = 5 * 60000;

    /** 加锁后是否校验已同步从节点数量。 */
    private boolean checkLockSyncedSlaves = true;

    /** 锁/信号量等操作的从节点同步超时（毫秒）。 */
    private long slavesSyncTimeout = 1000;

    /** Reliable Topic 订阅者看门狗超时（毫秒）。 */
    private long reliableTopicWatchdogTimeout = TimeUnit.MINUTES.toMillis(10);

    /** Pub/Sub 是否按到达顺序处理同频道消息。 */
    private boolean keepPubSubOrder = true;

    /** 是否在 Redis 端缓存 Lua 脚本以提升性能。 */
    private boolean useScriptCache = true;

    /** 过期条目清理的最小间隔（秒）。 */
    private int minCleanUpDelay = 5;

    /** 过期条目清理的最大间隔（秒）。 */
    private int maxCleanUpDelay = 30*60;

    /** 单次清理操作删除的过期键数量。 */
    private int cleanUpKeysAmount = 100;

    /** Netty Bootstrap/Channel 钩子。 */
    private NettyHook nettyHook = new DefaultNettyHook();

    /** 连接建立/断开事件监听器。 */
    private ConnectionListener connectionListener;

    /** 是否向 Codec 提供线程上下文 ClassLoader。 */
    private boolean useThreadClassLoader = true;

    /** DNS 地址解析组工厂。 */
    private AddressResolverGroupFactory addressResolverGroupFactory = new SequentialDnsAddressResolverFactory();

    /** 是否延迟到首次 Redis 调用时才建立连接。 */
    private boolean lazyInitialization;

    /** Redis 协议版本（RESP2/RESP3）。 */
    private Protocol protocol = Protocol.RESP2;

    /** 声明需支持的 Valkey 能力集。 */
    private Set<ValkeyCapability> valkeyCapabilities = Collections.emptySet();

    /** 全局 Redisson 对象名称映射器。 */
    private NameMapper nameMapper = NameMapper.direct();

    /** 全局 Redis 命令名映射器。 */
    private CommandMapper commandMapper = CommandMapper.direct();
    
    /** SSL 证书校验模式，防止中间人攻击。 */
    private SslVerificationMode sslVerificationMode = SslVerificationMode.STRICT;

    /** SSL 密钥库类型。 */
    private String sslKeystoreType;

    /** SSL 实现提供方（JDK/OpenSSL 等）。 */
    private SslProvider sslProvider = SslProvider.JDK;

    /** SSL 信任库路径。 */
    private URL sslTruststore;

    /** SSL 信任库密码（每次建连时可热加载）。 */
    private String sslTruststorePassword;

    /** SSL 密钥库路径。 */
    private URL sslKeystore;

    /** SSL 密钥库密码。 */
    private String sslKeystorePassword;

    /** 允许的 SSL/TLS 协议版本列表。 */
    private String[] sslProtocols;

    /** 允许的 SSL 密码套件列表。 */
    private String[] sslCiphers;

    /** 自定义 SSL TrustManagerFactory。 */
    private TrustManagerFactory sslTrustManagerFactory;

    /** 自定义 SSL KeyManagerFactory。 */
    private KeyManagerFactory sslKeyManagerFactory;

    /** 是否启用 TCP KeepAlive。 */
    private boolean tcpKeepAlive = true;

    /** KeepAlive 探测失败多少次后断开连接。 */
    private int tcpKeepAliveCount;

    /** 连接空闲多少秒后开始发送 KeepAlive 探测。 */
    private int tcpKeepAliveIdle;

    /** KeepAlive 探测包发送间隔（秒）。 */
    private int tcpKeepAliveInterval;

    /** 未确认数据最大保留时间（毫秒，Epoll/IoUring 生效）。 */
    private int tcpUserTimeout;

    /** 是否启用 TCP_NODELAY（禁用 Nagle 算法）。 */
    private boolean tcpNoDelay = true;

    /** 创建空配置，尚未选择连接模式。 */
    public Config() {
    }

    /** 从已有配置深拷贝，包括各子模式配置与全局参数。 */
    public Config(Config oldConf) {
        setNettyHook(oldConf.getNettyHook());
        setNettyExecutor(oldConf.getNettyExecutor());
        setExecutor(oldConf.getExecutor());

        if (oldConf.getCodec() == null) {
            // 未指定编解码器时使用 Kryo5 作为默认
            oldConf.setCodec(new Kryo5Codec());
        }

        setConnectionListener(oldConf.getConnectionListener());
        setUseThreadClassLoader(oldConf.isUseThreadClassLoader());
        setMinCleanUpDelay(oldConf.getMinCleanUpDelay());
        setMaxCleanUpDelay(oldConf.getMaxCleanUpDelay());
        setCleanUpKeysAmount(oldConf.getCleanUpKeysAmount());
        setUseScriptCache(oldConf.isUseScriptCache());
        setKeepPubSubOrder(oldConf.isKeepPubSubOrder());
        setLockWatchdogTimeout(oldConf.getLockWatchdogTimeout());
        setLockWatchdogBatchSize(oldConf.getLockWatchdogBatchSize());
        setFairLockWaitTimeout(oldConf.getFairLockWaitTimeout());
        setCheckLockSyncedSlaves(oldConf.isCheckLockSyncedSlaves());
        setSlavesSyncTimeout(oldConf.getSlavesSyncTimeout());
        setNettyThreads(oldConf.getNettyThreads());
        setThreads(oldConf.getThreads());
        setUsername(oldConf.getUsername());
        setPassword(oldConf.getPassword());
        setCredentialsResolver(oldConf.getCredentialsResolver());
        setCodec(oldConf.getCodec());
        setReferenceEnabled(oldConf.isReferenceEnabled());
        setEventLoopGroup(oldConf.getEventLoopGroup());
        setTransportMode(oldConf.getTransportMode());
        setAddressResolverGroupFactory(oldConf.getAddressResolverGroupFactory());
        setReliableTopicWatchdogTimeout(oldConf.getReliableTopicWatchdogTimeout());
        setLazyInitialization(oldConf.isLazyInitialization());
        setProtocol(oldConf.getProtocol());
        setValkeyCapabilities(oldConf.getValkeyCapabilities());
        setNameMapper(oldConf.getNameMapper());
        setCommandMapper(oldConf.getCommandMapper());
        setSslProvider(oldConf.getSslProvider());
        setSslTruststore(oldConf.getSslTruststore());
        setSslTruststorePassword(oldConf.getSslTruststorePassword());
        setSslKeystoreType(oldConf.getSslKeystoreType());
        setSslKeystore(oldConf.getSslKeystore());
        setSslKeystorePassword(oldConf.getSslKeystorePassword());
        setSslProtocols(oldConf.getSslProtocols());
        setSslCiphers(oldConf.getSslCiphers());
        setSslKeyManagerFactory(oldConf.getSslKeyManagerFactory());
        setSslTrustManagerFactory(oldConf.getSslTrustManagerFactory());
        setSslVerificationMode(oldConf.getSslVerificationMode());

        if (oldConf.getSingleServerConfig() != null) {
            setSingleServerConfig(new SingleServerConfig(oldConf.getSingleServerConfig()));
        }
        if (oldConf.getMasterSlaveServersConfig() != null) {
            setMasterSlaveServersConfig(new MasterSlaveServersConfig(oldConf.getMasterSlaveServersConfig()));
        }
        if (oldConf.getSentinelServersConfig() != null) {
            setSentinelServersConfig(new SentinelServersConfig(oldConf.getSentinelServersConfig()));
        }
        if (oldConf.getClusterServersConfig() != null) {
            setClusterServersConfig(new ClusterServersConfig(oldConf.getClusterServersConfig()));
        }
        if (oldConf.getReplicatedServersConfig() != null) {
            setReplicatedServersConfig(new ReplicatedServersConfig(oldConf.getReplicatedServersConfig()));
        }
    }

    public NettyHook getNettyHook() {
        return nettyHook;
    }

    /**
     * 设置应用于 Netty Bootstrap 与 Channel 的钩子，用于自定义初始化逻辑。
     *
     * @param nettyHook Netty 钩子实例
     * @return 当前配置实例
     */
    public Config setNettyHook(NettyHook nettyHook) {
        this.nettyHook = nettyHook;
        return this;
    }

    /**
     * 设置 Redis 数据编解码器，默认为 Kryo5Codec。
     *
     * @see org.redisson.client.codec.Codec
     * @see org.redisson.codec.Kryo5Codec
     * 
     * @param codec 编解码器实例
     * @return 当前配置实例
     */
    public Config setCodec(Codec codec) {
        this.codec = codec;
        return this;
    }

    public Codec getCodec() {
        return codec;
    }

    /**
     * 是否启用 Redisson Reference（跨 JVM 对象引用）特性。
     * <p>
     * Default value is <code>true</code>
     * 
     * @return 若 Reference 特性已启用则返回 <code>true</code>
     */
    public boolean isReferenceEnabled() {
        return referenceEnabled;
    }

    /**
     * 启用或禁用 Redisson Reference 特性。
     * <p>
     * Default value is <code>true</code>
     * 
     * @param redissonReferenceEnabled 是否启用
     */
    public void setReferenceEnabled(boolean redissonReferenceEnabled) {
        this.referenceEnabled = redissonReferenceEnabled;
    }

    /**
     * 初始化并返回集群模式配置；与其他连接模式互斥。
     *
     * @return 集群配置实例
     */
    public ClusterServersConfig useClusterServers() {
        return useClusterServers(new ClusterServersConfig());
    }

    ClusterServersConfig useClusterServers(ClusterServersConfig config) {
        checkMasterSlaveServersConfig();
        checkSentinelServersConfig();
        checkSingleServerConfig();
        checkReplicatedServersConfig();

        if (clusterServersConfig == null) {
            clusterServersConfig = config;
        }
        return clusterServersConfig;
    }

    protected ClusterServersConfig getClusterServersConfig() {
        return clusterServersConfig;
    }

    protected void setClusterServersConfig(ClusterServersConfig clusterServersConfig) {
        this.clusterServersConfig = clusterServersConfig;
    }

    /**
     * 初始化复制模式配置，常用于 Azure Redis Cache 或 AWS ElastiCache。
     *
     * @return 复制模式配置实例
     */
    public ReplicatedServersConfig useReplicatedServers() {
        return useReplicatedServers(new ReplicatedServersConfig());
    }

    ReplicatedServersConfig useReplicatedServers(ReplicatedServersConfig config) {
        checkClusterServersConfig();
        checkMasterSlaveServersConfig();
        checkSentinelServersConfig();
        checkSingleServerConfig();

        if (replicatedServersConfig == null) {
            replicatedServersConfig = config;
        }
        return replicatedServersConfig;
    }

    protected ReplicatedServersConfig getReplicatedServersConfig() {
        return replicatedServersConfig;
    }

    protected void setReplicatedServersConfig(ReplicatedServersConfig replicatedServersConfig) {
        this.replicatedServersConfig = replicatedServersConfig;
    }

    /**
     * 初始化单节点模式配置。
     *
     * @return 单节点配置实例
     */
    public SingleServerConfig useSingleServer() {
        return useSingleServer(new SingleServerConfig());
    }

    SingleServerConfig useSingleServer(SingleServerConfig config) {
        checkClusterServersConfig();
        checkMasterSlaveServersConfig();
        checkSentinelServersConfig();
        checkReplicatedServersConfig();

        if (singleServerConfig == null) {
            singleServerConfig = config;
        }
        return singleServerConfig;
    }

    protected SingleServerConfig getSingleServerConfig() {
        return singleServerConfig;
    }

    protected void setSingleServerConfig(SingleServerConfig singleConnectionConfig) {
        this.singleServerConfig = singleConnectionConfig;
    }

    /**
     * 初始化哨兵模式配置。
     *
     * @return 哨兵配置实例
     */
    public SentinelServersConfig useSentinelServers() {
        return useSentinelServers(new SentinelServersConfig());
    }

    SentinelServersConfig useSentinelServers(SentinelServersConfig sentinelServersConfig) {
        checkClusterServersConfig();
        checkSingleServerConfig();
        checkMasterSlaveServersConfig();
        checkReplicatedServersConfig();

        if (this.sentinelServersConfig == null) {
            this.sentinelServersConfig = sentinelServersConfig;
        }
        return this.sentinelServersConfig;
    }

    protected SentinelServersConfig getSentinelServersConfig() {
        return sentinelServersConfig;
    }

    protected void setSentinelServersConfig(SentinelServersConfig sentinelConnectionConfig) {
        this.sentinelServersConfig = sentinelConnectionConfig;
    }

    /**
     * 初始化主从模式配置。
     *
     * @return 主从配置实例
     */
    public MasterSlaveServersConfig useMasterSlaveServers() {
        return useMasterSlaveServers(new MasterSlaveServersConfig());
    }

    MasterSlaveServersConfig useMasterSlaveServers(MasterSlaveServersConfig config) {
        checkClusterServersConfig();
        checkSingleServerConfig();
        checkSentinelServersConfig();
        checkReplicatedServersConfig();

        if (masterSlaveServersConfig == null) {
            masterSlaveServersConfig = config;
        }
        return masterSlaveServersConfig;
    }

    protected MasterSlaveServersConfig getMasterSlaveServersConfig() {
        return masterSlaveServersConfig;
    }

    protected void setMasterSlaveServersConfig(MasterSlaveServersConfig masterSlaveConnectionConfig) {
        this.masterSlaveServersConfig = masterSlaveConnectionConfig;
    }

    /** 当前是否使用集群模式。 */
    public boolean isClusterConfig() {
        return clusterServersConfig != null;
    }

    /** 当前是否使用哨兵模式。 */
    public boolean isSentinelConfig() {
        return sentinelServersConfig != null;
    }

    /** 当前是否使用单节点模式。 */
    public boolean isSingleConfig() {
        return singleServerConfig != null;
    }

    /**
     * 设置 Redis 认证密码；无需认证时传 null。
     * <p>
     * Default is <code>null</code>
     *
     * @param password 连接密码
     * @return 当前配置实例
     */
    public Config setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getPassword() {
        return password;
    }

    /**
     * 设置 Redis 认证用户名（Redis 6.0+ ACL）；无需时传 null。
     * <p>
     * Default is <code>null</code>
     * <p>
     * Requires Redis 6.0+
     *
     * @param username 连接用户名
     * @return 当前配置实例
     */
    public Config setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public CredentialsResolver getCredentialsResolver() {
        return credentialsResolver;
    }

    /**
     * 设置凭据解析器，在连接建立时动态获取认证信息，支持令牌轮换。
     *
     * @see EntraIdCredentialsResolver
     *
     * @param credentialsResolver 凭据解析器实例
     * @return 当前配置实例
     */
    public Config setCredentialsResolver(CredentialsResolver credentialsResolver) {
        this.credentialsResolver = credentialsResolver;
        return this;
    }

    public int getThreads() {
        return threads;
    }

    /**
     * 设置 RTopic 监听器、RRemoteService 调用处理器与 RExecutorService 任务共享的业务线程数。
     * <p>
     * Default is <code>16</code>.
     * <p>
     * <code>0</code> means <code>current_processors_amount * 2</code>
     *
     * @param threads 线程数量
     * @return 当前配置实例
     */
    public Config setThreads(int threads) {
        this.threads = threads;
        return this;
    }

    /** 确保尚未启用集群模式（连接模式互斥校验）。 */
    private void checkClusterServersConfig() {
        if (clusterServersConfig != null) {
            throw new IllegalStateException("cluster servers config already used!");
        }
    }

    /** 确保尚未启用哨兵模式。 */
    private void checkSentinelServersConfig() {
        if (sentinelServersConfig != null) {
            throw new IllegalStateException("sentinel servers config already used!");
        }
    }

    /** 确保尚未启用主从模式。 */
    private void checkMasterSlaveServersConfig() {
        if (masterSlaveServersConfig != null) {
            throw new IllegalStateException("master/slave servers already used!");
        }
    }

    /** 确保尚未启用单节点模式。 */
    private void checkSingleServerConfig() {
        if (singleServerConfig != null) {
            throw new IllegalStateException("single server config already used!");
        }
    }

    /** 确保尚未启用复制模式。 */
    private void checkReplicatedServersConfig() {
        if (replicatedServersConfig != null) {
            throw new IllegalStateException("Replication servers config already used!");
        }
    }

    /**
     * 设置 Netty 传输模式（NIO/Epoll/KQueue 等）。
     * <p>
     * Default is {@link TransportMode#NIO}
     *
     * @param transportMode 传输模式
     * @return 当前配置实例
     */
    public Config setTransportMode(TransportMode transportMode) {
        this.transportMode = transportMode;
        return this;
    }

    public TransportMode getTransportMode() {
        return transportMode;
    }

    /**
     * 设置 Redisson 所有 Redis 客户端共享的 Netty I/O 线程数。
     * <p>
     * Default is <code>32</code>.
     * <p>
     * <code>0</code> means <code>current_processors_amount * 2</code>
     *
     * @param nettyThreads Netty 线程数
     * @return 当前配置实例
     */
    public Config setNettyThreads(int nettyThreads) {
        this.nettyThreads = nettyThreads;
        return this;
    }

    public int getNettyThreads() {
        return nettyThreads;
    }

    public Executor getNettyExecutor() {
        return nettyExecutor;
    }

    /**
     * 使用外部 Executor 执行 Netty 任务（不推荐虚拟线程）。
     * <p>
     * Virtual threads are not recommended
     * <p>
     * The caller is responsible for closing the Executor.
     *
     * @param nettyExecutor Netty 执行器
     * @return 当前配置实例
     */
    public Config setNettyExecutor(Executor nettyExecutor) {
        this.nettyExecutor = nettyExecutor;
        return this;
    }

    /**
     * 使用外部 ExecutorService 处理 RTopic/RPatternTopic 监听、
     * RRemoteService 调用与 RExecutorService 任务。
     * <p>
     * The caller is responsible for closing the ExecutorService.
     * 
     * @param executor 业务线程池
     * @return 当前配置实例
     */
    public Config setExecutor(ExecutorService executor) {
        this.executor = executor;
        return this;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    /**
     * 使用外部 EventLoopGroup 处理所有 Redis 连接的 Netty I/O。
     * 同一 JVM 内多个 Redisson 实例可共享一个 EventLoopGroup 以节省线程。
     * <p>
     * Only {@link io.netty.channel.epoll.EpollEventLoopGroup}, 
     * {@link io.netty.channel.kqueue.KQueueEventLoopGroup}
     * {@link io.netty.channel.nio.NioEventLoopGroup} can be used.
     * <p>
     * The caller is responsible for closing the EventLoopGroup.
     *
     * @param eventLoopGroup EventLoopGroup 实例
     * @return 当前配置实例
     */
    public Config setEventLoopGroup(EventLoopGroup eventLoopGroup) {
        this.eventLoopGroup = eventLoopGroup;
        return this;
    }

    public EventLoopGroup getEventLoopGroup() {
        return eventLoopGroup;
    }

    /**
     * 仅在未指定 leaseTimeout 加锁时生效：看门狗未续期时，锁在 lockWatchdogTimeout 后自动过期，
     * 防止客户端崩溃导致死锁。
     * <p>
     * This prevents against infinity locked locks due to Redisson client crush or
     * any other reason when lock can't be released in proper way.
     * <p>
     * Default is 30000 milliseconds
     *
     * @param lockWatchdogTimeout 看门狗超时（毫秒）
     * @return 当前配置实例
     */
    public Config setLockWatchdogTimeout(long lockWatchdogTimeout) {
        this.lockWatchdogTimeout = lockWatchdogTimeout;
        return this;
    }

    public long getLockWatchdogTimeout() {
        return lockWatchdogTimeout;
    }


    /**
     * 公平锁未指定 waitTimeout 时的默认最大等待时间。
     *
     * Default is 5*60000 milliseconds
     *
     * @param fairLockWaitTimeout 等待超时（毫秒）
     * @return 当前配置实例
     */
    public Config setFairLockWaitTimeout(long fairLockWaitTimeout) {
        this.fairLockWaitTimeout = fairLockWaitTimeout;
        return this;
    }

    public long getFairLockWaitTimeout() {
        return fairLockWaitTimeout;
    }

    /**
     * This parameter is only used if lock has been acquired without leaseTimeout parameter definition.
     * 单次看门狗执行批量续期的锁数量，影响续期效率与 Redis 负载。
     * <p>
     * Default is 100
     *
     * @param lockWatchdogBatchSize 单次看门狗处理的锁数量
     * @return 当前配置实例
     */
    public Config setLockWatchdogBatchSize(int lockWatchdogBatchSize) {
        this.lockWatchdogBatchSize = lockWatchdogBatchSize;
        return this;
    }
    public int getLockWatchdogBatchSize() {
        return lockWatchdogBatchSize;
    }

    /**
     * 加锁后是否校验已同步从节点数是否满足要求，保证读写一致性。
     * <p>
     * Default is <code>true</code>.
     *
     * @param checkLockSyncedSlaves 是否校验从节点同步
     * @return 当前配置实例
     */
    public Config setCheckLockSyncedSlaves(boolean checkLockSyncedSlaves) {
        this.checkLockSyncedSlaves = checkLockSyncedSlaves;
        return this;
    }

    public boolean isCheckLockSyncedSlaves() {
        return checkLockSyncedSlaves;
    }

    /**
     * 同一 Pub/Sub 频道内消息是否严格按到达顺序处理（否则可并发）。 
     * <p>
     * This setting applied only for PubSub messages per channel.
     * <p>
     * Default is <code>true</code>.
     * 
     * @param keepPubSubOrder 是否保持顺序
     * @return 当前配置实例
     */
    public Config setKeepPubSubOrder(boolean keepPubSubOrder) {
        this.keepPubSubOrder = keepPubSubOrder;
        return this;
    }

    public boolean isKeepPubSubOrder() {
        return keepPubSubOrder;
    }

    /**
     * 切换 DNS 地址解析实现；高并发解析可改用 RoundRobinDnsAddressResolverGroup。
     * 
     * @param addressResolverGroupFactory
     * @return config
     */
    public Config setAddressResolverGroupFactory(AddressResolverGroupFactory addressResolverGroupFactory) {
        this.addressResolverGroupFactory = addressResolverGroupFactory;
        return this;
    }

    public AddressResolverGroupFactory getAddressResolverGroupFactory() {
        return addressResolverGroupFactory;
    }

    /**
     * 从 YAML 字符串加载配置。
     *
     * @param content YAML 配置内容
     * @return 解析后的 Config 实例
     */
    public static Config fromYAML(String content) {
        ConfigSupport support = new ConfigSupport();
        return support.fromYAML(content, Config.class);
    }

    /**
     * 从 InputStream 读取 YAML 配置。
     *
     * @param inputStream 输入流
     * @return 解析后的 Config 实例
     */
    public static Config fromYAML(InputStream inputStream) {
        ConfigSupport support = new ConfigSupport();
        return support.fromYAML(inputStream, Config.class);
    }

    /**
     * 从文件读取 YAML 配置。
     *
     * @param file 配置文件
     * @return 解析后的 Config 实例
     * @throws IOException 读取失败
     */
    public static Config fromYAML(File file) throws IOException {
        return fromYAML(file, null);
    }

    public static Config fromYAML(File file, ClassLoader classLoader) throws IOException {
        ConfigSupport support = new ConfigSupport();
        return support.fromYAML(file, Config.class, classLoader);
    }

    /**
     * 从 URL 读取 YAML 配置。
     *
     * @param url 配置 URL
     * @return 解析后的 Config 实例
     * @throws IOException 读取失败
     */
    public static Config fromYAML(URL url) throws IOException {
        ConfigSupport support = new ConfigSupport();
        return support.fromYAML(url, Config.class);
    }

    /**
     * 从 Reader 读取 YAML 配置。
     *
     * @param reader 字符读取器
     * @return 解析后的 Config 实例
     * @throws IOException 读取失败
     */
    public static Config fromYAML(Reader reader) throws IOException {
        ConfigSupport support = new ConfigSupport();
        return support.fromYAML(reader, Config.class);
    }

    /**
     * 将当前配置序列化为 YAML 字符串。
     *
     * @return YAML 格式配置
     * @throws IOException 序列化失败
     */
    public String toYAML() throws IOException {
        ConfigSupport support = new ConfigSupport();
        return support.toYAML(this);
    }

    /**
     * 是否在 Redis 端缓存 Lua 脚本，可加速脚本类命令并减少网络流量。 
     * Most Redisson methods are Lua-script based and this setting turned
     * on could increase speed of such methods execution and save network traffic.
     * <p>
     * Default is <code>true</code>.
     * 
     * @param useScriptCache 是否启用脚本缓存
     * @return 当前配置实例
     */
    public Config setUseScriptCache(boolean useScriptCache) {
        this.useScriptCache = useScriptCache;
        return this;
    }

    public boolean isUseScriptCache() {
        return useScriptCache;
    }

    public int getMinCleanUpDelay() {
        return minCleanUpDelay;
    }
    
    /**
     * 过期条目后台清理的最小间隔（秒），应用于带 TTL 的缓存结构。
     * <p>
     * Applied to JCache, RSetCache, RMapCache, RListMultimapCache, RSetMultimapCache objects.
     * <p>
     * Default is <code>5</code>.
     * 
     * @param minCleanUpDelay 最小清理间隔（秒）
     * @return 当前配置实例
     */
    public Config setMinCleanUpDelay(int minCleanUpDelay) {
        this.minCleanUpDelay = minCleanUpDelay;
        return this;
    }

    public int getMaxCleanUpDelay() {
        return maxCleanUpDelay;
    }
    
    /**
     * 过期条目后台清理的最大间隔（秒）。
     * <p>
     * Applied to JCache, RSetCache, RMapCache, RListMultimapCache, RSetMultimapCache objects.
     * <p>
     * Default is <code>1800</code>.
     *
     * @param maxCleanUpDelay 最大清理间隔（秒）
     * @return 当前配置实例
     */
    public Config setMaxCleanUpDelay(int maxCleanUpDelay) {
        this.maxCleanUpDelay = maxCleanUpDelay;
        return this;
    }

    public int getCleanUpKeysAmount() {
        return cleanUpKeysAmount;
    }

    /**
     * 单次清理任务最多删除的过期键数量。
     * <p>
     * Applied to JCache, RSetCache, RMapCache, RListMultimapCache, RSetMultimapCache objects.
     * <p>
     * Default is <code>100</code>.
     *
     * @param cleanUpKeysAmount 单次删除键数量
     * @return 当前配置实例
     */
    public Config setCleanUpKeysAmount(int cleanUpKeysAmount) {
        this.cleanUpKeysAmount = cleanUpKeysAmount;
        return this;
    }

    public boolean isUseThreadClassLoader() {
        return useThreadClassLoader;
    }

    /**
     * 是否向 Codec 提供线程上下文 ClassLoader，可解决 Tomcat 等容器中的 ClassNotFoundException。
     * <p>
     * Default is <code>true</code>.
     *
     * @param useThreadClassLoader 是否使用上下文 ClassLoader
     * @return 当前配置实例
     */
    public Config setUseThreadClassLoader(boolean useThreadClassLoader) {
        this.useThreadClassLoader = useThreadClassLoader;
        return this;
    }

    public long getReliableTopicWatchdogTimeout() {
        return reliableTopicWatchdogTimeout;
    }

    /**
     * Reliable Topic 订阅者看门狗未续期时的过期时间，防止消息无限堆积。
     * <p>
     * This prevents against infinity grow of stored messages in topic due to Redisson client crush or
     * any other reason when subscriber can't consumer messages anymore.
     * <p>
     * Default is 600000 milliseconds
     *
     * @param timeout 超时（毫秒）
     * @return 当前配置实例
     */
    public Config setReliableTopicWatchdogTimeout(long timeout) {
        this.reliableTopicWatchdogTimeout = timeout;
        return this;
    }

    public ConnectionListener getConnectionListener() {
        return connectionListener;
    }

    /**
     * 设置连接监听器，在连接建立或断开时回调。
     *
     * @param connectionListener 连接监听器
     * @return 当前配置实例
     */
    public Config setConnectionListener(ConnectionListener connectionListener) {
        this.connectionListener = connectionListener;
        return this;
    }

    public long getSlavesSyncTimeout() {
        return slavesSyncTimeout;
    }

    /**
     * RLock/RSemaphore 等操作等待从节点同步的超时时间。
     * <p>
     * Default is <code>1000</code> milliseconds.
     *
     * @param timeout timeout in milliseconds
     * @return config
     */
    public Config setSlavesSyncTimeout(long timeout) {
        this.slavesSyncTimeout = timeout;
        return this;
    }

    public boolean isLazyInitialization() {
        return lazyInitialization;
    }

    /**
     * 是否延迟连接：true 表示首次 Redis 调用时才建连，false 表示创建 Redisson 时即连接。
     * <p>
     * Default value is <code>false</code>
     *
     * @param lazyInitialization 是否延迟初始化连接
     * @return 当前配置实例
     */
    public Config setLazyInitialization(boolean lazyInitialization) {
        this.lazyInitialization = lazyInitialization;
        return this;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    /**
     * 设置 Redis 协议版本（RESP2/RESP3）。
     * <p>
     * Default value is <code>RESP2</code>
     *
     * @param protocol 协议版本
     * @return 当前配置实例
     */
    public Config setProtocol(Protocol protocol) {
        this.protocol = protocol;
        return this;
    }

    public Set<ValkeyCapability> getValkeyCapabilities() {
        return valkeyCapabilities;
    }

    /**
     * 声明客户端需支持的 Valkey 能力集合。
     *
     * @param valkeyCapabilities Valkey 能力集合
     * @return 当前配置实例
     */
    public Config setValkeyCapabilities(Set<ValkeyCapability> valkeyCapabilities) {
        this.valkeyCapabilities = valkeyCapabilities;
        return this;
    }

    public NameMapper getNameMapper() {
        return nameMapper;
    }

    /**
     * 设置全局 Redisson 对象名称映射器（如加前缀隔离环境）。
     *
     * @param nameMapper 名称映射器
     * @return 当前配置实例
     */
    public Config setNameMapper(NameMapper nameMapper) {
        this.nameMapper = nameMapper;
        return this;
    }

    public CommandMapper getCommandMapper() {
        return commandMapper;
    }

    /**
     * 设置全局 Redis 命令名映射器。
     *
     * @param commandMapper 命令映射器
     * @return 当前配置实例
     */
    public Config setCommandMapper(CommandMapper commandMapper) {
        this.commandMapper = commandMapper;
        return this;
    }
    
    public SslProvider getSslProvider() {
        return sslProvider;
    }
    
    /**
     * 设置 SSL 实现提供方（JDK 或 OpenSSL 等）。
     * <p>
     * Default is <code>JDK</code>
     *
     * @param sslProvider SSL 提供方
     * @return 当前配置实例
     */
    public Config setSslProvider(SslProvider sslProvider) {
        this.sslProvider = sslProvider;
        return this;
    }
    
    public URL getSslTruststore() {
        return sslTruststore;
    }
    
    /**
     * 设置 SSL 信任库路径。
     * <p>
     * Default is <code>null</code>
     *
     * @param sslTruststore 信任库 URL
     * @return 当前配置实例
     */
    public Config setSslTruststore(URL sslTruststore) {
        this.sslTruststore = sslTruststore;
        return this;
    }
    
    public String getSslTruststorePassword() {
        return sslTruststorePassword;
    }
    
    /**
     * 设置 SSL 信任库密码；每次新建连接时读取，支持热更新。
     * <p>
     * Default is <code>null</code>
     *
     * @param sslTruststorePassword 信任库密码
     * @return 当前配置实例
     */
    public Config setSslTruststorePassword(String sslTruststorePassword) {
        this.sslTruststorePassword = sslTruststorePassword;
        return this;
    }
    
    public URL getSslKeystore() {
        return sslKeystore;
    }
    
    /**
     * 设置 SSL 密钥库路径，每次建连时读取。
     * <p>
     * Default is <code>null</code>
     *
     * @param sslKeystore 密钥库 URL
     * @return 当前配置实例
     */
    public Config setSslKeystore(URL sslKeystore) {
        this.sslKeystore = sslKeystore;
        return this;
    }
    
    public String getSslKeystorePassword() {
        return sslKeystorePassword;
    }
    
    /**
     * 设置 SSL 密钥库密码。
     * <p>
     * Default is <code>null</code>
     *
     * @param sslKeystorePassword 密钥库密码
     * @return 当前配置实例
     */
    public Config setSslKeystorePassword(String sslKeystorePassword) {
        this.sslKeystorePassword = sslKeystorePassword;
        return this;
    }
    
    public String[] getSslProtocols() {
        return sslProtocols;
    }
    
    /**
     * 设置允许的 SSL/TLS 协议版本，如 TLSv1.3、TLSv1.2。
     * <p>
     * Default is <code>null</code>
     *
     * @param sslProtocols 协议版本数组
     * @return 当前配置实例
     */
    public Config setSslProtocols(String[] sslProtocols) {
        this.sslProtocols = sslProtocols;
        return this;
    }
    
    public String getSslKeystoreType() {
        return sslKeystoreType;
    }
    
    /**
     * 设置 SSL 密钥库类型（如 PKCS12、JKS）。
     * <p>
     * Default is <code>null</code>
     *
     * @param sslKeystoreType 密钥库类型
     * @return 当前配置实例
     */
    public Config setSslKeystoreType(String sslKeystoreType) {
        this.sslKeystoreType = sslKeystoreType;
        return this;
    }
    
    public String[] getSslCiphers() {
        return sslCiphers;
    }
    
    /**
     * 设置允许的 SSL 密码套件列表。
     * <p>
     * Default is <code>null</code>
     *
     * @param sslCiphers 密码套件数组
     * @return 当前配置实例
     */
    public Config setSslCiphers(String[] sslCiphers) {
        this.sslCiphers = sslCiphers;
        return this;
    }
    
    public TrustManagerFactory getSslTrustManagerFactory() {
        return sslTrustManagerFactory;
    }
    
    /**
     * 设置自定义 SSL TrustManagerFactory。
     * <p>
     * Default is <code>null</code>
     *
     * @param trustManagerFactory TrustManagerFactory 实例
     * @return 当前配置实例
     */
    public Config setSslTrustManagerFactory(TrustManagerFactory trustManagerFactory) {
        this.sslTrustManagerFactory = trustManagerFactory;
        return this;
    }
    
    public KeyManagerFactory getSslKeyManagerFactory() {
        return sslKeyManagerFactory;
    }
    
    /**
     * 设置自定义 SSL KeyManagerFactory。
     * <p>
     * Default is <code>null</code>
     *
     * @param keyManagerFactory KeyManagerFactory 实例
     * @return 当前配置实例
     */
    public Config setSslKeyManagerFactory(KeyManagerFactory keyManagerFactory) {
        this.sslKeyManagerFactory = keyManagerFactory;
        return this;
    }
    
    public SslVerificationMode getSslVerificationMode() {
        return sslVerificationMode;
    }
    
    /**
     * 设置 SSL 证书校验模式，防范中间人攻击。
     *
     * <p>
     * Default is <code>SslVerificationMode.STRICT</code>
     *
     * @param sslVerificationMode 校验模式
     * @return 当前配置实例
     */
    public Config setSslVerificationMode(SslVerificationMode sslVerificationMode) {
        this.sslVerificationMode = sslVerificationMode;
        return this;
    }
    
    public boolean isTcpKeepAlive() {
        return tcpKeepAlive;
    }
    
    /**
     * 启用 TCP KeepAlive，检测死连接。
     * <p>
     * Default is <code>true</code>
     *
     * @param tcpKeepAlive 是否启用
     * @return 当前配置实例
     */
    public Config setTcpKeepAlive(boolean tcpKeepAlive) {
        this.tcpKeepAlive = tcpKeepAlive;
        return this;
    }
    
    public int getTcpKeepAliveCount() {
        return tcpKeepAliveCount;
    }
    
    /**
     * 断开连接前允许发送的最大 KeepAlive 探测次数。
     *
     * @param tcpKeepAliveCount 最大探测次数
     * @return 当前配置实例
     */
    public Config setTcpKeepAliveCount(int tcpKeepAliveCount) {
        this.tcpKeepAliveCount = tcpKeepAliveCount;
        return this;
    }
    
    public int getTcpKeepAliveIdle() {
        return tcpKeepAliveIdle;
    }
    
    /**
     * 连接空闲多少秒后开始发送 KeepAlive 探测包。
     *
     * @param tcpKeepAliveIdle 空闲秒数
     * @return 当前配置实例
     */
    public Config setTcpKeepAliveIdle(int tcpKeepAliveIdle) {
        this.tcpKeepAliveIdle = tcpKeepAliveIdle;
        return this;
    }
    
    public int getTcpKeepAliveInterval() {
        return tcpKeepAliveInterval;
    }
    
    /**
     * KeepAlive 探测包之间的间隔（秒）。
     *
     * @param tcpKeepAliveInterval 探测间隔（秒）
     * @return 当前配置实例
     */
    public Config setTcpKeepAliveInterval(int tcpKeepAliveInterval) {
        this.tcpKeepAliveInterval = tcpKeepAliveInterval;
        return this;
    }
    
    public int getTcpUserTimeout() {
        return tcpUserTimeout;
    }
    
    /**
     * 未确认数据的最大保留时间（毫秒），超时则强制关闭连接；仅 Epoll/IoUring 传输生效。
     * <p>
     * This setting is applied only to Epoll and IoUring transport.
     *
     * @param tcpUserTimeout 超时（毫秒）
     * @return 当前配置实例
     */
    public Config setTcpUserTimeout(int tcpUserTimeout) {
        this.tcpUserTimeout = tcpUserTimeout;
        return this;
    }
    
    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }
    
    /**
     * 启用 TCP_NODELAY，降低小 packet 延迟（禁用 Nagle）。
     * <p>
     * Default is <code>true</code>
     *
     * @param tcpNoDelay 是否启用
     * @return 当前配置实例
     */
    public Config setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
        return this;
    }
}
