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

import apache.rocketmq.v2.ForwardMessageToDeadLetterQueueRequest;
import apache.rocketmq.v2.ForwardMessageToDeadLetterQueueResponse;
import java.util.concurrent.CompletableFuture;
import org.apache.rocketmq.common.consumer.ReceiptHandle;
import org.apache.rocketmq.proxy.common.MessageReceiptHandle;
import org.apache.rocketmq.proxy.common.ProxyContext;
import org.apache.rocketmq.proxy.grpc.v2.AbstractMessagingActivity;
import org.apache.rocketmq.proxy.grpc.v2.channel.GrpcChannelManager;
import org.apache.rocketmq.proxy.grpc.v2.common.GrpcClientSettingsManager;
import org.apache.rocketmq.proxy.grpc.v2.common.ResponseBuilder;
import org.apache.rocketmq.proxy.processor.MessagingProcessor;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;

/**
 * 转发消息到死信队列（DLQ）的 gRPC v2 Activity：解析回执句柄并委托 {@link MessagingProcessor} 执行转发。
 */
public class ForwardMessageToDLQActivity extends AbstractMessagingActivity {

    /** 构造 DLQ 转发 Activity 并注入消息处理器与 gRPC 通道管理器。 */
    public ForwardMessageToDLQActivity(MessagingProcessor messagingProcessor,
        GrpcClientSettingsManager grpcClientSettingsManager, GrpcChannelManager grpcChannelManager) {
        super(messagingProcessor, grpcClientSettingsManager, grpcChannelManager);
    }

    /** 将指定消息转发至死信队列，返回异步 gRPC 响应。 */
    public CompletableFuture<ForwardMessageToDeadLetterQueueResponse> forwardMessageToDeadLetterQueue(ProxyContext ctx,
        ForwardMessageToDeadLetterQueueRequest request) {
        CompletableFuture<ForwardMessageToDeadLetterQueueResponse> future = new CompletableFuture<>();
        try {
            // 校验 Topic 与消费者组
            validateTopicAndConsumerGroup(request.getTopic(), request.getGroup());

            String group = request.getGroup().getName();
            String handleString = request.getReceiptHandle();
            // 移除本地缓存的回执句柄，获取完整句柄字符串
            MessageReceiptHandle messageReceiptHandle = messagingProcessor.removeReceiptHandle(ctx, grpcChannelManager.getChannel(ctx.getClientID()), group, request.getMessageId(), request.getReceiptHandle());
            if (messageReceiptHandle != null) {
                handleString = messageReceiptHandle.getReceiptHandleStr();
            }
            // 解码回执句柄为 Broker 可识别的结构
            ReceiptHandle receiptHandle = ReceiptHandle.decode(handleString);

            // 可选 Lite Topic 名称
            String liteTopic = request.hasLiteTopic() ? request.getLiteTopic() : null;

            return this.messagingProcessor.forwardMessageToDeadLetterQueue(
                ctx,
                receiptHandle,
                request.getMessageId(),
                request.getGroup().getName(),
                request.getTopic().getName(),
                liteTopic
            ).thenApply(result -> convertToForwardMessageToDeadLetterQueueResponse(ctx, result));
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
        return future;
    }

    /** 将 Broker Remoting 响应转换为 gRPC DLQ 转发响应。 */
    protected ForwardMessageToDeadLetterQueueResponse convertToForwardMessageToDeadLetterQueueResponse(ProxyContext ctx,
        RemotingCommand result) {
        return ForwardMessageToDeadLetterQueueResponse.newBuilder()
            .setStatus(ResponseBuilder.getInstance().buildStatus(result.getCode(), result.getRemark()))
            .build();
    }
}
