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

package org.apache.rocketmq.common.lite;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消费组对某 Topic 下 Lite Topic 集合的订阅关系，线程安全维护 liteTopicSet。
 */
public class LiteSubscription {
    /** 消费组名。 */
    private String group;
    /** 父 Topic 名。 */
    private String topic;
    /** 已订阅的 Lite Topic 名称集合。 */
    private final Set<String> liteTopicSet = ConcurrentHashMap.newKeySet();
    /** 最近一次变更时间戳（毫秒）。 */
    private volatile long updateTime = System.currentTimeMillis();

    /** 添加单个 Lite Topic 订阅并刷新 updateTime。 */
    public boolean addLiteTopic(String liteTopic) {
        updateTime();
        return this.liteTopicSet.add(liteTopic);
    }

    /** 批量添加 Lite Topic 订阅。 */
    public void addLiteTopic(Collection<String> set) {
        updateTime();
        this.liteTopicSet.addAll(set);
    }

    /** 移除单个 Lite Topic 订阅。 */
    public boolean removeLiteTopic(String liteTopic) {
        updateTime();
        return this.liteTopicSet.remove(liteTopic);
    }

    /** 批量移除 Lite Topic 订阅。 */
    public void removeLiteTopic(Collection<String> set) {
        updateTime();
        this.liteTopicSet.removeAll(set);
    }

    public String getGroup() {
        return group;
    }

    public LiteSubscription setGroup(String group) {
        this.group = group;
        return this;
    }

    public String getTopic() {
        return topic;
    }

    public LiteSubscription setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public Set<String> getLiteTopicSet() {
        return liteTopicSet;
    }

    public LiteSubscription setLiteTopicSet(Set<String> liteTopicSet) {
        this.liteTopicSet.addAll(liteTopicSet);
        return this;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    /** 将 updateTime 设为当前时间。 */
    private void updateTime() {
        this.updateTime = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "LiteSubscription{" +
            "group='" + group + '\'' +
            ", topic='" + topic + '\'' +
            ", liteTopicSet=" + liteTopicSet +
            ", updateTime=" + updateTime +
            '}';
    }
}
