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
package org.apache.rocketmq.store;

import java.nio.ByteBuffer;
import java.util.Map;
import org.apache.rocketmq.remoting.protocol.heartbeat.SubscriptionData;

/**
 * 默认消息过滤器：基于 SubscriptionData 在 ConsumeQueue 层按 Tag 码过滤。
 * CommitLog 层默认全部匹配。
 */
public class DefaultMessageFilter implements MessageFilter {

    /** 订阅表达式与 Tag 集合。 */
    private SubscriptionData subscriptionData;

    /** 使用给定订阅数据构造过滤器。 */
    public DefaultMessageFilter(final SubscriptionData subscriptionData) {
        this.subscriptionData = subscriptionData;
    }

    /** 按 ConsumeQueue 中的 tagsCode 判断是否匹配订阅。 */
    @Override
    public boolean isMatchedByConsumeQueue(Long tagsCode, ConsumeQueueExt.CqExtUnit cqExtUnit) {
        if (null == tagsCode || null == subscriptionData) {
            return true;
        }

        if (subscriptionData.isClassFilterMode()) {
            return true;
        }

        return subscriptionData.getSubString().equals(SubscriptionData.SUB_ALL)
            || subscriptionData.getCodeSet().contains(tagsCode.intValue());
    }

    /** CommitLog 层默认不过滤，恒为 true。 */
    @Override
    public boolean isMatchedByCommitLog(ByteBuffer msgBuffer, Map<String, String> properties) {
        return true;
    }
}
