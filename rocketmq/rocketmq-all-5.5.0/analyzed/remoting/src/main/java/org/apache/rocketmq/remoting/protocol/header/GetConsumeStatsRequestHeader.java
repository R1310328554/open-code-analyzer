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
package org.apache.rocketmq.remoting.protocol.header;

import com.google.common.base.MoreObjects;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.action.Action;
import org.apache.rocketmq.common.action.RocketMQAction;
import org.apache.rocketmq.common.resource.ResourceType;
import org.apache.rocketmq.common.resource.RocketMQResource;
import org.apache.rocketmq.remoting.annotation.CFNotNull;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.protocol.RequestCode;
import org.apache.rocketmq.remoting.rpc.TopicRequestHeader;

/**
 * 获取消费进度统计的请求头：指定消费组及单个或多个 Topic。
 * 若提供 topicList 则忽略 topic 单字段。
 */
@RocketMQAction(value = RequestCode.GET_CONSUME_STATS, action = Action.GET)
public class GetConsumeStatsRequestHeader extends TopicRequestHeader {
    private static final String TOPIC_NAME_SEPARATOR = ";";

    /** 消费组名称。 */
    @CFNotNull
    @RocketMQResource(ResourceType.GROUP)
    private String consumerGroup;

    /** 单个目标 Topic（topicList 为空时生效）。 */
    @RocketMQResource(ResourceType.TOPIC)
    private String topic;

    /** 多个 Topic，分号分隔；非空时忽略 topic 字段。 */
    @RocketMQResource(value = ResourceType.TOPIC, splitter = TOPIC_NAME_SEPARATOR)
    private String topicList;

    @Override
    public void checkFields() throws RemotingCommandException {
    }

    /** 解析 topicList 为 Topic 名称列表，空则返回空列表。 */
    public List<String> fetchTopicList() {
        if (StringUtils.isBlank(topicList)) {
            return Collections.emptyList();
        }
        return Arrays.asList(StringUtils.split(topicList, TOPIC_NAME_SEPARATOR));
    }

    /** 以 Topic 列表更新 topicList 字符串。 */
    public void updateTopicList(List<String> topicList) {
        if (topicList == null || topicList.isEmpty()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        topicList.forEach(topic -> sb.append(topic).append(TOPIC_NAME_SEPARATOR));
        this.setTopicList(sb.toString());
    }

    /** 返回 Topic 列表字符串。 */
    public String getTopicList() {
        return topicList;
    }

    /** 设置 Topic 列表字符串。 */
    public void setTopicList(String topicList) {
        this.topicList = topicList;
    }

    /** 返回消费组名称。 */
    public String getConsumerGroup() {
        return consumerGroup;
    }

    /** 设置消费组名称。 */
    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    /** 返回单个 Topic。 */
    public String getTopic() {
        return topic;
    }

    /** 设置单个 Topic。 */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /** 返回含消费组与 Topic 的调试字符串。 */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("consumerGroup", consumerGroup)
            .add("topic", topic)
            .toString();
    }
}
