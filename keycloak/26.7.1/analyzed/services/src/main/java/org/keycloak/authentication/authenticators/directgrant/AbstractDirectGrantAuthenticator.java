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

package org.keycloak.authentication.authenticators.directgrant;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.Config;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.representations.idm.OAuth2ErrorRepresentation;

/**
 * Direct Grant（资源所有者密码凭证）认证器抽象基类：同时实现 {@link Authenticator} 与 {@link AuthenticatorFactory}，提供 OAuth2 JSON 错误响应构建及工厂默认空实现。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public abstract class AbstractDirectGrantAuthenticator implements Authenticator, AuthenticatorFactory {
    /**
     * 构建 OAuth2 标准 JSON 错误响应。
     * @param status HTTP 状态码
     * @param error OAuth2 error 字段
     * @param errorDescription 错误描述
     * @return JAX-RS Response
     */
    public Response errorResponse(int status, String error, String errorDescription) {
        OAuth2ErrorRepresentation errorRep = new OAuth2ErrorRepresentation(error, errorDescription);
        return Response.status(status).entity(errorRep).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Override
    public void action(AuthenticationFlowContext context) {

    }

    @Override
    public void close() {

    }

    @Override
    /** @return 自身作为单例认证器实例 */
    public Authenticator create(KeycloakSession session) {
        return this;
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }
}
