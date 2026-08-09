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
package org.apache.rocketmq.remoting.protocol.statictopic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;

/**
 * Topic 重映射操作包装：携带 epoch、Broker 配置变更与迁入/迁出 Broker 集合。
 * 支持 CREATE_OR_UPDATE 与 REMAPPING 两种操作类型。
 */
public class TopicRemappingDetailWrapper extends RemotingSerializable {
    /** 操作类型：创建或更新映射。 */
    public static final String TYPE_CREATE_OR_UPDATE = "CREATE_OR_UPDATE";
    /** 操作类型：重映射（迁移逻辑队列）。 */
    public static final String TYPE_REMAPPING = "REMAPPING";

    /** 重映射前配置键后缀。 */
    public static final String SUFFIX_BEFORE = ".before";
    /** 重映射后配置键后缀。 */
    public static final String SUFFIX_AFTER = ".after";


    /** Topic 名称。 */
    private String topic;
    /** 操作类型（CREATE_OR_UPDATE 或 REMAPPING）。 */
    private String type;
    /** 映射 epoch，用于版本隔离。 */
    private long epoch;

    /** Broker 名称到 Topic 配置与映射的组合映射。 */
    private Map<String, TopicConfigAndQueueMapping> brokerConfigMap = new HashMap<>();

    /** 需要迁入映射的 Broker 集合。 */
    private Set<String> brokerToMapIn = new HashSet<>();

    /** 需要迁出映射的 Broker 集合。 */
    private Set<String> brokerToMapOut = new HashSet<>();

    public TopicRemappingDetailWrapper() {

    }

    public TopicRemappingDetailWrapper(String topic, String type, long epoch, Map<String, TopicConfigAndQueueMapping> brokerConfigMap, Set<String> brokerToMapIn, Set<String> brokerToMapOut) {
        this.topic = topic;
        this.type = type;
        this.epoch = epoch;
        this.brokerConfigMap = brokerConfigMap;
        this.brokerToMapIn = brokerToMapIn;
        this.brokerToMapOut = brokerToMapOut;
    }

    /** 返回 Topic 名称。 */
    public String getTopic() {
        return topic;
    }

    /** 返回操作类型。 */
    public String getType() {
        return type;
    }

    /** 返回映射 epoch。 */
    public long getEpoch() {
        return epoch;
    }

    /** 返回 Broker 配置映射。 */
    public Map<String, TopicConfigAndQueueMapping> getBrokerConfigMap() {
        return brokerConfigMap;
    }

    /** 返回迁入 Broker 集合。 */
    public Set<String> getBrokerToMapIn() {
        return brokerToMapIn;
    }

    /** 返回迁出 Broker 集合。 */
    public Set<String> getBrokerToMapOut() {
        return brokerToMapOut;
    }

    public void setBrokerConfigMap(Map<String, TopicConfigAndQueueMapping> brokerConfigMap) {
        this.brokerConfigMap = brokerConfigMap;
    }

    public void setBrokerToMapIn(Set<String> brokerToMapIn) {
        this.brokerToMapIn = brokerToMapIn;
    }

    public void setBrokerToMapOut(Set<String> brokerToMapOut) {
        this.brokerToMapOut = brokerToMapOut;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setEpoch(long epoch) {
        this.epoch = epoch;
    }
}
