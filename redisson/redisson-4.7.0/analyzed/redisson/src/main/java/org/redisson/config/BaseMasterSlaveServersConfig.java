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

import org.redisson.client.FailedConnectionDetector;
import org.redisson.client.FailedNodeDetector;
import org.redisson.connection.balancer.LoadBalancer;
import org.redisson.connection.balancer.RoundRobinLoadBalancer;

/**
 * 主从/哨兵/集群等「一主多从」拓扑的公共配置基类。
 * <p>定义读写连接池大小、读模式 {@link ReadMode}、订阅模式、
 * 从节点负载均衡 {@link LoadBalancer} 及 DNS 监控等。
 *
 * @author Nikita Koksharov
 *
 * @param <T> 配置类型
 */
public class BaseMasterSlaveServersConfig<T extends BaseMasterSlaveServersConfig<T>> extends BaseConfig<T> {

    /** 多个从节点之间的读请求负载均衡器。 */
    private LoadBalancer loadBalancer = new RoundRobinLoadBalancer();

    /** 每个从节点的读连接池最小空闲连接数。 */
    private int slaveConnectionMinimumIdleSize = 24;

    /** 每个从节点的读连接池最大连接数。 */
    private int slaveConnectionPoolSize = 64;

    /** 标记为失败的从节点重连尝试间隔（毫秒）。 */
    private int failedSlaveReconnectionInterval = 3000;

    @Deprecated
    private int failedSlaveCheckInterval = 180000;
    
    /** 主节点写连接池最小空闲连接数。 */
    private int masterConnectionMinimumIdleSize = 24;

    /** 主节点写连接池最大连接数。 */
    private int masterConnectionPoolSize = 64;

    /** 读命令路由模式，默认从从节点读取。 */
    private ReadMode readMode = ReadMode.SLAVE;

    /** 从节点 LOADING 时是否回退到主节点读。 */
    private boolean fallbackLoadingToMaster = true;
    
    /** Pub/Sub 订阅连接所连节点类型。 */
    private SubscriptionMode subscriptionMode = SubscriptionMode.MASTER;
    
    /**
     * Redis 'slave' node minimum idle subscription (pub/sub) connection amount for <b>each</b> slave node
     */
    private int subscriptionConnectionMinimumIdleSize = 1;

    /**
     * Redis 'slave' node maximum subscription (pub/sub) connection pool size for <b>each</b> slave node
     */
    private int subscriptionConnectionPoolSize = 50;

    /** DNS 解析监控间隔（毫秒），-1 禁用。 */
    private long dnsMonitoringInterval = 5000;

    private int dnsMonitoringTimes = 1;

    /** 判定从节点是否失败的检测器。 */
    private FailedNodeDetector failedSlaveNodeDetector = new FailedConnectionDetector();
    
    public BaseMasterSlaveServersConfig() {
    }

    BaseMasterSlaveServersConfig(T config) {
        super(config);
        setLoadBalancer(config.getLoadBalancer());
        setMasterConnectionPoolSize(config.getMasterConnectionPoolSize());
        setSlaveConnectionPoolSize(config.getSlaveConnectionPoolSize());
        setSubscriptionConnectionPoolSize(config.getSubscriptionConnectionPoolSize());
        setMasterConnectionMinimumIdleSize(config.getMasterConnectionMinimumIdleSize());
        setSlaveConnectionMinimumIdleSize(config.getSlaveConnectionMinimumIdleSize());
        setSubscriptionConnectionMinimumIdleSize(config.getSubscriptionConnectionMinimumIdleSize());
        setReadMode(config.getReadMode());
        setFallbackLoadingToMaster(config.isFallbackLoadingToMaster());
        setSubscriptionMode(config.getSubscriptionMode());
        setDnsMonitoringInterval(config.getDnsMonitoringInterval());
        setDnsMonitoringTimes(config.getDnsMonitoringTimes());
        setFailedSlaveReconnectionInterval(config.getFailedSlaveReconnectionInterval());
        setFailedSlaveNodeDetector(config.getFailedSlaveNodeDetector());
    }

    /**
     * 每个从节点的读连接池最大容量。
     * <p>默认 {@code 64}。
     *
     * @see #setSlaveConnectionMinimumIdleSize(int)
     * @param slaveConnectionPoolSize 池大小
     * @return config
     */
    public T setSlaveConnectionPoolSize(int slaveConnectionPoolSize) {
        this.slaveConnectionPoolSize = slaveConnectionPoolSize;
        return (T) this;
    }
    public int getSlaveConnectionPoolSize() {
        return slaveConnectionPoolSize;
    }
    
