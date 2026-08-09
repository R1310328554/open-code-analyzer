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
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.auth.authentication.builder.AuthenticationContextBuilder;
import org.apache.rocketmq.auth.authentication.builder.DefaultAuthenticationContextBuilder;
import org.apache.rocketmq.auth.authentication.context.DefaultAuthenticationContext;
import org.apache.rocketmq.auth.authentication.chain.DefaultAuthenticationHandler;
import org.apache.rocketmq.auth.config.AuthConfig;
import org.apache.rocketmq.common.chain.HandlerChain;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认认证提供者：组装 {@link DefaultAuthenticationContextBuilder} 与
 * {@link DefaultAuthenticationHandler} 责任链，并在完成后写审计日志。
 */
public class DefaultAuthenticationProvider implements AuthenticationProvider<DefaultAuthenticationContext> {

    /** 认证审计专用 Logger。 */
    protected final Logger log = LoggerFactory.getLogger(LoggerName.ROCKETMQ_AUTH_AUDIT_LOGGER_NAME);
    protected AuthConfig authConfig;
    protected Supplier<?> metadataService;
    protected AuthenticationContextBuilder<DefaultAuthenticationContext> authenticationContextBuilder;

    /** 保存配置并创建默认上下文构建器。 */
    @Override
    public void initialize(AuthConfig config, Supplier<?> metadataService) {
        this.authConfig = config;
        this.metadataService = metadataService;
        this.authenticationContextBuilder = new DefaultAuthenticationContextBuilder();
    }

    /** 执行认证链，无论成功失败均触发审计日志。 */
    @Override
    public CompletableFuture<Void> authenticate(DefaultAuthenticationContext context) {
        return this.newHandlerChain().handle(context)
            .whenComplete((nil, ex) -> doAuditLog(context, ex));
    }

    /** 构建 gRPC 场景的 {@link DefaultAuthenticationContext}。 */
    @Override
    public DefaultAuthenticationContext newContext(Metadata metadata, GeneratedMessageV3 request) {
        return this.authenticationContextBuilder.build(metadata, request);
    }

    /** 构建 Remoting 场景的 {@link DefaultAuthenticationContext}。 */
    @Override
    public DefaultAuthenticationContext newContext(ChannelHandlerContext context, RemotingCommand command) {
        return this.authenticationContextBuilder.build(context, command);
    }

    /** 创建仅含 {@link DefaultAuthenticationHandler} 的单节点责任链。 */
    protected HandlerChain<DefaultAuthenticationContext, CompletableFuture<Void>> newHandlerChain() {
        return HandlerChain.<DefaultAuthenticationContext, CompletableFuture<Void>>create()
            .addNext(new DefaultAuthenticationHandler(this.authConfig, metadataService));
    }

    /** 用户名非空时记录认证成功（debug）或失败（info）审计。 */
    protected void doAuditLog(DefaultAuthenticationContext context, Throwable ex) {
        if (StringUtils.isBlank(context.getUsername())) {
            return;
        }
        if (ex != null) {
            log.info("[AUTHENTICATION] User:{} is authenticated failed with Signature = {}.", context.getUsername(), context.getSignature());
        } else {
            log.debug("[AUTHENTICATION] User:{} is authenticated success with Signature = {}.", context.getUsername(), context.getSignature());
        }
    }
}
