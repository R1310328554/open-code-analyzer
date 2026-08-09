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
 * Redis 集群模式的服务器配置，继承 {@link BaseMasterSlaveServersConfig} 的通用连接参数。
 * <p>
 * 通过 {@link Config#useClusterServers()} 启用；需至少配置一个集群节点地址。
 *
 * @author Nikita Koksharov
 *
 */
public class ClusterServersConfig extends BaseMasterSlaveServersConfig<ClusterServersConfig> {

    /** NAT 地址映射器，用于将 Redis URI 映射为实际可达地址。 */
    private NatMapper natMapper = NatMapper.direct();

    /** Redis 集群节点地址列表，格式为 host:port。 */
    private List<String> nodeAddresses = new ArrayList<>();

    /** 集群拓扑扫描间隔（毫秒），用于发现新增/下线节点。 */
    private int scanInterval = 5000;

    /** 启动时是否校验集群槽位覆盖完整性。 */
    private boolean checkSlotsCoverage = true;

    /** 是否检查从节点 INFO REPLICATION 中的 master-link-status。 */
    private boolean checkMasterLinkStatus = false;

    /** 分片订阅模式（Redis 7.0+ 特性）。 */
    private ShardedSubscriptionMode shardedSubscriptionMode = ShardedSubscriptionMode.AUTO;

    /** Valkey 连接使用的逻辑库编号（需 Valkey 9.0+）。 */
    private int database = 0;

    /** 默认构造函数。 */
    public ClusterServersConfig() {
    }

    /** 从已有配置深拷贝构造。 */
    ClusterServersConfig(ClusterServersConfig config) {
        super(config);
        setNodeAddresses(config.getNodeAddresses());
        setScanInterval(config.getScanInterval());
        setNatMapper(config.getNatMapper());
        setCheckSlotsCoverage(config.isCheckSlotsCoverage());
        setCheckMasterLinkStatus(config.isCheckMasterLinkStatus());
        setShardedSubscriptionMode(config.getShardedSubscriptionMode());
        setDatabase(config.getDatabase());
    }

    /**
     * 追加 Redis 集群节点地址，格式为 <code>host:port</code>。
     *
     * @param addresses <code>host:port</code> 格式的地址
     * @return 当前配置实例（链式调用）
     */
    public ClusterServersConfig addNodeAddress(String... addresses) {
        nodeAddresses.addAll(Arrays.asList(addresses));
        return this;
    }
    /** 返回集群节点地址列表。 */
    public List<String> getNodeAddresses() {
        return nodeAddresses;
    }
    /** 设置集群节点地址列表。 */
    public void setNodeAddresses(List<String> nodeAddresses) {
        this.nodeAddresses = nodeAddresses;
    }

    /** 返回集群扫描间隔（毫秒）。 */
    public int getScanInterval() {
        return scanInterval;
    }
    /**
     * 设置 Redis 集群拓扑扫描间隔（毫秒）。
     * <p>
     * 默认值为 <code>5000</code>。
     *
     * @param scanInterval 扫描间隔（毫秒）
     * @return 当前配置实例
     */
    public ClusterServersConfig setScanInterval(int scanInterval) {
        this.scanInterval = scanInterval;
        return this;
    }

    /** 是否启用启动时槽位覆盖检查。 */
    public boolean isCheckSlotsCoverage() {
        return checkSlotsCoverage;
    }

    /**
     * 启用 Redisson 启动时的集群槽位覆盖检查。
     * <p>
     * 默认值为 <code>true</code>。
     *
     * @param checkSlotsCoverage 是否检查槽位覆盖
     * @return 当前配置实例
     */
    public ClusterServersConfig setCheckSlotsCoverage(boolean checkSlotsCoverage) {
        this.checkSlotsCoverage = checkSlotsCoverage;
        return this;
    }

    /** 是否检查从节点主从链路状态。 */
    public boolean isCheckMasterLinkStatus() {
        return checkMasterLinkStatus;
    }

    /**
     * 启用对从节点 INFO REPLICATION 命令返回的 master-link-status 字段的检查。
     * <p>
     * 默认值为 <code>false</code>。
     *
     * @param checkMasterLinkStatus 是否检查主从链路状态
     * @return 当前配置实例
     */
    public ClusterServersConfig setCheckMasterLinkStatus(boolean checkMasterLinkStatus) {
        this.checkMasterLinkStatus = checkMasterLinkStatus;
        return this;
    }

    /*
     * 请改用 {@link #setNatMapper(NatMapper)}
     */
    @Deprecated
    public ClusterServersConfig setNatMap(Map<String, String> natMap) {
        HostPortNatMapper mapper = new HostPortNatMapper();
        mapper.setHostsPortMap(natMap);
        this.natMapper = mapper;
        return this;
    }

    /** 返回 NAT 地址映射器。 */
    public NatMapper getNatMapper() {
        return natMapper;
    }

    /**
     * 定义 NAT 映射器，将 Redis URI 映射为实际连接地址。
     * 应用于所有 Redis 连接。
     *
     * @see HostNatMapper
     * @see HostPortNatMapper
     *
     * @param natMapper NAT 映射器实例
     * @return 当前配置实例
     */
    public ClusterServersConfig setNatMapper(NatMapper natMapper) {
        this.natMapper = natMapper;
        return this;
    }

    /** 返回分片订阅模式。 */
    public ShardedSubscriptionMode getShardedSubscriptionMode() {
        return shardedSubscriptionMode;
    }

    /**
     * 定义是否启用 Redis 7.0+ 提供的分片订阅（Sharded Pub/Sub）特性。
     * <p>
     * Used in RMapCache, RLocalCachedMap, RCountDownLatch, RLock, RPermitExpirableSemaphore,
     * RSemaphore, RLongAdder, RDoubleAdder, Micronaut Session, Apache Tomcat Manager objects.
     * <p>
     * 默认值为 <code>AUTO</code>。
     *
     * @param shardedSubscriptionMode 分片订阅模式
     * @return 当前配置实例
     */
    public ClusterServersConfig setShardedSubscriptionMode(ShardedSubscriptionMode shardedSubscriptionMode) {
        this.shardedSubscriptionMode = shardedSubscriptionMode;
        return this;
    }

    /**
     * 设置 Valkey 连接使用的逻辑库编号。
     * <p>
     * Default is <code>0</code>
     * <p>
     * <b>Requires <b>Valkey 9.0.0 and higher.</b>
     *
     * @param database 逻辑库编号
     * @return 当前配置实例
     */
    public ClusterServersConfig setDatabase(int database) {
        this.database = database;
        return this;
    }
    /** 返回 Valkey 逻辑库编号。 */
    public int getDatabase() {
        return database;
    }

}
