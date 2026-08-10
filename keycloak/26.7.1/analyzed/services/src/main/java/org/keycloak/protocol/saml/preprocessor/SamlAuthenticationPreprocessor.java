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
package org.keycloak.protocol.saml.preprocessor;

import org.keycloak.dom.saml.v2.protocol.AuthnRequestType;
import org.keycloak.dom.saml.v2.protocol.LogoutRequestType;
import org.keycloak.dom.saml.v2.protocol.StatusResponseType;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.sessions.AuthenticationSessionModel;

/**
 * SAML 认证预处理器提供者接口：在登录/登出请求与响应收发各阶段介入修改 SAML 消息。
 * <p>同时继承 {@link org.keycloak.provider.Provider} 与 {@link org.keycloak.provider.ProviderFactory}，可注册为 SPI 实现。</p>
 * 
 * @author <a href="mailto:gideon.caranzo@thalesgroup.com">Gideon Caranzo</a>
 *
 */
public interface SamlAuthenticationPreprocessor extends Provider, ProviderFactory<SamlAuthenticationPreprocessor> {

    /** 处理收到的 AuthnRequest 之前调用 */

    default AuthnRequestType beforeProcessingLoginRequest(AuthnRequestType authnRequest,
            AuthenticationSessionModel authSession) {
        return authnRequest;
    }

    /**
     * 处理收到的 LogoutRequest 之前调用。
     * @param clientSession 客户端会话；身份代理等场景可为 null
     */
    default LogoutRequestType beforeProcessingLogoutRequest(LogoutRequestType logoutRequest,
            UserSessionModel authSession, AuthenticatedClientSessionModel clientSession) {
        return logoutRequest;
    }

    /** 向外发送 AuthnRequest 之前调用 */

    default AuthnRequestType beforeSendingLoginRequest(AuthnRequestType authnRequest,
            AuthenticationSessionModel clientSession) {
        return authnRequest;
    }

    /**
     * 向外发送 LogoutRequest 之前调用。
     * @param clientSession 客户端会话；身份代理等场景可为 null
     */
    default LogoutRequestType beforeSendingLogoutRequest(LogoutRequestType logoutRequest,
            UserSessionModel authSession, AuthenticatedClientSessionModel clientSession) {
        return logoutRequest;
    }

    /** 处理收到的登录响应（StatusResponse）之前调用 */

    default StatusResponseType beforeProcessingLoginResponse(StatusResponseType statusResponse,
            AuthenticationSessionModel authSession) {
        return statusResponse;
    }

    /** 向客户端发送 SAML 响应之前调用 */

    default StatusResponseType beforeSendingResponse(StatusResponseType statusResponse,
            AuthenticatedClientSessionModel clientSession) {
        return statusResponse;
    }

}
