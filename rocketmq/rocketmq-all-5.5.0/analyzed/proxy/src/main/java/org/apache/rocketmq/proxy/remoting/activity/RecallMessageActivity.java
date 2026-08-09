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
import org.apache.rocketmq.common.attribute.TopicMessageType;
import org.apache.rocketmq.proxy.common.ProxyContext;
import org.apache.rocketmq.proxy.config.ConfigurationManager;
import org.apache.rocketmq.proxy.processor.MessagingProcessor;
import org.apache.rocketmq.proxy.processor.validator.DefaultTopicMessageTypeValidator;
import org.apache.rocketmq.proxy.processor.validator.TopicMessageTypeValidator;
import org.apache.rocketmq.proxy.remoting.pipeline.RequestPipeline;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;
import org.apache.rocketmq.remoting.protocol.header.RecallMessageRequestHeader;

import java.time.Duration;

/**
 * 消息撤回 Remoting Activity：校验延迟 Topic 类型后转发撤回请求。
 */
public class RecallMessageActivity extends AbstractRemotingActivity {
    TopicMessageTypeValidator topicMessageTypeValidator;

    /** 构造消息撤回 Activity 并初始化 Topic 类型校验器。 */
    public RecallMessageActivity(RequestPipeline requestPipeline,
                                 MessagingProcessor messagingProcessor) {
        super(requestPipeline, messagingProcessor);
        this.topicMessageTypeValidator = new DefaultTopicMessageTypeValidator();
    }

    @Override
    public RemotingCommand processRequest0(ChannelHandlerContext ctx, RemotingCommand request,
        ProxyContext context) throws Exception {
        // 解析撤回请求头
        RecallMessageRequestHeader requestHeader = request.decodeCommandCustomHeader(RecallMessageRequestHeader.class);
        String topic = requestHeader.getTopic();
        // 启用类型校验时确认 Topic 为延迟消息类型
        if (ConfigurationManager.getProxyConfig().isEnableTopicMessageTypeCheck()) {
            TopicMessageType messageType = messagingProcessor.getMetadataService().getTopicMessageType(context, topic);
            // 仅允许 DELAY 类型 Topic 执行撤回
            topicMessageTypeValidator.validate(messageType, TopicMessageType.DELAY);
        }
        // 2 秒超时转发至 Broker
        return request(ctx, request, context, Duration.ofSeconds(2).toMillis());
    }
}
