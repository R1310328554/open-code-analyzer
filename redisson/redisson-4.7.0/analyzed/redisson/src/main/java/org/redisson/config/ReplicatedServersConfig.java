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

/**
 * 托管复制组（Replication Group）模式配置，适用于 Azure Redis Cache、
 * AWS ElastiCache 等：一个主端点 + 多个只读副本，通过定期扫描发现拓扑。
 * <p>继承 {@link BaseMasterSlaveServersConfig} 的读写池与读模式；
 * 通过 {@link org.redisson.config.Config#useReplicatedServers()} 启用。
 *
 * @author Steve Ungerer
 * @author Nikita Koksharov
 */
public class ReplicatedServersConfig extends BaseMasterSlaveServersConfig<ReplicatedServersConfig> {

    /** 连接前 URI 映射器，默认恒等。 */
    private NatMapper natMapper = NatMapper.direct();

    /** 复制组节点地址列表（通常含主端点），格式 {@code host:port}。 */
    private List<String> nodeAddresses = new ArrayList<>();

    /** 拓扑扫描间隔（毫秒），用于发现主从角色变化。 */
    private int scanInterval = 1000;

    /** 逻辑库索引。 */
    private int database = 0;

    /** 扫描时是否检测配置主机名的 DNS/IP 变化。 */
    private boolean monitorIPChanges = false;

    public ReplicatedServersConfig() {
    }

    ReplicatedServersConfig(ReplicatedServersConfig config) {
        super(config);
        setNodeAddresses(config.getNodeAddresses());
        setScanInterval(config.getScanInterval());
        setDatabase(config.getDatabase());
        setMonitorIPChanges(config.isMonitorIPChanges());
    }

    /**
     * 添加复制组节点地址，格式 {@code host:port}。
     *
     * @param addresses 节点地址
     * @return config
     */
    public ReplicatedServersConfig addNodeAddress(String... addresses) {
        nodeAddresses.addAll(Arrays.asList(addresses));
        return this;
    }
    public List<String> getNodeAddresses() {
        return nodeAddresses;
    }
    public void setNodeAddresses(List<String> nodeAddresses) {
        this.nodeAddresses = nodeAddresses;
    }

    public int getScanInterval() {
        return scanInterval;
    }
    /**
     * 设置拓扑扫描间隔（毫秒）。
     * <p>默认 {@code 1000}。
     *
     * @param scanInterval 间隔毫秒数
     * @return config
     */
    public ReplicatedServersConfig setScanInterval(int scanInterval) {
        this.scanInterval = scanInterval;
        return this;
    }

    /**
     * 设置逻辑库索引。
     * <p>默认 {@code 0}。
     *
     * @param database 库编号
     * @return config
     */
    public ReplicatedServersConfig setDatabase(int database) {
        this.database = database;
        return this;
    }
    public int getDatabase() {
        return database;
    }

    /**
     * 扫描时是否监控配置中各主机名的 IP 变化（DNS 漂移场景）。
     * <p>默认 {@code false}。
     *
     * @param monitorIPChanges 是否监控
     * @return config
     */
    public ReplicatedServersConfig setMonitorIPChanges(boolean monitorIPChanges) {
        this.monitorIPChanges = monitorIPChanges;
        return this;
    }

    public NatMapper getNatMapper() {
        return natMapper;
    }

    /**
     * 设置应用于全部连接的 NAT 映射器。
     *
     * @see HostNatMapper
     * @see HostPortNatMapper
     *
     * @param natMapper NAT 映射器
     * @return config
     */
    public ReplicatedServersConfig setNatMapper(NatMapper natMapper) {
        this.natMapper = natMapper;
        return this;
    }

    public boolean isMonitorIPChanges() {
        return monitorIPChanges;
    }
}
