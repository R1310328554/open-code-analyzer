/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.authentication.authenticators.conditional;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

/**
 * 条件认证器接口：在认证流程中评估是否满足特定条件以决定是否执行子流程。
 * <p>不调用 {@link #authenticate}，由流程引擎调用 {@link #matchCondition} 判定分支。</p>
 */
public interface ConditionalAuthenticator extends Authenticator {
    /**
     * 评估当前认证上下文是否满足配置条件。
     *
     * @param context 认证流程上下文
     * @return 条件为真时执行子流程
     */
    boolean matchCondition(AuthenticationFlowContext context);

    /** 条件认证器不执行 authenticate，由 matchCondition 驱动分支。 */
    default void authenticate(AuthenticationFlowContext context) {
        // 条件认证器不会调用 authenticate
    }

    /** @return 默认对所有用户返回 true */
    default boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }
}
