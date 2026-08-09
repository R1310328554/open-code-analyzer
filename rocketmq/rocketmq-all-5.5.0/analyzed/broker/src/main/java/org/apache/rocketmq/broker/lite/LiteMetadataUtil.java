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

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import org.apache.rocketmq.broker.BrokerController;
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.common.attribute.TopicMessageType;
import org.apache.rocketmq.remoting.protocol.subscription.SubscriptionGroupConfig;

/**
 * Lite 元数据工具：从 Topic/Subscription 配置读取 lite 类型、绑定 topic、TTL 及订阅 group 映射。
 */
public class LiteMetadataUtil {

    /** 判断消费组是否允许消费。 */
    public static boolean isConsumeEnable(String group, BrokerController brokerController) {
        if (null == group || null == brokerController) {
            return false;
        }
        SubscriptionGroupConfig groupConfig =
            brokerController.getSubscriptionGroupManager().findSubscriptionGroupConfig(group);
        return null != groupConfig && groupConfig.isConsumeEnable();
    }

    /** 父 topic 的 {@link TopicMessageType} 是否为 LITE。 */
    public static boolean isLiteMessageType(String parentTopic, BrokerController brokerController) {
        if (null == parentTopic || null == brokerController) {
            return false;
        }
        TopicConfig topicConfig = brokerController.getTopicConfigManager().selectTopicConfig(parentTopic);
        return topicConfig != null && TopicMessageType.LITE.equals(topicConfig.getTopicMessageType());
    }

    /** 消费组是否配置了 liteBindTopic（lite 订阅组）。 */
    public static boolean isLiteGroupType(String group, BrokerController brokerController) {
        if (null == group || null == brokerController) {
            return false;
        }
        SubscriptionGroupConfig groupConfig =
            brokerController.getSubscriptionGroupManager().findSubscriptionGroupConfig(group);
        return null != groupConfig && groupConfig.getLiteBindTopic() != null;
    }

    /** 返回消费组绑定的 lite 父 topic；未配置则 null。 */
    public static String getLiteBindTopic(String group, BrokerController brokerController) {
        if (null == group || null == brokerController) {
            return null;
        }
        SubscriptionGroupConfig groupConfig =
            brokerController.getSubscriptionGroupManager().findSubscriptionGroupConfig(group);
        return null != groupConfig ? groupConfig.getLiteBindTopic() : null;
    }

    /** 消费组是否为 lite 独占订阅模式。 */
    public static boolean isSubLiteExclusive(String group, BrokerController brokerController) {
        if (null == group || null == brokerController) {
            return false;
        }
        SubscriptionGroupConfig groupConfig =
            brokerController.getSubscriptionGroupManager().findSubscriptionGroupConfig(group);
        return null != groupConfig && groupConfig.isLiteSubExclusive();
    }

    /** 独占模式下是否在重置位点时清空 offset。 */
    public static boolean isResetOffsetInExclusiveMode(String group, BrokerController brokerController) {
        if (null == group || null == brokerController) {
            return false;
        }
        SubscriptionGroupConfig groupConfig =
            brokerController.getSubscriptionGroupManager().findSubscriptionGroupConfig(group);
        return null != groupConfig && groupConfig.isResetOffsetInExclusiveMode();
    }

    /** 退订时是否重置消费位点。 */
    public static boolean isResetOffsetOnUnsubscribe(String group, BrokerController brokerController) {
        if (null == group || null == brokerController) {
            return false;
        }
        SubscriptionGroupConfig groupConfig =
            brokerController.getSubscriptionGroupManager().findSubscriptionGroupConfig(group);
        return null != groupConfig && groupConfig.isResetOffsetOnUnsubscribe();
    }

    /** 返回 group 级 maxClientEventCount，未配置则用 broker 默认值。 */
    public static int getMaxClientEventCount(String group, BrokerController brokerController) {
        if (null == group || null == brokerController) {
            return -1;
        }
        SubscriptionGroupConfig groupConfig =
            brokerController.getSubscriptionGroupManager().findSubscriptionGroupConfig(group);
        if (null == groupConfig || groupConfig.getMaxClientEventCount() <= 0) {
            return brokerController.getBrokerConfig().getMaxClientEventCount();
        }
        return groupConfig.getMaxClientEventCount();
    }

    /** 是否为 wildcard lite 消费组（通配订阅父 topic 下全部 LMQ）。 */
    public static boolean isWildcardGroup(String group, BrokerController brokerController) {
        if (null == group || null == brokerController) {
            return false;
        }
        SubscriptionGroupConfig groupConfig =
            brokerController.getSubscriptionGroupManager().findSubscriptionGroupConfig(group);
        return groupConfig != null && groupConfig.isWildcardLiteGroup();
    }

    /** 构建 lite topic 名 → 过期分钟数 的 TTL 映射。 */
    public static Map<String, Integer> getTopicTtlMap(BrokerController brokerController) {
        if (null == brokerController) {
            return Collections.emptyMap();
        }
        ConcurrentMap<String, TopicConfig> topicConfigTable =
            brokerController.getTopicConfigManager().getTopicConfigTable();

        return topicConfigTable.entrySet().stream()
            .filter(entry -> entry.getValue().getTopicMessageType().equals(TopicMessageType.LITE))
            .collect(Collectors.toMap(
                entry -> entry.getKey(),
                entry -> entry.getValue().getLiteTopicExpiration()
            ));
    }

    /** 构建 lite 父 topic → 订阅该 topic 的 group 集合 映射。 */
    public static Map<String, Set<String>> getSubscriberGroupMap(BrokerController brokerController) {
        if (null == brokerController) {
            return Collections.emptyMap();
        }
        ConcurrentMap<String, SubscriptionGroupConfig> groupTable =
            brokerController.getSubscriptionGroupManager().getSubscriptionGroupTable();

        return groupTable.entrySet().stream()
            .filter(entry -> entry.getValue().getLiteBindTopic() != null)
            .collect(Collectors.groupingBy(
                entry -> entry.getValue().getLiteBindTopic(),
                Collectors.mapping(Map.Entry::getKey, Collectors.toSet())
            ));
    }
}
