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

import org.apache.rocketmq.remoting.protocol.RemotingSerializable;

/**
 * 查询 Broker 成员组的 Remoting 响应体，封装同 brokerName 下各副本地址。
 */
public class GetBrokerMemberGroupResponseBody extends RemotingSerializable {
    // 同一 Broker 组内各 brokerId 与地址映射
    /** 成员组详情。 */
    private BrokerMemberGroup brokerMemberGroup;

    /** 返回 Broker 成员组。 */
    public BrokerMemberGroup getBrokerMemberGroup() {
        return brokerMemberGroup;
    }

    /** 设置 Broker 成员组。 */
    public void setBrokerMemberGroup(final BrokerMemberGroup brokerMemberGroup) {
        this.brokerMemberGroup = brokerMemberGroup;
    }
}