    /**
     * When the retry interval <code>failedSlavesReconnectionTimeout<code/>
     * reached Redisson tries to connect to failed Redis node reported by <code>failedSlaveNodeDetector</code>.
     * <p>
     * On every such timeout event Redisson tries
     * to connect to failed Redis server.
     * <p>
     * Default is 3000
     *
     * @param failedSlavesReconnectionTimeout - retry timeout in milliseconds
     * @return config
     */

    public T setFailedSlaveReconnectionInterval(int failedSlavesReconnectionTimeout) {
        this.failedSlaveReconnectionInterval = failedSlavesReconnectionTimeout;
        return (T) this;
    }

    public int getFailedSlaveReconnectionInterval() {
        return failedSlaveReconnectionInterval;
    }

    
    /**
     * Use {@link #setFailedSlaveNodeDetector(FailedNodeDetector)} instead.
     *
     * @param slaveFailsInterval - time interval in milliseconds
     * @return config
     */
    @Deprecated
    public T setFailedSlaveCheckInterval(int slaveFailsInterval) {
        log.error("failedSlaveCheckInterval setting is deprecated and will be removed in future releases. Use failedSlaveNodeDetector setting instead");
        this.failedSlaveCheckInterval = slaveFailsInterval;
        this.failedSlaveNodeDetector = new FailedConnectionDetector(slaveFailsInterval);
        return (T) this;
    }
    @Deprecated
    public int getFailedSlaveCheckInterval() {
        return failedSlaveCheckInterval;
    }

    /**
     * Redis 'master' server connection pool size.
     * <p>
     * Default is <code>64</code>
     *
     * @see #setMasterConnectionMinimumIdleSize(int)
     * 
     * @param masterConnectionPoolSize - pool size
     * @return config
     *
     */
    public T setMasterConnectionPoolSize(int masterConnectionPoolSize) {
        this.masterConnectionPoolSize = masterConnectionPoolSize;
        return (T) this;
    }
    public int getMasterConnectionPoolSize() {
        return masterConnectionPoolSize;
    }

    /**
     * 设置多从节点读负载均衡器，默认轮询。
     *
     * @param loadBalancer 负载均衡实现
     * @return config
     *
     * @see org.redisson.connection.balancer.RandomLoadBalancer
     * @see org.redisson.connection.balancer.RoundRobinLoadBalancer
     * @see org.redisson.connection.balancer.WeightedRoundRobinBalancer
     */
    public T setLoadBalancer(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
        return (T) this;
    }
    public LoadBalancer getLoadBalancer() {
        return loadBalancer;
    }

    /**
     * Maximum connection pool size for subscription (pub/sub) channels
     * <p>
     * Default is <code>50</code>
     * <p>
     * @see #setSubscriptionConnectionMinimumIdleSize(int)
     * 
     * @param subscriptionConnectionPoolSize - pool size
     * @return config
     */
    public T setSubscriptionConnectionPoolSize(int subscriptionConnectionPoolSize) {
        this.subscriptionConnectionPoolSize = subscriptionConnectionPoolSize;
        return (T) this;
    }
    public int getSubscriptionConnectionPoolSize() {
        return subscriptionConnectionPoolSize;
    }

    
    /**
     * Minimum idle connection pool size for subscription (pub/sub) channels
     * <p>
     * Default is <code>24</code>
     * <p>
     * @see #setSlaveConnectionPoolSize(int)
     * 
     * @param slaveConnectionMinimumIdleSize - pool size
     * @return config
     */
    public T setSlaveConnectionMinimumIdleSize(int slaveConnectionMinimumIdleSize) {
        this.slaveConnectionMinimumIdleSize = slaveConnectionMinimumIdleSize;
        return (T) this;
    }
    public int getSlaveConnectionMinimumIdleSize() {
        return slaveConnectionMinimumIdleSize;
    }

    /**
     * Redis 'master' node minimum idle connection amount for <b>each</b> slave node
     * <p>
     * Default is <code>24</code>
     * <p>
     * @see #setMasterConnectionPoolSize(int)
     * 
     * @param masterConnectionMinimumIdleSize - pool size
     * @return config
     */
    public T setMasterConnectionMinimumIdleSize(int masterConnectionMinimumIdleSize) {
        this.masterConnectionMinimumIdleSize = masterConnectionMinimumIdleSize;
        return (T) this;
    }
    public int getMasterConnectionMinimumIdleSize() {
        return masterConnectionMinimumIdleSize;
    }

