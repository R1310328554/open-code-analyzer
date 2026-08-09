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
package org.apache.rocketmq.auth.authentication.strategy;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.auth.authentication.context.AuthenticationContext;
import org.apache.rocketmq.auth.authentication.context.DefaultAuthenticationContext;
import org.apache.rocketmq.auth.authentication.exception.AuthenticationException;
import org.apache.rocketmq.auth.config.AuthConfig;
import org.apache.rocketmq.common.Pair;
import org.apache.rocketmq.common.constant.CommonConstants;

/**
 * 有状态认证策略：按 channelId（及用户名）缓存认证结果，减少重复校验开销。
 */
public class StatefulAuthenticationStrategy extends AbstractAuthenticationStrategy {

    /** 缓存键为 channelId 或 channelId#username，值为成功标志与异常。 */
    protected Cache<String, Pair<Boolean, AuthenticationException>> authCache;

    /** 按配置创建带过期与容量上限的 Caffeine 缓存。 */
    public StatefulAuthenticationStrategy(AuthConfig authConfig, Supplier<?> metadataService) {
        super(authConfig, metadataService);
        this.authCache = Caffeine.newBuilder()
            .expireAfterWrite(authConfig.getStatefulAuthenticationCacheExpiredSecond(), TimeUnit.SECONDS)
            .maximumSize(authConfig.getStatefulAuthenticationCacheMaxNum())
            .build();
    }

    /** 无 channelId 时直接认证；否则命中缓存或执行后缓存结果。 */
    @Override
    public void evaluate(AuthenticationContext context) {
        if (StringUtils.isBlank(context.getChannelId())) {
            this.doEvaluate(context);
            return;
        }
        Pair<Boolean, AuthenticationException> result = this.authCache.get(buildKey(context), key -> {
            try {
                this.doEvaluate(context);
                return Pair.of(true, null);
            } catch (AuthenticationException ex) {
                return Pair.of(false, ex);
            }
        });
        if (result != null && result.getObject1() == Boolean.FALSE) {
            throw result.getObject2();
        }
    }

    /** 构建缓存键：channelId 或 channelId#username。 */
    private String buildKey(AuthenticationContext context) {
        if (context instanceof DefaultAuthenticationContext) {
            DefaultAuthenticationContext ctx = (DefaultAuthenticationContext) context;
            if (StringUtils.isBlank(ctx.getUsername())) {
                return ctx.getChannelId();
            }
            return ctx.getChannelId() + CommonConstants.POUND + ctx.getUsername();
        }
        throw new AuthenticationException("The request of {} is not support.", context.getClass().getSimpleName());
    }
}
