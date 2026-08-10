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
package org.keycloak.broker.provider;

import jakarta.ws.rs.core.UriInfo;

import org.keycloak.broker.provider.util.IdentityBrokerState;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.sessions.AuthenticationSessionModel;

/**
 * 身份联邦登录请求上下文，封装会话、领域、认证会话、HTTP 请求、URI 与 broker 状态。
 * <p>由 {@link IdentityProvider#performLogin(AuthenticationRequest)} 消费，用于构造对外 IdP 的认证重定向。</p>
 *
 * @author Pedro Igor
 */
public class AuthenticationRequest {

    private final KeycloakSession session;
    private final UriInfo uriInfo;
    private final IdentityBrokerState state;
    private final HttpRequest httpRequest;
    private final RealmModel realm;
    private final String redirectUri;
    private final AuthenticationSessionModel authSession;

    /** 构造包含完整登录流程上下文的认证请求。 */
    public AuthenticationRequest(KeycloakSession session, RealmModel realm, AuthenticationSessionModel authSession, HttpRequest httpRequest, UriInfo uriInfo, IdentityBrokerState state, String redirectUri) {
        this.session = session;
        this.realm = realm;
        this.httpRequest = httpRequest;
        this.uriInfo = uriInfo;
        this.state = state;
        this.redirectUri = redirectUri;
        this.authSession = authSession;
    }

    public KeycloakSession getSession() {
        return session;
    }

    public UriInfo getUriInfo() {
        return this.uriInfo;
    }

    public IdentityBrokerState getState() {
        return this.state;
    }

    public HttpRequest getHttpRequest() {
        return this.httpRequest;
    }

    public RealmModel getRealm() {
        return this.realm;
    }

    /**
     * 返回认证请求中必须携带的重定向 URL，用于接收身份提供者回调。
     *
     * <p>Returns the redirect url that must be included in an authentication request in order to process responses from an
     * identity provider.</p>
     *
     * @return
     */
    public String getRedirectUri() {
        return this.redirectUri;
    }

    public AuthenticationSessionModel getAuthenticationSession() {
        return this.authSession;
    }
}
