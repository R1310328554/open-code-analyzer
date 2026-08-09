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

package org.apache.rocketmq.proxy.remoting.pipeline;

import io.netty.channel.ChannelHandlerContext;
import org.apache.rocketmq.proxy.common.ProxyContext;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;

/**
 * Remoting 请求处理流水线接口：支持链式组合多个预处理阶段。
 */
public interface RequestPipeline {

    /** 执行当前流水线阶段；异常将中断后续处理。 */
    void execute(ChannelHandlerContext ctx, RemotingCommand request, ProxyContext context) throws Exception;

    /** 将 source 与本阶段串联，先执行 source 再执行当前阶段。 */
    default RequestPipeline pipe(RequestPipeline source) {
        // 按序执行上游与当前流水线
        return (ctx, request, context) -> {
            source.execute(ctx, request, context);
            execute(ctx, request, context);
        };
    }
}
