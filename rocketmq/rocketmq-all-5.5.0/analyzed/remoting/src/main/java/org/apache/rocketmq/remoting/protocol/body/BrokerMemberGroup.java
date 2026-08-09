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

package org.apache.rocketmq.remoting.protocol.body;

import com.google.common.base.Objects;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;

/**
 * Broker 成员组：同一 brokerName 下各 brokerId 与地址映射，用于路由与 HA 选举。
 */
public class BrokerMemberGroup extends RemotingSerializable {
    /** 所属集群名。 */
    private String cluster;
    /** Broker 逻辑名称。 */
    private String brokerName;
    /** brokerId → 访问地址。 */
    private Map<Long/* brokerId */, String/* broker address */> brokerAddrs;

    // 供 JSON/Remoting 反序列化使用的无参构造
    /** 默认构造，初始化空地址表。 */
    public BrokerMemberGroup() {
        this.brokerAddrs = new HashMap<>();
    }

    /** 指定集群与 Broker 名构造成员组。 */
    public BrokerMemberGroup(final String cluster, final String brokerName) {
        this.cluster = cluster;
        this.brokerName = brokerName;
        this.brokerAddrs = new HashMap<>();
    }

    /** 返回当前成员中最小 brokerId，空表时返回 0。 */
    public long minimumBrokerId() {
        if (this.brokerAddrs.isEmpty()) {
            return 0;
        }
        return Collections.min(brokerAddrs.keySet());
    }

    /** 返回集群名。 */
    public String getCluster() {
        return cluster;
    }

    public void setCluster(final String cluster) {
        this.cluster = cluster;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(final String brokerName) {
        this.brokerName = brokerName;
    }

    /** 返回 brokerId 地址映射。 */
    public Map<Long, String> getBrokerAddrs() {
        return brokerAddrs;
    }

    public void setBrokerAddrs(final Map<Long, String> brokerAddrs) {
        this.brokerAddrs = brokerAddrs;
    }

    /** 按 cluster、brokerName、brokerAddrs 判等。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BrokerMemberGroup that = (BrokerMemberGroup) o;
        return Objects.equal(cluster, that.cluster) &&
            Objects.equal(brokerName, that.brokerName) &&
            Objects.equal(brokerAddrs, that.brokerAddrs);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(cluster, brokerName, brokerAddrs);
    }

    /** 返回成员组可读字符串。 */
    @Override
    public String toString() {
        return "BrokerMemberGroup{" +
            "cluster='" + cluster + '\'' +
            ", brokerName='" + brokerName + '\'' +
            ", brokerAddrs=" + brokerAddrs +
            '}';
    }
}
