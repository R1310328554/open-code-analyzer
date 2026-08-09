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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;

/**
 * 静态 Topic 队列映射摘要：注册到 Broker/NameServer 用于构建路由。
 * 含 epoch 防脏读、逻辑到物理 queueId 映射等元数据。
 */
public class TopicQueueMappingInfo extends RemotingSerializable {
    /** 映射层级 0：当前 Broker 上的 Leader。 */
    public static final int LEVEL_0 = 0;

    /** Topic 名称（冗余字段）。 */
    String topic; // redundant field
    /** 元数据作用域，默认全局。 */
    String scope = MixAll.METADATA_SCOPE_GLOBAL;
    /** 逻辑队列总数。 */
    int totalQueues;
    /** 托管 Broker 名称。 */
    String bname;  //identify the hosted broker name
    /** 映射 epoch，用于隔离旧脏数据。 */
    long epoch; //important to fence the old dirty data
    /** 是否为脏数据标记。 */
    boolean dirty; //indicate if the data is dirty
    /** 逻辑 queueId 到物理 queueId 的当前映射（用于构建路由）。 */
    protected ConcurrentMap<Integer/*logicId*/, Integer/*physicalId*/> currIdMap = new ConcurrentHashMap<>();

    public TopicQueueMappingInfo() {

    }

    public TopicQueueMappingInfo(String topic, int totalQueues, String bname, long epoch) {
        this.topic = topic;
        this.totalQueues = totalQueues;
        this.bname = bname;
        this.epoch = epoch;
        this.dirty = false;
    }

    /** 返回是否为脏数据。 */
    public boolean isDirty() {
        return dirty;
    }

    /** 设置脏数据标记。 */
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    /** 返回逻辑队列总数。 */
    public int getTotalQueues() {
        return totalQueues;
    }


    /** 返回托管 Broker 名称。 */
    public String getBname() {
        return bname;
    }

    /** 返回 Topic 名称。 */
    public String getTopic() {
        return topic;
    }

    /** 返回映射 epoch。 */
    public long getEpoch() {
        return epoch;
    }

    public void setEpoch(long epoch) {
        this.epoch = epoch;
    }

    public void setTotalQueues(int totalQueues) {
        this.totalQueues = totalQueues;
    }

    /** 返回逻辑到物理 queueId 映射。 */
    public ConcurrentMap<Integer, Integer> getCurrIdMap() {
        return currIdMap;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setBname(String bname) {
        this.bname = bname;
    }

    public void setCurrIdMap(ConcurrentMap<Integer, Integer> currIdMap) {
        this.currIdMap = currIdMap;
    }

    /** 返回元数据作用域。 */
    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TopicQueueMappingInfo)) return false;

        TopicQueueMappingInfo info = (TopicQueueMappingInfo) o;

        if (totalQueues != info.totalQueues) return false;
        if (epoch != info.epoch) return false;
        if (dirty != info.dirty) return false;
        if (topic != null ? !topic.equals(info.topic) : info.topic != null) return false;
        if (scope != null ? !scope.equals(info.scope) : info.scope != null) return false;
        if (bname != null ? !bname.equals(info.bname) : info.bname != null) return false;
        return currIdMap != null ? currIdMap.equals(info.currIdMap) : info.currIdMap == null;
    }

    @Override
    public int hashCode() {
        int result = topic != null ? topic.hashCode() : 0;
        result = 31 * result + (scope != null ? scope.hashCode() : 0);
        result = 31 * result + totalQueues;
        result = 31 * result + (bname != null ? bname.hashCode() : 0);
        result = 31 * result + (int) (epoch ^ (epoch >>> 32));
        result = 31 * result + (dirty ? 1 : 0);
        result = 31 * result + (currIdMap != null ? currIdMap.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TopicQueueMappingInfo{" +
                "topic='" + topic + '\'' +
                ", scope='" + scope + '\'' +
                ", totalQueues=" + totalQueues +
                ", bname='" + bname + '\'' +
                ", epoch=" + epoch +
                ", dirty=" + dirty +
                ", currIdMap=" + currIdMap +
                '}';
    }
}
