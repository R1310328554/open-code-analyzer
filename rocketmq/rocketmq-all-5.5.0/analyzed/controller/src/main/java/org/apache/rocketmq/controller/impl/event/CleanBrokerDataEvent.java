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

import java.util.Set;

/**
 * 清理指定 Broker 元数据的控制器事件，用于下线或故障恢复场景。
 */
public class CleanBrokerDataEvent implements EventMessage {

    /** 待清理的 Broker 名称。 */
    private String brokerName;

    /** 需要清理的 BrokerId 集合。 */
    private Set<Long> brokerIdSetToClean;

    /** 构造 Broker 数据清理事件。 */
    public CleanBrokerDataEvent(String brokerName, Set<Long> brokerIdSetToClean) {
        this.brokerName = brokerName;
        this.brokerIdSetToClean = brokerIdSetToClean;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public void setBrokerIdSetToClean(Set<Long> brokerIdSetToClean) {
        this.brokerIdSetToClean = brokerIdSetToClean;
    }

    public Set<Long> getBrokerIdSetToClean() {
        return brokerIdSetToClean;
    }

    /** 返回事件类型 {@link EventType#CLEAN_BROKER_DATA_EVENT}。 */
    @Override
    public EventType getEventType() {
        return EventType.CLEAN_BROKER_DATA_EVENT;
    }

    @Override
    public String toString() {
        return "CleanBrokerDataEvent{" +
            "brokerName='" + brokerName + '\'' +
            ", brokerIdSetToClean=" + brokerIdSetToClean +
            '}';
    }
}
