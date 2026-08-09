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

package org.apache.rocketmq.broker.lite;

import com.google.common.hash.Hashing;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.broker.BrokerController;
import org.apache.rocketmq.broker.topic.TopicRouteInfoManager;
import org.apache.rocketmq.client.impl.producer.TopicPublishInfo;
import org.apache.rocketmq.common.lite.LiteUtil;
import org.apache.rocketmq.common.message.MessageQueue;

import java.util.List;

/**
 * 一致性哈希 lite 分片：按 liteTopic 哈希值映射到父 topic 路由中的 MessageQueue 所属 broker。
 */
public class LiteShardingImpl implements LiteSharding {

    private final BrokerController brokerController;
    private final TopicRouteInfoManager topicRouteInfoManager;

    /** 注入 Broker 与 topic 路由管理器。 */
    public LiteShardingImpl(BrokerController brokerController, TopicRouteInfoManager topicRouteInfoManager) {
        this.brokerController = brokerController;
        this.topicRouteInfoManager = topicRouteInfoManager;
    }

    @Override
    /** 对 liteTopic 做 consistentHash，选取 writeQueue 对应 broker；路由缺失时回退当前 broker。 */
    public String shardingByLmqName(String parentTopic, String lmqName) {
        TopicPublishInfo topicPublishInfo = topicRouteInfoManager.tryToFindTopicPublishInfo(parentTopic);
        if (topicPublishInfo == null) {
            // if topic not exist, return current broker
            return brokerController.getBrokerConfig().getBrokerName();
        }
        List<MessageQueue> writeQueues = topicPublishInfo.getMessageQueueList();
        if (CollectionUtils.isEmpty(writeQueues)) {
            return brokerController.getBrokerConfig().getBrokerName();
        }
        String liteTopic = LiteUtil.getLiteTopic(lmqName);
        if (StringUtils.isEmpty(liteTopic)) {
            return brokerController.getBrokerConfig().getBrokerName();
        }
        int bucket = Hashing.consistentHash(liteTopic.hashCode(), writeQueues.size());
        MessageQueue targetQueue = writeQueues.get(bucket);
        return targetQueue.getBrokerName();
    }
}
