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
import java.util.List;
import org.apache.rocketmq.auth.authentication.exception.AuthenticationException;
import org.apache.rocketmq.auth.authorization.AuthorizationEvaluator;
import org.apache.rocketmq.auth.authorization.context.AuthorizationContext;
import org.apache.rocketmq.auth.authorization.exception.AuthorizationException;
import org.apache.rocketmq.auth.authorization.factory.AuthorizationFactory;
import org.apache.rocketmq.auth.config.AuthConfig;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.proxy.common.ProxyContext;
import org.apache.rocketmq.proxy.processor.MessagingProcessor;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;

/**
 * Remoting 授权流水线：启用时校验请求对 Topic/Group 等资源的访问权限。
 */
public class AuthorizationPipeline implements RequestPipeline {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerName.PROXY_LOGGER_NAME);
    private final AuthConfig authConfig;
    private final AuthorizationEvaluator authorizationEvaluator;

    /** 构造授权流水线并获取 {@link AuthorizationEvaluator}。 */
    public AuthorizationPipeline(AuthConfig authConfig, MessagingProcessor messagingProcessor) {
        this.authConfig = authConfig;
        this.authorizationEvaluator = AuthorizationFactory.getEvaluator(authConfig, messagingProcessor::getMetadataService);
    }

    @Override
    public void execute(ChannelHandlerContext ctx, RemotingCommand request, ProxyContext context) throws Exception {
        // 未启用授权时直接跳过
        if (!authConfig.isAuthorizationEnabled()) {
            return;
        }
        try {
            // 从请求解析一个或多个授权上下文
            List<AuthorizationContext> contexts = newContexts(request, ctx, context);
            // 批量评估资源访问权限
            authorizationEvaluator.evaluate(contexts);
        } catch (AuthorizationException | AuthenticationException ex) {
            throw ex;
        }  catch (Throwable ex) {
            LOGGER.error("authorize failed, request:{}", request, ex);
            throw ex;
        }
    }

    /** 基于 Remoting 请求创建授权上下文列表。 */
    protected List<AuthorizationContext> newContexts(RemotingCommand request, ChannelHandlerContext ctx, ProxyContext context) {
        return AuthorizationFactory.newContexts(authConfig, ctx, request);
    }
}
