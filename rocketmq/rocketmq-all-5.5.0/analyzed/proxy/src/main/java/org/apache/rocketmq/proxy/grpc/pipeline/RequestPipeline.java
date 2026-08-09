/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.rocketmq.proxy.grpc.pipeline;

import com.google.protobuf.GeneratedMessageV3;
import io.grpc.Metadata;
import org.apache.rocketmq.proxy.common.ProxyContext;

/**
 * gRPC 请求处理管道接口：各阶段（上下文初始化、认证、授权等）按序执行。
 */
public interface RequestPipeline {

    /** 执行当前管道阶段逻辑。 */
    void execute(ProxyContext context, Metadata headers, GeneratedMessageV3 request);

    /** 将当前阶段链接到上游 Pipeline，形成顺序执行链。 */
    default RequestPipeline pipe(RequestPipeline source) {
        return (ctx, headers, request) -> {
            source.execute(ctx, headers, request);
            execute(ctx, headers, request);
        };
    }
}
