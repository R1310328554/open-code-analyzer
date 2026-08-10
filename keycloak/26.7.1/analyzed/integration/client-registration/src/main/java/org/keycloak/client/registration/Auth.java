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

package org.keycloak.client.registration;

import java.util.Base64;

import org.keycloak.representations.idm.ClientInitialAccessPresentation;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.oidc.OIDCClientRepresentation;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;

/**
 * 客户端注册 HTTP 请求的认证策略抽象基类。
 * <p>
 * 子类通过 {@link #addAuth(HttpRequest)} 向 Apache HttpClient 请求注入 Authorization 头；
 * 工厂方法支持 Bearer 令牌（初始访问令牌、注册访问令牌等）与 Basic 客户端凭据两种模式。
 * </p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public abstract class Auth {

    /**
     * 向 HTTP 请求添加认证头。
     *
     * @param request 待发送的 Apache HttpClient 请求
     */
    public abstract void addAuth(HttpRequest request);

    /**
     * 使用原始 Bearer 令牌字符串创建认证策略。
     *
     * @param token 访问令牌或注册令牌
     * @return Bearer 认证实例
     */
    public static Auth token(String token) {
        return new BearerTokenAuth(token);
    }

    /**
     * 从初始访问令牌表示创建 Bearer 认证。
     *
     * @param initialAccess {@link ClientInitialAccessPresentation} 实例
     * @return Bearer 认证实例
     */
    public static Auth token(ClientInitialAccessPresentation initialAccess) {
        return new BearerTokenAuth(initialAccess.getToken());
    }

    /**
     * 从 Keycloak 客户端表示中的 registrationAccessToken 创建 Bearer 认证。
     *
     * @param client {@link ClientRepresentation} 实例
     * @return Bearer 认证实例
     */
    public static Auth token(ClientRepresentation client) {
        return new BearerTokenAuth(client.getRegistrationAccessToken());
    }

    /**
     * 从 OIDC 动态客户端注册表示中的 registration_access_token 创建 Bearer 认证。
     *
     * @param client {@link OIDCClientRepresentation} 实例
     * @return Bearer 认证实例
     */
    public static Auth token(OIDCClientRepresentation client) {
        return new BearerTokenAuth(client.getRegistrationAccessToken());
    }

    /**
     * 使用客户端 ID 与密钥创建 HTTP Basic 认证。
     *
     * @param clientId 客户端标识
     * @param clientSecret 客户端密钥
     * @return Basic 认证实例
     */
    public static Auth client(String clientId, String clientSecret) {
        return new BasicAuth(clientId, clientSecret);
    }

    /** Bearer 令牌认证实现。 */
    private static class BearerTokenAuth extends Auth {

        private String token;

        public BearerTokenAuth(String token) {
            this.token = token;
        }

        @Override
        public void addAuth(HttpRequest request) {
            request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
    }

    /** HTTP Basic 认证实现（clientId:clientSecret Base64 编码）。 */
    private static class BasicAuth extends Auth {

        private String username;
        private String password;

        public BasicAuth(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        public void addAuth(HttpRequest request) {
            String val = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
            request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + val);
        }
    }

}
