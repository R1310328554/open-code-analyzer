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
package org.apache.rocketmq.proxy.grpc.pipeline;

import com.google.protobuf.GeneratedMessageV3;
import io.grpc.Context;
import io.grpc.Metadata;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.auth.authentication.AuthenticationEvaluator;
import org.apache.rocketmq.auth.authentication.context.AuthenticationContext;
import org.apache.rocketmq.auth.authentication.context.DefaultAuthenticationContext;
import org.apache.rocketmq.auth.authentication.exception.AuthenticationException;
import org.apache.rocketmq.auth.authentication.factory.AuthenticationFactory;
import org.apache.rocketmq.auth.config.AuthConfig;
import org.apache.rocketmq.common.constant.GrpcConstants;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.proxy.common.ProxyContext;
import org.apache.rocketmq.proxy.common.utils.GrpcUtils;
import org.apache.rocketmq.proxy.processor.MessagingProcessor;

/**
 * 身份认证 Pipeline：在请求进入业务层前执行 AK/SK 或 Token 认证评估。
 */
public class AuthenticationPipeline implements RequestPipeline {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerName.PROXY_LOGGER_NAME);
    /** 认证模块配置。 */
    private final AuthConfig authConfig;
    /** 认证评估器，由工厂按配置创建。 */
    private final AuthenticationEvaluator authenticationEvaluator;

    /** 构造认证 Pipeline 并绑定元数据服务。 */
    public AuthenticationPipeline(AuthConfig authConfig, MessagingProcessor messagingProcessor) {
        this.authConfig = authConfig;
        this.authenticationEvaluator = AuthenticationFactory.getEvaluator(authConfig, messagingProcessor::getMetadataService);
    }

    /** 认证开关开启时构建上下文并执行认证评估。 */
    @Override
    public void execute(ProxyContext context, Metadata headers, GeneratedMessageV3 request) {
        if (!authConfig.isAuthenticationEnabled()) {
            return;
        }
        try {
            Metadata metadata = GrpcConstants.METADATA.get(Context.current());
            AuthenticationContext authenticationContext = newContext(context, metadata, request);
            authenticationEvaluator.evaluate(authenticationContext);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Throwable ex) {
            LOGGER.error("authenticate failed, request:{}", request, ex);
            throw ex;
        }
    }

    /**
     * 创建认证上下文，子类可覆写以扩展字段。
     *
     * @param context Proxy 上下文
     * @param headers gRPC 请求头
     * @param request Protobuf 请求体
     * @return 认证上下文实例
     */
    protected AuthenticationContext newContext(ProxyContext context, Metadata headers, GeneratedMessageV3 request) {
        AuthenticationContext result = AuthenticationFactory.newContext(authConfig, headers, request);
        if (result instanceof DefaultAuthenticationContext) {
            DefaultAuthenticationContext defaultAuthenticationContext = (DefaultAuthenticationContext) result;
            if (StringUtils.isNotBlank(defaultAuthenticationContext.getUsername())) {
                GrpcUtils.putHeaderIfNotExist(headers, GrpcConstants.AUTHORIZATION_AK, defaultAuthenticationContext.getUsername());
            }
        }
        return result;
    }
}