    /**
     * Redis 'slave' node minimum idle subscription (pub/sub) connection amount for <b>each</b> slave node.
     * <p>
     * Default is <code>1</code>
     * <p>
     * @see #setSubscriptionConnectionPoolSize(int)
     * 
     * @param subscriptionConnectionMinimumIdleSize - pool size
     * @return config
     */
    public T setSubscriptionConnectionMinimumIdleSize(int subscriptionConnectionMinimumIdleSize) {
        this.subscriptionConnectionMinimumIdleSize = subscriptionConnectionMinimumIdleSize;
        return (T) this;
    }
    public int getSubscriptionConnectionMinimumIdleSize() {
        return subscriptionConnectionMinimumIdleSize;
    }

    
    /**
     * 设置读操作使用的节点类型。
     * <p>默认 {@code SLAVE}。
     *
     * @param readMode 读模式
     * @return config
     */
    public T setReadMode(ReadMode readMode) {
        this.readMode = readMode;
        return (T) this;
    }
    public ReadMode getReadMode() {
        return readMode;
    }

    /**
     * 从节点返回 LOADING 时是否改在主节点重试读命令。
     * <p>默认 {@code true}。
     *
     * @param fallbackLoadingToMaster 为 true 则回退主节点
     * @return config
     */
    public T setFallbackLoadingToMaster(boolean fallbackLoadingToMaster) {
        this.fallbackLoadingToMaster = fallbackLoadingToMaster;
        return (T) this;
    }

    public boolean isFallbackLoadingToMaster() {
        return fallbackLoadingToMaster;
    }
    
    /** 读与订阅均指向主节点时返回 true（无从节点流量）。 */
    public boolean isSlaveNotUsed() {
        return getReadMode() == ReadMode.MASTER && getSubscriptionMode() == SubscriptionMode.MASTER;
    }

    /**
     * Set node type used for subscription operation.
     * <p>
     * Default is <code>MASTER</code>
     *
     * @param subscriptionMode param
     * @return config
     */
    public T setSubscriptionMode(SubscriptionMode subscriptionMode) {
        this.subscriptionMode = subscriptionMode;
        return (T) this;
    }
    public SubscriptionMode getSubscriptionMode() {
        return subscriptionMode;
    }

    /**
     * Interval in milliseconds to check the endpoint's DNS<p>
     * Applications must ensure the JVM DNS cache TTL is low enough to support this.<p>
     * Set <code>-1</code> to disable.
     * <p>
     * Default is <code>5000</code>.
     *
     * @param dnsMonitoringInterval time
     * @return config
     */
    public T setDnsMonitoringInterval(long dnsMonitoringInterval) {
        this.dnsMonitoringInterval = dnsMonitoringInterval;
        return (T) this;
    }
    public long getDnsMonitoringInterval() {
        return dnsMonitoringInterval;
    }

    public int getDnsMonitoringTimes() {
        return dnsMonitoringTimes;
    }
    
    /**
     * The number of times per check the endpoint's DNS<p>
     * Applications must ensure the JVM DNS cache TTL is low enough to support this.<p>
     * <p>
     * Default is <code>1</code>.
     *
     * @param dnsMonitoringTimes number of times
     * @return config
     */
    public T setDnsMonitoringTimes(int dnsMonitoringTimes) {
        this.dnsMonitoringTimes = dnsMonitoringTimes;
        return (T) this;
    }

    /**
     * 设置从节点失败检测器。
     * <p>默认 {@code FailedConnectionDetector}。
     *
     * @param failedNodeDetector 检测器实例
     * @return config
     *
     * @see org.redisson.client.FailedConnectionDetector
     * @see org.redisson.client.FailedCommandsDetector
     * @see org.redisson.client.FailedCommandsTimeoutDetector
     */
    public T setFailedSlaveNodeDetector(FailedNodeDetector failedNodeDetector) {
        this.failedSlaveNodeDetector = failedNodeDetector;
        return (T) this;
    }
    public FailedNodeDetector getFailedSlaveNodeDetector() {
        return failedSlaveNodeDetector;
    }

}
