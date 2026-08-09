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

/**
 * Broker 生命周期监听器：在 Controller 检测到
 * Broker 不活跃（心跳超时或通道断开）时触发回调。
 */
public interface BrokerLifecycleListener {
    /**
     * Broker 变为不活跃时调用。
     *
     * @param clusterName 集群名
     * @param brokerName  Broker 组名
     * @param brokerId    下线副本 ID，强制选举时可为 null
     */
    void onBrokerInactive(final String clusterName, final String brokerName, final Long brokerId);
}
