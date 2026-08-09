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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.rocketmq.remoting.protocol.DataVersion;
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;
import org.apache.rocketmq.remoting.protocol.subscription.SubscriptionGroupConfig;

/**
 * 订阅组元数据序列化包装：含订阅组表、禁消费表及数据版本号。
 */
public class SubscriptionGroupWrapper extends RemotingSerializable {
    /** groupName → 订阅组配置。 */
    private ConcurrentMap<String, SubscriptionGroupConfig> subscriptionGroupTable =
        new ConcurrentHashMap<>(1024);
    /** groupName →（Topic → 禁消费标志）映射。 */
    private ConcurrentMap<String, ConcurrentMap<String, Integer>> forbiddenTable =
        new ConcurrentHashMap<>(1024);
    /** 订阅组数据版本，用于增量同步。 */
    private DataVersion dataVersion = new DataVersion();

    /** 返回订阅组配置表。 */
    public ConcurrentMap<String, SubscriptionGroupConfig> getSubscriptionGroupTable() {
        return subscriptionGroupTable;
    }

    public void setSubscriptionGroupTable(
        ConcurrentMap<String, SubscriptionGroupConfig> subscriptionGroupTable) {
        this.subscriptionGroupTable = subscriptionGroupTable;
    }

    /** 返回禁消费表。 */
    public ConcurrentMap<String, ConcurrentMap<String, Integer>> getForbiddenTable() {
        return forbiddenTable;
    }

    public void setForbiddenTable(ConcurrentMap<String, ConcurrentMap<String, Integer>> forbiddenTable) {
        this.forbiddenTable = forbiddenTable;
    }

    /** 返回数据版本。 */
    public DataVersion getDataVersion() {
        return dataVersion;
    }

    public void setDataVersion(DataVersion dataVersion) {
        this.dataVersion = dataVersion;
    }
}
