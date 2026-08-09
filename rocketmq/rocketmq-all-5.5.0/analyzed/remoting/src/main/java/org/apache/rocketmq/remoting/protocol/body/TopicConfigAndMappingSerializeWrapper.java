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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.rocketmq.remoting.protocol.DataVersion;
import org.apache.rocketmq.remoting.protocol.statictopic.TopicQueueMappingDetail;
import org.apache.rocketmq.remoting.protocol.statictopic.TopicQueueMappingInfo;

/**
 * Topic 配置与静态 Topic 队列映射的联合序列化包装，继承 {@link TopicConfigSerializeWrapper}。
 */
public class TopicConfigAndMappingSerializeWrapper extends TopicConfigSerializeWrapper {
    /** Topic → 队列映射概要信息。 */
    private Map<String/* topic */, TopicQueueMappingInfo> topicQueueMappingInfoMap = new ConcurrentHashMap<>();

    /** Topic → 队列映射详情。 */
    private Map<String/* topic */, TopicQueueMappingDetail> topicQueueMappingDetailMap = new ConcurrentHashMap<>();

    /** 映射数据版本号。 */
    private DataVersion mappingDataVersion = new DataVersion();


    /** 返回 Topic 队列映射概要表。 */
    public Map<String, TopicQueueMappingInfo> getTopicQueueMappingInfoMap() {
        return topicQueueMappingInfoMap;
    }

    public void setTopicQueueMappingInfoMap(Map<String, TopicQueueMappingInfo> topicQueueMappingInfoMap) {
        this.topicQueueMappingInfoMap = topicQueueMappingInfoMap;
    }

    /** 返回 Topic 队列映射详情表。 */
    public Map<String, TopicQueueMappingDetail> getTopicQueueMappingDetailMap() {
        return topicQueueMappingDetailMap;
    }

    public void setTopicQueueMappingDetailMap(Map<String, TopicQueueMappingDetail> topicQueueMappingDetailMap) {
        this.topicQueueMappingDetailMap = topicQueueMappingDetailMap;
    }

    public DataVersion getMappingDataVersion() {
        return mappingDataVersion;
    }

    public void setMappingDataVersion(DataVersion mappingDataVersion) {
        this.mappingDataVersion = mappingDataVersion;
    }

    /** 从 Topic 配置包装转换为带映射信息的包装。 */
    public static TopicConfigAndMappingSerializeWrapper from(TopicConfigSerializeWrapper wrapper) {
        if (wrapper instanceof  TopicConfigAndMappingSerializeWrapper) {
            return (TopicConfigAndMappingSerializeWrapper) wrapper;
        }
        TopicConfigAndMappingSerializeWrapper mappingSerializeWrapper =  new TopicConfigAndMappingSerializeWrapper();
        mappingSerializeWrapper.setDataVersion(wrapper.getDataVersion());
        mappingSerializeWrapper.setTopicConfigTable(wrapper.getTopicConfigTable());
        return mappingSerializeWrapper;
    }
}
