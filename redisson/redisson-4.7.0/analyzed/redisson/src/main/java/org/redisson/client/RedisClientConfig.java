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
package org.redisson.client;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.DuplexChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.resolver.AddressResolverGroup;
import io.netty.util.Timer;
import org.redisson.config.*;
import org.redisson.misc.RedisURI;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.net.InetSocketAddress;
import java.net.URL;
import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

/**
 * {@link RedisClient} 的配置对象，采用流式 setter 构建。
 * <p>
 * 涵盖连接地址、超时、SSL、凭据、Netty 钩子、故障检测与协议版本等选项。
 *
 * @author Nikita Koksharov
 *
 */
public class RedisClientConfig {

    /** Redis 连接 URI（含协议、主机、端口等）。 */
    private RedisURI address;
    /** 已解析的套接字地址（可选，与 address 配合使用）。 */
    private InetSocketAddress addr;
    
    /** Netty 定时器，未设置时由客户端自动创建。 */
    private Timer timer;
    /** 回调执行线程池，未设置时由客户端自动创建。 */
    private ExecutorService executor;
    /** Netty 事件循环组，未设置时由客户端自动创建。 */
    private EventLoopGroup group;
    /** DNS 地址解析器组。 */
    private AddressResolverGroup<InetSocketAddress> resolverGroup;
    /** 套接字 Channel 实现类，默认 {@link NioSocketChannel}。 */
    private Class<? extends DuplexChannel> socketChannelClass = NioSocketChannel.class;
    /** TCP 连接超时（毫秒）。 */
    private int connectTimeout = 10000;
    /** 命令执行超时（毫秒）。 */
    private int commandTimeout = 10000;

    /** ACL 用户名。 */
    private String username;
    /** 认证密码。 */
    private String password;
    /** 默认数据库编号。 */
    private int database;
    /** CLIENT SETNAME 使用的客户端名称。 */
    private String clientName;
    /** 是否以只读模式连接（集群从节点）。 */
    private boolean readOnly;
    /** Pub/Sub 消息是否保持订阅顺序。 */
    private boolean keepPubSubOrder = true;
    /** 连接保活 PING 间隔（毫秒），0 表示禁用。 */
    private int pingConnectionInterval;
    /** 是否启用 TCP SO_KEEPALIVE。 */
    private boolean keepAlive;
    /** TCP KeepAlive 探测次数。 */
    private int tcpKeepAliveCount;
    /** TCP KeepAlive 空闲时间（秒）。 */
    private int tcpKeepAliveIdle;
    /** TCP KeepAlive 探测间隔（秒）。 */
    private int tcpKeepAliveInterval;
    /** TCP 用户超时（毫秒）。 */
    private int tcpUserTimeout;
    /** 是否启用 TCP_NODELAY（禁用 Nagle）。 */
    private boolean tcpNoDelay;
    
    private String sslHostname;
    private SslVerificationMode sslVerificationMode = SslVerificationMode.STRICT;
    private SslProvider sslProvider = SslProvider.JDK;
    private String sslKeystoreType;
    private URL sslTruststore;
    private String sslTruststorePassword;
    private URL sslKeystore;
    private String sslKeystorePassword;
    private String[] sslProtocols;
    private String[] sslCiphers;
    private TrustManagerFactory sslTrustManagerFactory;
    private KeyManagerFactory sslKeyManagerFactory;
    /** Netty 初始化钩子。 */
    private NettyHook nettyHook = new DefaultNettyHook();
    /** 动态凭据解析器。 */
    private CredentialsResolver credentialsResolver = new DefaultCredentialsResolver();
    /** 连接建立成功时的回调。 */
    private Consumer<InetSocketAddress> connectedListener;
    /** 连接断开时的回调。 */
    private Consumer<InetSocketAddress> disconnectedListener;

    /** Redis 命令名映射器（兼容 Valkey 等变体）。 */
    private CommandMapper commandMapper = new DefaultCommandMapper();

    /** 节点故障检测策略。 */
    private FailedNodeDetector failedNodeDetector = new FailedConnectionDetector();

    /** RESP 协议版本，默认 RESP2。 */
    private Protocol protocol = Protocol.RESP2;

    /** 服务端能力集合（HELLO 协商）。 */
    private Set<ValkeyCapability> capabilities = Collections.emptySet();

