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
package org.apache.rocketmq.broker.topic;

import org.apache.rocketmq.broker.BrokerController;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.common.constant.PermName;

/**
 * Lite/LMQ Topic 配置管理器：LMQ topic 按需生成单队列读写配置，不参与持久化更新。
 */
public class LmqTopicConfigManager extends TopicConfigManager {
    public LmqTopicConfigManager(BrokerController brokerController) {
        super(brokerController);
    }

    @Override
    /** LMQ topic 返回单队列默认配置，否则走常规 Topic 表。 */
    public TopicConfig selectTopicConfig(final String topic) {
        if (MixAll.isLmq(topic)) {
            return simpleLmqTopicConfig(topic);
        }
        return super.selectTopicConfig(topic);
    }

    @Override
    public void updateTopicConfig(final TopicConfig topicConfig) {
        if (topicConfig == null || MixAll.isLmq(topicConfig.getTopicName())) {
            return;
        }
        super.updateTopicConfig(topicConfig);
    }

    /** 构造 LMQ 单队列 TopicConfig（1 读 1 写，读写权限）。 */
    private TopicConfig simpleLmqTopicConfig(String topic) {
        return new TopicConfig(topic, 1, 1, PermName.PERM_READ | PermName.PERM_WRITE);
    }

    @Override
    /** LMQ topic 恒视为存在。 */
    public boolean containsTopic(String topic) {
        if (MixAll.isLmq(topic)) {
            return true;
        }
        return super.containsTopic(topic);
    }

}
