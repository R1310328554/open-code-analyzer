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
package org.apache.rocketmq.auth.authentication.provider;

import com.google.protobuf.GeneratedMessageV3;
import io.grpc.Metadata;
import io.netty.channel.ChannelHandlerContext;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.apache.rocketmq.auth.config.AuthConfig;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;

/**
 * 认证提供者 SPI：封装上下文构建与认证责任链执行，支持 gRPC 与 Remoting 两种入口。
 */
public interface AuthenticationProvider<AuthenticationContext> {

    /** 注入配置与元数据服务，初始化上下文构建器。 */
    void initialize(AuthConfig config, Supplier<?> metadataService);

    /** 对给定上下文执行认证责任链。 */
    CompletableFuture<Void> authenticate(AuthenticationContext context);

    /** 从 gRPC Metadata 与 Protobuf 请求构建认证上下文。 */
    AuthenticationContext newContext(Metadata metadata, GeneratedMessageV3 request);

    /** 从 Netty 通道与 Remoting 命令构建认证上下文。 */
    AuthenticationContext newContext(ChannelHandlerContext context, RemotingCommand command);
}