    /** 重连延迟策略。 */
    private DelayStrategy reconnectionDelay = new EqualJitterDelay(Duration.ofMillis(100), Duration.ofSeconds(10));

    public RedisClientConfig() {
    }
    
    /** 复制构造，供 {@link RedisClient} 内部创建配置快照。 */
    RedisClientConfig(RedisClientConfig config) {
        super();
        this.nettyHook = config.nettyHook;
        this.addr = config.addr;
        this.address = config.address;
        this.timer = config.timer;
        this.executor = config.executor;
        this.group = config.group;
        this.socketChannelClass = config.socketChannelClass;
        this.connectTimeout = config.connectTimeout;
        this.commandTimeout = config.commandTimeout;
        this.password = config.password;
        this.username = config.username;
        this.database = config.database;
        this.clientName = config.clientName;
        this.readOnly = config.readOnly;
        this.keepPubSubOrder = config.keepPubSubOrder;
        this.pingConnectionInterval = config.pingConnectionInterval;
        this.keepAlive = config.keepAlive;
        this.tcpNoDelay = config.tcpNoDelay;
        this.sslProvider = config.sslProvider;
        this.sslTruststore = config.sslTruststore;
        this.sslTruststorePassword = config.sslTruststorePassword;
        this.sslKeystore = config.sslKeystore;
        this.sslKeystorePassword = config.sslKeystorePassword;
        this.sslProtocols = config.sslProtocols;
        this.sslCiphers = config.sslCiphers;
        this.resolverGroup = config.resolverGroup;
        this.sslHostname = config.sslHostname;
        this.credentialsResolver = config.credentialsResolver;
        this.connectedListener = config.connectedListener;
        this.disconnectedListener = config.disconnectedListener;
        this.sslKeyManagerFactory = config.sslKeyManagerFactory;
        this.sslTrustManagerFactory = config.sslTrustManagerFactory;
        this.commandMapper = config.commandMapper;
        if (config.failedNodeDetector != null) {
            this.failedNodeDetector = config.failedNodeDetector.copy();
        }
        this.tcpKeepAliveCount = config.tcpKeepAliveCount;
        this.tcpKeepAliveIdle = config.tcpKeepAliveIdle;
        this.tcpKeepAliveInterval = config.tcpKeepAliveInterval;
        this.tcpUserTimeout = config.tcpUserTimeout;
        this.protocol = config.protocol;
        this.sslKeystoreType = config.sslKeystoreType;
        this.sslVerificationMode = config.sslVerificationMode;
        this.capabilities = config.capabilities;
        this.reconnectionDelay = config.reconnectionDelay;
    }

    public NettyHook getNettyHook() {
        return nettyHook;
    }
    public RedisClientConfig setNettyHook(NettyHook nettyHook) {
        this.nettyHook = nettyHook;
        return this;
    }

    public String getSslHostname() {
        return sslHostname;
    }
    public RedisClientConfig setSslHostname(String sslHostname) {
        this.sslHostname = sslHostname;
        return this;
    }

    /** 以主机名与端口设置 Redis 地址。 */
    public RedisClientConfig setAddress(String host, int port) {
        this.address = new RedisURI(RedisURI.REDIS_PROTOCOL + host + ":" + port);
        return this;
    }
    public RedisClientConfig setAddress(String address) {
        this.address = new RedisURI(address);
        return this;
    }
    public RedisClientConfig setAddress(InetSocketAddress addr, RedisURI address) {
        this.addr = addr;
        this.address = address;
        return this;
    }
    public RedisClientConfig setAddress(RedisURI address) {
        this.address = address;
        return this;
    }
    public RedisURI getAddress() {
        return address;
    }
    public InetSocketAddress getAddr() {
        return addr;
    }
    
    public Timer getTimer() {
        return timer;
    }
    public RedisClientConfig setTimer(Timer timer) {
        this.timer = timer;
        return this;
    }
    
    public ExecutorService getExecutor() {
        return executor;
    }
    public RedisClientConfig setExecutor(ExecutorService executor) {
        this.executor = executor;
        return this;
    }
    
    public EventLoopGroup getGroup() {
        return group;
    }
    public RedisClientConfig setGroup(EventLoopGroup group) {
        this.group = group;
        return this;
    }
    
