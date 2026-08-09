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
package org.apache.rocketmq.remoting.protocol.subscription;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;

/**
 * 消费组禁读配置：标记某 group 对指定 topic 是否允许拉取消息。
 */
public class GroupForbidden extends RemotingSerializable {

    /** 受限 Topic 名称。 */
    private String  topic;
    /** 消费组名称。 */
    private String  group;
    /** 是否可读（false 表示禁止消费该 Topic）。 */
    private Boolean readable;

    /** 返回 Topic 名称。 */
    public String getTopic() {
        return topic;
    }

    /** 设置 Topic 名称。 */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /** 返回消费组名称。 */
    public String getGroup() {
        return group;
    }

    /** 设置消费组名称。 */
    public void setGroup(String group) {
        this.group = group;
    }

    /** 返回是否可读。 */
    public Boolean getReadable() {
        return readable;
    }

    /** 设置是否可读。 */
    public void setReadable(Boolean readable) {
        this.readable = readable;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((group == null) ? 0 : group.hashCode());
        result = prime * result + ((readable == null) ? 0 : readable.hashCode());
        result = prime * result + ((topic == null) ? 0 : topic.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GroupForbidden other = (GroupForbidden) obj;
        return new EqualsBuilder()
                .append(topic, other.topic)
                .append(group, other.group)
                .append(readable, other.readable)
                .isEquals();
    }

    @Override
    public String toString() {
        return "GroupForbidden [topic=" + topic + ", group=" + group + ", readable=" + readable + "]";
    }

}
