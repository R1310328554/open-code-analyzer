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
package org.apache.rocketmq.proxy.grpc.v2;

import apache.rocketmq.v2.Resource;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.proxy.grpc.v2.channel.GrpcChannelManager;
import org.apache.rocketmq.proxy.grpc.v2.common.GrpcClientSettingsManager;
import org.apache.rocketmq.proxy.grpc.v2.common.GrpcValidator;
import org.apache.rocketmq.proxy.processor.MessagingProcessor;

/**
 * gRPC v2 消息活动基类：注入 {@link MessagingProcessor} 并提供 Topic/消费组等参数校验。
 */
public abstract class AbstractMessagingActivity {
    protected static final Logger log = LoggerFactory.getLogger(LoggerName.PROXY_LOGGER_NAME);
    /** 消息处理核心处理器。 */
    protected final MessagingProcessor messagingProcessor;
    /** 客户端设置管理器。 */
    protected final GrpcClientSettingsManager grpcClientSettingsManager;
    /** gRPC 通道生命周期管理器。 */
    protected final GrpcChannelManager grpcChannelManager;

    /** 注入消息处理器与 gRPC 客户端管理组件。 */
    public AbstractMessagingActivity(MessagingProcessor messagingProcessor,
                                     GrpcClientSettingsManager grpcClientSettingsManager, GrpcChannelManager grpcChannelManager) {
        this.messagingProcessor = messagingProcessor;
        this.grpcClientSettingsManager = grpcClientSettingsManager;
        this.grpcChannelManager = grpcChannelManager;
    }

    /** 校验 Topic 资源名称合法性。 */
    protected void validateTopic(Resource topic) {
        GrpcValidator.getInstance().validateTopic(topic);
    }

    protected void validateLiteTopic(String liteTopic) {
        GrpcValidator.getInstance().validateLiteTopic(liteTopic);
    }

    /** 校验消费组资源名称合法性。 */
    protected void validateConsumerGroup(Resource consumerGroup) {
        GrpcValidator.getInstance().validateConsumerGroup(consumerGroup);
    }

    protected void validateTopicAndConsumerGroup(Resource topic, Resource consumerGroup) {
        GrpcValidator.getInstance().validateTopicAndConsumerGroup(topic, consumerGroup);
    }

    /** 校验消息不可见时间是否在允许范围内。 */
    protected void validateInvisibleTime(long invisibleTime) {
        GrpcValidator.getInstance().validateInvisibleTime(invisibleTime);
    }

    protected void validateInvisibleTime(long invisibleTime, long minInvisibleTime) {
        GrpcValidator.getInstance().validateInvisibleTime(invisibleTime, minInvisibleTime);
    }
}
