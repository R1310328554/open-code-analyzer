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

/**
 * 单 Redis 节点部署模式配置。
 * <p>指定一个 {@code host:port} 地址及读写/订阅连接池、逻辑库、DNS 监控等；
 * 通过 {@link org.redisson.config.Config#useSingleServer()} 启用。
 *
 * @author Nikita Koksharov
 *
 */
public class SingleServerConfig extends BaseConfig<SingleServerConfig> {

    /** Redis 服务地址，格式 {@code host:port} 或 {@code redis://...}。 */
    private String address;

    /** 订阅连接池最小空闲连接数。 */
    private int subscriptionConnectionMinimumIdleSize = 1;

    /** 订阅（Pub/Sub）连接池最大容量。 */
    private int subscriptionConnectionPoolSize = 50;

    /** 普通读写连接池最小空闲连接数。 */
    private int connectionMinimumIdleSize = 24;

    /** 普通读写连接池最大容量。 */
    private int connectionPoolSize = 64;

    /** 逻辑库索引（db）。 */
    private int database = 0;

    /** DNS 解析监控间隔（毫秒），{@code -1} 禁用。 */
    private long dnsMonitoringInterval = 5000;

    /** 每次 DNS 检查周期内解析次数。 */
    private int dnsMonitoringTimes = 1;

    SingleServerConfig() {
    }

    SingleServerConfig(SingleServerConfig config) {
        super(config);
        setAddress(config.getAddress());
        setConnectionPoolSize(config.getConnectionPoolSize());
        setSubscriptionConnectionPoolSize(config.getSubscriptionConnectionPoolSize());
        setDnsMonitoringInterval(config.getDnsMonitoringInterval());
        setDnsMonitoringTimes(config.getDnsMonitoringTimes());
        setSubscriptionConnectionMinimumIdleSize(config.getSubscriptionConnectionMinimumIdleSize());
        setConnectionMinimumIdleSize(config.getConnectionMinimumIdleSize());
        setDatabase(config.getDatabase());
    }

    /**
     * 设置读写连接池最大容量。
     * <p>默认 {@code 64}。
     *
     * @param connectionPoolSize 池大小
     * @return config
     */
    public SingleServerConfig setConnectionPoolSize(int connectionPoolSize) {
        this.connectionPoolSize = connectionPoolSize;
        return this;
    }
    public int getConnectionPoolSize() {
        return connectionPoolSize;
    }

    /**
     * 设置订阅连接池最大容量。
     * <p>默认 {@code 50}。
     *
     * @param subscriptionConnectionPoolSize 池大小
     * @return config
     */
    public SingleServerConfig setSubscriptionConnectionPoolSize(int subscriptionConnectionPoolSize) {
        this.subscriptionConnectionPoolSize = subscriptionConnectionPoolSize;
        return this;
    }
    public int getSubscriptionConnectionPoolSize() {
        return subscriptionConnectionPoolSize;
    }

    /**
     * 设置 Redis 地址，格式 {@code host:port}。
     *
     * @param address Redis 地址
     * @return config
     */
    public SingleServerConfig setAddress(String address) {
        if (address != null) {
            this.address = address;
        }
        return this;
    }
    public String getAddress() {
        return address;
    }

    /**
     * 设置端点 DNS 监控间隔（毫秒）；需配合较低的 JVM DNS 缓存 TTL。
     * <p>{@code -1} 禁用；默认 {@code 5000}。
     *
     * @param dnsMonitoringInterval 间隔毫秒数
     * @return config
     */
    public SingleServerConfig setDnsMonitoringInterval(long dnsMonitoringInterval) {
        this.dnsMonitoringInterval = dnsMonitoringInterval;
        return this;
    }
    public long getDnsMonitoringInterval() {
        return dnsMonitoringInterval;
    }

    public int getDnsMonitoringTimes() {
        return dnsMonitoringTimes;
    }
    
    /**
     * 每个 DNS 检查周期内对端点解析的次数。
     * <p>默认 {@code 1}。
     *
     * @param dnsMonitoringTimes 解析次数
     * @return config
     */
    public SingleServerConfig setDnsMonitoringTimes(int dnsMonitoringTimes) {
        this.dnsMonitoringTimes = dnsMonitoringTimes;
        return this;
    }

    /**
     * 设置订阅连接池最小空闲数。
     * <p>默认 {@code 1}。
     *
     * @param subscriptionConnectionMinimumIdleSize 最小空闲连接数
     * @return config
     */
    public SingleServerConfig setSubscriptionConnectionMinimumIdleSize(int subscriptionConnectionMinimumIdleSize) {
        this.subscriptionConnectionMinimumIdleSize = subscriptionConnectionMinimumIdleSize;
        return this;
    }
    public int getSubscriptionConnectionMinimumIdleSize() {
        return subscriptionConnectionMinimumIdleSize;
    }

    /**
     * 设置读写连接池最小空闲数。
     * <p>默认 {@code 24}。
     *
     * @param connectionMinimumIdleSize 最小空闲连接数
     * @return config
     */
    public SingleServerConfig setConnectionMinimumIdleSize(int connectionMinimumIdleSize) {
        this.connectionMinimumIdleSize = connectionMinimumIdleSize;
        return this;
    }
    public int getConnectionMinimumIdleSize() {
        return connectionMinimumIdleSize;
    }

    /**
     * 设置逻辑库索引。
     * <p>默认 {@code 0}。
     *
     * @param database 库编号
     * @return config
     */
    public SingleServerConfig setDatabase(int database) {
        this.database = database;
        return this;
    }
    public int getDatabase() {
        return database;
    }

}
