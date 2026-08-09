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

package org.apache.rocketmq.remoting.pipeline;

import io.netty.channel.ChannelHandlerContext;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;

/**
 * Remoting 请求处理流水线：在业务处理器之前插入拦截逻辑，支持链式组合。
 */
public interface RequestPipeline {

    /** 处理入站请求，可抛出异常中断后续流程。 */
    void execute(ChannelHandlerContext ctx, RemotingCommand request) throws Exception;

    /** 将当前流水线接在 source 之后，形成组合链。 */
    default RequestPipeline pipe(RequestPipeline source) {
        return (ctx, request) -> {
            source.execute(ctx, request);
            execute(ctx, request);
        };
    }
}
