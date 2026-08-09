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
package org.apache.rocketmq.controller.impl.heartbeat;

import java.io.Serializable;
import org.apache.rocketmq.common.UtilAll;

import java.util.Objects;

/**
 * Broker 身份标识：由集群名、Broker 名与 BrokerId 唯一确定一个副本。
 */
public class BrokerIdentityInfo implements Serializable {

    private static final long serialVersionUID = 883597359635995567L;
    /** 集群名称。 */
    private final String clusterName;

    /** Broker 名称（Broker Set）。 */
    private final String brokerName;

    /** Broker 副本 ID。 */
    private final Long brokerId;

    /** 构造不可变 Broker 身份三元组。 */
    public BrokerIdentityInfo(String clusterName, String brokerName, Long brokerId) {
        this.clusterName = clusterName;
        this.brokerName = brokerName;
        this.brokerId = brokerId;
    }

    public String getClusterName() {
        return clusterName;
    }

    public Long getBrokerId() {
        return brokerId;
    }

    public String getBrokerName() {
        return brokerName;
    }

    /** 判断集群名、Broker 名与 ID 是否均为空。 */
    public boolean isEmpty() {
        return UtilAll.isBlank(clusterName) && UtilAll.isBlank(brokerName) && brokerId == null;
    }

    @Override
    /** 按 clusterName、brokerName、brokerId 三元组判等。 */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }

        if (obj instanceof BrokerIdentityInfo) {
            BrokerIdentityInfo addr = (BrokerIdentityInfo) obj;
            return clusterName.equals(addr.clusterName) && brokerName.equals(addr.brokerName) && brokerId.equals(addr.brokerId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.clusterName, this.brokerName, this.brokerId);
    }

    @Override
    public String toString() {
        return "BrokerIdentityInfo{" +
            "clusterName='" + clusterName + '\'' +
            ", brokerName='" + brokerName + '\'' +
            ", brokerId=" + brokerId +
            '}';
    }
}
