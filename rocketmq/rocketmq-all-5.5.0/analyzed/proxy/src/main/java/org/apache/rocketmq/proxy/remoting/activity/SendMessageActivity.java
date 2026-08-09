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
import java.util.Map;
import org.apache.rocketmq.common.attribute.TopicMessageType;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.common.message.MessageDecoder;
import org.apache.rocketmq.proxy.common.ProxyContext;
import org.apache.rocketmq.proxy.config.ConfigurationManager;
import org.apache.rocketmq.proxy.processor.MessagingProcessor;
import org.apache.rocketmq.proxy.processor.validator.DefaultTopicMessageTypeValidator;
import org.apache.rocketmq.proxy.processor.validator.TopicMessageTypeValidator;
import org.apache.rocketmq.proxy.remoting.pipeline.RequestPipeline;
import org.apache.rocketmq.remoting.protocol.NamespaceUtil;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;
import org.apache.rocketmq.remoting.protocol.RequestCode;
import org.apache.rocketmq.remoting.protocol.header.SendMessageRequestHeader;

/**
 * 消息发送 Remoting Activity：校验 Topic 类型并转发单条/批量/回退消息。
 */
public class SendMessageActivity extends AbstractRemotingActivity {
    TopicMessageTypeValidator topicMessageTypeValidator;

    /** 构造发送消息 Activity 并初始化类型校验器。 */
    public SendMessageActivity(RequestPipeline requestPipeline,
        MessagingProcessor messagingProcessor) {
        super(requestPipeline, messagingProcessor);
        this.topicMessageTypeValidator = new DefaultTopicMessageTypeValidator();
    }

    @Override
    protected RemotingCommand processRequest0(ChannelHandlerContext ctx, RemotingCommand request,
        ProxyContext context) throws Exception {
        switch (request.getCode()) {
            case RequestCode.SEND_MESSAGE: // 单条发送
            case RequestCode.SEND_MESSAGE_V2:
            case RequestCode.SEND_BATCH_MESSAGE: { // 批量发送
                return sendMessage(ctx, request, context);
            }
            case RequestCode.CONSUMER_SEND_MSG_BACK: { // 消费失败回退
                return consumerSendMessage(ctx, request, context);
            }
            default:
                break;
        }
        return null;
    }

    /** 校验 Topic 消息类型、注册事务订阅后转发发送请求。 */
    protected RemotingCommand sendMessage(ChannelHandlerContext ctx, RemotingCommand request,
        ProxyContext context) throws Exception {
        SendMessageRequestHeader requestHeader = SendMessageRequestHeader.parseRequestHeader(request);
        String topic = requestHeader.getTopic();
        Map<String, String> property = MessageDecoder.string2messageProperties(requestHeader.getProperties());
        TopicMessageType messageType = TopicMessageType.parseFromMessageProperty(property);
        if (isNeedCheckTopicMessageType(property)) {
            if (topicMessageTypeValidator != null) {
                // 重试 Topic 与 DLQ 跳过类型校验
                if (!NamespaceUtil.isRetryTopic(topic) && !NamespaceUtil.isDLQTopic(topic)) {
                    TopicMessageType topicMessageType = messagingProcessor.getMetadataService().getTopicMessageType(context, topic);
                    topicMessageTypeValidator.validate(topicMessageType, messageType);
                }
            }
        }
        if (!NamespaceUtil.isRetryTopic(topic) && !NamespaceUtil.isDLQTopic(topic)) {
            // 事务消息需注册事务订阅
            if (TopicMessageType.TRANSACTION.equals(messageType)) {
                messagingProcessor.addTransactionSubscription(context, requestHeader.getProducerGroup(), requestHeader.getTopic());
            }
        }
        return request(ctx, request, context, Duration.ofSeconds(3).toMillis());
    }

    /** 转发消费失败回退（send msg back）请求。 */
    protected RemotingCommand consumerSendMessage(ChannelHandlerContext ctx, RemotingCommand request,
        ProxyContext context) throws Exception {
        return request(ctx, request, context, Duration.ofSeconds(3).toMillis());
    }

    /** 判断是否需校验 Topic 消息类型（排除转发标记）。 */
    private boolean isNeedCheckTopicMessageType(Map<String, String> property) {
        return ConfigurationManager.getProxyConfig().isEnableTopicMessageTypeCheck()
            && !property.containsKey(MessageConst.PROPERTY_TRANSFER_FLAG);
    }
}
