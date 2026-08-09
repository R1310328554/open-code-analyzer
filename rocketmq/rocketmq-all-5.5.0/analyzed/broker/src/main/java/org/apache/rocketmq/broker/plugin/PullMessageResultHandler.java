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

package org.apache.rocketmq.broker.plugin;

import io.netty.channel.Channel;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;
import org.apache.rocketmq.remoting.protocol.header.PullMessageRequestHeader;
import org.apache.rocketmq.remoting.protocol.heartbeat.SubscriptionData;
import org.apache.rocketmq.remoting.protocol.statictopic.TopicQueueMappingContext;
import org.apache.rocketmq.remoting.protocol.subscription.SubscriptionGroupConfig;
import org.apache.rocketmq.store.GetMessageResult;
import org.apache.rocketmq.store.MessageFilter;

/**
 * Pull 消息结果处理器插件：在 Store 返回 GetMessageResult 后定制响应组装逻辑。
 */
public interface PullMessageResultHandler {

    /**
     * 处理 Store 拉取结果并构造/改写 Remoting 响应。
     *
     * @return 最终响应；返回 null 表示沿用默认处理
     */
    RemotingCommand handle(final GetMessageResult getMessageResult,
                           final RemotingCommand request,
                           final PullMessageRequestHeader requestHeader,
                           final Channel channel,
                           final SubscriptionData subscriptionData,
                           final SubscriptionGroupConfig subscriptionGroupConfig,
                           final boolean brokerAllowSuspend,
                           final MessageFilter messageFilter,
                           final RemotingCommand response,
                           final TopicQueueMappingContext mappingContext,
                           final long beginTimeMills);
}
