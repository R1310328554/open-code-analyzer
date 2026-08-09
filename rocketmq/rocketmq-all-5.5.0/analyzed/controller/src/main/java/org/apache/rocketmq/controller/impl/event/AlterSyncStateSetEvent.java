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
package org.apache.rocketmq.controller.impl.event;

import java.util.HashSet;
import java.util.Set;

/**
 * 修改目标 Broker 同步副本集（syncStateSet）的控制器事件。
 * 由 AlterSyncStateSet API 触发。
 */
public class AlterSyncStateSetEvent implements EventMessage {

    /** 目标 Broker 名称。 */
    private final String brokerName;
    /** 新的同步副本 BrokerId 集合。 */
    private final Set<Long/*BrokerId*/> newSyncStateSet;

    /** 构造同步副本集变更事件（防御性拷贝集合）。 */
    public AlterSyncStateSetEvent(String brokerName, Set<Long> newSyncStateSet) {
        this.brokerName = brokerName;
        this.newSyncStateSet = new HashSet<>(newSyncStateSet);
    }

    @Override
    public EventType getEventType() {
        return EventType.ALTER_SYNC_STATE_SET_EVENT;
    }

    /** 返回目标 Broker 名称。 */
    public String getBrokerName() {
        return brokerName;
    }

    /** 返回新的同步副本集副本（返回拷贝）。 */
    public Set<Long> getNewSyncStateSet() {
        return new HashSet<>(newSyncStateSet);
    }

    @Override
    public String toString() {
        return "AlterSyncStateSetEvent{" +
            "brokerName='" + brokerName + '\'' +
            ", newSyncStateSet=" + newSyncStateSet +
            '}';
    }
}
