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
package org.apache.rocketmq.auth.authorization.provider;

import com.google.protobuf.GeneratedMessageV3;
import io.grpc.Metadata;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.rocketmq.auth.authorization.builder.AuthorizationContextBuilder;
import org.apache.rocketmq.auth.authorization.builder.DefaultAuthorizationContextBuilder;
import org.apache.rocketmq.auth.authorization.chain.AclAuthorizationHandler;
import org.apache.rocketmq.auth.authorization.chain.UserAuthorizationHandler;
import org.apache.rocketmq.auth.authorization.context.DefaultAuthorizationContext;
import org.apache.rocketmq.auth.authorization.enums.Decision;
import org.apache.rocketmq.auth.config.AuthConfig;
import org.apache.rocketmq.common.action.Action;
import org.apache.rocketmq.common.chain.HandlerChain;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认授权提供者：组装 {@link DefaultAuthorizationContextBuilder} 与
 * User/Acl 责任链，并在完成后写审计日志。
 */
public class DefaultAuthorizationProvider implements AuthorizationProvider<DefaultAuthorizationContext> {

    protected final Logger log = LoggerFactory.getLogger(LoggerName.ROCKETMQ_AUTH_AUDIT_LOGGER_NAME);
    protected AuthConfig authConfig;
    protected Supplier<?> metadataService;
    protected AuthorizationContextBuilder authorizationContextBuilder;

    /** 委托双参数 initialize，元数据服务为 null。 */
    @Override
    public void initialize(AuthConfig config) {
        this.initialize(config, null);
    }

    /** 保存配置并创建 {@link DefaultAuthorizationContextBuilder}。 */
    @Override
    public void initialize(AuthConfig config, Supplier<?> metadataService) {
        this.authConfig = config;
        this.metadataService = metadataService;
        this.authorizationContextBuilder = new DefaultAuthorizationContextBuilder(config);
    }

    /** 执行责任链并在完成时写审计日志。 */
    @Override
    public CompletableFuture<Void> authorize(DefaultAuthorizationContext context) {
        return this.newHandlerChain().handle(context)
            .whenComplete((nil, ex) -> doAuditLog(context, ex));
    }

    /** 从 gRPC 请求构建授权上下文。 */
    @Override
    public List<DefaultAuthorizationContext> newContexts(Metadata metadata, GeneratedMessageV3 message) {
        return this.authorizationContextBuilder.build(metadata, message);
    }

    /** 从 Remoting 请求构建授权上下文。 */
    @Override
    public List<DefaultAuthorizationContext> newContexts(ChannelHandlerContext context, RemotingCommand command) {
        return this.authorizationContextBuilder.build(context, command);
    }

    /** 构建 User → Acl 授权责任链。 */
    protected HandlerChain<DefaultAuthorizationContext, CompletableFuture<Void>> newHandlerChain() {
        return HandlerChain.<DefaultAuthorizationContext, CompletableFuture<Void>>create()
            .addNext(new UserAuthorizationHandler(authConfig, metadataService))
            .addNext(new AclAuthorizationHandler(authConfig, metadataService));
    }

    /** 记录授权审计日志；拒绝时 INFO，允许时 DEBUG。 */
    protected void doAuditLog(DefaultAuthorizationContext context, Throwable ex) {
        if (context.getSubject() == null) {
            return;
        }
        Decision decision = Decision.ALLOW;
        if (ex != null) {
            decision = Decision.DENY;
        }
        String subject = context.getSubject().getSubjectKey();
        String actions = context.getActions().stream().map(Action::getName)
            .collect(Collectors.joining(","));
        String sourceIp = context.getSourceIp();
        String resource = context.getResource().getResourceKey();
        String request = context.getRpcCode();
        String format = "[AUTHORIZATION] Subject = {} is {} Action = {} from sourceIp = {} on resource = {} for request = {}.";
        if (decision == Decision.ALLOW) {
            log.debug(format, subject, decision.getName(), actions, sourceIp, resource, request);
        } else {
            log.info(format, subject, decision.getName(), actions, sourceIp, resource, request);
        }
    }
}