    public Class<? extends DuplexChannel> getSocketChannelClass() {
        return socketChannelClass;
    }
    public RedisClientConfig setSocketChannelClass(Class<? extends DuplexChannel> socketChannelClass) {
        this.socketChannelClass = socketChannelClass;
        return this;
    }
    
    public int getConnectTimeout() {
        return connectTimeout;
    }
    public RedisClientConfig setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }
    
    public int getCommandTimeout() {
        return commandTimeout;
    }
    public RedisClientConfig setCommandTimeout(int commandTimeout) {
        this.commandTimeout = commandTimeout;
        return this;
    }
    
    public SslProvider getSslProvider() {
        return sslProvider;
    }
    public RedisClientConfig setSslProvider(SslProvider sslMode) {
        this.sslProvider = sslMode;
        return this;
    }
    
    public URL getSslTruststore() {
        return sslTruststore;
    }
    public RedisClientConfig setSslTruststore(URL sslTruststore) {
        this.sslTruststore = sslTruststore;
        return this;
    }
    
    public URL getSslKeystore() {
        return sslKeystore;
    }
    public RedisClientConfig setSslKeystore(URL sslKeystore) {
        this.sslKeystore = sslKeystore;
        return this;
    }
    
    public String getSslKeystorePassword() {
        return sslKeystorePassword;
    }
    public RedisClientConfig setSslKeystorePassword(String sslKeystorePassword) {
        this.sslKeystorePassword = sslKeystorePassword;
        return this;
    }

    public String getSslTruststorePassword() {
        return sslTruststorePassword;
    }
    public RedisClientConfig setSslTruststorePassword(String sslTruststorePassword) {
        this.sslTruststorePassword = sslTruststorePassword;
        return this;
    }

    @Deprecated
    public boolean isSslEnableEndpointIdentification() {
        return this.sslVerificationMode == SslVerificationMode.STRICT;
    }
    @Deprecated
    public RedisClientConfig setSslEnableEndpointIdentification(boolean enableEndpointIdentification) {
        if (enableEndpointIdentification) {
            this.sslVerificationMode = SslVerificationMode.STRICT;
        } else {
            this.sslVerificationMode = SslVerificationMode.NONE;
        }
        return this;
    }

    public String getPassword() {
        return password;
    }
    public RedisClientConfig setPassword(String password) {
        this.password = password;
        return this;
    }
    
    public int getDatabase() {
        return database;
    }
    public RedisClientConfig setDatabase(int database) {
        this.database = database;
        return this;
    }
    
    public String getClientName() {
        return clientName;
    }
    public RedisClientConfig setClientName(String clientName) {
        this.clientName = clientName;
        return this;
    }
    
    public boolean isReadOnly() {
        return readOnly;
    }
    public RedisClientConfig setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    public boolean isKeepPubSubOrder() {
        return keepPubSubOrder;
    }
    public RedisClientConfig setKeepPubSubOrder(boolean keepPubSubOrder) {
        this.keepPubSubOrder = keepPubSubOrder;
        return this;
    }

    public int getPingConnectionInterval() {
        return pingConnectionInterval;
    }    
    public RedisClientConfig setPingConnectionInterval(int pingConnectionInterval) {
        this.pingConnectionInterval = pingConnectionInterval;
        return this;
    }
    
    public boolean isKeepAlive() {
        return keepAlive;
    }
    public RedisClientConfig setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
        return this;
    }

    public int getTcpKeepAliveCount() {
        return tcpKeepAliveCount;
    }
    public RedisClientConfig setTcpKeepAliveCount(int tcpKeepAliveCount) {
        this.tcpKeepAliveCount = tcpKeepAliveCount;
        return this;
    }

    public int getTcpKeepAliveIdle() {
        return tcpKeepAliveIdle;
    }
    public RedisClientConfig setTcpKeepAliveIdle(int tcpKeepAliveIdle) {
        this.tcpKeepAliveIdle = tcpKeepAliveIdle;
        return this;
    }

    public int getTcpKeepAliveInterval() {
        return tcpKeepAliveInterval;
    }
    public RedisClientConfig setTcpKeepAliveInterval(int tcpKeepAliveInterval) {
        this.tcpKeepAliveInterval = tcpKeepAliveInterval;
        return this;
    }

    public int getTcpUserTimeout() {
        return tcpUserTimeout;
    }

    public RedisClientConfig setTcpUserTimeout(int tcpUserTimeout) {
        this.tcpUserTimeout = tcpUserTimeout;
        return this;
    }

    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }
    public RedisClientConfig setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
        return this;
    }

    public AddressResolverGroup<InetSocketAddress> getResolverGroup() {
        return resolverGroup;
    }
    public RedisClientConfig setResolverGroup(AddressResolverGroup<InetSocketAddress> resolverGroup) {
        this.resolverGroup = resolverGroup;
        return this;
    }

    public String getUsername() {
        return username;
    }
    public RedisClientConfig setUsername(String username) {
        this.username = username;
        return this;
    }

    public String[] getSslProtocols() {
        return sslProtocols;
    }
    public RedisClientConfig setSslProtocols(String[] sslProtocols) {
        this.sslProtocols = sslProtocols;
        return this;
    }

    public String[] getSslCiphers() {
        return sslCiphers;
    }

    public RedisClientConfig setSslCiphers(String[] sslCiphers) {
        this.sslCiphers = sslCiphers;
        return this;
    }

    public CredentialsResolver getCredentialsResolver() {
        return credentialsResolver;
    }

    public RedisClientConfig setCredentialsResolver(CredentialsResolver credentialsResolver) {
        this.credentialsResolver = credentialsResolver;
        return this;
    }

    public Consumer<InetSocketAddress> getConnectedListener() {
        return connectedListener;
    }
    public RedisClientConfig setConnectedListener(Consumer<InetSocketAddress> connectedListener) {
        this.connectedListener = connectedListener;
        return this;
    }

    public Consumer<InetSocketAddress> getDisconnectedListener() {
        return disconnectedListener;
    }
    public RedisClientConfig setDisconnectedListener(Consumer<InetSocketAddress> disconnectedListener) {
        this.disconnectedListener = disconnectedListener;
        return this;
    }

    public TrustManagerFactory getSslTrustManagerFactory() {
        return sslTrustManagerFactory;
    }

    public RedisClientConfig setSslTrustManagerFactory(TrustManagerFactory sslTrustManagerFactory) {
        this.sslTrustManagerFactory = sslTrustManagerFactory;
        return this;
    }

    public KeyManagerFactory getSslKeyManagerFactory() {
        return sslKeyManagerFactory;
    }

    public RedisClientConfig setSslKeyManagerFactory(KeyManagerFactory sslKeyManagerFactory) {
        this.sslKeyManagerFactory = sslKeyManagerFactory;
        return this;
    }

    public CommandMapper getCommandMapper() {
        return commandMapper;
    }

    public RedisClientConfig setCommandMapper(CommandMapper commandMapper) {
        this.commandMapper = commandMapper;
        return this;
    }

    /** 返回节点故障检测器。 */
    public FailedNodeDetector getFailedNodeDetector() {
        return failedNodeDetector;
    }

    /** 设置节点故障检测策略。 */
    public RedisClientConfig setFailedNodeDetector(FailedNodeDetector failedNodeDetector) {
        this.failedNodeDetector = failedNodeDetector;
        return this;
    }

    /** 返回 RESP 协议版本。 */
    public Protocol getProtocol() {
        return protocol;
    }

    public RedisClientConfig setProtocol(Protocol protocol) {
        this.protocol = protocol;
        return this;
    }

    public String getSslKeystoreType() {
        return sslKeystoreType;
    }

    public RedisClientConfig setSslKeystoreType(String sslKeystoreType) {
        this.sslKeystoreType = sslKeystoreType;
        return this;
    }

    public SslVerificationMode getSslVerificationMode() {
        return sslVerificationMode;
    }
    public RedisClientConfig setSslVerificationMode(SslVerificationMode sslVerificationMode) {
        this.sslVerificationMode = sslVerificationMode;
        return this;
    }

    public Set<ValkeyCapability> getCapabilities() {
        return capabilities;
    }

    public RedisClientConfig setCapabilities(Set<ValkeyCapability> capabilities) {
        this.capabilities = capabilities;
        return this;
    }

    /** 返回重连延迟策略。 */
    public DelayStrategy getReconnectionDelay() {
        return reconnectionDelay;
    }
    public RedisClientConfig setReconnectionDelay(DelayStrategy reconnectionDelay) {
        this.reconnectionDelay = reconnectionDelay;
        return this;
    }
}
