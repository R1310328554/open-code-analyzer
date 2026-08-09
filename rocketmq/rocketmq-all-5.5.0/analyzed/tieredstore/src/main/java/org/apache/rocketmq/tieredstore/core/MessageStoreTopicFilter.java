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

package org.apache.rocketmq.tieredstore.core;

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.PopAckConstants;
import org.apache.rocketmq.common.topic.TopicValidator;
import org.apache.rocketmq.tieredstore.MessageStoreConfig;

/**
 * 基于配置与内置规则的 Topic 过滤器：排除系统 Topic、Revive 前缀、集群/ Broker 名及 LMQ。
 */
public class MessageStoreTopicFilter implements MessageStoreFilter {

    /** Topic 黑名单集合。 */
    private final Set<String> topicBlackSet;

    /** 初始化黑名单，默认包含集群名与 Broker 名。 */
    public MessageStoreTopicFilter(MessageStoreConfig storeConfig) {
        this.topicBlackSet = new HashSet<>();
        this.topicBlackSet.add(storeConfig.getBrokerClusterName());
        this.topicBlackSet.add(storeConfig.getBrokerName());
    }

    /** {@inheritDoc} 空白名、系统 Topic、Revive 前缀、黑名单及 LMQ 均返回 true。 */
    @Override
    public boolean filterTopic(String topicName) {
        if (StringUtils.isBlank(topicName)) {
            return true;
        }
        return TopicValidator.isSystemTopic(topicName) ||
            PopAckConstants.isStartWithRevivePrefix(topicName) ||
            this.topicBlackSet.contains(topicName) ||
            MixAll.isLmq(topicName);
    }

    /** {@inheritDoc} */
    @Override
    public void addTopicToBlackList(String topicName) {
        this.topicBlackSet.add(topicName);
    }
}
