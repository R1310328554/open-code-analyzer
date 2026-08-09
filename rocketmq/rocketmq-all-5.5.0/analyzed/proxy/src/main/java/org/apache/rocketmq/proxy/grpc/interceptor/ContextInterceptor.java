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

package org.apache.rocketmq.proxy.grpc.interceptor;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import org.apache.rocketmq.common.constant.GrpcConstants;

/**
 * gRPC 上下文拦截器：将请求 Metadata 绑定到 {@link GrpcConstants#METADATA} Context 键供下游读取。
 */
public class ContextInterceptor implements ServerInterceptor {

    /** 将 gRPC Metadata 写入 Context 并继续调用链。 */
    @Override
    public <R, W> ServerCall.Listener<R> interceptCall(
        ServerCall<R, W> call,
        Metadata headers,
        ServerCallHandler<R, W> next
    ) {
        // 将请求头绑定到 Context，供 Pipeline 与业务层访问
        Context context = Context.current().withValue(GrpcConstants.METADATA, headers);
        return Contexts.interceptCall(context, call, headers, next);
    }
}
