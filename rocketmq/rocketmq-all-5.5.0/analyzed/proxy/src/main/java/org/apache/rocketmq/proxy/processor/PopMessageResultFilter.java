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
package org.apache.rocketmq.proxy.processor;

import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.proxy.common.ProxyContext;
import org.apache.rocketmq.remoting.protocol.heartbeat.SubscriptionData;

/**
 * POP 消息结果过滤器：对拉取到的消息按订阅规则决定去向（匹配/DLQ/返回/不匹配）。
 */
public interface PopMessageResultFilter {

    /** 过滤结果枚举。 */
    enum FilterResult {
        TO_DLQ, // 转发至死信队列
        NO_MATCH, // 不匹配订阅规则
        MATCH, // 匹配订阅规则
        TO_RETURN // 直接返回客户端
    }

    /** 过滤单条 POP 消息并返回处理决策。 */
    FilterResult filterMessage(ProxyContext ctx, String consumerGroup, SubscriptionData subscriptionData,
        MessageExt messageExt);
}
