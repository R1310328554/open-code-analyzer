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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Redis Sentinel 高可用模式配置。
 * <p>通过 Sentinel 地址发现当前主节点与从节点列表，并监听主从切换；
 * 继承 {@link BaseMasterSlaveServersConfig} 的读写池与读模式。
 * <p>通过 {@link org.redisson.config.Config#useSentinelServers()} 启用。
 *
 * @author Nikita Koksharov
 *
 */
public class SentinelServersConfig extends BaseMasterSlaveServersConfig<SentinelServersConfig> {

    /** Sentinel 节点地址列表，格式 {@code host:port}。 */
    private List<String> sentinelAddresses = new ArrayList<>();
    
    /** 连接 URI 的 NAT 映射器。 */
    private NatMapper natMapper = NatMapper.direct();

    /** Sentinel 监控的主节点逻辑名称（master set name）。 */
    private String masterName;

    /** Sentinel 认证用户名（可与 Redis 主从不同）。 */
    private String sentinelUsername;

    /** Sentinel 认证密码（可与 Redis 主从不同）。 */
    private String sentinelPassword;

    /** 逻辑库索引。 */
    private int database = 0;
    
    /** Sentinel 拓扑扫描间隔（毫秒）。 */
    private int scanInterval = 1000;

    /** 启动时是否校验 Sentinel 列表一致性。 */
    private boolean checkSentinelsList = true;

    /** 是否结合 master-link-status 检查从节点同步状态。 */
    private boolean checkSlaveStatusWithSyncing = true;

    /** 是否通过 Sentinel 自动发现完整 Sentinel 集群。 */
    private boolean sentinelsDiscovery = true;

    public SentinelServersConfig() {
    }

    SentinelServersConfig(SentinelServersConfig config) {
        super(config);
        setSentinelAddresses(config.getSentinelAddresses());
        setMasterName(config.getMasterName());
        setDatabase(config.getDatabase());
        setScanInterval(config.getScanInterval());
        setNatMapper(config.getNatMapper());
        setCheckSentinelsList(config.isCheckSentinelsList());
        setSentinelUsername(config.getSentinelUsername());
        setSentinelPassword(config.getSentinelPassword());
        setCheckSlaveStatusWithSyncing(config.isCheckSlaveStatusWithSyncing());
        setSentinelsDiscovery(config.isSentinelsDiscovery());
    }

    /**
     * 设置 Sentinel 监控的主节点名称，用于查询主从地址与故障转移监听。
     *
     * @param masterName 主节点逻辑名
     * @return config
     */
    public SentinelServersConfig setMasterName(String masterName) {
        this.masterName = masterName;
        return this;
    }
    public String getMasterName() {
        return masterName;
    }

    /**
     * 设置 Sentinel 连接认证用户名。
     *
     * @param sentinelUsername Sentinel 用户名
     * @return config
     */
    public SentinelServersConfig setSentinelUsername(String sentinelUsername) {
        this.sentinelUsername = sentinelUsername;
        return this;
    }

    public String getSentinelUsername() {
        return sentinelUsername;
    }

    /**
     * 设置 Sentinel 认证密码（仅当与 Redis 主从密码不同时需要）。
     *
     * @param sentinelPassword Sentinel 密码
     * @return config
     */
    public SentinelServersConfig setSentinelPassword(String sentinelPassword) {
        this.sentinelPassword = sentinelPassword;
        return this;
    }
    public String getSentinelPassword() {
        return sentinelPassword;
    }


    /**
     * 添加 Sentinel 地址，格式 {@code host:port}，可一次传入多个。
     *
     * @param addresses Sentinel 地址
     * @return config
     */
    public SentinelServersConfig addSentinelAddress(String... addresses) {
        sentinelAddresses.addAll(Arrays.asList(addresses));
        return this;
    }
    public List<String> getSentinelAddresses() {
        return sentinelAddresses;
    }
    public void setSentinelAddresses(List<String> sentinelAddresses) {
        this.sentinelAddresses = sentinelAddresses;
    }

    /**
     * 设置逻辑库索引。
     * <p>默认 {@code 0}。
     *
     * @param database 库编号
     * @return config
     */
    public SentinelServersConfig setDatabase(int database) {
        this.database = database;
        return this;
    }
    public int getDatabase() {
        return database;
    }

    public int getScanInterval() {
        return scanInterval;
    }
    /**
     * 设置 Sentinel 扫描间隔（毫秒）。
     * <p>默认 {@code 1000}。
     *
     * @param scanInterval 间隔毫秒数
     * @return config
     */
    public SentinelServersConfig setScanInterval(int scanInterval) {
        this.scanInterval = scanInterval;
        return this;
    }

    /*
     * Use {@link #setNatMapper(NatMapper)}
     */
    @Deprecated
    public SentinelServersConfig setNatMap(Map<String, String> natMap) {
        HostPortNatMapper mapper = new HostPortNatMapper();
        mapper.setHostsPortMap(natMap);
        this.natMapper = mapper;
        return this;
    }

    public NatMapper getNatMapper() {
        return natMapper;
    }

    /**
     * 设置应用于全部 Redis/Sentinel 连接的 NAT 映射器。
     *
     * @see HostNatMapper
     * @see HostPortNatMapper
     *
     * @param natMapper NAT 映射器
     * @return config
     */
    public SentinelServersConfig setNatMapper(NatMapper natMapper) {
        this.natMapper = natMapper;
        return this;
    }

    public boolean isCheckSentinelsList() {
        return checkSentinelsList;
    }

    /**
     * 启动时是否校验各 Sentinel 返回的 Sentinel 列表一致。
     * <p>默认 {@code true}。
     *
     * @param checkSentinelsList 是否校验
     * @return config
     */
    public SentinelServersConfig setCheckSentinelsList(boolean checkSentinelsList) {
        this.checkSentinelsList = checkSentinelsList;
        return this;
    }

    public boolean isCheckSlaveStatusWithSyncing() {
        return checkSlaveStatusWithSyncing;
    }

    /**
     * 是否通过 Sentinel 的 master-link-status 判断从节点是否在同步。
     * <p>默认 {@code true}。
     *
     * @param checkSlaveStatusWithSyncing 是否检查
     * @return config
     */
    public SentinelServersConfig setCheckSlaveStatusWithSyncing(boolean checkSlaveStatusWithSyncing) {
        this.checkSlaveStatusWithSyncing = checkSlaveStatusWithSyncing;
        return this;
    }

    public boolean isSentinelsDiscovery() {
        return sentinelsDiscovery;
    }

    /**
     * 是否启用 Sentinel 自动发现（从已知 Sentinel 获取完整列表）。
     * <p>默认 {@code true}。
     *
     * @param sentinelsDiscovery 是否发现
     * @return config
     */
    public SentinelServersConfig setSentinelsDiscovery(boolean sentinelsDiscovery) {
        this.sentinelsDiscovery = sentinelsDiscovery;
        return this;
    }
}
