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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 主从（Master-Slave）部署模式配置。
 * <p>显式指定一个主节点地址与若干从节点地址，继承 {@link BaseMasterSlaveServersConfig}
 * 的读写连接池、读模式 {@link ReadMode}、负载均衡等公共项。
 * <p>通过 {@link org.redisson.config.Config#useMasterSlaveServers()} 启用。
 *
 * @author Nikita Koksharov
 *
 */
public class MasterSlaveServersConfig extends BaseMasterSlaveServersConfig<MasterSlaveServersConfig> {

    /** 从节点地址集合，格式 {@code host:port}。 */
    private Set<String> slaveAddresses = new HashSet<String>();

    /** 主节点地址，格式 {@code host:port}。 */
    private String masterAddress;

    /** 连接使用的 Redis 逻辑库索引（db）。 */
    private int database = 0;

    /** 默认构造。 */
    public MasterSlaveServersConfig() {
    }

    /** 拷贝构造，复制主从地址与 db 及父类字段。 */
    MasterSlaveServersConfig(MasterSlaveServersConfig config) {
        super(config);
        setLoadBalancer(config.getLoadBalancer());
        setMasterAddress(config.getMasterAddress());
        setSlaveAddresses(config.getSlaveAddresses());
        setDatabase(config.getDatabase());
    }

    /**
     * 设置主节点地址，格式 {@code host:port}。
     *
     * @param masterAddress Redis 主节点地址
     * @return config
     */
    public MasterSlaveServersConfig setMasterAddress(String masterAddress) {
        this.masterAddress = masterAddress;
        return this;
    }
    public String getMasterAddress() {
        return masterAddress;
    }

    /**
     * 批量添加从节点地址，格式 {@code host:port}。
     *
     * @param addresses 从节点地址
     * @return config
     */
    public MasterSlaveServersConfig addSlaveAddress(String... addresses) {
        slaveAddresses.addAll(Arrays.asList(addresses));
        return this;
    }
    public MasterSlaveServersConfig addSlaveAddress(String slaveAddress) {
        slaveAddresses.add(slaveAddress);
        return this;
    }
    public Set<String> getSlaveAddresses() {
        return slaveAddresses;
    }
    public void setSlaveAddresses(Set<String> readAddresses) {
        this.slaveAddresses = readAddresses;
    }

    /**
     * 设置逻辑库索引。
     * <p>默认 {@code 0}。
     *
     * @param database 库编号
     * @return config
     */
    public MasterSlaveServersConfig setDatabase(int database) {
        this.database = database;
        return this;
    }
    public int getDatabase() {
        return database;
    }

}
