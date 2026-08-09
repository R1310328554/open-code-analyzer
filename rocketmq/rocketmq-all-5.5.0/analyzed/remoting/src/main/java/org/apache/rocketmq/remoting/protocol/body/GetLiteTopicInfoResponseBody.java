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

import org.apache.rocketmq.common.entity.ClientGroup;
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;
import org.apache.rocketmq.remoting.protocol.admin.TopicOffset;

import java.util.Set;

/**
 * Lite Topic 元信息响应：订阅者、Topic 位点及是否分片到 Broker。
 */
public class GetLiteTopicInfoResponseBody extends RemotingSerializable {

    /** 父 Topic 名。 */
    private String parentTopic;
    /** Lite Topic 名。 */
    private String liteTopic;
    /** 订阅该 Lite Topic 的客户端组集合。 */
    private Set<ClientGroup> subscriber;
    /** Topic 各队列 min/max 位点。 */
    private TopicOffset topicOffset;
    /** 是否已分片路由到 Broker。 */
    private boolean shardingToBroker;

    public String getParentTopic() {
        return parentTopic;
    }

    public void setParentTopic(String parentTopic) {
        this.parentTopic = parentTopic;
    }

    public String getLiteTopic() {
        return liteTopic;
    }

    public void setLiteTopic(String liteTopic) {
        this.liteTopic = liteTopic;
    }

    /** 返回订阅者集合。 */
    public Set<ClientGroup> getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(Set<ClientGroup> subscriber) {
        this.subscriber = subscriber;
    }

    public TopicOffset getTopicOffset() {
        return topicOffset;
    }

    public void setTopicOffset(TopicOffset topicOffset) {
        this.topicOffset = topicOffset;
    }

    /** 返回是否已分片到 Broker。 */
    public boolean isShardingToBroker() {
        return shardingToBroker;
    }

    public void setShardingToBroker(boolean shardingToBroker) {
        this.shardingToBroker = shardingToBroker;
    }
}
