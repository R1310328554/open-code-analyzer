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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * 静态 Topic 队列映射详情：在 {@link TopicQueueMappingInfo} 基础上
 * 持有当前 Broker 托管的逻辑队列映射项（不注册到 NameServer）。
 */
public class TopicQueueMappingDetail extends TopicQueueMappingInfo {

    /** 当前 Broker 托管的全局 ID 到映射项列表（不注册 NameServer，非 null）。 */
    private ConcurrentMap<Integer/*global id*/, List<LogicQueueMappingItem>> hostedQueues = new ConcurrentHashMap<>();

    //make sure there is a default constructor
    public TopicQueueMappingDetail() {

    }

    public TopicQueueMappingDetail(String topic, int totalQueues, String bname, long epoch) {
        super(topic, totalQueues, bname, epoch);
    }



    /** 向映射详情写入指定 globalId 的映射项列表。 */
    public static boolean putMappingInfo(TopicQueueMappingDetail mappingDetail, Integer globalId, List<LogicQueueMappingItem> mappingInfo) {
        if (mappingInfo.isEmpty()) {
            return true;
        }
        mappingDetail.hostedQueues.put(globalId, mappingInfo);
        return true;
    }

    /** 读取指定 globalId 的映射项列表。 */
    public static List<LogicQueueMappingItem> getMappingInfo(TopicQueueMappingDetail mappingDetail, Integer globalId) {
        return mappingDetail.hostedQueues.get(globalId);
    }

    /** 构建 globalId 到物理 queueId 的映射（level 0 表示当前 Leader）。 */
    public static ConcurrentMap<Integer, Integer> buildIdMap(TopicQueueMappingDetail mappingDetail, int level) {
        //level 0 means current leader in this broker
        //level 1 means previous leader in this broker, reserved for
        assert level == LEVEL_0 ;

        if (mappingDetail.hostedQueues == null || mappingDetail.hostedQueues.isEmpty()) {
            return new ConcurrentHashMap<>();
        }
        ConcurrentMap<Integer, Integer> tmpIdMap = new ConcurrentHashMap<>();
        for (Map.Entry<Integer, List<LogicQueueMappingItem>> entry: mappingDetail.hostedQueues.entrySet()) {
            Integer globalId =  entry.getKey();
            List<LogicQueueMappingItem> items = entry.getValue();
            if (level == LEVEL_0
                    && items.size() >= 1) {
                LogicQueueMappingItem curr = items.get(items.size() - 1);
                if (mappingDetail.bname.equals(curr.getBname())) {
                    tmpIdMap.put(globalId, curr.getQueueId());
                }
            }
        }
        return tmpIdMap;
    }


    /** 根据映射计算指定 globalId 的最大静态队列偏移。 */
    public static long computeMaxOffsetFromMapping(TopicQueueMappingDetail mappingDetail, Integer globalId) {
        List<LogicQueueMappingItem> mappingItems = getMappingInfo(mappingDetail, globalId);
        if (mappingItems == null
                || mappingItems.isEmpty()) {
            return -1;
        }
        LogicQueueMappingItem item =  mappingItems.get(mappingItems.size() - 1);
        return item.computeMaxStaticQueueOffset();
    }


    /** 克隆为可注册到 NameServer 的 {@link TopicQueueMappingInfo}。 */
    public static TopicQueueMappingInfo cloneAsMappingInfo(TopicQueueMappingDetail mappingDetail) {
        TopicQueueMappingInfo topicQueueMappingInfo = new TopicQueueMappingInfo(mappingDetail.topic, mappingDetail.totalQueues, mappingDetail.bname, mappingDetail.epoch);
        topicQueueMappingInfo.currIdMap = TopicQueueMappingDetail.buildIdMap(mappingDetail, LEVEL_0);
        return topicQueueMappingInfo;
    }

    /** 判断该 globalId 是否可按物理队列处理（无映射或 logicOffset 为 0）。 */
    public static boolean checkIfAsPhysical(TopicQueueMappingDetail mappingDetail, Integer globalId) {
        List<LogicQueueMappingItem> mappingItems = getMappingInfo(mappingDetail, globalId);
        return mappingItems == null
                || mappingItems.size() == 1
                &&  mappingItems.get(0).getLogicOffset() == 0;
    }

    /** 返回托管队列映射表。 */
    public ConcurrentMap<Integer, List<LogicQueueMappingItem>> getHostedQueues() {
        return hostedQueues;
    }

    /** 设置托管队列映射表。 */
    public void setHostedQueues(ConcurrentMap<Integer, List<LogicQueueMappingItem>> hostedQueues) {
        this.hostedQueues = hostedQueues;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof TopicQueueMappingDetail)) return false;

        TopicQueueMappingDetail that = (TopicQueueMappingDetail) o;

        return new EqualsBuilder()
                .append(hostedQueues, that.hostedQueues)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(hostedQueues)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "TopicQueueMappingDetail{" +
                "hostedQueues=" + hostedQueues +
                ", topic='" + topic + '\'' +
                ", totalQueues=" + totalQueues +
                ", bname='" + bname + '\'' +
                ", epoch=" + epoch +
                ", dirty=" + dirty +
                ", currIdMap=" + currIdMap +
                '}';
    }
}
