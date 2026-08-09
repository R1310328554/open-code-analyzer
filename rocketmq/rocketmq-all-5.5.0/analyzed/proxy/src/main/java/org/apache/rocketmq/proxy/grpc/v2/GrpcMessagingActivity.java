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

import apache.rocketmq.v2.AckMessageRequest;
import apache.rocketmq.v2.AckMessageResponse;
import apache.rocketmq.v2.ChangeInvisibleDurationRequest;
import apache.rocketmq.v2.ChangeInvisibleDurationResponse;
import apache.rocketmq.v2.EndTransactionRequest;
import apache.rocketmq.v2.EndTransactionResponse;
import apache.rocketmq.v2.ForwardMessageToDeadLetterQueueRequest;
import apache.rocketmq.v2.ForwardMessageToDeadLetterQueueResponse;
import apache.rocketmq.v2.HeartbeatRequest;
import apache.rocketmq.v2.HeartbeatResponse;
import apache.rocketmq.v2.NotifyClientTerminationRequest;
import apache.rocketmq.v2.NotifyClientTerminationResponse;
import apache.rocketmq.v2.QueryAssignmentRequest;
import apache.rocketmq.v2.QueryAssignmentResponse;
import apache.rocketmq.v2.QueryRouteRequest;
import apache.rocketmq.v2.QueryRouteResponse;
import apache.rocketmq.v2.RecallMessageRequest;
import apache.rocketmq.v2.RecallMessageResponse;
import apache.rocketmq.v2.ReceiveMessageRequest;
import apache.rocketmq.v2.ReceiveMessageResponse;
import apache.rocketmq.v2.SendMessageRequest;
import apache.rocketmq.v2.SendMessageResponse;
import apache.rocketmq.v2.SyncLiteSubscriptionRequest;
import apache.rocketmq.v2.SyncLiteSubscriptionResponse;
import apache.rocketmq.v2.TelemetryCommand;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.CompletableFuture;
import org.apache.rocketmq.proxy.common.ProxyContext;
import org.apache.rocketmq.common.utils.StartAndShutdown;

/**
 * gRPC v2 消息活动接口：定义路由、收发、事务与 Telemetry 等 RPC 入口。
 */
public interface GrpcMessagingActivity extends StartAndShutdown {

    /** 查询 Topic 路由。 */
    CompletableFuture<QueryRouteResponse> queryRoute(ProxyContext ctx, QueryRouteRequest request);

    /** 客户端心跳保活。 */
    CompletableFuture<HeartbeatResponse> heartbeat(ProxyContext ctx, HeartbeatRequest request);

    /** 发送单条或批量消息。 */
    CompletableFuture<SendMessageResponse> sendMessage(ProxyContext ctx, SendMessageRequest request);

    CompletableFuture<QueryAssignmentResponse> queryAssignment(ProxyContext ctx, QueryAssignmentRequest request);

    /** 流式 POP 拉取消息。 */
    void receiveMessage(ProxyContext ctx, ReceiveMessageRequest request,
        StreamObserver<ReceiveMessageResponse> responseObserver);

    /** 确认已消费消息。 */
    CompletableFuture<AckMessageResponse> ackMessage(ProxyContext ctx, AckMessageRequest request);

    CompletableFuture<ForwardMessageToDeadLetterQueueResponse> forwardMessageToDeadLetterQueue(ProxyContext ctx,
        ForwardMessageToDeadLetterQueueRequest request);

    /** 提交或回滚事务消息。 */
    CompletableFuture<EndTransactionResponse> endTransaction(ProxyContext ctx, EndTransactionRequest request);

    CompletableFuture<NotifyClientTerminationResponse> notifyClientTermination(ProxyContext ctx,
        NotifyClientTerminationRequest request);

    /** 修改 POP 消息不可见时长。 */
    CompletableFuture<ChangeInvisibleDurationResponse> changeInvisibleDuration(ProxyContext ctx,
        ChangeInvisibleDurationRequest request);

    CompletableFuture<RecallMessageResponse> recallMessage(ProxyContext ctx, RecallMessageRequest request);

    CompletableFuture<SyncLiteSubscriptionResponse> syncLiteSubscription(ProxyContext ctx, SyncLiteSubscriptionRequest request);

    /** 双向 Telemetry 流，用于服务端主动推送。 */
    ContextStreamObserver<TelemetryCommand> telemetry(StreamObserver<TelemetryCommand> responseObserver);
}
