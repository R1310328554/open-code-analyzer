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

import org.apache.rocketmq.remoting.protocol.RemotingSerializable;

import java.util.Set;

/**
 * 父 Topic 元信息响应：TTL、关联 Group、LMQ 配额及 Lite Topic 数量。
 */
public class GetParentTopicInfoResponseBody extends RemotingSerializable {

    /** 父 Topic 名。 */
    private String topic;
    /** 消息存活时间（秒）。 */
    private int ttl;
    /** 已订阅该 Topic 的 Group 集合。 */
    private Set<String> groups;
    /** 已分配 LMQ 数量。 */
    private int lmqNum;
    /** 下属 Lite Topic 数量。 */
    private int liteTopicCount;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    /** 返回消息 TTL。 */
    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public Set<String> getGroups() {
        return groups;
    }

    public void setGroups(Set<String> groups) {
        this.groups = groups;
    }

    public int getLmqNum() {
        return lmqNum;
    }

    public void setLmqNum(int lmqNum) {
        this.lmqNum = lmqNum;
    }

    /** 返回 Lite Topic 数量。 */
    public int getLiteTopicCount() {
        return liteTopicCount;
    }

    public void setLiteTopicCount(int liteTopicCount) {
        this.liteTopicCount = liteTopicCount;
    }
}
