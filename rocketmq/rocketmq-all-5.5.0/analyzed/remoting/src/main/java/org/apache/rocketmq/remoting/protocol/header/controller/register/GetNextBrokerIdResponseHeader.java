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

package org.apache.rocketmq.remoting.protocol.header.controller.register;

import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;

/**
 * 查询下一个可用 brokerId 的响应头：返回集群、Broker 组名与分配的 nextBrokerId。
 */
public class GetNextBrokerIdResponseHeader implements CommandCustomHeader {

    /** 集群名称。 */
    private String clusterName;

    /** Broker 组名称。 */
    private String brokerName;

    /** Controller 分配的下一个 brokerId。 */
    private Long nextBrokerId;

    /** 默认构造。 */
    public GetNextBrokerIdResponseHeader() {
    }

    /** 指定集群与 Broker 组名的构造（nextBrokerId 为空）。 */
    public GetNextBrokerIdResponseHeader(String clusterName, String brokerName) {
        this(clusterName, brokerName, null);
    }

    /** 指定集群、Broker 组名与 nextBrokerId 的构造。 */
    public GetNextBrokerIdResponseHeader(String clusterName, String brokerName, Long nextBrokerId) {
        this.clusterName = clusterName;
        this.brokerName = brokerName;
        this.nextBrokerId = nextBrokerId;
    }

    /** 返回含集群、Broker 组名与 nextBrokerId 的调试字符串。 */
    @Override
    public String toString() {
        return "GetNextBrokerIdResponseHeader{" +
                "clusterName='" + clusterName + '\'' +
                ", brokerName='" + brokerName + '\'' +
                ", nextBrokerId=" + nextBrokerId +
                '}';
    }

    /** 校验响应头字段（本类无额外约束，空实现）。 */
    @Override
    public void checkFields() throws RemotingCommandException {

    }

    /** 设置 nextBrokerId。 */
    public void setNextBrokerId(Long nextBrokerId) {
        this.nextBrokerId = nextBrokerId;
    }

    /** 返回 nextBrokerId。 */
    public Long getNextBrokerId() {
        return nextBrokerId;
    }

    /** 返回集群名称。 */
    public String getClusterName() {
        return clusterName;
    }

    /** 设置集群名称。 */
    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
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
