/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.testsuite.authentication;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

/**
 * 延迟认证器：在认证流程中休眠指定毫秒数后再标记成功，用于测试超时与并发场景。
 *
 * @author rmartinc
 */
public class DelayedAuthenticator implements Authenticator {

    /**
     * 执行认证：读取配置的延迟时间，休眠后直接标记成功。
     *
     * @param context 认证流程上下文
     */
    @Override
    public void authenticate(AuthenticationFlowContext context) {
        final long time = context.getAuthenticatorConfig() != null
                ? Long.parseLong(context.getAuthenticatorConfig().getConfig().getOrDefault("delay", "1000"))
                : 1000;
        if (time > 0) {
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        context.success();
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        // 无操作
    }

    /** {@inheritDoc} 不依赖已登录用户。 */
    @Override
    public boolean requiresUser() {
        return false;
    }

    /** {@inheritDoc} 对所有用户均视为已配置。 */
    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // 无操作
    }

    @Override
    public void close() {
        // 无操作
    }
}
