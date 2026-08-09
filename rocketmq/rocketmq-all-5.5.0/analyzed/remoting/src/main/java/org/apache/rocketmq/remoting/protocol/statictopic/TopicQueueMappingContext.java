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

import com.google.common.collect.ImmutableList;
import java.util.List;

/**
 * 静态 Topic 队列映射上下文：聚合 Topic、全局 ID、映射详情与映射项列表。
 * 用于 Leader 判定与当前映射项定位。
 */
public class TopicQueueMappingContext  {
    /** Topic 名称。 */
    private String topic;
    /** 逻辑队列全局 ID。 */
    private Integer globalId;
    /** 当前 Broker 上的映射详情。 */
    private TopicQueueMappingDetail mappingDetail;
    /** 逻辑队列映射项列表。 */
    private List<LogicQueueMappingItem> mappingItemList;
    /** 当前 Leader 映射项。 */
    private LogicQueueMappingItem leaderItem;

    /** 当前正在使用的映射项。 */
    private LogicQueueMappingItem currentItem;

    public TopicQueueMappingContext(String topic, Integer globalId, TopicQueueMappingDetail mappingDetail, List<LogicQueueMappingItem> mappingItemList, LogicQueueMappingItem leaderItem) {
        this.topic = topic;
        this.globalId = globalId;
        this.mappingDetail = mappingDetail;
        this.mappingItemList = mappingItemList;
        this.leaderItem = leaderItem;

    }


    /** 判断当前 Broker 是否为该逻辑队列的 Leader。 */
    public boolean isLeader() {
        return leaderItem != null && leaderItem.getBname().equals(mappingDetail.getBname());
    }


    /** 返回 Topic 名称。 */
    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    /** 返回全局逻辑队列 ID。 */
    public Integer getGlobalId() {
        return globalId;
    }

    public void setGlobalId(Integer globalId) {
        this.globalId = globalId;
    }


    /** 返回映射详情。 */
    public TopicQueueMappingDetail getMappingDetail() {
        return mappingDetail;
    }

    public void setMappingDetail(TopicQueueMappingDetail mappingDetail) {
        this.mappingDetail = mappingDetail;
    }

    /** 返回映射项列表。 */
    public List<LogicQueueMappingItem> getMappingItemList() {
        return mappingItemList;
    }

    public void setMappingItemList(ImmutableList<LogicQueueMappingItem> mappingItemList) {
        this.mappingItemList = mappingItemList;
    }

    /** 返回 Leader 映射项。 */
    public LogicQueueMappingItem getLeaderItem() {
        return leaderItem;
    }

    public void setLeaderItem(LogicQueueMappingItem leaderItem) {
        this.leaderItem = leaderItem;
    }

    /** 返回当前映射项。 */
    public LogicQueueMappingItem getCurrentItem() {
        return currentItem;
    }

    /** 设置当前映射项。 */
    public void setCurrentItem(LogicQueueMappingItem currentItem) {
        this.currentItem = currentItem;
    }

    public void setMappingItemList(List<LogicQueueMappingItem> mappingItemList) {
        this.mappingItemList = mappingItemList;
    }
}
