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
package org.apache.rocketmq.auth.authorization.builder;

import com.google.protobuf.GeneratedMessageV3;
import io.grpc.Metadata;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;
import org.apache.rocketmq.auth.authorization.context.DefaultAuthorizationContext;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;

/**
 * 授权上下文构建器：将 gRPC 或 Remoting 请求解析为 {@link DefaultAuthorizationContext} 列表。
 */
public interface AuthorizationContextBuilder {

    /** 从 gRPC Metadata 与 Protobuf 消息提取待授权资源与动作。 */
    List<DefaultAuthorizationContext> build(Metadata metadata, GeneratedMessageV3 message);

    /** 从 Netty 通道与 Remoting 命令构建授权上下文。 */
    List<DefaultAuthorizationContext> build(ChannelHandlerContext context, RemotingCommand command);
}
