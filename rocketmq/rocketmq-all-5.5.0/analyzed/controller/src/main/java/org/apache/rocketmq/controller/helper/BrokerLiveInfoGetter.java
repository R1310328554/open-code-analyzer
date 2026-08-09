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

package org.apache.rocketmq.controller.helper;

import org.apache.rocketmq.controller.impl.heartbeat.BrokerLiveInfo;

/**
 * Broker 存活信息查询器：供选主策略获取
 * epoch、maxOffset、electionPriority 等排序字段。
 */
public interface BrokerLiveInfoGetter {

    /**
     * 按集群与 Broker 身份获取存活详情。
     *
     * @param clusterName 集群名
     * @param brokerName  Broker 组名
     * @param brokerId    副本 ID
     * @return 对应 {@link BrokerLiveInfo}
     */
    BrokerLiveInfo get(String clusterName, String brokerName, Long brokerId);

}
