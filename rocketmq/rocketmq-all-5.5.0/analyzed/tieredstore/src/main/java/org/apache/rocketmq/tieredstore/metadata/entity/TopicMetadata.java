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
package org.apache.rocketmq.tieredstore.metadata.entity;

import com.alibaba.fastjson2.annotation.JSONField;

/**
 * Topic 元数据：维护 Topic 数值 ID、名称、保留时长与状态。
 */
public class TopicMetadata {

    /** Topic 数值 ID，用于索引与路径哈希。 */
    @JSONField(ordinal = 1)
    private long topicId;

    /** Topic 名称。 */
    @JSONField(ordinal = 2)
    private String topic;

    /** Topic 在分层存储中的状态。 */
    @JSONField(ordinal = 3)
    private int status;

    /** 消息保留时长（毫秒）。 */
    @JSONField(ordinal = 4)
    private long reserveTime;

    /** 元数据最后更新时间戳。 */
    @JSONField(ordinal = 5)
    private long updateTimestamp;

    // fastjson 反序列化使用的默认构造器
    @SuppressWarnings("unused")
    public TopicMetadata() {
    }

    /** 构造 Topic 元数据并记录更新时间。 */
    public TopicMetadata(long topicId, String topic, long reserveTime) {
        this.topicId = topicId;
        this.topic = topic;
        this.reserveTime = reserveTime;
        this.updateTimestamp = System.currentTimeMillis();
    }

    public long getTopicId() {
        return topicId;
    }

    public void setTopicId(long topicId) {
        this.topicId = topicId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public long getReserveTime() {
        return reserveTime;
    }

    public void setReserveTime(long reserveTime) {
        this.reserveTime = reserveTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setUpdateTimestamp(long updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }
}
