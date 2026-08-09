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

import java.util.List;

/**
 * 批量 Ack Pop 消息的请求体：指定 Broker 及 {@link BatchAck} 列表。
 */
public class BatchAckMessageRequestBody extends RemotingSerializable {
    /** 目标 Broker 名称。 */
    private String brokerName;
    /** 待提交的批量 Ack 条目。 */
    private List<BatchAck> acks;

    /** 返回 Broker 名称。 */
    public String getBrokerName() {
        return brokerName;
    }

    /** 设置 Broker 名称。 */
    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    /** 返回 Ack 列表。 */
    public List<BatchAck> getAcks() {
        return acks;
    }

    /** 设置 Ack 列表。 */
    public void setAcks(List<BatchAck> acks) {
        this.acks = acks;
    }
}
