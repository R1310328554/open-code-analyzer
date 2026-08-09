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

package org.apache.rocketmq.proxy.grpc.v2.producer;

import apache.rocketmq.v2.Code;
import apache.rocketmq.v2.RecallMessageRequest;
import apache.rocketmq.v2.RecallMessageResponse;
import apache.rocketmq.v2.Resource;
import org.apache.rocketmq.proxy.common.ProxyContext;
import org.apache.rocketmq.proxy.grpc.v2.AbstractMessagingActivity;
import org.apache.rocketmq.proxy.grpc.v2.channel.GrpcChannelManager;
import org.apache.rocketmq.proxy.grpc.v2.common.GrpcClientSettingsManager;
import org.apache.rocketmq.proxy.grpc.v2.common.ResponseBuilder;
import org.apache.rocketmq.proxy.processor.MessagingProcessor;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * 撤回已发送消息的 gRPC v2 Activity：校验 Topic 后委托 {@link MessagingProcessor#recallMessage} 执行撤回。
 */
public class RecallMessageActivity extends AbstractMessagingActivity {

    /** 构造消息撤回 Activity 并注入依赖组件。 */
    public RecallMessageActivity(MessagingProcessor messagingProcessor,
                                 GrpcClientSettingsManager grpcClientSettingsManager, GrpcChannelManager grpcChannelManager) {
        super(messagingProcessor, grpcClientSettingsManager, grpcChannelManager);
    }

    /** 按 recallHandle 撤回指定 Topic 下的消息，返回新 messageId。 */
    public CompletableFuture<RecallMessageResponse> recallMessage(ProxyContext ctx,
        RecallMessageRequest request) {
        CompletableFuture<RecallMessageResponse> future = new CompletableFuture<>();

        try {
            // 获取并校验 Topic 资源
            Resource topic = request.getTopic();
            // 校验 Topic 合法性
            validateTopic(topic);

            future = this.messagingProcessor.recallMessage(
                ctx,
                topic.getName(),
                request.getRecallHandle(),
                Duration.ofSeconds(2).toMillis() // 撤回操作超时 2 秒
            ).thenApply(result -> RecallMessageResponse.newBuilder()
                .setMessageId(result)
                // 构建成功状态响应
                .setStatus(ResponseBuilder.getInstance().buildStatus(Code.OK, Code.OK.name()))
                .build());
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
        return future;
    }
}
