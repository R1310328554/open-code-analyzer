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
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Controller 选举 Master 响应体：Broker 成员组与同步状态集（syncStateSet）。
 */
public class ElectMasterResponseBody extends RemotingSerializable {
    /** 选举后的 Broker 成员组信息。 */
    private BrokerMemberGroup brokerMemberGroup;
    /** 处于同步状态的 brokerId 集合。 */
    private Set<Long> syncStateSet;

    // 供序列化框架使用的默认构造
    /** 默认构造，初始化空 syncStateSet。 */
    public ElectMasterResponseBody() {
        this.syncStateSet = new HashSet<Long>();
        this.brokerMemberGroup = null;
    }

    /** 仅指定 syncStateSet 的构造。 */
    public ElectMasterResponseBody(final Set<Long> syncStateSet) {
        this.syncStateSet = syncStateSet;
        this.brokerMemberGroup = null;
    }

    /** 指定成员组与 syncStateSet 的完整构造。 */
    public ElectMasterResponseBody(final BrokerMemberGroup brokerMemberGroup, final Set<Long> syncStateSet) {
        this.brokerMemberGroup = brokerMemberGroup;
        this.syncStateSet = syncStateSet;
    }

    /** 比较成员组与 syncStateSet 是否相等。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ElectMasterResponseBody that = (ElectMasterResponseBody) o;
        return Objects.equal(brokerMemberGroup, that.brokerMemberGroup) &&
            Objects.equal(syncStateSet, that.syncStateSet);
    }

    /** 基于成员组与 syncStateSet 计算哈希。 */
    @Override
    public int hashCode() {
        return Objects.hashCode(brokerMemberGroup, syncStateSet);
    }

    /** 返回调试字符串。 */
    @Override
    public String toString() {
        return "BrokerMemberGroup{" +
            "brokerMemberGroup='" + brokerMemberGroup.toString() + '\'' +
            ", syncStateSet='" + syncStateSet.toString() +
            '}';
    }

    /** 设置 Broker 成员组。 */
    public void setBrokerMemberGroup(BrokerMemberGroup brokerMemberGroup) {
        this.brokerMemberGroup = brokerMemberGroup;
    }

    /** 返回 Broker 成员组。 */
    public BrokerMemberGroup getBrokerMemberGroup() {
        return brokerMemberGroup;
    }

    /** 设置 syncStateSet。 */
    public void setSyncStateSet(Set<Long> syncStateSet) {
        this.syncStateSet = syncStateSet;
    }

    /** 返回 syncStateSet。 */
    public Set<Long> getSyncStateSet() {
        return syncStateSet;
    }
}
