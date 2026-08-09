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
package org.apache.rocketmq.auth.authorization;

import java.util.List;
import java.util.function.Supplier;
import org.apache.commons.collections.CollectionUtils;
import org.apache.rocketmq.auth.authorization.context.AuthorizationContext;
import org.apache.rocketmq.auth.authorization.factory.AuthorizationFactory;
import org.apache.rocketmq.auth.authorization.strategy.AuthorizationStrategy;
import org.apache.rocketmq.auth.config.AuthConfig;

/**
 * 授权评估入口：通过 {@link AuthorizationFactory} 获取策略并对上下文列表逐条评估。
 */
public class AuthorizationEvaluator {

    private final AuthorizationStrategy authorizationStrategy;

    /** 使用默认元数据服务（null）构造。 */
    public AuthorizationEvaluator(AuthConfig authConfig) {
        this(authConfig, null);
    }

    /** 指定元数据服务 Supplier 构造授权策略。 */
    public AuthorizationEvaluator(AuthConfig authConfig, Supplier<?> metadataService) {
        this.authorizationStrategy = AuthorizationFactory.getStrategy(authConfig, metadataService);
    }

    /** 对非空上下文列表依次调用策略 {@code evaluate}。 */
    public void evaluate(List<AuthorizationContext> contexts) {
        if (CollectionUtils.isEmpty(contexts)) {
            return;
        }
        contexts.forEach(this.authorizationStrategy::evaluate);
    }
}
