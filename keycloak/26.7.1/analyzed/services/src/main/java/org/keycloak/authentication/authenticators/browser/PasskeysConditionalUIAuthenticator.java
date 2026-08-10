/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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
 *
 */

/**
 * Passkeys 条件式 UI 认证器（已弃用），在浏览器登录页自动展示设备上已存储的 Passkeys 列表，适用于无用户名（login-less）认证场景。
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
package org.keycloak.authentication.authenticators.browser;

import jakarta.ws.rs.core.Response;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.KeycloakSession;

@Deprecated(since = "26.3", forRemoval = true)
public class PasskeysConditionalUIAuthenticator extends WebAuthnPasswordlessAuthenticator {

    /** @param session 当前 Keycloak 会话 */
    public PasskeysConditionalUIAuthenticator(KeycloakSession session) {
        super(session);
    }

    @Override
    /** 调用父类认证逻辑后，渲染 Passkeys 条件式 UI 登录表单。 */
    public void authenticate(AuthenticationFlowContext context) {
        super.authenticate(context);
        Response challenge = context.form()
                .createForm("login-passkeys-conditional-authenticate.ftl");
        context.challenge(challenge);
    }

}
