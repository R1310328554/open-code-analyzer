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
package org.apache.rocketmq.controller.elect;

import java.util.Set;

/**
 * Master 选举策略接口：根据同步副本集与全部副本
 * 在 Controller 触发选主时返回新 Master 的 brokerId。
 */
public interface ElectPolicy {

    /**
     * 执行 Master 选举。
     *
     * @param clusterName       Broker 所属集群名
     * @param brokerName        Broker 组名
     * @param syncStateBrokers  SyncStateSet 内副本 ID 集合
     * @param allReplicaBrokers 全部注册副本 ID 集合
     * @param oldMaster         原 Master 的 brokerId
     * @param brokerId          优先或指定的 brokerId
     * @return 新 Master 的 brokerId；无法选出时返回 null
     */
    Long elect(String clusterName, String brokerName, Set<Long> syncStateBrokers, Set<Long> allReplicaBrokers,
        Long oldMaster, Long brokerId);

}
