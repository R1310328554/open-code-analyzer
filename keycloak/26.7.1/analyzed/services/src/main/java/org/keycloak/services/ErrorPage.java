/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.services;

import jakarta.ws.rs.core.Response;

import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.sessions.AuthenticationSessionModel;

/**
 * 登录/认证流程错误页工具：通过 {@link LoginFormsProvider} 渲染 HTML 错误页。
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class ErrorPage {

    /**
     * 创建带国际化消息的认证错误页响应。
     * @param session Keycloak 会话
     * @param authenticationSession 认证会话（可为 null）
     * @param status HTTP 状态码
     * @param message 消息键或文本
     * @param parameters 消息参数
     * @return JAX-RS 错误页 {@link Response}
     */
        return session.getProvider(LoginFormsProvider.class).setAuthenticationSession(authenticationSession).setError(message, parameters).createErrorPage(status);
    }


}
