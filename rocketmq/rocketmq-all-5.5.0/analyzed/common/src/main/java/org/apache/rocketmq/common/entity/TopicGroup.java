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

package org.apache.rocketmq.common.entity;

import java.util.Objects;

/**
 * Topic 与消费组的复合键，用于 Map/Set 去重与哈希索引。
 */
public class TopicGroup {

    /** Topic 名称。 */
    public final String topic;
    /** 消费组名。 */
    public final String group;
    /** 缓存的 hashCode（懒计算，0 表示未计算）。 */
    private int hash; // 默认为 0

    /**
     * @param topic Topic 名称
     * @param group 消费组名
     */
    public TopicGroup(String topic, String group) {
        this.topic = topic;
        this.group = group;
    }

    /** 按 topic 与 group 判等。 */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TopicGroup that = (TopicGroup) o;
        return Objects.equals(topic, that.topic) && Objects.equals(group, that.group);
    }

    /** 懒计算并缓存 hashCode。 */
    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = Objects.hash(topic, group);
        }
        return hash;
    }

    /** 返回调试字符串。 */
    @Override
    public String toString() {
        return "TopicGroup{" +
            "topic='" + topic + '\'' +
            ", group='" + group + '\'' +
            '}';
    }
}
