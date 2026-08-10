/*
 *  Copyright 2016 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.keycloak.authorization.client.resource;

import org.keycloak.authorization.client.Configuration;
import org.keycloak.authorization.client.representation.ServerConfiguration;
import org.keycloak.authorization.client.representation.TokenIntrospectionResponse;
import org.keycloak.authorization.client.util.Http;
import org.keycloak.authorization.client.util.TokenCallable;

/**
 * Protection API 端点入口，聚合资源、权限票据与策略子资源。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class ProtectionResource {

    private final TokenCallable pat;
    private final Http http;
    private final Configuration configuration;
    private ServerConfiguration serverConfiguration;

    public ProtectionResource(Http http, ServerConfiguration serverConfiguration, Configuration configuration, TokenCallable pat) {
        if (pat == null) {
            throw new RuntimeException("No access token was provided when creating client for Protection API.");
        }

        this.http = http;
        this.serverConfiguration = serverConfiguration;
        this.configuration = configuration;
        this.pat = pat;
    }

    /**
     * 创建 {@link ProtectedResource}，用于 UMA 资源注册与管理。
     *
     * @return {@link ProtectedResource} 实例
     */
    public ProtectedResource resource() {
        return new ProtectedResource(http, serverConfiguration, configuration, pat);
    }

    /**
     * 创建 {@link PermissionResource}，用于权限票据（permission ticket）管理。
     *
     * @return {@link PermissionResource} 实例
     */
    public PermissionResource permission() {
        return new PermissionResource(http, serverConfiguration, pat);
    }

    /** 返回指定资源 ID 的 {@link PolicyResource}（用户托管权限）。 */
    public PolicyResource policy(String resourceId) {
        return new PolicyResource(resourceId, http, serverConfiguration, pat);
    }

    /**
     * 通过令牌内省端点 introspect 给定的 RPT（Requesting Party Token）。
     *
     * @param rpt 待内省的 RPT
     * @return {@link TokenIntrospectionResponse}
     */
    public TokenIntrospectionResponse introspectRequestingPartyToken(String rpt) {
        return this.http.<TokenIntrospectionResponse>post(serverConfiguration.getIntrospectionEndpoint())
                .authentication()
                    .client()
                .form()
                    .param("token_type_hint", "requesting_party_token")
                    .param("token", rpt)
                .response().json(TokenIntrospectionResponse.class).execute();
    }
}
