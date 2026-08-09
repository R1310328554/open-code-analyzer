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

package org.apache.rocketmq.proxy.remoting.activity;

import io.netty.channel.ChannelHandlerContext;
import java.time.Duration;
import org.apache.rocketmq.broker.client.ConsumerGroupInfo;
import org.apache.rocketmq.common.sysflag.PullSysFlag;
import org.apache.rocketmq.proxy.common.ProxyContext;
import org.apache.rocketmq.proxy.processor.MessagingProcessor;
import org.apache.rocketmq.proxy.remoting.pipeline.RequestPipeline;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;
import org.apache.rocketmq.remoting.protocol.ResponseCode;
import org.apache.rocketmq.remoting.protocol.header.PullMessageRequestHeader;
import org.apache.rocketmq.remoting.protocol.heartbeat.SubscriptionData;

/**
 * Pull 消费 Remoting Activity：补全订阅信息后转发拉取请求。
 */
public class PullMessageActivity extends AbstractRemotingActivity {
    /** 构造 Pull 消费 Activity。 */
    public PullMessageActivity(RequestPipeline requestPipeline,
        MessagingProcessor messagingProcessor) {
        super(requestPipeline, messagingProcessor);
    }

    @Override
    protected RemotingCommand processRequest0(ChannelHandlerContext ctx, RemotingCommand request,
        ProxyContext context) throws Exception {
        PullMessageRequestHeader requestHeader = (PullMessageRequestHeader) request.decodeCommandCustomHeader(PullMessageRequestHeader.class);
        int sysFlag = requestHeader.getSysFlag();
        // 请求未携带订阅信息时从本地消费组缓存补全
        if (!PullSysFlag.hasSubscriptionFlag(sysFlag)) {
            // 查询消费组注册信息
            ConsumerGroupInfo consumerInfo = messagingProcessor.getConsumerGroupInfo(context, requestHeader.getConsumerGroup());
            if (consumerInfo == null) {
                return RemotingCommand.buildErrorResponse(ResponseCode.SUBSCRIPTION_NOT_LATEST,
                    "the consumer's subscription not latest");
            }
            // 查找 Topic 对应订阅表达式
            SubscriptionData subscriptionData = consumerInfo.findSubscriptionData(requestHeader.getTopic());
            if (subscriptionData == null) {
                return RemotingCommand.buildErrorResponse(ResponseCode.SUBSCRIPTION_NOT_EXIST,
                    "the consumer's subscription not exist");
            }
            // 写入订阅字符串与表达式类型后重编码请求头
            requestHeader.setSysFlag(PullSysFlag.buildSysFlagWithSubscription(sysFlag));
            requestHeader.setSubscription(subscriptionData.getSubString());
            requestHeader.setExpressionType(subscriptionData.getExpressionType());
            request.writeCustomHeader(requestHeader);
            request.makeCustomHeaderToNet();
        }
        // 挂起超时加 10 秒缓冲后转发
        long timeoutMillis = requestHeader.getSuspendTimeoutMillis() + Duration.ofSeconds(10).toMillis();
        return request(ctx, request, context, timeoutMillis);
    }
}
