/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.remoting.protocol.route;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.MixAll;

/**
 * Broker 路由数据：描述 Broker 组所属集群、各 brokerId 实例地址及可用区信息。
 */
public class BrokerData implements Comparable<BrokerData> {
    /** 所属集群（分片）名称。 */
    private String cluster;
    /** Broker 组名称。 */
    private String brokerName;

    /** brokerId → 实例地址映射（含 Master 与 Slave）。 */
    private HashMap<Long, String> brokerAddrs;
    /** 可用区名称。 */
    private String zoneName;
    /** 随机数生成器（Master 不可用时选 Slave）。 */
    private final Random random = new Random();

    /** 是否允许 Acting Master（兼容旧版 HA）。 */
    private boolean enableActingMaster = false;

    /** 默认构造。 */
    public BrokerData() {

    }

    /** 拷贝构造。 */
    public BrokerData(BrokerData brokerData) {
        this.cluster = brokerData.cluster;
        this.brokerName = brokerData.brokerName;
        if (brokerData.brokerAddrs != null) {
            this.brokerAddrs = new HashMap<>(brokerData.brokerAddrs);
        }
        this.zoneName = brokerData.zoneName;
        this.enableActingMaster = brokerData.enableActingMaster;
    }

    /** 指定集群、Broker 组与地址映射的构造。 */
    public BrokerData(String cluster, String brokerName, HashMap<Long, String> brokerAddrs) {
        this.cluster = cluster;
        this.brokerName = brokerName;
        this.brokerAddrs = brokerAddrs;
    }

    /** 指定集群、Broker 组、地址映射与 Acting Master 标志的构造。 */
    public BrokerData(String cluster, String brokerName, HashMap<Long, String> brokerAddrs,
        boolean enableActingMaster) {
        this.cluster = cluster;
        this.brokerName = brokerName;
        this.brokerAddrs = brokerAddrs;
        this.enableActingMaster = enableActingMaster;
    }

    /** 指定集群、Broker 组、地址映射、Acting Master 与可用区的构造。 */
    public BrokerData(String cluster, String brokerName, HashMap<Long, String> brokerAddrs, boolean enableActingMaster,
        String zoneName) {
        this.cluster = cluster;
        this.brokerName = brokerName;
        this.brokerAddrs = brokerAddrs;
        this.enableActingMaster = enableActingMaster;
        this.zoneName = zoneName;
    }

    /**
     * 从已注册地址中选取 Broker 地址：优先 Master，不可用时随机选 Slave。
     *
     * @return Broker 地址
     */
    public String selectBrokerAddr() {
        String masterAddress = this.brokerAddrs.get(MixAll.MASTER_ID);

        if (masterAddress == null) {
            List<String> addrs = new ArrayList<>(brokerAddrs.values());
            return addrs.get(random.nextInt(addrs.size()));
        }

        return masterAddress;
    }

    /** 返回 brokerId 地址映射。 */
    public HashMap<Long, String> getBrokerAddrs() {
        return brokerAddrs;
    }

    /** 设置 brokerId 地址映射。 */
    public void setBrokerAddrs(HashMap<Long, String> brokerAddrs) {
        this.brokerAddrs = brokerAddrs;
    }

    /** 返回集群名称。 */
    public String getCluster() {
        return cluster;
    }

    /** 设置集群名称。 */
    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    /** 返回是否启用 Acting Master。 */
    public boolean isEnableActingMaster() {
        return enableActingMaster;
    }

    /** 设置 Acting Master 标志。 */
    public void setEnableActingMaster(boolean enableActingMaster) {
        this.enableActingMaster = enableActingMaster;
    }

    /** 返回可用区名称。 */
    public String getZoneName() {
        return zoneName;
    }

    /** 设置可用区名称。 */
    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    /** 基于 brokerName 与地址映射计算哈希码。 */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((brokerAddrs == null) ? 0 : brokerAddrs.hashCode());
        result = prime * result + ((brokerName == null) ? 0 : brokerName.hashCode());
        return result;
    }

    /** 比较 brokerName 与地址映射是否相等。 */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BrokerData other = (BrokerData) obj;
        if (brokerAddrs == null) {
            if (other.brokerAddrs != null) {
                return false;
            }
        } else if (!brokerAddrs.equals(other.brokerAddrs)) {
            return false;
        }
        return StringUtils.equals(brokerName, other.brokerName);
    }

    /** 返回含 brokerName 与地址映射的调试字符串。 */
    @Override
    public String toString() {
        return "BrokerData [brokerName=" + brokerName + ", brokerAddrs=" + brokerAddrs + ", enableActingMaster=" + enableActingMaster + "]";
    }

    /** 按 brokerName 字典序比较。 */
    @Override
    public int compareTo(BrokerData o) {
        return this.brokerName.compareTo(o.getBrokerName());
    }

    /** 返回 Broker 组名称。 */
    public String getBrokerName() {
        return brokerName;
    }

    /** 设置 Broker 组名称。 */
    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }
}
