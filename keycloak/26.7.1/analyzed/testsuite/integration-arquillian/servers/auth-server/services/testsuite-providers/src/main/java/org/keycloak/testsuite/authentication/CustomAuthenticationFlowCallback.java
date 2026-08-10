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

package org.keycloak.testsuite.authentication;

import org.keycloak.authentication.AuthenticationFlowCallback;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.AuthenticationFlowException;
import org.keycloak.models.AuthenticationFlowModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

/**
 * 自定义认证流程回调，用于测试顶层流程成功时的异常传播行为。
 *
 * @author <a href="mailto:mabartos@redhat.com">Martin Bartos</a>
 */
public class CustomAuthenticationFlowCallback implements AuthenticationFlowCallback {

    /** 顶层流程成功回调中抛出的预期错误消息。 */
    public static final String EXPECTED_ERROR_MESSAGE = "Custom Authentication Flow Callback message";

    /**
     * 顶层认证流程成功时故意抛出 {@link AuthenticationFlowException}，验证回调错误处理。
     */
    @Override
    public void onTopFlowSuccess(AuthenticationFlowModel topFlow) {
        throw new AuthenticationFlowException(AuthenticationFlowError.GENERIC_AUTHENTICATION_ERROR, "detail", EXPECTED_ERROR_MESSAGE);
    }

    @Override
    public void onParentFlowSuccess(AuthenticationFlowContext context) {

    }

    /** {@inheritDoc} 认证步骤直接标记成功。 */
    @Override
    public void authenticate(AuthenticationFlowContext context) {
        context.success();
    }

    @Override
    public void action(AuthenticationFlowContext context) {
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return false;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

    }

    @Override
    public void close() {

    }
}
