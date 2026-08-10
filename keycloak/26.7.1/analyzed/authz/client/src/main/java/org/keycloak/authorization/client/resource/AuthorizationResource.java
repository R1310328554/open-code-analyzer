/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.authorization.client.resource;


import java.util.List;
import java.util.concurrent.Callable;

import org.keycloak.authorization.client.AuthorizationDeniedException;
import org.keycloak.authorization.client.Configuration;
import org.keycloak.authorization.client.representation.ServerConfiguration;
import org.keycloak.authorization.client.util.Http;
import org.keycloak.authorization.client.util.HttpMethod;
import org.keycloak.authorization.client.util.HttpMethodResponse;
import org.keycloak.authorization.client.util.Throwables;
import org.keycloak.authorization.client.util.TokenCallable;
import org.keycloak.representations.idm.authorization.AuthorizationRequest;
import org.keycloak.representations.idm.authorization.AuthorizationResponse;
import org.keycloak.representations.idm.authorization.Permission;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * 向授权服务器申请权限（RPT）的 API 入口。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class AuthorizationResource {

    private Configuration configuration;
    private ServerConfiguration serverConfiguration;
    private Http http;
    private TokenCallable token;

    public AuthorizationResource(Configuration configuration, ServerConfiguration serverConfiguration, Http http, TokenCallable token) {
        this.configuration = configuration;
        this.serverConfiguration = serverConfiguration;
        this.http = http;
        this.token = token;
    }

    /**
     * 向服务器查询全部可授予权限。
     *
     * @return 含 RPT 及已授予权限的 {@link AuthorizationResponse}
     * @throws AuthorizationDeniedException 服务器拒绝授权时
     */
    public AuthorizationResponse authorize() throws AuthorizationDeniedException {
        return authorize(new AuthorizationRequest());
    }

    /**
     * 根据 {@link AuthorizationRequest} 向服务器申请权限。
     *
     * @param request {@link AuthorizationRequest}（不可为 {@code null}）
     * @return 含 RPT 及已授予权限的 {@link AuthorizationResponse}
     * @throws AuthorizationDeniedException 服务器拒绝授权时
     */
    public AuthorizationResponse authorize(final AuthorizationRequest request) throws AuthorizationDeniedException {
        return invoke(request, new TypeReference<AuthorizationResponse>(){});
    }

    /**
     * 以 permissions 响应模式查询权限列表。
     *
     * @param request {@link AuthorizationRequest}（不可为 {@code null}）
     * @return 服务器授予的权限列表
     * @throws AuthorizationDeniedException 服务器拒绝授权时
     */
    public List<Permission> getPermissions(final AuthorizationRequest request) throws AuthorizationDeniedException {
        AuthorizationRequest.Metadata metadata;

        if (request.getMetadata() == null) {
            metadata = new AuthorizationRequest.Metadata();
            request.setMetadata(metadata);
        } else {
            metadata = request.getMetadata();
        }

        metadata.setResponseMode("permissions");

        return (List<Permission>) invoke(request, new TypeReference<List<Permission>>(){});
    }

    /** 执行 UMA 授权请求，必要时通过 {@link TokenCallable} 刷新令牌并重试。 */
    private <T> T invoke(AuthorizationRequest request, TypeReference<T> responseType) {
        if (request == null) {
            throw new IllegalArgumentException("Authorization request must not be null");
        }

        Callable<T> callable = new Callable<T>() {
            @Override
            public T call() throws Exception {
                if (request.getAudience() == null) {
                    request.setAudience(configuration.getResource());
                }

                HttpMethod<T> method = http.post(serverConfiguration.getTokenEndpoint());

                if (token != null) {
                    method = method.authorizationBearer(token.call());
                }

                HttpMethodResponse<T> response = method
                        .authentication()
                        .uma(request)
                        .response()
                        .json(responseType);

                return response.execute();
            }
        };
        try {
            return callable.call();
        } catch (Exception cause) {
            return Throwables.retryAndWrapExceptionIfNecessary(callable, token, "Failed to obtain authorization data", cause);
        }
    }
}
