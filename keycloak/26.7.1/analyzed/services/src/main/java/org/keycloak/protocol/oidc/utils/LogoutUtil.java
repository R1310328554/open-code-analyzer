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
 *
 */

package org.keycloak.protocol.oidc.utils;

import java.net.URI;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.utils.SystemClientUtil;
import org.keycloak.protocol.oidc.OIDCAdvancedConfigWrapper;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;

/**
 * OIDC 登出工具：构建登出完成后的重定向响应或信息页。
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class LogoutUtil {

    /**
     * 登出流程结束后发送响应：302 重定向至 post_logout_redirect_uri，或显示成功信息页（含可选确认页）。
     * @param session Keycloak 会话
     * @param logoutSession 登出认证会话
     * @return JAX-RS 响应
     */
        String redirectUri = logoutSession.getAuthNote(OIDCLoginProtocol.LOGOUT_REDIRECT_URI);
        URI finalRedirectUri = getRedirectUriWithAttachedState(redirectUri, logoutSession);
        OIDCAdvancedConfigWrapper config = OIDCAdvancedConfigWrapper.fromClientModel(logoutSession.getClient());
        LoginFormsProvider loginFormsProvider = session.getProvider(LoginFormsProvider.class);

        if (finalRedirectUri != null) {
            if (!config.isLogoutConfirmationEnabled()) {
                return Response.status(302).location(finalRedirectUri).build();
            }
            loginFormsProvider.setAttribute("pageRedirectUri", finalRedirectUri.toString());
        }

        SystemClientUtil.checkSkipLink(session, logoutSession);

        return loginFormsProvider
                .setSuccess(Messages.SUCCESS_LOGOUT)
                .setDetachedAuthSession()
                .createInfoPage();
    }


    /**
     * 在 post_logout_redirect_uri 上附加 state 查询参数（若存在）。
     * @param redirectUri 基础重定向 URI
     * @param logoutSession 登出认证会话
     * @return 含 state 的完整 URI，redirectUri 为 null 时返回 null
     */
        if (redirectUri == null) return null;
        String state = logoutSession.getAuthNote(OIDCLoginProtocol.LOGOUT_STATE_PARAM);

        UriBuilder uriBuilder = UriBuilder.fromUri(redirectUri);
        if (state != null) {
            uriBuilder.queryParam(OIDCLoginProtocol.STATE_PARAM, state);
        }
        return uriBuilder.build();
    }
}
